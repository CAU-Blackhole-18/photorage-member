package cauBlackHole.photoragemember.adapter.web;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import cauBlackHole.photoragemember.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<MemberResponseDto> signUp(@RequestBody @Validated MemberRequestSignUpDto memberRequestDto) {
        return ResponseEntity.ok(authService.signUp(memberRequestDto));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<JwtTokenDto> signIn(@RequestBody @Validated MemberRequestSignInDto memberRequestDto) {

        return ResponseEntity.ok(authService.signIn(memberRequestDto));
    }

    @PostMapping("/reissue")
    public ResponseEntity<JwtTokenDto> reissue(@RequestBody JwtTokenRequestDto tokenRequestDto, HttpServletRequest request) {
        request.setAttribute("reissue", true);
        return ResponseEntity.ok(authService.reissue(tokenRequestDto, request));
    }
}
