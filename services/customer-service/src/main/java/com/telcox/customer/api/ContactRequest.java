package com.telcox.customer.api;

import com.telcox.customer.domain.ContactType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContactRequest(
        @NotNull ContactType contactType,
        @NotBlank String contactValue,
        boolean isPrimary
) {
}
