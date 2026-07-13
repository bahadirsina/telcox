package com.telcox.payment.processing;

import com.telcox.payment.retry.PaymentRetryExecutor;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentRetryPaymentExecutor implements PaymentRetryExecutor {
    private final PaymentRepository payments;
    private final MockCreditCardGateway gateway;
    private final Clock clock;

    @Autowired
    public PaymentRetryPaymentExecutor(PaymentRepository payments, MockCreditCardGateway gateway) {
        this(payments, gateway, Clock.systemUTC());
    }

    PaymentRetryPaymentExecutor(PaymentRepository payments, MockCreditCardGateway gateway, Clock clock) {
        this.payments = payments;
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RetryResult retry(UUID paymentId) {
        Payment payment = payments.findById(paymentId)
                .orElse(null);
        if (payment == null) {
            return RetryResult.failure("Payment not found");
        }
        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            return RetryResult.success();
        }
        MockCreditCardGateway.AuthorizationResult result = gateway.retry(payment);
        if (result.approved()) {
            payment.markCaptured(result.authorizationCode(), clock.instant());
            return RetryResult.success();
        }
        payment.markFailed(result.failureReason());
        return RetryResult.failure(result.failureReason());
    }
}
