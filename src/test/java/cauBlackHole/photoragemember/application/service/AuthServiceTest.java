package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.jwt.JwtTokenProvider;
import cauBlackHole.photoragemember.domain.Authority;
import cauBlackHole.photoragemember.domain.Member;
import cauBlackHole.photoragemember.infrastructure.NaverMailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private MemberPort memberPort;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private NaverMailSender naverMailSender;

    @InjectMocks
    private  AuthService authService;

    @Test
    @DisplayName("회원 가입 성공")
    void signUpSuccess() {
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(
                "photorage@gmail.com",
                "nickname",
                "name",
                "caublackhole1!"
        );
        when(memberPort.findByEmail(memberRequestSignUpDto.getEmail())).thenReturn(Optional.ofNullable(null));
        when(memberPort.create(any(Member.class))).thenReturn(Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "name",
                "nickname",
                Authority.ROLE_USER
        ));
        //when
        MemberResponseDto memberResponseDto = authService.signUp(memberRequestSignUpDto);
        //then
        assertThat(memberResponseDto.getEmail()).isEqualTo(memberRequestSignUpDto.getEmail());
        assertThat(memberResponseDto.getName()).isEqualTo(memberRequestSignUpDto.getName());
        assertThat(memberResponseDto.getNickname()).isEqualTo(memberRequestSignUpDto.getNickname());
    }
    @Test
    @DisplayName("회원 가입 실패")
    void signUpFail() {
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(
                "photorage@gmail.com",
                "nickname",
                "name",
                "caublackhole1!"
        );
        when(memberPort.findByEmail(memberRequestSignUpDto.getEmail()))
                .thenReturn(Optional.of(Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "kang",
                "kang",
                Authority.ROLE_USER
        )));

        assertThatThrownBy(()->authService.signUp(memberRequestSignUpDto))
                .isInstanceOf(BadRequestException.class);
    }
/*
    @Test
    @DisplayName("회원 로그인 성공")
    void signInSuccess() {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "photorage@gmail.com",
                "caublackhole1!"
        );
        when(memberPort.findByEmail(memberRequestSignInDto.getEmail()))
                .thenReturn(Optional.of(Member.createMember(
                        "photorage@gmail.com",
                        "caublackhole1!",
                        "name",
                        "nickname",
                        Authority.ROLE_USER
                )));
        when(passwordEncoder.matches(any(CharSequence.class), any(String.class))).thenReturn(true);
        when(authenticationManagerBuilder.getObject().authenticate(memberRequestSignInDto.toAuthentication()))
                .thenReturn(any(Authentication.class));
        when(jwtTokenProvider.generateTokenDto(any(Authentication.class))).thenReturn(
                new JwtTokenDto("Bear",
                        "accessToken",
                        "refreshToken",
                        10000L,
                        10000L));

        //when
        JwtTokenDto jwtTokenDto = authService.signIn(memberRequestSignInDto);
        //then
        assertThat(jwtTokenDto.getGrantType()).isEqualTo("Bear");
        assertThat(jwtTokenDto.getAccessToken()).isEqualTo("accessToken");
        assertThat(jwtTokenDto.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(jwtTokenDto.getAccessTokenExpiresIn()).isEqualTo(10000L);
        assertThat(jwtTokenDto.getRefreshTokenExpiresIn()).isEqualTo(10000L);
    }
*/
    @Test
    @DisplayName("회원 로그인 실패(이메일 틀림)")
    void signInFaileEmail() {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "photorage@gmail.com",
                "caublackhole1!"
        );
        when(memberPort.findByEmail(memberRequestSignInDto.getEmail()))
                .thenReturn(Optional.ofNullable(null));

        //then
        assertThatThrownBy(()->authService.signIn(memberRequestSignInDto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("회원 로그인 실패(비밀번호 틀림)")
    void signInFailPassword() {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "photorage@gmail.com",
                "caublackhole1!"
        );
        when(memberPort.findByEmail(memberRequestSignInDto.getEmail()))
                .thenReturn(Optional.of(Member.createMember(
                        "photorage@gmail.com",
                        "caublackhole2!",
                        "name",
                        "nickname",
                        Authority.ROLE_USER
                )));

        when(passwordEncoder.matches(any(CharSequence.class), any(String.class))).thenReturn(false);
        //then
        assertThatThrownBy(()->authService.signIn(memberRequestSignInDto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void logout() {
    }

    @Test
    void reissue() {
    }

    @Test
    void findPassword() {
    }
}