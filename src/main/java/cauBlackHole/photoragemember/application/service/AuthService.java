package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.port.inPort.AuthServiceUseCase;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
import cauBlackHole.photoragemember.config.jwt.JwtTokenProvider;
import cauBlackHole.photoragemember.domain.MemberDomainModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceUseCase {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberPort memberPort;
    private final RedisTemplate redisTemplate;

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
        MemberDomainModel memberDomainModel = MemberDomainModel.of(
                memberRequestSignUpDto.getEmail(),
                memberRequestSignUpDto.getName(),
                passwordEncoder.encode(memberRequestSignUpDto.getPassword())
        );

        return MemberResponseDto.of(this.memberPort.create(memberDomainModel));
    }

    @Transactional
    @Override
    public JwtTokenDto signIn(MemberRequestSignInDto memberRequestSignInDto) {
        MemberDomainModel memberDomainModel = this.memberPort.findByEmail(memberRequestSignInDto.getEmail()).orElseThrow(
                ()-> new UnauthorizedException(
                        ErrorCode.INVALID_EMAIL,
                        "잘못된 이메일 입니다."
                )
        );
        if(!passwordEncoder.matches(memberRequestSignInDto.getPassword(), memberDomainModel.getPassword())){
            throw new UnauthorizedException(
                    ErrorCode.INVALID_PASSWORD,
                    "비밀번호를 잘못 입력했습니다."
            );
        }

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
    public String logout(JwtTokenRequestDto jwtTokenRequestDto)
    {
        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(jwtTokenRequestDto.getAccessToken())) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_JWT, "잘못된 요청입니다.");
        }
        // 2. Access Token 에서 User의 name(id)을 가져온다.
        Authentication authentication = jwtTokenProvider.getAuthentication(jwtTokenRequestDto.getAccessToken());
        // 3. Redis 에서 해당 User의 name(email) 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제합니다.
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT:" + authentication.getName());
        }
        // 4. 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = jwtTokenProvider.getExpiration(jwtTokenRequestDto.getAccessToken());
        redisTemplate.opsForValue()
                .set(jwtTokenRequestDto.getAccessToken(), "logout", expiration, TimeUnit.MILLISECONDS);

        return "로그아웃 되었습니다.";
    }
    @Transactional
    @Override
    public JwtTokenDto reissue(JwtTokenRequestDto jwtTokenRequestDto) {
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
}
