package cauBlackHole.photoragemember.config.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    /**
     * 400 Bad Request
     */
    INVALID_TYPE("ME005"),
    INVALID_INPUT_VALUE("ME006"),

    INVALID_HTTP_METHOD("ME007"),

    INVALID_EMAIL("ME003"),
    INVALID_PASSWORD("ME004"),

    INVALID_NAME("ME014"),

    INVALID_NICKNAME("ME015"),

    DUPLICATE_EMAIL("ME008"),


    /**
     * 401 Unauthorized
     */
    INVALID_ACCESS_JWT("ME001"),
    INVALID_REFRESH_JWT("ME002"),

    WRONG_PASSWORD("ME013"),


    /**
     * 403 Forbidden
     */
    NOT_ADMIN("ME012"),
    PAUSE_USER("ME009"),
    DELETE_USER("ME010"),

    /**
     * 404 Not Found
     */
    NOT_FOUND_USER("ME011"),

    /**
     * 409 CONFLICT
     */
    DELETE_FAIL("ME997"),
    UPDATE_FAIL("ME998"),
    CONFLICT("ME999");

    private String code;

    ErrorCode(String code) {this.code = code;}
}
