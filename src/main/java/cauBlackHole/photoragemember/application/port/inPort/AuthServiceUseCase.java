package cauBlackHole.photoragemember.application.port.inPort;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestFindPasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignInDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestSignUpDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberResponseDto;

public interface AuthServiceUseCase {
    JwtTokenDto signIn(MemberRequestSignInDto memberRequestSignInDto);
    MemberResponseDto signUp(MemberRequestSignUpDto memberRequestSignUpDto);
    String logout(JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto);
    JwtTokenDto reissue(JwtTokenRequestReissueDto jwtTokenRequestDto);

    String findPassword(MemberRequestFindPasswordDto findPasswordDto);
}
