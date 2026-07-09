package com.telcox.customer.api;

import com.telcox.customer.domain.ConsentChannel;
import com.telcox.customer.domain.ConsentType;
import com.telcox.customer.domain.CustomerConsent;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsentResponse(
        UUID id,
        UUID customerId,
        ConsentType consentType,
        ConsentChannel channel,
        boolean granted,
        LocalDateTime grantedAt,
        LocalDateTime revokedAt
) {
    public static ConsentResponse from(CustomerConsent consent) {
        return new ConsentResponse(
                consent.getId(),
                consent.getCustomerId(),
                consent.getConsentType(),
                consent.getChannel(),
                consent.isGranted(),
                consent.getGrantedAt(),
                consent.getRevokedAt()
        );
    }
}
