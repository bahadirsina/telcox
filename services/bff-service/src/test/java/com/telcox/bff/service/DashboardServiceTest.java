package com.telcox.bff.service;

import com.telcox.bff.config.BffProperties;
import com.telcox.bff.model.DashboardSummaryResponse;
import com.telcox.bff.model.UserContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Test
    void includesRoleScopedCountersAndUpstreamStatus() {
        UpstreamClient upstreamClient = mock(UpstreamClient.class);
        BffProperties properties = new BffProperties();
        properties.setDashboardCacheTtl(Duration.ofSeconds(1));
        DashboardService service = new DashboardService(upstreamClient, new NoopBffCacheService(), properties);
        UserContext user = new UserContext("u1", "Ada", "", Set.of("FINANCE"));

        when(upstreamClient.getList("order-service", "/api/v1/orders", user, "corr"))
                .thenReturn(com.telcox.bff.model.UpstreamResult.ok("order-service", List.of(Map.of("id", "o1"))));
        when(upstreamClient.getList("subscription-service", "/api/v1/subscriptions", user, "corr"))
                .thenReturn(com.telcox.bff.model.UpstreamResult.ok("subscription-service", List.of()));
        when(upstreamClient.getList("billing-service", "/api/v1/invoices", user, "corr"))
                .thenReturn(com.telcox.bff.model.UpstreamResult.unavailable("billing-service", "HTTP 404", List.of()));

        DashboardSummaryResponse response = service.summary(user, "corr");

        assertThat(response.cached()).isFalse();
        assertThat(response.counters()).extracting("label").containsExactly("orders", "subscriptions", "invoices");
        assertThat(response.upstream()).anyMatch(status -> status.service().equals("billing-service") && !status.available());
    }

    @Test
    void cachesDashboardSummaryWithConfiguredShortTtl() {
        UpstreamClient upstreamClient = mock(UpstreamClient.class);
        BffProperties properties = new BffProperties();
        properties.setDashboardCacheTtl(Duration.ofSeconds(45));
        RecordingBffCacheService cacheService = new RecordingBffCacheService();
        DashboardService service = new DashboardService(upstreamClient, cacheService, properties);
        UserContext user = new UserContext("u-cache", "Ada", "", Set.of("SUPPORT"));

        when(upstreamClient.getList("order-service", "/api/v1/orders", user, "corr-cache"))
                .thenReturn(com.telcox.bff.model.UpstreamResult.ok("order-service", List.of(Map.of("id", "o1"))));
        when(upstreamClient.getList("subscription-service", "/api/v1/subscriptions", user, "corr-cache"))
                .thenReturn(com.telcox.bff.model.UpstreamResult.ok("subscription-service", List.of()));
        when(upstreamClient.getList("ticket-service", "/api/v1/tickets", user, "corr-cache"))
                .thenReturn(com.telcox.bff.model.UpstreamResult.ok("ticket-service", List.of(Map.of("id", "t1"))));

        DashboardSummaryResponse first = service.summary(user, "corr-cache");
        DashboardSummaryResponse second = service.summary(user, "corr-cache");

        assertThat(first.cached()).isFalse();
        assertThat(second.cached()).isTrue();
        assertThat(cacheService.lastTtl).isEqualTo(Duration.ofSeconds(45));
        assertThat(cacheService.lastKey).isEqualTo("bff:dashboard-summary:u-cache:SUPPORT");
        verify(upstreamClient).getList("order-service", "/api/v1/orders", user, "corr-cache");
        verify(upstreamClient).getList("subscription-service", "/api/v1/subscriptions", user, "corr-cache");
        verify(upstreamClient).getList("ticket-service", "/api/v1/tickets", user, "corr-cache");
        verify(upstreamClient, never()).getList("billing-service", "/api/v1/invoices", user, "corr-cache");
    }

    static class NoopBffCacheService extends BffCacheService {
        NoopBffCacheService() {
            super(null, null);
        }

        @Override
        public <T> java.util.Optional<T> get(String key, Class<T> type) {
            return java.util.Optional.empty();
        }

        @Override
        public void put(String key, Object value, Duration ttl) {
        }
    }

    static class RecordingBffCacheService extends BffCacheService {
        private Object value;
        private String lastKey;
        private Duration lastTtl;

        RecordingBffCacheService() {
            super(null, null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(String key, Class<T> type) {
            return Optional.ofNullable((T) value);
        }

        @Override
        public void put(String key, Object value, Duration ttl) {
            this.lastKey = key;
            this.value = value;
            this.lastTtl = ttl;
        }
    }
}
