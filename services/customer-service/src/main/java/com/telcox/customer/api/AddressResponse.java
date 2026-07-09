package com.telcox.customer.api;

import com.telcox.customer.domain.AddressType;
import com.telcox.customer.domain.CustomerAddress;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        UUID customerId,
        AddressType addressType,
        String country,
        String city,
        String district,
        String street,
        String buildingNo,
        String postalCode,
        boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AddressResponse from(CustomerAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getCustomerId(),
                address.getAddressType(),
                address.getCountry(),
                address.getCity(),
                address.getDistrict(),
                address.getStreet(),
                address.getBuildingNo(),
                address.getPostalCode(),
                address.isDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}
