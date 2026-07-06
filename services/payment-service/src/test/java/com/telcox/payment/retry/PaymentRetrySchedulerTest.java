package com.telcox.payment.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PaymentRetrySchedulerTest {
    private final PaymentRetryScheduleRepository schedules =
            Mockito.mock(PaymentRetryScheduleRepository.class);
    private final Instant failedAt = Instant.parse("2026-07-06T10:00:00Z");
    private final PaymentRetryScheduler scheduler =
            new PaymentRetryScheduler(schedules, Clock.fixed(failedAt, ZoneOffset.UTC));

    @Test
    void createsFirstRetryAtTwentyFourHours() {
        UUID paymentId = UUID.randomUUID();
        when(schedules.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRetrySchedule result = scheduler.scheduleFirstFailure(paymentId, null);

        assertThat(result.getAttemptNumber()).isEqualTo(1);
        assertThat(result.getNextRetryAt()).isEqualTo(failedAt.plusSeconds(24 * 3600));
    }

    @Test
    void rejectsDuplicateSchedule() {
        UUID paymentId = UUID.randomUUID();
        when(schedules.existsByPaymentId(paymentId)).thenReturn(true);

        assertThatThrownBy(() -> scheduler.scheduleFirstFailure(paymentId, failedAt))
                .isInstanceOf(IllegalStateException.class);
    }
}
