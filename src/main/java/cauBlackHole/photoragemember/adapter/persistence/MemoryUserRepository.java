package cauBlackHole.photoragemember.adapter.persistence;

import cauBlackHole.photoragemember.application.port.out.UserRepository;
import cauBlackHole.photoragemember.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class MemoryUserRepository implements UserRepository {

    private static Map<Long, User> store = new HashMap<>();
    private static long sequence = 0L;

    @Override
    public void reset() {
        store.clear();
    }

    @Override
    public User save(User user) {
        user.setId(++sequence);
        log.info("save: user = {}", user);
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(User user) {
        log.info("delete : user={}", user);
        store.remove(user.getId());
    }

    @Override
    public Optional<User> findById(Long id) {
        Optional<User> user = Optional.ofNullable(store.get(id));
        if(user.isPresent()){
            log.info("findById : user = {}", user.get());
        }
        else{
            log.info("no Users");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        log.info("findAll : users = {}", store.values());
        return new ArrayList<>(store.values());
    }

    @Override
    public List<User> findByName(String name) {
        List<User> findUsers = findAll().stream().
                filter(member -> member.getName().equals(name)).
                collect(Collectors.toList());
        log.info("find by name = {}", findUsers);
        return findUsers;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<User> findUser = findAll().stream().
                filter(member -> member.getEmail().equals(email)).
                findFirst();

        if(findUser.isPresent()){
            log.info("find by email = {}", findUser.get());
        }
        else{
            log.info("no Users");
        }
        return findUser;
    }
}
