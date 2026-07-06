package com.telcox.payment.processing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.telcox.payment.idempotency.PaymentIdempotencyGuard;
import com.telcox.payment.retry.PaymentRetryScheduler;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class PaymentProcessingServiceTest {
    private final PaymentRepository payments = Mockito.mock(PaymentRepository.class);
    private final PaymentIdempotencyGuard idempotencyGuard = Mockito.mock(PaymentIdempotencyGuard.class);
    private final MockCreditCardGateway gateway = Mockito.mock(MockCreditCardGateway.class);
    private final PaymentRetryScheduler retryScheduler = Mockito.mock(PaymentRetryScheduler.class);
    private final Instant now = Instant.parse("2026-07-07T12:00:00Z");
    private final PaymentProcessingService service = new PaymentProcessingService(
            payments,
            idempotencyGuard,
            gateway,
            retryScheduler,
            Clock.fixed(now, ZoneOffset.UTC)
    );

    @Test
    void processesApprovedPaymentWithIdempotencyLease() {
        ProcessPaymentRequest request = request("4111111111111111", "149.90");
        when(payments.findByIdempotencyKey("pay-key-1")).thenReturn(Optional.empty());
        when(idempotencyGuard.tryAcquire("pay-key-1")).thenReturn(Optional.of(lease()));
        when(gateway.charge(request))
                .thenReturn(MockCreditCardGateway.AuthorizationResult.approved("MOCK-AUTH"));
        when(payments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = service.process(" pay-key-1 ", request);

        assertThat(response.status()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(response.cardLastFour()).isEqualTo("1111");
        assertThat(response.idempotentReplay()).isFalse();
        verify(retryScheduler, never()).scheduleFirstFailure(any(), any());
    }

    @Test
    void returnsStoredPaymentForDuplicateIdempotencyKey() {
        ProcessPaymentRequest request = request("4111111111111111", "149.90");
        Payment existing = Payment.create(
                "pay-key-1",
                request,
                MockCreditCardGateway.AuthorizationResult.approved("MOCK-AUTH"),
                now
        );
        when(payments.findByIdempotencyKey("pay-key-1")).thenReturn(Optional.of(existing));

        PaymentResponse response = service.process("pay-key-1", request);

        assertThat(response.paymentId()).isEqualTo(existing.getId());
        assertThat(response.idempotentReplay()).isTrue();
        verify(idempotencyGuard, never()).tryAcquire(any());
        verify(gateway, never()).charge(any());
    }

    @Test
    void schedulesRetryForDeclinedPayment() {
        ProcessPaymentRequest request = request("4000000000000002", "149.90");
        when(payments.findByIdempotencyKey("pay-key-2")).thenReturn(Optional.empty());
        when(idempotencyGuard.tryAcquire("pay-key-2")).thenReturn(Optional.of(lease()));
        when(gateway.charge(request))
                .thenReturn(MockCreditCardGateway.AuthorizationResult.declined("Mock issuer declined payment"));
        when(payments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = service.process("pay-key-2", request);

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("Mock issuer declined payment");
        verify(retryScheduler).scheduleFirstFailure(eq(response.paymentId()), eq(now));
    }

    @Test
    void rejectsConcurrentDuplicateLease() {
        ProcessPaymentRequest request = request("4111111111111111", "149.90");
        when(payments.findByIdempotencyKey("pay-key-3")).thenReturn(Optional.empty());
        when(idempotencyGuard.tryAcquire("pay-key-3")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.process("pay-key-3", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already being processed");
    }

    private static ProcessPaymentRequest request(String cardNumber, String amount) {
        return new ProcessPaymentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal(amount),
                "TRY",
                cardNumber,
                12,
                2030,
                "123",
                "Ada Lovelace"
        );
    }

    private static PaymentIdempotencyGuard.Lease lease() {
        return new PaymentIdempotencyGuard.Lease(
                "payment:idempotency:digest",
                "lease-token"
        );
    }
}
