package com.telcox.payment.processing;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MockCreditCardGateway {
    public AuthorizationResult charge(ProcessPaymentRequest request) {
        String cardNumber = normalize(request.cardNumber());
        if (!isLuhnValid(cardNumber)) {
            return AuthorizationResult.declined("Invalid card number");
        }
        if (cardNumber.endsWith("0002")) {
            return AuthorizationResult.declined("Mock issuer declined payment");
        }
        if (request.amount().compareTo(BigDecimal.valueOf(10_000)) > 0) {
            return AuthorizationResult.declined("Mock limit exceeded");
        }
        return AuthorizationResult.approved("MOCK-" + UUID.randomUUID());
    }

    public AuthorizationResult retry(Payment payment) {
        if (payment.getAmount().compareTo(BigDecimal.valueOf(10_000)) > 0) {
            return AuthorizationResult.declined("Mock limit exceeded");
        }
        return AuthorizationResult.approved("MOCK-RETRY-" + UUID.randomUUID());
    }

    private static String normalize(String cardNumber) {
        return cardNumber.replaceAll("\\s+", "");
    }

    private static boolean isLuhnValid(String cardNumber) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int index = cardNumber.length() - 1; index >= 0; index--) {
            int digit = Character.digit(cardNumber.charAt(index), 10);
            if (digit < 0) {
                return false;
            }
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    public record AuthorizationResult(boolean approved, String authorizationCode, String failureReason) {
        static AuthorizationResult approved(String authorizationCode) {
            return new AuthorizationResult(true, authorizationCode, null);
        }

        static AuthorizationResult declined(String reason) {
            return new AuthorizationResult(false, null, reason);
        }
    }
}
