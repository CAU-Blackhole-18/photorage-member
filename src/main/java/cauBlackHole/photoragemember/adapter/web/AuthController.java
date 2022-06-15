package cauBlackHole.photoragemember.adapter.web;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestFindPasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    public MemberResponseDto signUp(@RequestBody @Validated MemberRequestSignUpDto memberRequestDto) {
        return this.authService.signUp(memberRequestDto);
    }

    @PostMapping("/sign-in")
    @ResponseStatus(value = HttpStatus.OK)
    public JwtTokenDto signIn(@RequestBody @Validated MemberRequestSignInDto memberRequestDto) {

        return this.authService.signIn(memberRequestDto);
    }

    @PostMapping("/logout")
    @ResponseStatus(value = HttpStatus.OK)
    public String logout(@RequestBody @Validated JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto){
        return this.authService.logout(jwtTokenRequestLogoutDto);
    }

    @PostMapping("/reissue")
    public ResponseEntity<JwtTokenDto> reissue(@RequestBody @Validated JwtTokenRequestReissueDto jwtTokenRequestDto) {
        return ResponseEntity.ok(this.authService.reissue(jwtTokenRequestDto));
    }

    @PostMapping("/password")
    @ResponseStatus(value = HttpStatus.OK)
    public String findPassword(@RequestBody @Validated MemberRequestFindPasswordDto findPasswordDto)
    {
        return this.authService.findPassword(findPasswordDto);
    }
}
