package com.telcox.customer.api;

import com.telcox.customer.domain.CustomerSegment;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

/**
 * FR-01: Bireysel musteri kaydi istegi.
 */
public record CustomerRegistrationRequest(
        UUID identityUserId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String nationalId,
        LocalDate dateOfBirth,
        CustomerSegment segment
) {
}
