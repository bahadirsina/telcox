package com.telcox.usage.quota;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usage/subscriptions/{subscriptionId}/quotas")
public class QuotaController {
    private final QuotaReadService quotaReadService;

    public QuotaController(QuotaReadService quotaReadService) {
        this.quotaReadService = quotaReadService;
    }

    @GetMapping
    public List<QuotaReadService.QuotaResponse> current(
            @PathVariable UUID subscriptionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant at) {
        return quotaReadService.current(subscriptionId, at);
    }
}
