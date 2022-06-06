package cauBlackHole.photoragemember.application.port.inPort;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthServiceUseCase {
    JwtTokenDto signIn(MemberRequestSignInDto memberRequestSignInDto);
    MemberResponseDto signUp(MemberRequestSignUpDto memberRequestSignUpDto);
    String logout(@RequestBody JwtTokenRequestDto jwtTokenRequestDto);
    JwtTokenDto reissue(JwtTokenRequestDto jwtTokenRequestDto);
}
