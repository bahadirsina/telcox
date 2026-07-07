package com.telcox.notification.api;

import com.telcox.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationTemplateRequest(
        @NotBlank String templateCode,
        @NotNull NotificationChannel channel,
        String locale,
        String subjectTemplate,
        @NotBlank String contentTemplate,
        Boolean active
) {
}
