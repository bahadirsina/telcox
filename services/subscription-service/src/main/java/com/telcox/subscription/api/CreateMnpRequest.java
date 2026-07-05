package com.telcox.subscription.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMnpRequest(
        @NotNull UUID customerId,
        @NotBlank String msisdn,
        @NotBlank String donorOperator,
        @NotBlank String recipientOperator,
        @NotBlank String planCode,
        String simIccid
) {
}
