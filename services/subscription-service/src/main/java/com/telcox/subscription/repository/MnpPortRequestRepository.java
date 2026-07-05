package com.telcox.subscription.repository;

import com.telcox.subscription.domain.MnpPortRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MnpPortRequestRepository extends JpaRepository<MnpPortRequest, UUID> {
    List<MnpPortRequest> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
