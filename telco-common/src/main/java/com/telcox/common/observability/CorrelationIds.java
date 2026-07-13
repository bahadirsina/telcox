package com.telcox.common.observability;

import java.util.UUID;

public final class CorrelationIds {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationIds() {
    }

    public static String resolve(String candidate, String prefix) {
        if (candidate != null && !candidate.isBlank()) {
            return candidate.trim();
        }
        return prefix + "-" + UUID.randomUUID();
    }
}
