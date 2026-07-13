package com.telcox.bff.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record OperationStatusResponse(
        UUID operationId,
        UUID orderId,
        String state,
        String orderStatus,
        String correlationId,
        OffsetDateTime updatedAt,
        List<Map<String, Object>> saga,
        Map<String, Object> order,
        List<UpstreamStatus> upstream
) {
}
