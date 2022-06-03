package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.adapter.persistence.MemberRepository;
import cauBlackHole.photoragemember.adapter.persistence.RefreshTokenRepository;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.port.in.AuthServiceUseCase;
import cauBlackHole.photoragemember.application.port.out.MemberPort;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
import cauBlackHole.photoragemember.config.jwt.JwtTokenProvider;
import cauBlackHole.photoragemember.config.jwt.RefreshToken;
import cauBlackHole.photoragemember.domain.Member;
import cauBlackHole.photoragemember.domain.MemberDomainModel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceUseCase {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private final MemberPort memberPort;

    private  final MemberRepository memberRepository;

    @Transactional
    @Override
    public MemberResponseDto signUp(MemberRequestSignUpDto memberRequestSignUpDto) {

        if (memberRepository.existsByEmail(memberRequestSignUpDto.getEmail())) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다");
        }

        Member member = memberRequestSignUpDto.toMember(passwordEncoder);
        return MemberResponseDto.of(memberRepository.save(member));
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

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(jwtTokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        // 5. 토큰 발급
        return jwtTokenDto;
    }

    @Transactional
    @Override
    public JwtTokenDto reissue(JwtTokenRequestDto jwtTokenRequestDto, HttpServletRequest request) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(jwtTokenRequestDto.getRefreshToken(), request)) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(jwtTokenRequestDto.getAccessToken());

        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치하는지 검사
        if (!refreshToken.getValue().equals(jwtTokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        JwtTokenDto jwtTokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = refreshToken.updateValue(jwtTokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        // 토큰 발급
        return jwtTokenDto;
    }
}
