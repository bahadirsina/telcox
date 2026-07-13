package com.telcox.bff.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record OperationRecord(
        UUID operationId,
        UUID orderId,
        String state,
        OffsetDateTime submittedAt,
        OffsetDateTime updatedAt,
        Map<String, Object> snapshot
) {
}
