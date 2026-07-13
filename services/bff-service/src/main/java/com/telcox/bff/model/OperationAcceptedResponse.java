package com.telcox.bff.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OperationAcceptedResponse(
        UUID operationId,
        UUID orderId,
        String state,
        String statusUrl,
        String eventsUrl,
        OffsetDateTime submittedAt
) {
}
