package cauBlackHole.photoragemember.application.service;


import cauBlackHole.photoragemember.adapter.persistence.MemberRepository;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.port.in.MemberServiceUseCase;
import cauBlackHole.photoragemember.application.port.out.MemberPort;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.config.exception.NotFoundException;
import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
import cauBlackHole.photoragemember.config.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberServiceUseCase {
    private final MemberPort memberPort;
    @Transactional(readOnly = true)
    public MemberResponseDto getMemberInfo(String email) {
        return this.memberPort.findByEmail(email)
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER, "유저 정보가 없습니다."));
    }

    // 현재 SecurityContext 에 있는 유저 정보 가져오기
    @Transactional(readOnly = true)
    public MemberResponseDto getMyInfo() {
        return this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER,"로그인 유저 정보가 없습니다."));
    }
}