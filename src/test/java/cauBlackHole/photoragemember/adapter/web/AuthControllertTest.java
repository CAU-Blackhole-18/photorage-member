package cauBlackHole.photoragemember.adapter.web;

import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.service.AuthService;
import cauBlackHole.photoragemember.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllertTest {

    private static final String EMAIL = "photorage@gmail.com";
    private static final String NAME = "KangWooSeok";
    private static final String NICKNAME = "Stone";
    private static final String PASSWORD = "caublackhole1!";

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("회원 가입 성공")
    void signUpSuccess() throws Exception {
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        MemberResponseDto memberResponseDto = new MemberResponseDto("id", EMAIL, NAME, NICKNAME);

        //when
        ResultActions perform = mockMvc.perform(
                post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestSignUpDto)
                        ));

        //then
        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value(NAME))
                .andExpect(jsonPath("nickname").value(NICKNAME))
                .andDo(print());
    }
/*
    @Test
    @DisplayName("회원 가입 실패(이메일 중복)")
    void signUpFailDuplicateEmail() throws Exception {
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(
                "photorage@gmail.com", "nickname", "name", "caublackhole1!"
        );
        given(authService.signUp(memberRequestSignUpDto)).willThrow(new BadRequestException(
                ErrorCode.DUPLICATE_EMAIL,
                "중복되는 이메일 입니다."
        ));

        assertThatThrownBy(()->authController.signUp(memberRequestSignUpDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복되는 이메일 입니다.");
        verify(authService).signUp(memberRequestSignUpDto);
    }

    @Test
    @DisplayName("회원 로그인 성공")
    void signInSuccess() {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "photorage@gmail.com",
                "caublackhole1!"
        );
        JwtTokenDto jwtTokenDto = new JwtTokenDto(
                "Bear",
                "accessToken",
                "refreshToken",
                1000L,
                1000L);

        given(this.authService.signIn(memberRequestSignInDto))
                .willReturn(jwtTokenDto);

        //when
        ResponseEntity<JwtTokenDto> responseEntity = authController.signIn(memberRequestSignInDto);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getGrantType()).isEqualTo("Bear");
        assertThat(responseEntity.getBody().getAccessToken()).isEqualTo("accessToken");
        assertThat(responseEntity.getBody().getRefreshToken()).isEqualTo("refreshToken");
        assertThat(responseEntity.getBody().getAccessTokenExpiresIn()).isEqualTo(1000L);
        assertThat(responseEntity.getBody().getRefreshTokenExpiresIn()).isEqualTo(1000L);
        verify(authService).signIn(any(MemberRequestSignInDto.class));
    }
    @Test
    @DisplayName("회원 로그인 실패(이메일 틀림)")
    void signInFailWrongEmail() {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "photorage@gmail.com",
                "caublackhole1!"
        );
        given(this.authService.signIn(memberRequestSignInDto))
                .willThrow(new BadRequestException(
                        ErrorCode.INVALID_EMAIL,
                        "이메일이 틀렸습니다."));

        //when
        assertThatThrownBy(()->authController.signIn(memberRequestSignInDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이메일이 틀렸습니다.");
        verify(authService).signIn(any(MemberRequestSignInDto.class));
    }

    @Test
    @DisplayName("회원 로그인 실패(비밀번호 틀림)")
    void signInFailWrongPassword() {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "photorage@gmail.com",
                "caublackhole1!"
        );
        given(this.authService.signIn(memberRequestSignInDto))
                .willThrow(new BadRequestException(ErrorCode.INVALID_PASSWORD, "비밀번호가 틀렸습니다."));

        //when
        assertThatThrownBy(()->authController.signIn(memberRequestSignInDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("비밀번호가 틀렸습니다.");
        verify(authService).signIn(any(MemberRequestSignInDto.class));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() {
        //given
        JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto = new JwtTokenRequestLogoutDto(
            "accessToken"
        );
        given(this.authService.logout(jwtTokenRequestLogoutDto)).willReturn("로그아웃 되었습니다.");

        //when
        String logoutResult = this.authService.logout(jwtTokenRequestLogoutDto);

        //then
        assertThat(logoutResult).isEqualTo("로그아웃 되었습니다.");
        verify(authService).logout(any(JwtTokenRequestLogoutDto.class));
    }

    @Test
    void reissue() {
    }

    @Test
    void findPassword() {
    }*/
}