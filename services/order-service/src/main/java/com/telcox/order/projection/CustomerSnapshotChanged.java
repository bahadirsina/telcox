package com.telcox.order.projection;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record CustomerSnapshotChanged(
        UUID eventId,
        UUID customerId,
        String customerNumber,
        String fullName,
        String customerStatus,
        String segment,
        OffsetDateTime sourceUpdatedAt
) {
    public CustomerSnapshotChanged {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        requireText(customerNumber, "customerNumber");
        requireText(fullName, "fullName");
        requireText(customerStatus, "customerStatus");
        Objects.requireNonNull(sourceUpdatedAt, "sourceUpdatedAt must not be null");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
