package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestFindPasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
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
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

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
        when(memberPort.findByEmail(memberRequestSignUpDto.getEmail())).thenReturn(Optional.empty());
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
        verify(memberPort).findByEmail(anyString());
        verify(memberPort).create(any(Member.class));
    }
    @Test
    @DisplayName("회원 가입 실패(이메일 중복)")
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
                .isInstanceOf(BadRequestException.class).hasMessage("중복되는 이메일 입니다.");
        verify(memberPort).findByEmail(anyString());
    }

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
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManagerBuilder.getObject().authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtTokenProvider.generateTokenDto(authentication)).thenReturn(
                new JwtTokenDto("Bear",
                        "accessToken",
                        "refreshToken",
                        10000L,
                        10000L));
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        //when
        JwtTokenDto jwtTokenDto = authService.signIn(memberRequestSignInDto);
        //then
        assertThat(jwtTokenDto.getGrantType()).isEqualTo("Bear");
        assertThat(jwtTokenDto.getAccessToken()).isEqualTo("accessToken");
        assertThat(jwtTokenDto.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(jwtTokenDto.getAccessTokenExpiresIn()).isEqualTo(10000L);
        assertThat(jwtTokenDto.getRefreshTokenExpiresIn()).isEqualTo(10000L);
        verify(memberPort).findByEmail(anyString());
        verify(passwordEncoder).matches(any(CharSequence.class), any(String.class));
        verify(jwtTokenProvider).generateTokenDto(any(Authentication.class));
        verify(redisTemplate).opsForValue();
    }

    @Test
    @DisplayName("회원 로그인 실패(이메일 틀림)")
    void signInFailEmail() {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "photorage@gmail.com",
                "caublackhole1!"
        );
        when(memberPort.findByEmail(memberRequestSignInDto.getEmail()))
                .thenReturn(Optional.empty());

        //then
        assertThatThrownBy(()->authService.signIn(memberRequestSignInDto))
                .isInstanceOf(BadRequestException.class).hasMessage("이메일이 틀렸습니다.");

        verify(memberPort).findByEmail(anyString());
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
                .isInstanceOf(BadRequestException.class).hasMessage("비밀번호가 틀렸습니다.");
        verify(memberPort).findByEmail(anyString());
        verify(passwordEncoder).matches(any(CharSequence.class), any(String.class));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() {
        //given
        JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto = new JwtTokenRequestLogoutDto("accessToken");
        when(jwtTokenProvider.validateToken(jwtTokenRequestLogoutDto.getAccessToken())).thenReturn(true);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("hi");
        when(jwtTokenProvider.getAuthentication(jwtTokenRequestLogoutDto.getAccessToken())).thenReturn(authentication);
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valueOps.get(anyString())).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(jwtTokenProvider.getExpiration(jwtTokenRequestLogoutDto.getAccessToken())).thenReturn(1000L);

        //when
        String logout = authService.logout(jwtTokenRequestLogoutDto);
        //then
        assertThat(logout).isEqualTo("로그아웃 되었습니다.");
        verify(jwtTokenProvider).validateToken(anyString());
        verify(jwtTokenProvider).getAuthentication(anyString());
        verify(redisTemplate, times(2)).opsForValue();
        verify(jwtTokenProvider).getExpiration(anyString());
    }

    @Test
    @DisplayName("로그아웃 실패")
    void logoutFail() {
        //given
        JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto = new JwtTokenRequestLogoutDto("accessToken");
        when(jwtTokenProvider.validateToken(jwtTokenRequestLogoutDto.getAccessToken())).thenReturn(false);
        //when
        //then
        assertThatThrownBy(()->authService.logout(jwtTokenRequestLogoutDto))
                .isInstanceOf(UnauthorizedException.class).hasMessage("잘못된 요청입니다.");
        verify(jwtTokenProvider).validateToken(anyString());
    }

    @Test
    @DisplayName("jwt 토큰 reissue 성공")
    void reissueSuccess() {
        //given
        JwtTokenRequestReissueDto jwtTokenRequestReissueDto = new JwtTokenRequestReissueDto("accessToken", "refreshToken");
        when(jwtTokenProvider.validateToken(jwtTokenRequestReissueDto.getRefreshToken())).thenReturn(true);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("hi");
        when(jwtTokenProvider.getAuthentication(jwtTokenRequestReissueDto.getAccessToken())).thenReturn(authentication);

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valueOps.get("RT:" + authentication.getName())).thenReturn("refreshToken");
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        when(jwtTokenProvider.generateTokenDto(authentication)).thenReturn(new JwtTokenDto(
                "Bear",
                "accessToken",
                "refreshToken",
                1000L,
                1000L
        ));
        //when
        JwtTokenDto reissue = authService.reissue(jwtTokenRequestReissueDto);

        //then
        assertThat(reissue.getGrantType()).isEqualTo("Bear");
        assertThat(reissue.getAccessToken()).isEqualTo("accessToken");
        assertThat(reissue.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(reissue.getAccessTokenExpiresIn()).isEqualTo(1000L);
        assertThat(reissue.getRefreshTokenExpiresIn()).isEqualTo(1000L);
        verify(jwtTokenProvider).validateToken(anyString());
        verify(jwtTokenProvider).getAuthentication(anyString());
        verify(redisTemplate, times(2)).opsForValue();
        verify(jwtTokenProvider).generateTokenDto(any(Authentication.class));
    }

    @Test
    @DisplayName("jwt 토큰 reissue 실패(refresh토큰이 틀림)")
    void reissueFailWrongRefreshToken() {
        //given
        JwtTokenRequestReissueDto jwtTokenRequestReissueDto = new JwtTokenRequestReissueDto("accessToken", "refreshToken");
        when(jwtTokenProvider.validateToken(jwtTokenRequestReissueDto.getRefreshToken())).thenReturn(false);

        //when
        //then
        assertThatThrownBy(()->authService.reissue(jwtTokenRequestReissueDto))
                .isInstanceOf(UnauthorizedException.class).hasMessage("Refresh Token이 유효하지 않습니다.");
        verify(jwtTokenProvider).validateToken(anyString());
    }

    @Test
    @DisplayName("jwt 토큰 reissue 실패(로그아웃되어 refreshToken이 Redis에 없는 경우")
    void reissueFailNoRefreshTokenInRedis() {
        //given
        JwtTokenRequestReissueDto jwtTokenRequestReissueDto = new JwtTokenRequestReissueDto("accessToken", "refreshToken");
        when(jwtTokenProvider.validateToken(jwtTokenRequestReissueDto.getRefreshToken())).thenReturn(true);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("hi");
        when(jwtTokenProvider.getAuthentication(jwtTokenRequestReissueDto.getAccessToken())).thenReturn(authentication);

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valueOps.get("RT:" + authentication.getName())).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        //when
        //then
        assertThatThrownBy(()->authService.reissue(jwtTokenRequestReissueDto))
                .isInstanceOf(UnauthorizedException.class).hasMessage("잘못된 요청입니다.");

        verify(jwtTokenProvider).validateToken(anyString());
        verify(jwtTokenProvider).getAuthentication(anyString());
        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    @DisplayName("jwt 토큰 reissue 실패(현재 보낸 refreshToken 유효하지 않음")
    void reissueFailInvalidRefreshToken() {
        //given
        JwtTokenRequestReissueDto jwtTokenRequestReissueDto = new JwtTokenRequestReissueDto("accessToken", "refreshToken");
        when(jwtTokenProvider.validateToken(jwtTokenRequestReissueDto.getRefreshToken())).thenReturn(true);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("hi");
        when(jwtTokenProvider.getAuthentication(jwtTokenRequestReissueDto.getAccessToken())).thenReturn(authentication);

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valueOps.get("RT:" + authentication.getName())).thenReturn("noToken");
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        //when
        //then
        assertThatThrownBy(()->authService.reissue(jwtTokenRequestReissueDto))
                .isInstanceOf(UnauthorizedException.class).hasMessage("Refresh Token이 유효하지 않습니다.");
        verify(jwtTokenProvider).validateToken(anyString());
        verify(jwtTokenProvider).getAuthentication(anyString());
        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    @DisplayName("비밀번호 찾기 성공")
    void findPasswordSuccess() {
        //given
        MemberRequestFindPasswordDto findPasswordDto = new MemberRequestFindPasswordDto(
                "photorage@gmail.com",
                "name",
                "nickname"
        );
        when(memberPort.findByEmail(findPasswordDto.getEmail())).thenReturn(Optional.of(Member.createMember(
                "photorage@gmail.com",
                "askdaslfkjaf123214ads",
                "name",
                "nickname",
                Authority.ROLE_USER
        )));

        //when
        String password = authService.findPassword(findPasswordDto);
        //then
        assertThat(password).isEqualTo("이메일로 임시 비밀번호를 보냈습니다.");
        verify(memberPort).findByEmail(anyString());
    }

    @Test
    @DisplayName("비밀번호 찾기 실패(회원 못 찾음)")
    void findPasswordNoMember() {
        //given
        MemberRequestFindPasswordDto findPasswordDto = new MemberRequestFindPasswordDto(
                "photorage@gmail.com",
                "name",
                "nickname"
        );
        when(memberPort.findByEmail(findPasswordDto.getEmail())).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(()->authService.findPassword(findPasswordDto))
                .isInstanceOf(BadRequestException.class).hasMessage("이 이메일로 가입된 사용자가 없습니다.");
        verify(memberPort).findByEmail(anyString());
    }
}