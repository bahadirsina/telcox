package com.telcox.subscription.api;

import com.telcox.subscription.domain.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID customerId,
        UUID orderId,
        String msisdn,
        String simIccid,
        String planCode,
        SubscriptionStatus status,
        String statusReason,
        String correlationId,
        LocalDateTime activatedAt,
        LocalDateTime suspendedAt,
        LocalDateTime terminatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AddonResponse> addons
) {
}
