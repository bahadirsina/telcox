package com.telcox.payment.retry;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class PaymentRetryPolicy {
    public static final int MAX_ATTEMPTS = 3;
    private static final List<Duration> WINDOWS =
            List.of(Duration.ofHours(24), Duration.ofHours(72), Duration.ofHours(168));

    private PaymentRetryPolicy() {
    }

    public static Instant retryAt(Instant firstFailedAt, int attemptNumber) {
        if (attemptNumber < 1 || attemptNumber > MAX_ATTEMPTS) {
            throw new IllegalArgumentException("Retry attempt must be between 1 and " + MAX_ATTEMPTS);
        }
        return firstFailedAt.plus(WINDOWS.get(attemptNumber - 1));
    }
}
