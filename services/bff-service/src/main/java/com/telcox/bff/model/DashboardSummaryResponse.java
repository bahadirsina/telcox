package com.telcox.bff.model;

import java.time.OffsetDateTime;
import java.util.List;

public record DashboardSummaryResponse(
        OffsetDateTime generatedAt,
        boolean cached,
        UserContextResponse user,
        List<DashboardCounter> counters,
        List<UpstreamStatus> upstream
) {
}
