package cauBlackHole.photoragemember.application.DTO;

import cauBlackHole.photoragemember.config.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExceptionDto {
    private final String errorCode;
    private final String message;
    private final LocalDateTime timeStamp;

    public ExceptionDto(ErrorCode errorCode, String message) {
        this.errorCode = errorCode.getCode();
        this.message = message;
        this.timeStamp = LocalDateTime.now();
    }
}