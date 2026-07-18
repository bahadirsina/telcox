package com.telcox.bff.model;

import java.time.OffsetDateTime;
import java.util.List;

public record PlatformOpsResponse(
        OffsetDateTime generatedAt,
        boolean cached,
        String environment,
        String overallStatus,
        PlatformPulse pulse,
        List<ServiceStatus> services,
        List<ConnectorStatus> connectors,
        PrometheusStatus prometheus,
        List<PlatformEvent> events
) {
    public record PlatformPulse(
            int totalServices,
            int healthyServices,
            int degradedServices,
            int downServices,
            int totalRecords,
            int prometheusTargetsUp,
            int prometheusTargetsTotal,
            int connectorsRunning,
            int connectorsTotal
    ) {
    }

    public record ServiceStatus(
            String service,
            String label,
            String group,
            String status,
            boolean available,
            boolean dataAvailable,
            long latencyMs,
            int records,
            String detail
    ) {
    }

    public record ConnectorStatus(
            String name,
            String status,
            boolean available,
            int runningTasks,
            int totalTasks,
            String detail
    ) {
    }

    public record PrometheusStatus(
            boolean available,
            int targetsUp,
            int targetsTotal,
            String detail
    ) {
    }

    public record PlatformEvent(
            String type,
            String service,
            String status,
            String detail,
            OffsetDateTime occurredAt
    ) {
    }
}
