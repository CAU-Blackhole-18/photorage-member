package cauBlackHole.photoragemember.adapter.persistence;

import cauBlackHole.photoragemember.domain.Member;
import cauBlackHole.photoragemember.domain.MemberDomainModel;

public abstract class DomainModelMapper {
    protected MemberDomainModel entityToDomainModel(Member member) {
        return MemberDomainModel.of(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPassword(),
                member.getAuthority()
        );
    }
}
