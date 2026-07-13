package com.telcox.payment.retry;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentRetryScheduler {
    private final PaymentRetryScheduleRepository schedules;
    private final Clock clock;

    @Autowired
    public PaymentRetryScheduler(PaymentRetryScheduleRepository schedules) {
        this(schedules, Clock.systemUTC());
    }

    PaymentRetryScheduler(PaymentRetryScheduleRepository schedules, Clock clock) {
        this.schedules = schedules;
        this.clock = clock;
    }

    @Transactional
    public PaymentRetrySchedule scheduleFirstFailure(UUID paymentId, Instant failedAt) {
        if (schedules.existsByPaymentId(paymentId)) {
            throw new IllegalStateException("Retry schedule already exists for payment " + paymentId);
        }
        Instant effectiveFailureTime = failedAt == null ? clock.instant() : failedAt;
        return schedules.save(PaymentRetrySchedule.firstAttempt(paymentId, effectiveFailureTime));
    }
}
