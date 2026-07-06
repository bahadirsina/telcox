package com.telcox.payment.processing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String paymentReference,
        UUID customerId,
        UUID invoiceId,
        BigDecimal amount,
        String currency,
        String cardLastFour,
        PaymentStatus status,
        String failureReason,
        Instant initiatedAt,
        Instant completedAt,
        boolean idempotentReplay) {
    static PaymentResponse from(Payment payment, boolean replayed) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getCustomerId(),
                payment.getInvoiceId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getCardLastFour(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.getInitiatedAt(),
                payment.getCompletedAt(),
                replayed
        );
    }
}
