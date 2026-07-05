package com.telcox.subscription.api;

import com.telcox.subscription.domain.MnpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MnpResponse(
        UUID id,
        UUID subscriptionId,
        UUID customerId,
        String msisdn,
        String donorOperator,
        String recipientOperator,
        String planCode,
        String simIccid,
        MnpStatus status,
        String rejectionReason,
        String correlationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt
) {
}
