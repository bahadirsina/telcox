package com.telcox.payment.processing;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MockCreditCardGatewayTest {
    private final MockCreditCardGateway gateway = new MockCreditCardGateway();

    @Test
    void approvesValidMockVisaCard() {
        MockCreditCardGateway.AuthorizationResult result = gateway.charge(request("4111111111111111", "99.90"));

        assertThat(result.approved()).isTrue();
        assertThat(result.authorizationCode()).startsWith("MOCK-");
    }

    @Test
    void declinesConfiguredMockFailureCard() {
        MockCreditCardGateway.AuthorizationResult result = gateway.charge(request("4000000000000002", "99.90"));

        assertThat(result.approved()).isFalse();
        assertThat(result.failureReason()).isEqualTo("Mock issuer declined payment");
    }

    @Test
    void declinesInvalidCardNumber() {
        MockCreditCardGateway.AuthorizationResult result = gateway.charge(request("4111111111111112", "99.90"));

        assertThat(result.approved()).isFalse();
        assertThat(result.failureReason()).isEqualTo("Invalid card number");
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
}
