package com.telcox.customer.api;

import com.telcox.customer.domain.AddressType;
import jakarta.validation.constraints.NotNull;

public record AddressRequest(
        @NotNull AddressType addressType,
        String country,
        String city,
        String district,
        String street,
        String buildingNo,
        String postalCode,
        boolean isDefault
) {
}
