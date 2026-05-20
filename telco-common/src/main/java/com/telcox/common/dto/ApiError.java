package com.telcox.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.OffsetDateTime;

/**
 * RFC 7807 Problem Details uyumlu standart hata cevabi.
 * Tum servisler bu yapida hata dondurmelidir (API Tasarim Standartlari, sec 12).
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String correlationId,
        String errorCode,
        OffsetDateTime timestamp
) {
}
