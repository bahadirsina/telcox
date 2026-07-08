package com.telcox.customer.api;

import com.telcox.customer.domain.ConsentChannel;
import com.telcox.customer.domain.ConsentType;
import jakarta.validation.constraints.NotNull;

public record ConsentRequest(
        @NotNull ConsentType consentType,
        ConsentChannel channel,
        boolean granted
) {
}
