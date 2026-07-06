package com.telcox.payment.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

class PaymentRetryProcessorTest {
    private final PaymentRetryScheduleRepository schedules =
            Mockito.mock(PaymentRetryScheduleRepository.class);
    private final PaymentRetryExecutor executor = Mockito.mock(PaymentRetryExecutor.class);
    private final Instant failedAt = Instant.parse("2026-07-01T00:00:00Z");
    private final Instant now = failedAt.plusSeconds(24 * 3600);
    private final PaymentRetryProcessor processor = new PaymentRetryProcessor(
            schedules, executor, Clock.fixed(now, ZoneOffset.UTC));

    @Test
    void completesSuccessfulRetry() {
        PaymentRetrySchedule schedule = PaymentRetrySchedule.firstAttempt(UUID.randomUUID(), failedAt);
        when(schedules.findDue(any(), any(Pageable.class))).thenReturn(List.of(schedule));
        when(executor.retry(schedule.getPaymentId())).thenReturn(PaymentRetryExecutor.RetryResult.success());

        assertThat(processor.processDue(10)).isEqualTo(1);
        assertThat(schedule.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void failureSchedulesNextAbsoluteWindow() {
        PaymentRetrySchedule schedule = PaymentRetrySchedule.firstAttempt(UUID.randomUUID(), failedAt);
        when(schedules.findDue(any(), any(Pageable.class))).thenReturn(List.of(schedule));
        when(executor.retry(schedule.getPaymentId()))
                .thenReturn(PaymentRetryExecutor.RetryResult.failure("timeout"));

        processor.processDue(10);

        assertThat(schedule.getStatus()).isEqualTo("READY");
        assertThat(schedule.getAttemptNumber()).isEqualTo(2);
        assertThat(schedule.getNextRetryAt()).isEqualTo(failedAt.plusSeconds(72 * 3600));
    }
}
