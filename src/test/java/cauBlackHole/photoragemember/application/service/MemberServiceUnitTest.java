package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdateDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdatePasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.ConflictException;
import cauBlackHole.photoragemember.config.exception.NotFoundException;
import cauBlackHole.photoragemember.config.util.SecurityUtil;
import cauBlackHole.photoragemember.domain.Authority;
import cauBlackHole.photoragemember.domain.Member;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
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
}