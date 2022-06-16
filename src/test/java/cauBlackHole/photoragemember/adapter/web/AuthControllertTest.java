package cauBlackHole.photoragemember.adapter.web;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.application.service.AuthService;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
import cauBlackHole.photoragemember.domain.Authority;
import cauBlackHole.photoragemember.domain.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = "spring.config.location=classpath:application-localtest.yml")
@AutoConfigureMockMvc
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
    private MemberPort memberPort;

    @Autowired
    private AuthService authService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private Member testMember;
    @BeforeEach
    public void setUp()
    {
        testMember = Member.createMember(
                "test@gmail.com",
                "$2a$10$guhrZCzpW8MKg1hb7zpc5eWml573KR0cvLIFo0PkbFNDkBKIjoQ1C",
                "test",
                "test",
                Authority.ROLE_USER
        );
        this.memberPort.create(testMember);
    }
    @AfterEach
    public void delete(){
        this.memberPort.delete(testMember);
        Objects.requireNonNull(this.redisTemplate.keys("*")).forEach(k-> {
            redisTemplate.delete(k);
        });
    }

    @Test
    @DisplayName("회원 가입 성공")
    @Transactional
    public void signUpSuccess() throws Exception {
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);

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
        Optional<Member> findMember = this.memberPort.findByEmail(EMAIL);
        assertThat(findMember.get()).isNotNull();
        assertThat(findMember.get().getEmail()).isEqualTo(EMAIL);
        assertThat(findMember.get().getName()).isEqualTo(NAME);
        assertThat(findMember.get().getNickname()).isEqualTo(NICKNAME);
    }

    @Test
    @DisplayName("회원 가입 실패(이메일 중복)")
    @Transactional
    public void signUpFailDuplicateEmail() throws Exception {
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(
                "test@gmail.com", "nickname", "name", "caublackhole1!"
        );

        //when
        ResultActions perform = mockMvc.perform(
                post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestSignUpDto)
                        ));

       perform
               .andExpect(status().isBadRequest())
               .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(BadRequestException.class))
               .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("중복되는 이메일 입니다."))
               .andDo(print());
    }

    @Test
    @DisplayName("회원 로그인 성공")
    @Transactional
    public void signInSuccess() throws Exception {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "test@gmail.com",
                "caublackhole1!"
        );
        //when
        ResultActions perform = mockMvc.perform(
                post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestSignInDto)
                        )
        );

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("grantType").value("bearer"))
                .andExpect(jsonPath("accessToken").exists())
                .andExpect(jsonPath("accessToken").isString())
                .andExpect(jsonPath("refreshToken").exists())
                .andExpect(jsonPath("refreshToken").isString())
                .andExpect(jsonPath("accessTokenExpiresIn").exists())
                .andExpect(jsonPath("accessTokenExpiresIn").isNumber())
                .andExpect(jsonPath("refreshTokenExpiresIn").exists())
                .andExpect(jsonPath("refreshTokenExpiresIn").isNumber())
                .andDo(print());
        assertThat(redisTemplate.hasKey("RT:" + "test@gmail.com")).isSameAs(true);
    }

    @Test
    @DisplayName("회원 로그인 실패(이메일 틀림)")
    @Transactional
    public void signInFailWrongEmail() throws Exception {
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "photorage@gmail.com",
                "caublackhole1!"
        );

        //when
        ResultActions perform = mockMvc.perform(
                post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestSignInDto)
                        )
        );
        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(BadRequestException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("이메일이 틀렸습니다."));
    }

    @Test
    @DisplayName("회원 로그인 실패(비밀번호 틀림)")
    @Transactional
    public void signInFailWrongPassword() throws Exception{
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "test@gmail.com",
                "failpassword1!"
        );
        //when
        ResultActions perform = mockMvc.perform(
                post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestSignInDto)
                        )
        );
        //then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(BadRequestException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("비밀번호가 틀렸습니다."));
    }

    @Test
    @DisplayName("로그아웃 성공")
    @Transactional
    public void logoutSuccess() throws Exception{
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "test@gmail.com",
                "caublackhole1!"
        );
        JwtTokenDto jwtTokenDto = this.authService.signIn(memberRequestSignInDto);
        JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto = new JwtTokenRequestLogoutDto(jwtTokenDto.getAccessToken());
        //when
        MvcResult result = mockMvc.perform(
                post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(jwtTokenRequestLogoutDto)
                        )

        ).andReturn();
        String returnValue = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(returnValue).isEqualTo("로그아웃 되었습니다.");
        assertThat(redisTemplate.hasKey("RT:" + "test@gmail.com")).isSameAs(false);
        assertThat(redisTemplate.hasKey(jwtTokenDto.getAccessToken())).isSameAs(true);
        assertThat(redisTemplate.opsForValue().get(jwtTokenDto.getAccessToken())).isEqualTo("logout");
    }

    @Test
    @DisplayName("로그아웃 실패(accessToken not Validated)")
    @Transactional
    public void logoutFailUnvalidatedAccessToken() throws Exception{
        //given
        JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto = new JwtTokenRequestLogoutDto("wrongAccessToken");
        //when
        ResultActions perform = mockMvc.perform(
                post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(jwtTokenRequestLogoutDto)
                        )

        );
        //then
        perform.andExpect(status().isUnauthorized())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(UnauthorizedException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("잘못된 요청입니다."));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    @Transactional
    public void reissueSuccess() throws Exception {
        //given
        //given
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "test@gmail.com",
                "caublackhole1!"
        );
        JwtTokenDto jwtTokenDto = this.authService.signIn(memberRequestSignInDto);
        JwtTokenRequestReissueDto jwtTokenRequestReissueDto = new JwtTokenRequestReissueDto(jwtTokenDto.getAccessToken(), jwtTokenDto.getRefreshToken());
        //when
        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(jwtTokenRequestReissueDto)
                        )
        );
        //then
        perform.andExpect(status().isCreated())
                .andExpect(jsonPath("accessToken").exists())
                .andExpect(jsonPath("accessToken").isString())
                .andExpect(jsonPath("refreshToken").exists())
                .andExpect(jsonPath("refreshToken").isString());
        assertThat(redisTemplate.hasKey("RT:"+"test@gmail.com")).isSameAs(true);
    }

    //이메일 정보때문에 수동으로 테스트 해야함
    @Test
    void findPassword() {
    }
}