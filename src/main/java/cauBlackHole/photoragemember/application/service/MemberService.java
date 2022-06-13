package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdateDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdatePasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.port.inPort.MemberServiceUseCase;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.*;
import cauBlackHole.photoragemember.config.util.SecurityUtil;
import cauBlackHole.photoragemember.domain.MemberDomainModel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberServiceUseCase {
    private final MemberPort memberPort;
    private final PasswordEncoder passwordEncoder;

    // 현재 SecurityContext 에 있는 유저 정보 가져오기
    @Transactional(readOnly = true)
    @Override
    public MemberResponseDto getMyInfo() {
        return this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER,"로그인 유저 정보가 없습니다."));
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemberResponseDto> findByName(String name) {
        return this.memberPort.findByName(name)
                .stream()
                .map(MemberResponseDto::of)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public MemberResponseDto updateMyInfo(MemberRequestUpdateDto memberRequestUpdateDto) {
        MemberDomainModel memberDomainModel = this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(originMemberDomainModel -> originMemberDomainModel.update(memberRequestUpdateDto))
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER, "로그인 유저 정보가 없습니다."));

        return this.memberPort.update(memberDomainModel.getId(), memberDomainModel)
                .map(MemberResponseDto::of)
                .orElseThrow(()-> new ConflictException(ErrorCode.UPDATE_FAIL, "현재 사용자 정보 업데이트를 실패했습니다."));
    }

    @Transactional
    @Override
    public String leave() {
        MemberDomainModel memberDomainModel = this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .orElseThrow(()->new NotFoundException(ErrorCode.NOT_FOUND_USER,"로그인 유저 정보가 없습니다."));

        this.memberPort.delete(memberDomainModel);

        return "회원이 탈퇴되었습니다.";
    }

    @Transactional
    @Override
    public MemberResponseDto updatePassword(MemberRequestUpdatePasswordDto updatePasswordDto) {
        MemberDomainModel memberDomainModel = this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(m->m.matchPassword(passwordEncoder, updatePasswordDto.getOriginPassword()))
                .orElseThrow(()->new NotFoundException(ErrorCode.NOT_FOUND_USER,"로그인 유저 정보가 없습니다."));

        return this.memberPort.updatePassword(memberDomainModel.getId(), passwordEncoder.encode(updatePasswordDto.getNewPassword()))
                .map(MemberResponseDto::of)
                .orElseThrow(()-> new ConflictException(ErrorCode.UPDATE_FAIL, "비밀번호 업데이트를 실패했습니다."));
    }
}