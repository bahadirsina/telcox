package com.telcox.payment.processing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.telcox.payment.retry.PaymentRetryExecutor;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PaymentRetryPaymentExecutorTest {
    private final PaymentRepository payments = Mockito.mock(PaymentRepository.class);
    private final MockCreditCardGateway gateway = Mockito.mock(MockCreditCardGateway.class);
    private final PaymentRetryPaymentExecutor executor = new PaymentRetryPaymentExecutor(
            payments,
            gateway,
            Clock.fixed(Instant.parse("2026-07-07T12:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void capturesFailedPaymentWhenRetrySucceeds() {
        Payment payment = failedPayment();
        when(payments.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(gateway.retry(payment))
                .thenReturn(MockCreditCardGateway.AuthorizationResult.approved("MOCK-RETRY"));

        PaymentRetryExecutor.RetryResult result = executor.retry(payment.getId());

        assertThat(result.successful()).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(payment.getProviderAuthCode()).isEqualTo("MOCK-RETRY");
    }

    @Test
    void reportsFailureWhenPaymentDoesNotExist() {
        UUID paymentId = UUID.randomUUID();
        when(payments.findById(paymentId)).thenReturn(Optional.empty());

        PaymentRetryExecutor.RetryResult result = executor.retry(paymentId);

        assertThat(result.successful()).isFalse();
        assertThat(result.error()).isEqualTo("Payment not found");
    }

    private static Payment failedPayment() {
        return Payment.create(
                "retry-key",
                new ProcessPaymentRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("99.90"),
                        "TRY",
                        "4000000000000002",
                        12,
                        2030,
                        "123",
                        "Ada Lovelace"
                ),
                MockCreditCardGateway.AuthorizationResult.declined("Mock issuer declined payment"),
                Instant.parse("2026-07-07T10:00:00Z")
        );
    }
}
