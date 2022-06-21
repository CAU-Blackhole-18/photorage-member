package cauBlackHole.photoragemember.adapter.web;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.*;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;

import cauBlackHole.photoragemember.application.service.MemberService;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.NotFoundException;

import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
import cauBlackHole.photoragemember.domain.member.Authority;
import cauBlackHole.photoragemember.domain.member.Member;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
        //@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles({"localtest", "ci"})
class MemberControllerSuperTest {

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
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("내 정보 조회 성공")
    @Transactional
    public void getMyMemberInfoSuccess() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();
        //when
        ResultActions perform = mockMvc.perform(
                get("/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
        );
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value(NAME))
                .andExpect(jsonPath("nickname").value(NICKNAME));
    }
    @Test
    @DisplayName("내 정보 조회 실패")
    @Transactional
    public void getMyMemberInfoFail() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        Optional<Member> member = this.memberPort.findByEmail(EMAIL);
        this.memberPort.delete(member.get());
        //when
        ResultActions perform = mockMvc.perform(
                get("/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
        );
        //then
        perform.andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(NotFoundException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("로그인 유저 정보가 없습니다."));
    }

    @Test
    @DisplayName("이름으로 조회 성공")
    @Transactional
    public void findByNameSuccess() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        String name = NAME;
        //when
        MvcResult mvcResult = mockMvc.perform(
                get("/members/" + name)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
        ).andReturn();
        //then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<MemberResponseDto> returnList = Arrays.asList(mapper.readValue(contentAsString, MemberResponseDto[].class));
        assertThat(returnList.size()).isEqualTo(1);
        returnList.forEach(m->{
            assertThat(m.getName()).isEqualTo(NAME);
        });
    }

    @Test
    @DisplayName("내 정보 업데이트 성공(전부 다 바꿈)")
    @Transactional
    public void updateMyInfoSuccessUpdateAll() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto("updateName", "updateNickname");
        //when
        ResultActions perform = mockMvc.perform(
                patch("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestUpdateDto)
                        )
        );
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value("updateName"))
                .andExpect(jsonPath("nickname").value("updateNickname"));
        Optional<Member> findMember = this.memberPort.findByEmail(EMAIL);
        assertThat(findMember.get()).isNotNull();
        assertThat(findMember.get().getEmail()).isEqualTo(EMAIL);
        assertThat(findMember.get().getName()).isEqualTo("updateName");
        assertThat(findMember.get().getNickname()).isEqualTo("updateNickname");
    }

    @Test
    @DisplayName("내 정보 업데이트 성공(이름만 바꿈)")
    @Transactional
    public void updateMyInfoSuccessUpdateName() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto("updateName", null);
        //when
        ResultActions perform = mockMvc.perform(
                patch("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestUpdateDto)
                        )
        );
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value("updateName"))
                .andExpect(jsonPath("nickname").value(NICKNAME));

        Optional<Member> findMember = this.memberPort.findByEmail(EMAIL);
        assertThat(findMember.get()).isNotNull();
        assertThat(findMember.get().getEmail()).isEqualTo(EMAIL);
        assertThat(findMember.get().getName()).isEqualTo("updateName");
        assertThat(findMember.get().getNickname()).isEqualTo(NICKNAME);
    }

    @Test
    @DisplayName("내 정보 업데이트 성공(닉네임만 바꿈)")
    @Transactional
    public void updateMyInfoSuccessUpdateNickname() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto(null, "updateNickname");
        //when
        ResultActions perform = mockMvc.perform(
                patch("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestUpdateDto)
                        )
        );
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value(NAME))
                .andExpect(jsonPath("nickname").value("updateNickname"));

        Optional<Member> findMember = this.memberPort.findByEmail(EMAIL);
        assertThat(findMember.get()).isNotNull();
        assertThat(findMember.get().getEmail()).isEqualTo(EMAIL);
        assertThat(findMember.get().getName()).isEqualTo(NAME);
        assertThat(findMember.get().getNickname()).isEqualTo("updateNickname");
    }

    @Test
    @DisplayName("내 정보 업데이트 성공(전부 안 바꿈)")
    @Transactional
    public void updateMyInfoSuccessUpdateNOTAll() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto(null, null);
        //when
        ResultActions perform = mockMvc.perform(
                patch("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestUpdateDto)
                        )
        );
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value(NAME))
                .andExpect(jsonPath("nickname").value(NICKNAME));

        Optional<Member> findMember = this.memberPort.findByEmail(EMAIL);
        assertThat(findMember.get()).isNotNull();
        assertThat(findMember.get().getEmail()).isEqualTo(EMAIL);
        assertThat(findMember.get().getName()).isEqualTo(NAME);
        assertThat(findMember.get().getNickname()).isEqualTo(NICKNAME);
    }

    @Test
    @DisplayName("내 정보 업데이트 실패(업데이트할 사용자 정보 없음 DB에)")
    @Transactional
    public void updateMyInfoSuccessFailNoMemberData() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        MemberRequestUpdateDto memberRequestUpdateDto = new MemberRequestUpdateDto("updateName", "updateNickname");
        Optional<Member> member = this.memberPort.findByEmail(EMAIL);
        this.memberPort.delete(member.get());
        //when
        ResultActions perform = mockMvc.perform(
                patch("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestUpdateDto)
                        )
        );
        //then
        perform.andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(NotFoundException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("로그인 유저 정보가 없습니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    @Transactional
    void leaveSuccess() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();
        //when
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.delete("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
        ).andReturn();
        //then
        String returnValue = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(returnValue).isEqualTo("회원이 탈퇴되었습니다.");

        Optional<Member> findMember = this.memberPort.findByEmail(EMAIL);
        assertThat(findMember.isEmpty()).isSameAs(true);
    }

    @Test
    @DisplayName("회원 탈퇴 실패(로그인 유저 정보 없음)")
    @Transactional
    void leaveFailNoMemberData() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        Optional<Member> member = this.memberPort.findByEmail(EMAIL);
        this.memberPort.delete(member.get());

        //when
        ResultActions perform = mockMvc.perform(
                MockMvcRequestBuilders.delete("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
        );
        //then
        perform.andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(NotFoundException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("로그인 유저 정보가 없습니다."));
    }

    @Test
    @DisplayName("비밀번호 업데이트 성공")
    @Transactional
    void updatePasswordSuccess() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        MemberRequestUpdatePasswordDto memberRequestUpdatePasswordDto = new MemberRequestUpdatePasswordDto(
                PASSWORD, "newPassword12!"
        );
        //when
        ResultActions perform = mockMvc.perform(
                put("/members/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestUpdatePasswordDto)
                        )
        );
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value(NAME))
                .andExpect(jsonPath("nickname").value(NICKNAME));

        Optional<Member> findMember = this.memberPort.findByEmail(EMAIL);
        assertThat(findMember.get()).isNotNull();
        assertThat(findMember.get().getEmail()).isEqualTo(EMAIL);
        assertThat(findMember.get().getName()).isEqualTo(NAME);
        assertThat(findMember.get().getNickname()).isEqualTo(NICKNAME);
        assertThat(passwordEncoder.matches("newPassword12!", findMember.get().getPassword())).isSameAs(true);
    }

    @Test
    @DisplayName("비밀번호 업데이트 실패(로그인 유저 정보 없음)")
    @Transactional
    void updatePasswordFailNoMemberData() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        MemberRequestUpdatePasswordDto memberRequestUpdatePasswordDto = new MemberRequestUpdatePasswordDto(
                PASSWORD, "newPassword12!"
        );
        Optional<Member> member = this.memberPort.findByEmail(EMAIL);
        this.memberPort.delete(member.get());

        //when
        ResultActions perform = mockMvc.perform(
                put("/members/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestUpdatePasswordDto)
                        )
        );
        //then
        perform.andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(NotFoundException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("로그인 유저 정보가 없습니다."));
    }
    @Test
    @DisplayName("비밀번호 업데이트 실패(비밀번호가 틀림)")
    @Transactional
    void updatePasswordFailWrongPassword() throws Exception{
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.memberService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);

        String accessToken = jwtTokenDto.getAccessToken();

        MemberRequestUpdatePasswordDto memberRequestUpdatePasswordDto = new MemberRequestUpdatePasswordDto(
                "failpassword1!", "newPassword12!"
        );

        //when
        ResultActions perform = mockMvc.perform(
                put("/members/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestUpdatePasswordDto)
                        )
        );
        //then
        perform.andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(BadRequestException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("비밀번호가 틀렸습니다."));
    }

    @Test
    @DisplayName("회원 가입 성공")
    @Transactional
    public void signUpSuccess() throws Exception {
        //given
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);

        //when
        ResultActions perform = mockMvc.perform(
                post("/members/sign-up")
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
                .andExpect(jsonPath("nickname").value(NICKNAME));

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
                post("/members/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                mapper.writeValueAsString(memberRequestSignUpDto)
                        ));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(BadRequestException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("중복되는 이메일 입니다."));
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
                post("/members/sign-in")
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
                .andExpect(jsonPath("refreshTokenExpiresIn").isNumber());

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
                post("/members/sign-in")
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
                post("/members/sign-in")
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
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);
        JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto = new JwtTokenRequestLogoutDto(jwtTokenDto.getAccessToken());
        //when
        MvcResult result = mockMvc.perform(
                post("/members/logout")
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
                post("/members/logout")
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
        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(
                "test@gmail.com",
                "caublackhole1!"
        );
        JwtTokenDto jwtTokenDto = this.memberService.signIn(memberRequestSignInDto);
        JwtTokenRequestReissueDto jwtTokenRequestReissueDto = new JwtTokenRequestReissueDto(jwtTokenDto.getAccessToken(), jwtTokenDto.getRefreshToken());
        //when
        ResultActions perform = mockMvc.perform(
                post("/members/reissue")
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