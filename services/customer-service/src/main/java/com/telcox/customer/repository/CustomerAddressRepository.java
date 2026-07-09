package com.telcox.customer.repository;

import com.telcox.customer.domain.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByCustomerId(UUID customerId);
}
