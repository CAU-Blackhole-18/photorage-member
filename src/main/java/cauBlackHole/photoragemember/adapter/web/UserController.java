package cauBlackHole.photoragemember.adapter.web;

import cauBlackHole.photoragemember.application.DTO.UserRequestCreateDto;
import cauBlackHole.photoragemember.application.DTO.UserSignInDto;
import cauBlackHole.photoragemember.application.port.in.UserServiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserServiceUseCase userServiceUseCase;

    @PostMapping("/sign-up")
    public String signUp(@RequestBody UserRequestCreateDto userRequestCreateDto){
        return this.userServiceUseCase.signUp(userRequestCreateDto);
    }

    @PostMapping("/sign-in")
    public String signIn(@RequestBody UserSignInDto userSignInDto){
        return this.userServiceUseCase.signIn(userSignInDto);
    }
}
