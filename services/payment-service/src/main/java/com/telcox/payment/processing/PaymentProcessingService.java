package com.telcox.payment.processing;

import com.telcox.payment.idempotency.PaymentIdempotencyGuard;
import com.telcox.payment.retry.PaymentRetryScheduler;
import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class PaymentProcessingService {
    private final PaymentRepository payments;
    private final PaymentIdempotencyGuard idempotencyGuard;
    private final MockCreditCardGateway gateway;
    private final PaymentRetryScheduler retryScheduler;
    private final Clock clock;

    @Autowired
    public PaymentProcessingService(
            PaymentRepository payments,
            PaymentIdempotencyGuard idempotencyGuard,
            MockCreditCardGateway gateway,
            PaymentRetryScheduler retryScheduler) {
        this(payments, idempotencyGuard, gateway, retryScheduler, Clock.systemUTC());
    }

    PaymentProcessingService(
            PaymentRepository payments,
            PaymentIdempotencyGuard idempotencyGuard,
            MockCreditCardGateway gateway,
            PaymentRetryScheduler retryScheduler,
            Clock clock) {
        this.payments = payments;
        this.idempotencyGuard = idempotencyGuard;
        this.gateway = gateway;
        this.retryScheduler = retryScheduler;
        this.clock = clock;
    }

    @Transactional
    public PaymentResponse process(String idempotencyKey, ProcessPaymentRequest request) {
        String normalizedKey = normalize(idempotencyKey);
        return payments.findByIdempotencyKey(normalizedKey)
                .map(payment -> PaymentResponse.from(payment, true))
                .orElseGet(() -> createPayment(normalizedKey, request));
    }

    private PaymentResponse createPayment(String idempotencyKey, ProcessPaymentRequest request) {
        PaymentIdempotencyGuard.Lease lease = idempotencyGuard.tryAcquire(idempotencyKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Payment with this idempotency key is already being processed"));
        try {
            return payments.findByIdempotencyKey(idempotencyKey)
                    .map(payment -> PaymentResponse.from(payment, true))
                    .orElseGet(() -> persistNewPayment(idempotencyKey, request));
        } catch (RuntimeException exception) {
            idempotencyGuard.release(lease);
            throw exception;
        }
    }

    private PaymentResponse persistNewPayment(String idempotencyKey, ProcessPaymentRequest request) {
        Instant now = clock.instant();
        MockCreditCardGateway.AuthorizationResult authorization = gateway.charge(request);
        Payment payment = payments.save(Payment.create(idempotencyKey, request, authorization, now));
        if (payment.getStatus() == PaymentStatus.FAILED) {
            retryScheduler.scheduleFirstFailure(payment.getId(), now);
        }
        return PaymentResponse.from(payment, false);
    }

    private static String normalize(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
        }
        return idempotencyKey.trim();
    }
}
