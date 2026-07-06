package com.telcox.notification.api;

import com.telcox.notification.domain.DeliveryStatus;
import com.telcox.notification.domain.NotificationChannel;
import com.telcox.notification.domain.NotificationDelivery;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationDeliveryResponse(
        UUID id,
        UUID customerId,
        NotificationChannel channel,
        String recipient,
        String subject,
        String content,
        DeliveryStatus status,
        String failureReason,
        String correlationId,
        OffsetDateTime createdAt,
        OffsetDateTime sentAt
) {
    public static NotificationDeliveryResponse from(NotificationDelivery delivery) {
        return new NotificationDeliveryResponse(delivery.getId(), delivery.getCustomerId(),
                delivery.getChannel(), delivery.getRecipient(), delivery.getSubject(), delivery.getContent(),
                delivery.getStatus(), delivery.getFailureReason(), delivery.getCorrelationId(),
                delivery.getCreatedAt(), delivery.getSentAt());
    }
}
