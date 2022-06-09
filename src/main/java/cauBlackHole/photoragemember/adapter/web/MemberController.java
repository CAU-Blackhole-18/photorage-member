package cauBlackHole.photoragemember.adapter.web;

import cauBlackHole.photoragemember.application.DTO.member.MemberRequestFindPasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdateDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdatePasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
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
    public ResponseEntity<MemberResponseDto> getMyMemberInfo() {
        return ResponseEntity.ok(this.memberService.getMyInfo());
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
}