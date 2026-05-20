package com.telcox.common.exception;

/**
 * Tum servislerin paylasacagi standart hata kodlari.
 *
 * NOT: Spring'in HttpStatus enum'una bagimli olmamak icin status code int olarak tutulur.
 * Bu sayede telco-common kutuphanesi spring-web bagimliligi olmadan webmvc, webflux ve
 * salt JPA servislerinde tutarli sekilde kullanilabilir. HTTP cevabina map etme islemi
 * her servisin kendi GlobalExceptionHandler'inda yapilir.
 *
 * Servise ozel hatalar her servis kendi enum'unu da ekleyebilir.
 */
public enum ErrorCode {

    VALIDATION_FAILED("VALIDATION_FAILED", 400),
    NOT_FOUND("NOT_FOUND", 404),
    CONFLICT("CONFLICT", 409),
    UNAUTHORIZED("UNAUTHORIZED", 401),
    FORBIDDEN("FORBIDDEN", 403),
    DEPENDENCY_FAILURE("DEPENDENCY_FAILURE", 503),
    INTERNAL_ERROR("INTERNAL_ERROR", 500);

    private final String code;
    private final int httpStatus;

    ErrorCode(String code, int httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
