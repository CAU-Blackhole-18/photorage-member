package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.application.DTO.UserRequestCreateDto;
import cauBlackHole.photoragemember.application.DTO.UserSignInDto;
import cauBlackHole.photoragemember.application.port.in.UserServiceUseCase;
import cauBlackHole.photoragemember.domain.User;
import cauBlackHole.photoragemember.application.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceUseCase {
    private final UserRepository userRepository;

    public String signIn(UserSignInDto userSignInDto){

        Optional<User> findUser = userRepository.findByEmail(userSignInDto.getEmail());

        if(findUser.isEmpty()) return "Can't find Users";
        if(!findUser.get().getPassword().equals(userSignInDto.getPassword())) return "Wrong password";

        return "ok";
    }

    public String signUp(UserRequestCreateDto userRequestCreateDto){
        User user = new User();
        user.setEmail(userRequestCreateDto.getEmail());
        user.setName(userRequestCreateDto.getName());
        user.setPassword(userRequestCreateDto.getPassword());

        userRepository.save(user);

        return "ok";
    }
}
