package com.telcox.order.api;

import com.telcox.order.domain.SagaStepStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SagaStepResponse(
        UUID id,
        String stepName,
        SagaStepStatus status,
        String eventType,
        String message,
        LocalDateTime occurredAt
) {
}
