package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.*;
import cauBlackHole.photoragemember.application.port.inPort.MemberServiceUseCase;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.*;
import cauBlackHole.photoragemember.config.jwt.JwtTokenProvider;
import cauBlackHole.photoragemember.config.util.SecurityUtil;
import cauBlackHole.photoragemember.domain.member.Authority;
import cauBlackHole.photoragemember.domain.member.Member;
import cauBlackHole.photoragemember.infrastructure.NaverMailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberServiceUseCase {
    private final MemberPort memberPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NaverMailSender naverMailSender;

    // 현재 SecurityContext 에 있는 유저 정보 가져오기
    @Transactional(readOnly = true)
    @Override
    public MemberResponseDto getMyInfo() {
        return this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER,"로그인 유저 정보가 없습니다."));
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemberResponseDto> findByName(String name) {
        return this.memberPort.findByName(name)
                .stream()
                .map(MemberResponseDto::of)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public MemberResponseDto updateMyInfo(MemberRequestUpdateDto memberRequestUpdateDto) {
        Member updateMember = this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(member -> member.updateNameAndNickname(memberRequestUpdateDto))
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER, "로그인 유저 정보가 없습니다."));

        return this.memberPort.update(updateMember)
                .map(MemberResponseDto::of)
                .orElseThrow(()-> new ConflictException(ErrorCode.UPDATE_FAIL, "현재 사용자 정보 업데이트를 실패했습니다."));
    }

    @Transactional
    @Override
    public String leave() {
        Member member = this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER, "로그인 유저 정보가 없습니다."));

        this.memberPort.delete(member);

        return "회원이 탈퇴되었습니다.";
    }

    @Transactional
    @Override
    public MemberResponseDto updatePassword(MemberRequestUpdatePasswordDto updatePasswordDto) {
        Member findMember = this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(m -> m.matchPassword(passwordEncoder, updatePasswordDto.getOriginPassword()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER, "로그인 유저 정보가 없습니다."));

        findMember.updatePassword(passwordEncoder.encode(updatePasswordDto.getNewPassword()));

        return this.memberPort.update(findMember)
                .map(MemberResponseDto::of)
                .orElseThrow(()-> new ConflictException(ErrorCode.UPDATE_FAIL, "비밀번호 업데이트를 실패했습니다."));
    }

    @Transactional
    @Override
    public MemberResponseDto signUp(MemberRequestSignUpDto memberRequestSignUpDto) {

        this.memberPort.findByEmail(memberRequestSignUpDto.getEmail()).ifPresent(
                email->{
                    throw new BadRequestException(
                            ErrorCode.DUPLICATE_EMAIL,
                            "중복되는 이메일 입니다."
                    );
                }
        );
        Member newMember = Member.createMember(
                memberRequestSignUpDto.getEmail(),
                passwordEncoder.encode(memberRequestSignUpDto.getPassword()),
                memberRequestSignUpDto.getName(),
                memberRequestSignUpDto.getNickname(),
                Authority.ROLE_USER
        );

        return MemberResponseDto.of(this.memberPort.create(newMember));
    }

    @Transactional
    @Override
    public JwtTokenDto signIn(MemberRequestSignInDto memberRequestSignInDto) {
        Member findMember = this.memberPort.findByEmail(memberRequestSignInDto.getEmail()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.INVALID_EMAIL,
                        "이메일이 틀렸습니다."
                )
        );
        findMember.matchPassword(passwordEncoder, memberRequestSignInDto.getPassword());

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = memberRequestSignInDto.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        JwtTokenDto jwtTokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken redis에 저장
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(),
                        jwtTokenDto.getRefreshToken(),
                        jwtTokenDto.getRefreshTokenExpiresIn(),
                        TimeUnit.MILLISECONDS);

        // 5. 토큰 발급
        return jwtTokenDto;
    }
    @Transactional
    @Override
    public String logout(JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto)
    {
        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(jwtTokenRequestLogoutDto.getAccessToken())) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_JWT, "잘못된 요청입니다.");
        }
        // 2. Access Token 에서 User의 name(id)을 가져온다.
        Authentication authentication = jwtTokenProvider.getAuthentication(jwtTokenRequestLogoutDto.getAccessToken());
        // 3. Redis 에서 해당 User의 name(email) 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제합니다.
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT:" + authentication.getName());
        }
        // 4. 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = jwtTokenProvider.getExpiration(jwtTokenRequestLogoutDto.getAccessToken());
        redisTemplate.opsForValue()
                .set(jwtTokenRequestLogoutDto.getAccessToken(), "logout", expiration, TimeUnit.MILLISECONDS);

        return "로그아웃 되었습니다.";
    }
    @Transactional
    @Override
    public JwtTokenDto reissue(JwtTokenRequestReissueDto jwtTokenRequestDto) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(jwtTokenRequestDto.getRefreshToken())) {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_JWT, "Refresh Token이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(jwtTokenRequestDto.getAccessToken());

        // 3. Redis에서 Refresh Token 값 가져옴
        String refreshToken = (String)redisTemplate.opsForValue().get("RT:" + authentication.getName());

        //로그아웃되어 Redis 에 RefreshToken 이 존재하지 않는 경우 처리
        if(ObjectUtils.isEmpty(refreshToken)) {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_JWT, "잘못된 요청입니다.");
        }
        //refresh token이 있는 경우
        if(!refreshToken.equals(jwtTokenRequestDto.getRefreshToken()))
        {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_JWT, "Refresh Token이 유효하지 않습니다.");
        }

        // 4. 새로운 토큰 생성
        JwtTokenDto jwtTokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 5. RefreshToken Redis 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(),
                        jwtTokenDto.getRefreshToken(),
                        jwtTokenDto.getRefreshTokenExpiresIn(),
                        TimeUnit.MILLISECONDS);

        // 토큰 발급
        return jwtTokenDto;
    }

    @Transactional
    @Override
    public String findPassword(MemberRequestFindPasswordDto findPasswordDto) {
        Member findMember = this.memberPort.findByEmail(findPasswordDto.getEmail()).orElseThrow(() -> {
                    throw new BadRequestException(ErrorCode.INVALID_EMAIL, "이 이메일로 가입된 사용자가 없습니다.");
                }
        );
        findMember.validateFindPassword(findPasswordDto);

        naverMailSender.sendPassword(findMember);

        return "이메일로 임시 비밀번호를 보냈습니다.";
    }
}