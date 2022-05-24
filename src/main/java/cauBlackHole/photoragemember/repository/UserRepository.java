package cauBlackHole.photoragemember.repository;

import cauBlackHole.photoragemember.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    void delete(User user);

    Optional<User> findById(Long id);
    List<User> findAll();

    List<User> findByName(String name);

    Optional<User> findByEmail(String email);

    void reset();
}
