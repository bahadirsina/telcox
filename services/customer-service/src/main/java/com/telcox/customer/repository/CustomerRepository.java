package com.telcox.customer.repository;

import com.telcox.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByIdAndDeletedAtIsNull(UUID id);

    List<Customer> findByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<Customer> findByNationalId(String nationalId);

    Optional<Customer> findByIdentityUserId(UUID identityUserId);

    boolean existsByCustomerNumber(String customerNumber);

    boolean existsByNationalIdAndDeletedAtIsNull(String nationalId);
}
