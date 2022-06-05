package cauBlackHole.photoragemember.adapter.persistence;

import cauBlackHole.photoragemember.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByEmail(String email);
    List<Member> findByName(String name);
    Optional<Member> findByEmailAndName(String email, String name);
    boolean existsByEmail(String email);
}
