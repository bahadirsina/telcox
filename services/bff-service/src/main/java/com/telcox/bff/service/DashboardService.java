package com.telcox.bff.service;

import com.telcox.bff.config.BffProperties;
import com.telcox.bff.model.DashboardCounter;
import com.telcox.bff.model.DashboardSummaryResponse;
import com.telcox.bff.model.UpstreamResult;
import com.telcox.bff.model.UpstreamStatus;
import com.telcox.bff.model.UserContext;
import com.telcox.common.cache.CacheKeyPrefix;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final UpstreamClient upstreamClient;
    private final BffCacheService cacheService;
    private final BffProperties properties;

    public DashboardService(UpstreamClient upstreamClient, BffCacheService cacheService, BffProperties properties) {
        this.upstreamClient = upstreamClient;
        this.cacheService = cacheService;
        this.properties = properties;
    }

    public DashboardSummaryResponse summary(UserContext user, String correlationId) {
        String key = CacheKeyPrefix.buildKey(CacheKeyPrefix.BFF_DASHBOARD_SUMMARY, user.cacheScope());
        return cacheService.get(key, DashboardSummaryPayload.class)
                .map(payload -> payload.toResponse(true))
                .orElseGet(() -> loadAndCache(key, user, correlationId));
    }

    private DashboardSummaryResponse loadAndCache(String key, UserContext user, String correlationId) {
        UpstreamResult<List<Map<String, Object>>> orders =
                upstreamClient.getList("order-service", "/api/v1/orders", user, correlationId);
        UpstreamResult<List<Map<String, Object>>> subscriptions =
                upstreamClient.getList("subscription-service", "/api/v1/subscriptions", user, correlationId);

        List<UpstreamStatus> statuses = new ArrayList<>();
        statuses.add(orders.status());
        statuses.add(subscriptions.status());

        List<DashboardCounter> counters = new ArrayList<>();
        counters.add(new DashboardCounter("orders", orders.body().size(), orders.available() ? "live" : "unavailable"));
        counters.add(new DashboardCounter("subscriptions", subscriptions.body().size(), subscriptions.available() ? "live" : "unavailable"));

        if (user.canSeeBilling()) {
            UpstreamResult<List<Map<String, Object>>> invoices =
                    upstreamClient.getList("billing-service", "/api/v1/invoices", user, correlationId);
            counters.add(new DashboardCounter("invoices", invoices.body().size(), invoices.available() ? "live" : "unavailable"));
            statuses.add(invoices.status());
        }
        if (user.canSeeTickets()) {
            UpstreamResult<List<Map<String, Object>>> tickets =
                    upstreamClient.getList("ticket-service", "/api/v1/tickets", user, correlationId);
            counters.add(new DashboardCounter("tickets", tickets.body().size(), tickets.available() ? "live" : "unavailable"));
            statuses.add(tickets.status());
        }

        DashboardSummaryPayload payload = new DashboardSummaryPayload(
                OffsetDateTime.now(),
                user.toResponse(),
                counters,
                statuses
        );
        cacheService.put(key, payload, properties.getDashboardCacheTtl());
        return payload.toResponse(false);
    }

    public record DashboardSummaryPayload(
            OffsetDateTime generatedAt,
            com.telcox.bff.model.UserContextResponse user,
            List<DashboardCounter> counters,
            List<UpstreamStatus> upstream
    ) {
        DashboardSummaryResponse toResponse(boolean cached) {
            return new DashboardSummaryResponse(generatedAt, cached, user, counters, upstream);
        }
    }
}
