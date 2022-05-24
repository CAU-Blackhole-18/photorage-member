package cauBlackHole.photoragemember.service;

import cauBlackHole.photoragemember.domain.DTO.UserRequestCreateDto;
import cauBlackHole.photoragemember.domain.DTO.UserSignInDto;
import cauBlackHole.photoragemember.domain.User;
import cauBlackHole.photoragemember.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String signUp(UserRequestCreateDto userRequestCreateDto){
        User user = new User();
        user.setEmail(userRequestCreateDto.getEmail());
        user.setName(userRequestCreateDto.getName());
        user.setPassword(userRequestCreateDto.getPassword());

        userRepository.save(user);

        return "ok";
    }

    public String signIn(UserSignInDto userSignInDto){

        Optional<User> findUser = userRepository.findByEmail(userSignInDto.getEmail());

        if(findUser.isEmpty()) return "Can't find Users";
        if(!findUser.get().getPassword().equals(userSignInDto.getPassword())) return "Wrong password";

        return "ok";
    }
}
