package com.telcox.productcatalog.cache;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record TariffCacheValue(
        UUID tariffId,
        String tariffCode,
        String tariffName,
        BigDecimal amount,
        String currency,
        String status
) {
    public TariffCacheValue {
        Objects.requireNonNull(tariffId, "tariffId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        requireText(tariffCode, "tariffCode");
        requireText(tariffName, "tariffName");
        requireText(currency, "currency");
        requireText(status, "status");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
