package com.telcox.usage.cdr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.UUID;

public record CdrEvent(
        @NotBlank String externalCdrId,
        @NotNull UUID subscriptionId,
        @NotBlank String msisdn,
        @NotNull RecordType recordType,
        @NotNull Instant occurredAt,
        @PositiveOrZero long durationSeconds,
        @PositiveOrZero long dataVolumeBytes,
        @PositiveOrZero long smsCount) {
    public enum RecordType {
        VOICE, SMS, DATA
    }
}
