package com.telcox.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FR-03: Musteri adresi. ER: db.sql -> CUSTOMER_SERVICE_ADDRESS
 */
@Entity
@Table(name = "customer_service_address")
public class CustomerAddress {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(length = 255)
    private String street;

    @Column(name = "building_no", length = 50)
    private String buildingNo;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CustomerAddress() {
    }

    public CustomerAddress(UUID customerId, AddressType addressType, String country, String city, String district,
                           String street, String buildingNo, String postalCode, boolean isDefault) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.addressType = addressType;
        this.country = country;
        this.city = city;
        this.district = district;
        this.street = street;
        this.buildingNo = buildingNo;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void refreshUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void markAsDefault() {
        this.isDefault = true;
    }

    public void unmarkAsDefault() {
        this.isDefault = false;
    }

    public void update(String country, String city, String district, String street, String buildingNo, String postalCode) {
        this.country = country;
        this.city = city;
        this.district = district;
        this.street = street;
        this.buildingNo = buildingNo;
        this.postalCode = postalCode;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getStreet() {
        return street;
    }

    public String getBuildingNo() {
        return buildingNo;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
