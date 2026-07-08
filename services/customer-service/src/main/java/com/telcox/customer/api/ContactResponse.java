package com.telcox.customer.api;

import com.telcox.customer.domain.ContactType;
import com.telcox.customer.domain.CustomerContact;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        UUID customerId,
        ContactType contactType,
        String contactValue,
        boolean isVerified,
        boolean isPrimary,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
) {
    public static ContactResponse from(CustomerContact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getCustomerId(),
                contact.getContactType(),
                contact.getContactValue(),
                contact.isVerified(),
                contact.isPrimary(),
                contact.getVerifiedAt(),
                contact.getCreatedAt()
        );
    }
}
