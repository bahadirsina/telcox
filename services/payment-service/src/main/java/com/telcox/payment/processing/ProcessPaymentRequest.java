package com.telcox.payment.processing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequest(
        @NotNull UUID customerId,
        UUID invoiceId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
        @NotBlank @Pattern(regexp = "^[0-9 ]{12,23}$") String cardNumber,
        @NotNull @Min(1) @Max(12) Integer expiryMonth,
        @NotNull @Min(2026) @Max(2100) Integer expiryYear,
        @NotBlank @Pattern(regexp = "^[0-9]{3,4}$") String cvv,
        @NotBlank String cardHolderName) {
}
