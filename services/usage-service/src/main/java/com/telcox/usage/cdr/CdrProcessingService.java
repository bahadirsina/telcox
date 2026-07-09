package com.telcox.usage.cdr;

import com.telcox.common.event.EventEnvelope;
import com.telcox.usage.event.UsageQuotaEventPublisher;
import com.telcox.usage.quota.UsageQuota;
import com.telcox.usage.quota.UsageQuotaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CdrProcessingService {
    private static final BigDecimal SECONDS_PER_MINUTE = BigDecimal.valueOf(60);
    private static final BigDecimal BYTES_PER_MEBIBYTE = BigDecimal.valueOf(1_048_576);
    private final UsageRecordRepository records;
    private final ProcessedCdrEventRepository processedEvents;
    private final UsageQuotaRepository quotas;
    private final UsageQuotaEventPublisher quotaEventPublisher;
    private final Clock clock;

    public CdrProcessingService(
            UsageRecordRepository records, ProcessedCdrEventRepository processedEvents,
            UsageQuotaRepository quotas, UsageQuotaEventPublisher quotaEventPublisher) {
        this(records, processedEvents, quotas, quotaEventPublisher, Clock.systemUTC());
    }

    CdrProcessingService(
            UsageRecordRepository records, ProcessedCdrEventRepository processedEvents,
            UsageQuotaRepository quotas, UsageQuotaEventPublisher quotaEventPublisher, Clock clock) {
        this.records = records;
        this.processedEvents = processedEvents;
        this.quotas = quotas;
        this.quotaEventPublisher = quotaEventPublisher;
        this.clock = clock;
    }

    @Transactional
    public boolean process(EventEnvelope<CdrEvent> envelope) {
        CdrEvent event = envelope.payload();
        if (event == null) {
            throw new IllegalArgumentException("CDR payload must not be null");
        }
        if (processedEvents.existsByEventId(envelope.eventId())
                || records.existsByExternalCdrId(event.externalCdrId())) {
            return false;
        }
        UsageQuota.QuotaType quotaType = quotaType(event.recordType());
        UsageQuota quota = quotas.findActive(event.subscriptionId(), quotaType, event.occurredAt())
                .orElseThrow(() -> new IllegalStateException(
                        "Active quota not found for subscription " + event.subscriptionId()));
        Instant now = clock.instant();
        BigDecimal previousUsedAmount = quota.getUsedAmount();
        quota.consume(consumption(event), now);
        quotaEventPublisher.publishQuotaEvents(quota, previousUsedAmount, envelope.correlationId(), now);
        records.save(UsageRecord.from(event, now));
        processedEvents.save(ProcessedCdrEvent.success(envelope.eventId(), event, now));
        return true;
    }

    private static UsageQuota.QuotaType quotaType(CdrEvent.RecordType type) {
        return switch (type) {
            case VOICE -> UsageQuota.QuotaType.VOICE_MIN;
            case SMS -> UsageQuota.QuotaType.SMS_COUNT;
            case DATA -> UsageQuota.QuotaType.DATA_MB;
        };
    }

    private static BigDecimal consumption(CdrEvent event) {
        return switch (event.recordType()) {
            case VOICE -> BigDecimal.valueOf(event.durationSeconds())
                    .divide(SECONDS_PER_MINUTE, 4, RoundingMode.HALF_UP);
            case SMS -> BigDecimal.valueOf(event.smsCount());
            case DATA -> BigDecimal.valueOf(event.dataVolumeBytes())
                    .divide(BYTES_PER_MEBIBYTE, 4, RoundingMode.HALF_UP);
        };
    }
}
