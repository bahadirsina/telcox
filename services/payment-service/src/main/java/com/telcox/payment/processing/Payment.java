package com.telcox.payment.processing;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_service_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    @Id private UUID id;
    private String paymentReference;
    private String idempotencyKey;
    private UUID customerId;
    private UUID invoiceId;
    private BigDecimal amount;
    private String currency;
    private String cardLastFour;
    private String cardHolderName;
    private String providerAuthCode;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String failureReason;
    private Instant initiatedAt;
    private Instant completedAt;

    public static Payment create(
            String idempotencyKey,
            ProcessPaymentRequest request,
            MockCreditCardGateway.AuthorizationResult authorization,
            Instant now) {
        Payment payment = new Payment();
        payment.id = UUID.randomUUID();
        payment.paymentReference = "PAY-" + payment.id;
        payment.idempotencyKey = idempotencyKey.trim();
        payment.customerId = request.customerId();
        payment.invoiceId = request.invoiceId();
        payment.amount = request.amount();
        payment.currency = request.currency().trim().toUpperCase();
        payment.cardLastFour = lastFour(request.cardNumber());
        payment.cardHolderName = request.cardHolderName();
        payment.providerAuthCode = authorization.authorizationCode();
        payment.status = authorization.approved() ? PaymentStatus.CAPTURED : PaymentStatus.FAILED;
        payment.failureReason = authorization.approved() ? null : authorization.failureReason();
        payment.initiatedAt = now;
        payment.completedAt = authorization.approved() ? now : null;
        return payment;
    }

    public void markCaptured(String authorizationCode, Instant now) {
        status = PaymentStatus.CAPTURED;
        providerAuthCode = authorizationCode;
        failureReason = null;
        completedAt = now;
    }

    public void markFailed(String reason) {
        status = PaymentStatus.FAILED;
        failureReason = reason;
    }

    private static String lastFour(String cardNumber) {
        String digits = cardNumber.replaceAll("\\s+", "");
        return digits.substring(digits.length() - 4);
    }
}
