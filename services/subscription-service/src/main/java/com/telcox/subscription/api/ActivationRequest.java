package com.telcox.subscription.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ActivationRequest(
        @NotNull UUID customerId,
        UUID orderId,
        String msisdn,
        String simIccid,
        @NotBlank String planCode,
        List<String> addonCodes
) {
}
