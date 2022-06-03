package cauBlackHole.photoragemember.application.port.in;

import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;


public interface MemberServiceUseCase {

    public MemberResponseDto getMemberInfo(String email);

    // 현재 SecurityContext 에 있는 유저 정보 가져오기
    MemberResponseDto getMyInfo();
}
