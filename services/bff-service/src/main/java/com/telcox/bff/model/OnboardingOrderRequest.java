package com.telcox.bff.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OnboardingOrderRequest(
        @NotNull UUID customerId,
        @NotBlank String orderType,
        String planCode,
        List<String> addonCodes,
        String msisdn,
        String simIccid
) {
}
