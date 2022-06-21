package cauBlackHole.photoragemember.application.port.inPort;

import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestLogoutDto;
import cauBlackHole.photoragemember.application.DTO.jwt.JwtTokenRequestReissueDto;
import cauBlackHole.photoragemember.application.DTO.member.*;

import java.util.List;


public interface MemberServiceUseCase {
    // 현재 SecurityContext 에 있는 유저 정보 가져오기
    MemberResponseDto getMyInfo();

    List<MemberResponseDto> findByName(String name);

    MemberResponseDto updateMyInfo(MemberRequestUpdateDto memberRequestUpdateDto);

    String leave();

    MemberResponseDto updatePassword(MemberRequestUpdatePasswordDto updatePasswordDto);

    JwtTokenDto signIn(MemberRequestSignInDto memberRequestSignInDto);
    MemberResponseDto signUp(MemberRequestSignUpDto memberRequestSignUpDto);
    String logout(JwtTokenRequestLogoutDto jwtTokenRequestLogoutDto);
    JwtTokenDto reissue(JwtTokenRequestReissueDto jwtTokenRequestDto);

    String findPassword(MemberRequestFindPasswordDto findPasswordDto);
}
