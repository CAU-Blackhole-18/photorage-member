package cauBlackHole.photoragemember.controller;

import cauBlackHole.photoragemember.domain.DTO.UserRequestCreateDto;
import cauBlackHole.photoragemember.domain.DTO.UserSignInDto;
import cauBlackHole.photoragemember.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public String signUp(@RequestBody UserRequestCreateDto userRequestCreateDto){
        return this.userService.signUp(userRequestCreateDto);
    }

    @PostMapping("/sign-in")
    public String signIn(@RequestBody UserSignInDto userSignInDto){
        return this.userService.signIn(userSignInDto);
    }
}
