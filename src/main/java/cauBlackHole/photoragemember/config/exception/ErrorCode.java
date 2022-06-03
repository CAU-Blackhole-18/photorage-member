package cauBlackHole.photoragemember.config.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    /**
     * 400 Bad Request
     */
    INVALID_INPUT_VALUE("ME007"),
    INVALID_TYPE("ME005"),
    INVALID_HTTP_METHOD("ME006"),
    INVALID_EMAIL("ME003"),
    INVALID_PASSWORD("ME004"),

    /**
     * 401 Unauthorized
     */
    INVALID_ACCESS_JWT("ME001"),
    INVALID_REFRESH_JWT("ME002");

    private String code;

    ErrorCode(String code) {this.code = code;}
}
