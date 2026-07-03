package com.telcox.order.projection;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record TariffSnapshotChanged(
        UUID eventId,
        UUID tariffId,
        String tariffCode,
        String tariffName,
        BigDecimal amount,
        String currency,
        String tariffStatus,
        OffsetDateTime sourceUpdatedAt
) {
    public TariffSnapshotChanged {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(tariffId, "tariffId must not be null");
        requireText(tariffCode, "tariffCode");
        requireText(tariffName, "tariffName");
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        Currency.getInstance(currency);
        requireText(tariffStatus, "tariffStatus");
        Objects.requireNonNull(sourceUpdatedAt, "sourceUpdatedAt must not be null");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
