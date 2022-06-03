package cauBlackHole.photoragemember.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class MemoryMemberRepositoryTest {

    /*
    private UserRepository userRepository = new MemoryUserRepository();


    @AfterEach
    void afterEach(){
        userRepository.reset();
    }

    @Test
    void save(){
        User user = new User();
        user.setEmail("woosuk@naver.com"); user.setName("kang"); user.setPassword("1234");


        User saveUser = userRepository.save(user);
        Optional<User> findUser = userRepository.findById(saveUser.getId());

        assertThat(saveUser).isEqualTo(findUser.get());
    }

    @Test
    void findAll()
    {
        User user = new User();
        user.setEmail("woosuk@naver.com");  user.setName("kang"); user.setPassword("1234");

        User user1 = new User();
        user1.setEmail("kang@naver.com"); user1.setName("kang"); user1.setPassword("1234");

        userRepository.save(user); userRepository.save(user1);

        List<User> users = userRepository.findAll();

        assertThat(users.size()).isEqualTo(2);
        assertThat(users).contains(user,user1);
    }

    @Test
    void delete(){
        User user = new User();
        user.setEmail("woosuk@naver.com"); user.setName("kang"); user.setPassword("1234");

        User user1 = new User();
        user1.setEmail("kang@naver.com"); user1.setName("kang"); user1.setPassword("1234");

        userRepository.save(user); userRepository.save(user1);
        userRepository.delete(user);

        List<User> users = userRepository.findAll();

        assertThat(users.size()).isEqualTo(1);
        assertThat(users).contains(user1);
    }
    @Test
    void findByName(){
        User user = new User();
        user.setEmail("woosuk@naver.com"); user.setName("kang"); user.setPassword("1234");

        User user1 = new User();
        user1.setEmail("kang@naver.com"); user1.setName("kang"); user1.setPassword("1234");

        userRepository.save(user); userRepository.save(user1);

        List<User> findUsers = userRepository.findByName("kang");

        assertThat(findUsers).contains(user);
        assertThat(findUsers.size()).isEqualTo(2);
    }

    @Test
    void findByEmail(){
        User user = new User();
        user.setEmail("woosuk@naver.com"); user.setName("kang"); user.setPassword("1234");
        User user1 = new User();
        user1.setEmail("kang@naver.com"); user1.setName("kang"); user1.setPassword("1234");

        userRepository.save(user); userRepository.save(user1);

        Optional<User> findUsers = userRepository.findByEmail("woosuk@naver.com");

        assertThat(findUsers.get()).isEqualTo(user);
    }*/
}