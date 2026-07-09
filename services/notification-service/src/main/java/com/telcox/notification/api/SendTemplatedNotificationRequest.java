package com.telcox.notification.api;

import com.telcox.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record SendTemplatedNotificationRequest(
        UUID customerId,
        @NotBlank String templateCode,
        @NotNull NotificationChannel channel,
        String locale,
        @NotBlank String recipient,
        Map<String, String> variables
) {
}
