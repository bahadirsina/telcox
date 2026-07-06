package com.telcox.payment.retry;

import java.util.UUID;

@FunctionalInterface
public interface PaymentRetryExecutor {
    RetryResult retry(UUID paymentId);

    record RetryResult(boolean successful, String error) {
        public static RetryResult success() {
            return new RetryResult(true, null);
        }

        public static RetryResult failure(String error) {
            return new RetryResult(false, error);
        }
    }
}
