package com.telcox.usage.cdr;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_service_usage_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageRecord {
    @Id private UUID id;
    private String externalCdrId;
    private UUID subscriptionId;
    private String msisdn;
    @Enumerated(EnumType.STRING)
    private CdrEvent.RecordType recordType;
    private Instant occurredAt;
    private long durationSeconds;
    private long dataVolumeBytes;
    private long smsCount;
    private Instant createdAt;

    static UsageRecord from(CdrEvent event, Instant now) {
        UsageRecord record = new UsageRecord();
        record.id = UUID.randomUUID();
        record.externalCdrId = event.externalCdrId();
        record.subscriptionId = event.subscriptionId();
        record.msisdn = event.msisdn();
        record.recordType = event.recordType();
        record.occurredAt = event.occurredAt();
        record.durationSeconds = event.durationSeconds();
        record.dataVolumeBytes = event.dataVolumeBytes();
        record.smsCount = event.smsCount();
        record.createdAt = now;
        return record;
    }
}
