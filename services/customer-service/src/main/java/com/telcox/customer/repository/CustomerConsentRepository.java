package com.telcox.customer.repository;

import com.telcox.customer.domain.ConsentType;
import com.telcox.customer.domain.CustomerConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerConsentRepository extends JpaRepository<CustomerConsent, UUID> {
    List<CustomerConsent> findByCustomerId(UUID customerId);

    Optional<CustomerConsent> findByCustomerIdAndConsentType(UUID customerId, ConsentType consentType);
}
