package com.telcox.notification.projection;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record CustomerPreferenceChanged(
        UUID eventId,
        UUID customerId,
        String email,
        String phoneNumber,
        String preferredChannel,
        boolean emailEnabled,
        boolean smsEnabled,
        boolean pushEnabled,
        boolean marketingConsent,
        boolean transactionalConsent,
        OffsetDateTime sourceUpdatedAt
) {
    private static final Set<String> CHANNELS = Set.of("EMAIL", "SMS", "PUSH", "NONE");

    public CustomerPreferenceChanged {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(sourceUpdatedAt, "sourceUpdatedAt must not be null");
        if (!CHANNELS.contains(preferredChannel)) {
            throw new IllegalArgumentException("unsupported preferredChannel: " + preferredChannel);
        }
    }
}
