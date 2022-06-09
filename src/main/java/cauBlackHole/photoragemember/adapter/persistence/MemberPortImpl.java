package cauBlackHole.photoragemember.adapter.persistence;
import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.ConflictException;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.domain.Member;
import cauBlackHole.photoragemember.domain.MemberDomainModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberPortImpl extends DomainModelMapper implements MemberPort {

    private final MemberRepository memberRepository;

    @Override
    public Optional<MemberDomainModel> findEmail(String email) {
        return this.memberRepository.findByEmail(email).stream().map(this::entityToDomainModel).findAny();
    }

    @Override
    public Optional<MemberDomainModel> findForPassword(String email, String name) {
        return this.memberRepository.findByEmailAndName(email, name).map(this::entityToDomainModel);
    }

    @Override
    public Optional<MemberDomainModel> findById(String id) {
        return this.memberRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public List<MemberDomainModel> findByName(String name) {
        return this.memberRepository.findByName(name)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MemberDomainModel> findByEmail(String email) {
        return this.memberRepository.findByEmail(email).map(this::entityToDomainModel);
    }

    @Override
    public Optional<MemberDomainModel> update(String id, MemberDomainModel memberDomainModel) {
        return this.memberRepository.findById(id).map(
                srcUser -> {
                    srcUser.update(memberDomainModel.getName(), memberDomainModel.getNickname());
                    return this.entityToDomainModel(this.memberRepository.save(srcUser));
                }
        );
    }

    @Override
    public Optional<MemberDomainModel> updatePassword(String id, String password) {
        return this.memberRepository.findById(id).map(
                srcUser -> {
                    srcUser.setPassword(password);

                    return this.entityToDomainModel(this.memberRepository.save(srcUser));
                }
        );
    }

    @Override
    public MemberDomainModel create(MemberDomainModel memberDomainModel) {
        Member member = new Member(
                memberDomainModel.getEmail(),
                memberDomainModel.getPassword(),
                memberDomainModel.getName(),
                memberDomainModel.getNickname(),
                memberDomainModel.getAuthority()
        );
        return this.entityToDomainModel(this.memberRepository.save(member));
    }

    @Override
    public void delete(MemberDomainModel memberDomainModel) {
        try {
            this.memberRepository.deleteById(memberDomainModel.getId());
        }
        catch (Exception e){
            throw new ConflictException(ErrorCode.DELETE_FAIL, "회원 탈퇴를 실패하셨습니다");
        }
    }
}
