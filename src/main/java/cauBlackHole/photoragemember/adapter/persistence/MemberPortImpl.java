package cauBlackHole.photoragemember.adapter.persistence;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.ConflictException;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPortImpl implements MemberPort {

    private final MemberRepository memberRepository;

    @Override
    public List <Member> findEmail(String name, String nickname) {
        return this.memberRepository.findByNameAndNickname(name, nickname);
    }

    @Override
    public Optional<Member> findForPassword(String email, String name) {
        return this.memberRepository.findByEmailAndName(email, name).stream().findFirst();
    }

    @Override
    public Optional<Member> findById(String id) {
        return this.memberRepository.findById(id).stream().findFirst();
    }

    @Override
    public List<Member> findByName(String name) {
        return this.memberRepository.findByName(name);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return this.memberRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> update(Member updateMember) {

        return this.memberRepository.findById(updateMember.getId()).map(
                srcUser -> {
                    srcUser.update(updateMember.getName(), updateMember.getNickname());
                    return this.memberRepository.save(srcUser);
                }
        );
    }

    @Override
    public Member create(Member newMember) {
        return this.memberRepository.save(newMember);
    }

    @Override
    public void delete(Member member) {
        try {
            this.memberRepository.deleteById(member.getId());
        }
        catch (Exception e){
            throw new ConflictException(ErrorCode.DELETE_FAIL, "회원 탈퇴를 실패하셨습니다");
        }
    }
}
