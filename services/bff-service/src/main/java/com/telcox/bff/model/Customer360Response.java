package com.telcox.bff.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record Customer360Response(
        UUID customerId,
        OffsetDateTime generatedAt,
        UserContextResponse user,
        Map<String, Object> customer,
        List<Map<String, Object>> subscriptions,
        List<Map<String, Object>> orders,
        List<Map<String, Object>> invoices,
        List<Map<String, Object>> tickets,
        List<String> hiddenSections,
        List<UpstreamStatus> upstream
) {
}
