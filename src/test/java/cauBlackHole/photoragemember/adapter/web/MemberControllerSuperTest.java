package cauBlackHole.photoragemember.adapter.web;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.member.*;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.application.service.AuthService;

import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.NotFoundException;

import cauBlackHole.photoragemember.domain.Member;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken = "";

    @BeforeAll
    public void setup(){
        MemberRequestSignUpDto memberRequestSignUpDto = new MemberRequestSignUpDto(EMAIL, NICKNAME, NAME, PASSWORD);
        this.authService.signUp(memberRequestSignUpDto);

        MemberRequestSignInDto memberRequestSignInDto = new MemberRequestSignInDto(EMAIL, PASSWORD);
        JwtTokenDto jwtTokenDto = this.authService.signIn(memberRequestSignInDto);

        this.accessToken = jwtTokenDto.getAccessToken();
    }
    @Test
    @DisplayName("내 정보 조회 성공")
    @Transactional
    public void getMyMemberInfoSuccess() throws Exception{
        //given
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
        //when
        MvcResult mvcResult = mockMvc.perform(
                delete("/members")
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
        Optional<Member> member = this.memberPort.findByEmail(EMAIL);
        this.memberPort.delete(member.get());

        //when
        ResultActions perform = mockMvc.perform(
                delete("/members")
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

}