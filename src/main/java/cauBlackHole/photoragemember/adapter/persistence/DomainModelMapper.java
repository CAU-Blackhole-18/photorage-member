package cauBlackHole.photoragemember.adapter.persistence;

import cauBlackHole.photoragemember.domain.Member;
import cauBlackHole.photoragemember.domain.MemberDomainModel;

public abstract class DomainModelMapper {
    protected MemberDomainModel entityToDomainModel(Member member) {
        return new MemberDomainModel(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPassword(),
                member.getAuthority()
        );
    }
}