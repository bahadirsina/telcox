package com.telcox.notification.api;

import com.telcox.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendNotificationRequest(
        UUID customerId,
        @NotNull NotificationChannel channel,
        @NotBlank String recipient,
        String subject,
        @NotBlank String content
) {
}
