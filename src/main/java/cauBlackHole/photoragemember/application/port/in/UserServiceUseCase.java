package cauBlackHole.photoragemember.application.port.in;

import cauBlackHole.photoragemember.application.DTO.UserRequestCreateDto;
import cauBlackHole.photoragemember.application.DTO.UserSignInDto;

public interface UserServiceUseCase {
    String signIn(UserSignInDto userSignInDto);
    String signUp(UserRequestCreateDto userRequestCreateDto);
}
