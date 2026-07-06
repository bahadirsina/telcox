package com.telcox.payment.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class PaymentRetryPolicyTest {
    private final Instant failedAt = Instant.parse("2026-07-01T00:00:00Z");

    @Test
    void usesAbsoluteRetryWindows() {
        assertThat(PaymentRetryPolicy.retryAt(failedAt, 1)).isEqualTo(failedAt.plusSeconds(24 * 3600));
        assertThat(PaymentRetryPolicy.retryAt(failedAt, 2)).isEqualTo(failedAt.plusSeconds(72 * 3600));
        assertThat(PaymentRetryPolicy.retryAt(failedAt, 3)).isEqualTo(failedAt.plusSeconds(168 * 3600));
    }

    @Test
    void rejectsUnsupportedAttempts() {
        assertThatThrownBy(() -> PaymentRetryPolicy.retryAt(failedAt, 4))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
