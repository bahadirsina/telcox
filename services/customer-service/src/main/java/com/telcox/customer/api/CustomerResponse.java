package com.telcox.customer.api;

import com.telcox.customer.domain.Customer;
import com.telcox.customer.domain.CustomerSegment;
import com.telcox.customer.domain.CustomerStatus;
import com.telcox.customer.domain.CustomerType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID identityUserId,
        String customerNumber,
        CustomerType customerType,
        String firstName,
        String lastName,
        String nationalId,
        LocalDate dateOfBirth,
        CustomerSegment segment,
        CustomerStatus status,
        String statusReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getIdentityUserId(),
                customer.getCustomerNumber(),
                customer.getCustomerType(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getNationalId(),
                customer.getDateOfBirth(),
                customer.getSegment(),
                customer.getStatus(),
                customer.getStatusReason(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
