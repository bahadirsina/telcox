package com.telcox.payment.retry;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_service_retry_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRetrySchedule {
    @Id private UUID id;
    private UUID paymentId;
    private Instant firstFailedAt;
    private int attemptNumber;
    private Instant nextRetryAt;
    private String status;
    private String lastError;
    private Instant updatedAt;

    public static PaymentRetrySchedule firstAttempt(UUID paymentId, Instant firstFailedAt) {
        PaymentRetrySchedule schedule = new PaymentRetrySchedule();
        schedule.id = UUID.randomUUID();
        schedule.paymentId = paymentId;
        schedule.firstFailedAt = firstFailedAt;
        schedule.attemptNumber = 1;
        schedule.nextRetryAt = PaymentRetryPolicy.retryAt(firstFailedAt, 1);
        schedule.status = "READY";
        schedule.updatedAt = firstFailedAt;
        return schedule;
    }

    public void markProcessing(Instant now) {
        status = "PROCESSING";
        updatedAt = now;
    }

    public void complete(Instant now) {
        status = "COMPLETED";
        lastError = null;
        updatedAt = now;
    }

    public void fail(String error, Instant now) {
        lastError = error;
        updatedAt = now;
        if (attemptNumber == PaymentRetryPolicy.MAX_ATTEMPTS) {
            status = "EXHAUSTED";
            return;
        }
        attemptNumber++;
        nextRetryAt = PaymentRetryPolicy.retryAt(firstFailedAt, attemptNumber);
        status = "READY";
    }
}
