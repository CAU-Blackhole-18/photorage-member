package cauBlackHole.photoragemember.config.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class BaseRuntimeException extends RuntimeException{
    private final ErrorCode errorCode;
    private final String message;
}
