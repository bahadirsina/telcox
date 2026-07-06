package com.telcox.usage.cdr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.telcox.common.event.EventEnvelope;
import com.telcox.usage.quota.UsageQuota;
import com.telcox.usage.quota.UsageQuotaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CdrProcessingServiceTest {
    private final UsageRecordRepository records = Mockito.mock(UsageRecordRepository.class);
    private final ProcessedCdrEventRepository processed = Mockito.mock(ProcessedCdrEventRepository.class);
    private final UsageQuotaRepository quotas = Mockito.mock(UsageQuotaRepository.class);
    private final Instant now = Instant.parse("2026-07-06T10:00:00Z");
    private final CdrProcessingService service =
            new CdrProcessingService(records, processed, quotas, Clock.fixed(now, ZoneOffset.UTC));

    @Test
    void ignoresAlreadyProcessedEvent() {
        EventEnvelope<CdrEvent> envelope = envelope();
        when(processed.existsByEventId(envelope.eventId())).thenReturn(true);
        assertThat(service.process(envelope)).isFalse();
        verify(records, never()).save(any());
    }

    @Test
    void storesCdrAndConsumesQuotaAtomically() {
        EventEnvelope<CdrEvent> envelope = envelope();
        CdrEvent event = envelope.payload();
        UsageQuota quota = UsageQuota.create(event.subscriptionId(), UsageQuota.QuotaType.VOICE_MIN,
                BigDecimal.valueOf(100), now.minusSeconds(3600), now.plusSeconds(3600));
        when(quotas.findActive(event.subscriptionId(), UsageQuota.QuotaType.VOICE_MIN, event.occurredAt()))
                .thenReturn(Optional.of(quota));

        assertThat(service.process(envelope)).isTrue();
        assertThat(quota.getUsedAmount()).isEqualByComparingTo("2.0000");
        verify(records).save(any(UsageRecord.class));
        verify(processed).save(any(ProcessedCdrEvent.class));
    }

    @Test
    void missingQuotaFailsSoKafkaCanRetry() {
        EventEnvelope<CdrEvent> envelope = envelope();
        when(quotas.findActive(any(), any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.process(envelope)).isInstanceOf(IllegalStateException.class);
        verify(processed, never()).save(any());
    }

    private EventEnvelope<CdrEvent> envelope() {
        CdrEvent payload = new CdrEvent("cdr-100", UUID.randomUUID(), "905551112233",
                CdrEvent.RecordType.VOICE, now, 120, 0, 0);
        return new EventEnvelope<>(UUID.randomUUID(), "cdr-recorded", payload.subscriptionId(), "corr-100",
                1, "cdr-service", "CDR", OffsetDateTime.parse("2026-07-06T10:00:00Z"),
                payload, java.util.Map.of());
    }
}
