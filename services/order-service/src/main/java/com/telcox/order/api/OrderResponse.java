package com.telcox.order.api;

import com.telcox.order.domain.OrderStatus;
import com.telcox.order.domain.OrderType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        OrderType orderType,
        String planCode,
        List<String> addonCodes,
        String msisdn,
        String simIccid,
        OrderStatus status,
        String cancellationReason,
        String correlationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        List<SagaStepResponse> saga
) {
}
