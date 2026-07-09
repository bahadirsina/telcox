package com.telcox.notification.service;

import com.telcox.notification.domain.NotificationChannel;
import java.util.UUID;

public record RenderedNotification(
        UUID customerId,
        String templateCode,
        NotificationChannel channel,
        String recipient,
        String subject,
        String content
) {
}
