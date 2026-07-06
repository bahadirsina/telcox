package com.telcox.usage.quota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class QuotaReadServiceTest {
    @Test
    void calculatesRemainingAndUsagePercent() {
        UsageQuotaRepository repository = Mockito.mock(UsageQuotaRepository.class);
        Instant now = Instant.parse("2026-07-06T10:00:00Z");
        UUID subscriptionId = UUID.randomUUID();
        UsageQuota quota = UsageQuota.create(subscriptionId, UsageQuota.QuotaType.DATA_MB,
                BigDecimal.valueOf(1000), now.minusSeconds(3600), now.plusSeconds(3600));
        quota.consume(BigDecimal.valueOf(250), now);
        when(repository.findCurrent(subscriptionId, now)).thenReturn(List.of(quota));

        var response = new QuotaReadService(repository, Clock.fixed(now, ZoneOffset.UTC))
                .current(subscriptionId, null).getFirst();

        assertThat(response.remaining()).isEqualByComparingTo("750");
        assertThat(response.usagePercent()).isEqualByComparingTo("25.00");
    }
}
