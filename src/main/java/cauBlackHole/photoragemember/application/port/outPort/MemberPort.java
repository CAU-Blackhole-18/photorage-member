package cauBlackHole.photoragemember.application.port.outPort;

import cauBlackHole.photoragemember.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberPort {
    List<Member> findEmail(String name, String nickname);

    Optional<Member> findForPassword(String email, String name);

    Optional<Member> findById(String id);

    List<Member> findByName(String name);

    Optional<Member> findByEmail(String email);

    Optional<Member> update(Member updateMember);

    Member create(Member newMember);

    void delete(Member member);
}
