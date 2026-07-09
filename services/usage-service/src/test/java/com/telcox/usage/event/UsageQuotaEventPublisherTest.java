package com.telcox.usage.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcox.usage.quota.UsageQuota;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class UsageQuotaEventPublisherTest {
    private final UsageOutboxEventRepository outboxEvents = Mockito.mock(UsageOutboxEventRepository.class);
    private final UsageQuotaEventPublisher publisher = new UsageQuotaEventPublisher(
            outboxEvents,
            new ObjectMapper().findAndRegisterModules()
    );
    private final Instant now = Instant.parse("2026-07-09T08:00:00Z");

    @Test
    void publishesThresholdEventWhenQuotaCrossesEightyPercent() {
        UsageQuota quota = quota(new BigDecimal("100.0000"));
        quota.consume(new BigDecimal("85.0000"), now);
        when(outboxEvents.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        publisher.publishQuotaEvents(quota, BigDecimal.ZERO, "corr-80", now);

        UsageOutboxEvent event = savedEvent();
        assertThat(event.getEventType()).isEqualTo(UsageQuotaEventPublisher.THRESHOLD_EVENT_TYPE);
        assertThat(event.getAggregateId()).isEqualTo(quota.getId());
        assertThat(event.getAggregateType()).isEqualTo("USAGE_QUOTA");
        assertThat(event.getCorrelationId()).isEqualTo("corr-80");
        assertThat(event.getSchemaVersion()).isEqualTo(1);
        assertThat(event.getPayloadJson()).contains("\"thresholdPercent\":80");
        assertThat(quota.getLastThresholdPercent()).isEqualTo(80);
    }

    @Test
    void publishesOnlyNewThresholds() {
        UsageQuota quota = quota(new BigDecimal("100.0000"));
        quota.consume(new BigDecimal("85.0000"), now);
        when(outboxEvents.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        publisher.publishQuotaEvents(quota, BigDecimal.ZERO, "corr-80", now);

        publisher.publishQuotaEvents(quota, new BigDecimal("85.0000"), "corr-80-repeat", now.plusSeconds(60));

        Mockito.verify(outboxEvents).save(any());
    }

    @Test
    void publishesMultipleThresholdsAndOverageWhenQuotaIsExceeded() {
        UsageQuota quota = quota(new BigDecimal("100.0000"));
        quota.consume(new BigDecimal("110.0000"), now);
        when(outboxEvents.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        publisher.publishQuotaEvents(quota, new BigDecimal("70.0000"), "corr-overage", now);

        ArgumentCaptor<UsageOutboxEvent> captor = ArgumentCaptor.forClass(UsageOutboxEvent.class);
        verify(outboxEvents, Mockito.times(4)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(UsageOutboxEvent::getEventType)
                .containsExactly(
                        UsageQuotaEventPublisher.THRESHOLD_EVENT_TYPE,
                        UsageQuotaEventPublisher.THRESHOLD_EVENT_TYPE,
                        UsageQuotaEventPublisher.THRESHOLD_EVENT_TYPE,
                        UsageQuotaEventPublisher.OVERAGE_EVENT_TYPE
                );
        assertThat(captor.getAllValues().get(0).getPayloadJson()).contains("\"thresholdPercent\":80");
        assertThat(captor.getAllValues().get(1).getPayloadJson()).contains("\"thresholdPercent\":90");
        assertThat(captor.getAllValues().get(2).getPayloadJson()).contains("\"thresholdPercent\":100");
        assertThat(captor.getAllValues().get(3).getPayloadJson())
                .contains("\"overageAmount\":10.0000")
                .contains("\"overageDeltaAmount\":10.0000");
        assertThat(quota.getLastThresholdPercent()).isEqualTo(100);
    }

    @Test
    void doesNotPublishOverageWhenPreviousUsageWasAlreadyAboveLimitAndDidNotIncreaseOverage() {
        UsageQuota quota = quota(new BigDecimal("100.0000"));
        quota.consume(new BigDecimal("110.0000"), now);
        quota.markThresholdPublished(100, now);

        publisher.publishQuotaEvents(quota, new BigDecimal("115.0000"), "corr-no-delta", now);

        verify(outboxEvents, never()).save(any());
    }

    private UsageQuota quota(BigDecimal totalAllowance) {
        return UsageQuota.create(
                UUID.randomUUID(),
                UsageQuota.QuotaType.DATA_MB,
                totalAllowance,
                now.minusSeconds(3600),
                now.plusSeconds(3600)
        );
    }

    private UsageOutboxEvent savedEvent() {
        ArgumentCaptor<UsageOutboxEvent> captor = ArgumentCaptor.forClass(UsageOutboxEvent.class);
        verify(outboxEvents).save(captor.capture());
        return captor.getValue();
    }
}
