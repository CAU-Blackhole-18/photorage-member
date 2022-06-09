package cauBlackHole.photoragemember.application.port.outPort;

import cauBlackHole.photoragemember.domain.MemberDomainModel;

import java.util.List;
import java.util.Optional;

public interface MemberPort {
    Optional<MemberDomainModel> findEmail(String email);

    Optional<MemberDomainModel> findForPassword(String email, String name);

    Optional<MemberDomainModel> findById(String id);

    List<MemberDomainModel> findByName(String name);

    Optional<MemberDomainModel> findByEmail(String email);

    Optional<MemberDomainModel> update(String id, MemberDomainModel memberDomainModel);

    Optional<MemberDomainModel> updatePassword(String id, String password);

    MemberDomainModel create(MemberDomainModel memberDomainModel);

    void delete(MemberDomainModel memberDomainModel);
}
