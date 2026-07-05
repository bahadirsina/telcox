package com.telcox.order.api;

import com.telcox.order.domain.OrderType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotNull OrderType orderType,
        String planCode,
        List<String> addonCodes,
        String msisdn,
        String simIccid
) {
}
