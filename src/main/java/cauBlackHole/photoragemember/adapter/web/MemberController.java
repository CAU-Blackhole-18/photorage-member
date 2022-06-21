package cauBlackHole.photoragemember.adapter.web;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.*;
import cauBlackHole.photoragemember.application.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    @ResponseStatus(value = HttpStatus.OK)
    public MemberResponseDto getMyMemberInfo() {
        return this.memberService.getMyInfo();
    }

    @GetMapping("/{name}")
    @ResponseStatus(value = HttpStatus.OK)
    public List<MemberResponseDto> findByName(@PathVariable String name){
        return this.memberService.findByName(name);
    }

    @PatchMapping
    @ResponseStatus(value = HttpStatus.OK)
    public MemberResponseDto updateMyInfo(@RequestBody MemberRequestUpdateDto memberRequestUpdateDto)
    {
        return this.memberService.updateMyInfo(memberRequestUpdateDto);
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.OK)
    public String leave(){
        return this.memberService.leave();
    }

    @PutMapping("/password")
    @ResponseStatus(value = HttpStatus.OK)
    public MemberResponseDto updatePassword(@RequestBody @Validated MemberRequestUpdatePasswordDto updatePasswordDto){
        return this.memberService.updatePassword(updatePasswordDto);
    }

    @PostMapping("/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    public MemberResponseDto signUp(@RequestBody @Validated MemberRequestSignUpDto memberRequestDto) {
        return this.memberService.signUp(memberRequestDto);
    }

    @PostMapping("/sign-in")
    @ResponseStatus(value = HttpStatus.OK)
    public JwtTokenDto signIn(@RequestBody @Validated MemberRequestSignInDto memberRequestDto) {

        return this.memberService.signIn(memberRequestDto);
    }

    @PostMapping("/logout")
    @ResponseStatus(value = HttpStatus.OK)
    public String logout(@RequestBody @Validated JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto){
        return this.memberService.logout(jwtTokenRequestLogoutDto);
    }

    @PostMapping("/reissue")
    @ResponseStatus(value = HttpStatus.CREATED)
    public JwtTokenDto reissue(@RequestBody @Validated JwtTokenRequestReissueDto jwtTokenRequestDto) {
        return this.memberService.reissue(jwtTokenRequestDto);
    }

    @PostMapping("/password")
    @ResponseStatus(value = HttpStatus.OK)
    public String findPassword(@RequestBody @Validated MemberRequestFindPasswordDto findPasswordDto)
    {
        return this.memberService.findPassword(findPasswordDto);
    }
}