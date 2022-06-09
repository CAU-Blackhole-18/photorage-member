package cauBlackHole.photoragemember.application.port.inPort;

import cauBlackHole.photoragemember.application.DTO.member.MemberRequestFindPasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdateDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdatePasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;

import java.util.List;


public interface MemberServiceUseCase {
    // 현재 SecurityContext 에 있는 유저 정보 가져오기
    MemberResponseDto getMyInfo();

    List<MemberResponseDto> findByName(String name);

    MemberResponseDto updateMyInfo(MemberRequestUpdateDto memberRequestUpdateDto);

    String leave();

    MemberResponseDto updatePassword(MemberRequestUpdatePasswordDto updatePasswordDto);
}
