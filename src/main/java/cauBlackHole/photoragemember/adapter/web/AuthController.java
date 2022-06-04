package cauBlackHole.photoragemember.adapter.web;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<MemberResponseDto> signUp(@RequestBody @Validated MemberRequestSignUpDto memberRequestDto) {
        return ResponseEntity.ok(this.authService.signUp(memberRequestDto));
    }

    @PostMapping("/sign-in")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<JwtTokenDto> signIn(@RequestBody @Validated MemberRequestSignInDto memberRequestDto) {

        return ResponseEntity.ok(this.authService.signIn(memberRequestDto));
    }

    @PostMapping("/logout")
    @ResponseStatus(value = HttpStatus.OK)
    public String logout(@RequestBody JwtTokenRequestDto jwtTokenRequestDto){
        return this.authService.logout(jwtTokenRequestDto);
    }

    @PostMapping("/reissue")
    public ResponseEntity<JwtTokenDto> reissue(@RequestBody JwtTokenRequestDto jwtTokenRequestDto) {
        return ResponseEntity.ok(this.authService.reissue(jwtTokenRequestDto));
    }
}
