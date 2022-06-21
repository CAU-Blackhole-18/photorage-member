package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.*;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.ConflictException;
import cauBlackHole.photoragemember.config.exception.NotFoundException;
import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
import cauBlackHole.photoragemember.config.jwt.JwtTokenProvider;
import cauBlackHole.photoragemember.config.util.SecurityUtil;
import cauBlackHole.photoragemember.domain.member.Authority;
import cauBlackHole.photoragemember.domain.member.Member;
import cauBlackHole.photoragemember.infrastructure.NaverMailSender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceUnitTest {

    @Mock
    private MemberPort memberPort;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private MemberService memberService;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private NaverMailSender naverMailSender;

    private static MockedStatic<SecurityUtil> mSecurityUtil;

    @BeforeAll
    public static void beforeClass(){
        mSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterClass(){
        mSecurityUtil.close();
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfoSuccess() {
        //given
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())).thenReturn(
                Optional.of(Member.createMember(
                        "photorage@gmail.com",
                        "caublackhole1!",
                        "name",
                        "nickname",
                        Authority.ROLE_USER
                ))
        );
        //when
        MemberResponseDto myInfo = memberService.getMyInfo();
        //then
        assertThat(myInfo.getEmail()).isEqualTo("photorage@gmail.com");
        assertThat(myInfo.getName()).isEqualTo("name");
        assertThat(myInfo.getNickname()).isEqualTo("nickname");
        verify(memberPort).findByEmail(anyString());
    }

    @Test
    @DisplayName("내 정보 조회 실패(내 정보가 없을 경우)")
    void getMyInfoFailNotFound() {
        //given
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())).thenReturn(
                Optional.empty()
        );
        //when
        //then
        assertThatThrownBy(()->memberService.getMyInfo())
                .isInstanceOf(NotFoundException.class).hasMessage("로그인 유저 정보가 없습니다.");
        verify(memberPort).findByEmail(anyString());
    }

    @Test
    @DisplayName("이름으로 정보 조회 성공")
    void findByNameSuccess() {
        //given
        String name = "kang";
        Member member = Member.createMember(
          "photorage@gmail.com",
          "caublackhoel1!",
          "kang",
          "nickname",
          Authority.ROLE_USER
        );
        when(this.memberPort.findByName("kang")).thenReturn(List.of(member));

        //when
        List<MemberResponseDto> memberResponseDtoList = this.memberService.findByName(name);

        //then
        assertThat(memberResponseDtoList).isNotEmpty();
        verify(memberPort).findByName("kang");
    }

    @Test
    @DisplayName("이름으로 정보 조회 실패(찾는 이름이 없음)")
    void findByNameFailNoName() {
        //given
        String name = "kang";
        when(this.memberPort.findByName("kang")).thenReturn(List.of());

        //when
        List<MemberResponseDto> memberResponseDtoList = this.memberService.findByName(name);

        //then
        assertThat(memberResponseDtoList).isEmpty();
        verify(memberPort).findByName("kang");
    }

    @Test
    @DisplayName("내 정보 업데이트 성공(이름만, 닉네임 전부 다)")
    void updateMyInfoSuccessAll() {
        //given
        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto(
                "updateName",
                "updateNickname"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        Member updateMember = Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "name",
                "nickname",
                Authority.ROLE_USER
        );
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())).thenReturn(
                Optional.of(updateMember)
        );
        when(this.memberPort.update(updateMember)).thenReturn(
                Optional.of(Member.createMember(
                        "photorage@gmail.com",
                        "caublackhole1!",
                        "updateName",
                        "updateNickname",
                        Authority.ROLE_USER
                ))
        );
        //when
        MemberResponseDto memberResponseDto = this.memberService.updateMyInfo(memberRequestUpdateDto);
        //then
        assertThat(memberResponseDto.getName()).isEqualTo(memberRequestUpdateDto.getName());
        assertThat(memberResponseDto.getNickname()).isEqualTo(memberRequestUpdateDto.getNickname());
        verify(memberPort).findByEmail(anyString());
        verify(memberPort).update(any(Member.class));
    }

    @Test
    @DisplayName("내 정보 업데이트 성공(이름만)")
    void updateMyInfoSuccessOnlyName() {
        //given
        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto(
                "updateName",
                "null"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        Member updateMember = Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "name",
                "nickname",
                Authority.ROLE_USER
        );
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())).thenReturn(
                Optional.of(updateMember)
        );
        when(this.memberPort.update(updateMember)).thenReturn(
                Optional.of(Member.createMember(
                        "photorage@gmail.com",
                        "caublackhole1!",
                        "updateName",
                        "nickname",
                        Authority.ROLE_USER
                ))
        );
        //when
        MemberResponseDto memberResponseDto = this.memberService.updateMyInfo(memberRequestUpdateDto);
        //then
        assertThat(memberResponseDto.getName()).isEqualTo(memberRequestUpdateDto.getName());
        verify(memberPort).findByEmail(anyString());
        verify(memberPort).update(any(Member.class));
    }

    @Test
    @DisplayName("내 정보 업데이트 성공(별명만)")
    void updateMyInfoSuccessOnlyNickname() {
        //given
        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto(
                null,
                "updateNickname"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        Member updateMember = Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "name",
                "nickname",
                Authority.ROLE_USER
        );
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())).thenReturn(
                Optional.of(updateMember)
        );
        when(this.memberPort.update(updateMember)).thenReturn(
                Optional.of(Member.createMember(
                        "photorage@gmail.com",
                        "caublackhole1!",
                        "updateName",
                        "updateNickname",
                        Authority.ROLE_USER
                ))
        );
        //when
        MemberResponseDto memberResponseDto = this.memberService.updateMyInfo(memberRequestUpdateDto);
        //then
        assertThat(memberResponseDto.getNickname()).isEqualTo(memberRequestUpdateDto.getNickname());
        verify(memberPort).findByEmail(anyString());
        verify(memberPort).update(any(Member.class));
    }

    @Test
    @DisplayName("내 정보 업데이트 실패(로그인X)")
    void updateMyInfoFailNoLogin() {
        //given
        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto(
                "updateName",
                "updateNickname"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())).thenReturn(
                Optional.empty()
        );
        //when
        //then
        assertThatThrownBy(()->this.memberService.updateMyInfo(memberRequestUpdateDto))
                .isInstanceOf(NotFoundException.class).hasMessage("로그인 유저 정보가 없습니다.");
        verify(memberPort).findByEmail(anyString());
    }

    @Test
    @DisplayName("내 정보 업데이트 실패(충돌)")
    void updateMyInfoFailConflict() {
        //given
        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto(
                "updateName",
                "updateNickname"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        Member updateMember = Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "name",
                "nickname",
                Authority.ROLE_USER
        );
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())).thenReturn(
                Optional.of(updateMember)
        );
        when(this.memberPort.update(updateMember)).thenReturn(
                Optional.empty()
        );
        //when
        //then
        assertThatThrownBy(()->this.memberService.updateMyInfo(memberRequestUpdateDto))
                .isInstanceOf(ConflictException.class).hasMessage("현재 사용자 정보 업데이트를 실패했습니다.");
        verify(memberPort).findByEmail(anyString());
        verify(memberPort).update(any(Member.class));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void leaveSuccess() {
        //given
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail()))
                .thenReturn(Optional.of(Member.createMember(
                        "photorage@gmail.com",
                        "caublackhole1!",
                        "name",
                        "nickname",
                        Authority.ROLE_USER
                )));
        //when
        String leave = this.memberService.leave();
        //then
        assertThat(leave).isEqualTo("회원이 탈퇴되었습니다.");
        verify(memberPort).findByEmail(anyString());
    }
    @Test
    @DisplayName("회원 탈퇴 실패")
    void leaveFail() {
        //given
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail()))
                .thenReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(()->this.memberService.leave())
                .isInstanceOf(NotFoundException.class).hasMessage("로그인 유저 정보가 없습니다.");
        verify(memberPort).findByEmail(anyString());
    }

    @Test
    @DisplayName("비밀번호 업데이트 성공")
    void updatePasswordSuccess() {
        //given
        MemberRequestUpdatePasswordDto updatePasswordDto = new MemberRequestUpdatePasswordDto(
                "caublackhole1!",
                "caublackhole2!"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        Member findMember = Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "name",
                "nickname",
                Authority.ROLE_USER
        );
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail()))
                .thenReturn(Optional.of(findMember));
        when(passwordEncoder.matches(updatePasswordDto.getOriginPassword(), findMember.getPassword()))
                .thenReturn(true);

        when(passwordEncoder.encode(updatePasswordDto.getNewPassword())).thenReturn(updatePasswordDto.getNewPassword());

        when(this.memberPort.update(findMember)).thenReturn(Optional.of(Member.createMember(
                "photorage@gmail.com",
                updatePasswordDto.getNewPassword(),
                "name",
                "nickname",
                Authority.ROLE_USER
        )));
        //when
        MemberResponseDto memberResponseDto = this.memberService.updatePassword(updatePasswordDto);

        //then
        assertThat(memberResponseDto.getEmail()).isEqualTo("photorage@gmail.com");
        assertThat(memberResponseDto.getName()).isEqualTo("name");
        assertThat(memberResponseDto.getNickname()).isEqualTo("nickname");

        verify(memberPort).findByEmail(anyString());
        verify(passwordEncoder).matches(any(CharSequence.class), anyString());
        verify(passwordEncoder).encode(any(CharSequence.class));
        verify(memberPort).update(any(Member.class));
    }

    @Test
    @DisplayName("비밀번호 업데이트 실패(로그인 유저 정보x)")
    void updatePasswordFailNotLogin() {
        //given
        MemberRequestUpdatePasswordDto updatePasswordDto = new MemberRequestUpdatePasswordDto(
                "caublackhole1!",
                "caublackhole2!"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");

        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail()))
                .thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(()->this.memberService.updatePassword(updatePasswordDto))
                .isInstanceOf(NotFoundException.class).hasMessage("로그인 유저 정보가 없습니다.");

        verify(memberPort).findByEmail(anyString());
    }

    @Test
    @DisplayName("비밀번호 업데이트 실패(패스워드 틀림)")
    void updatePasswordFailWrongPassword() {
        //given
        MemberRequestUpdatePasswordDto updatePasswordDto = new MemberRequestUpdatePasswordDto(
                "caublackhole1!",
                "caublackhole2!"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        Member findMember = Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "name",
                "nickname",
                Authority.ROLE_USER
        );
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail()))
                .thenReturn(Optional.of(findMember));
        when(passwordEncoder.matches(updatePasswordDto.getOriginPassword(), findMember.getPassword()))
                .thenReturn(false);
        //when
        //then
        assertThatThrownBy(()->this.memberService.updatePassword(updatePasswordDto))
                .isInstanceOf(BadRequestException.class).hasMessage("비밀번호가 틀렸습니다.");

        verify(memberPort).findByEmail(anyString());
        verify(passwordEncoder).matches(any(CharSequence.class), anyString());
    }

    @Test
    @DisplayName("비밀번호 업데이트 실패(충돌)")
    void updatePasswordFailConflict() {
        //given
        MemberRequestUpdatePasswordDto updatePasswordDto = new MemberRequestUpdatePasswordDto(
                "caublackhole1!",
                "caublackhole2!"
        );
        when(SecurityUtil.getCurrentMemberEmail()).thenReturn("photorage@gmail.com");
        Member findMember = Member.createMember(
                "photorage@gmail.com",
                "caublackhole1!",
                "name",
                "nickname",
                Authority.ROLE_USER
        );
        when(this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail()))
                .thenReturn(Optional.of(findMember));
        when(passwordEncoder.matches(updatePasswordDto.getOriginPassword(), findMember.getPassword()))
                .thenReturn(true);

        when(passwordEncoder.encode(updatePasswordDto.getNewPassword())).thenReturn(updatePasswordDto.getNewPassword());

        when(this.memberPort.update(findMember)).thenReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(()->this.memberService.updatePassword(updatePasswordDto))
                .isInstanceOf(ConflictException.class).hasMessage("비밀번호 업데이트를 실패했습니다.");

        verify(memberPort).findByEmail(anyString());
        verify(passwordEncoder).matches(any(CharSequence.class), anyString());
        verify(passwordEncoder).encode(any(CharSequence.class));
        verify(memberPort).update(any(Member.class));
    }
    
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
        MemberResponseDto memberResponseDto = memberService.signUp(memberRequestSignUpDto);
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

        assertThatThrownBy(()->memberService.signUp(memberRequestSignUpDto))
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
        JwtTokenDto jwtTokenDto = memberService.signIn(memberRequestSignInDto);
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
        assertThatThrownBy(()->memberService.signIn(memberRequestSignInDto))
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
        assertThatThrownBy(()->memberService.signIn(memberRequestSignInDto))
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
        String logout = memberService.logout(jwtTokenRequestLogoutDto);
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
        assertThatThrownBy(()->memberService.logout(jwtTokenRequestLogoutDto))
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
        JwtTokenDto reissue = memberService.reissue(jwtTokenRequestReissueDto);

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
        assertThatThrownBy(()->memberService.reissue(jwtTokenRequestReissueDto))
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
        assertThatThrownBy(()->memberService.reissue(jwtTokenRequestReissueDto))
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
        assertThatThrownBy(()->memberService.reissue(jwtTokenRequestReissueDto))
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
        String password = memberService.findPassword(findPasswordDto);
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
        assertThatThrownBy(()->memberService.findPassword(findPasswordDto))
                .isInstanceOf(BadRequestException.class).hasMessage("이 이메일로 가입된 사용자가 없습니다.");
        verify(memberPort).findByEmail(anyString());
    }
}