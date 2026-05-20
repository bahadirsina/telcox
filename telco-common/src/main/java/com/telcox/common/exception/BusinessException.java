package com.telcox.common.exception;

import lombok.Getter;

/**
 * Mikroservislerin firlatabilecegi anlamli is hatasi.
 * GlobalExceptionHandler tarafindan yakalanip RFC 7807 ApiError'a donusturulur.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
