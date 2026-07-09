package com.telcox.productcatalog.repository;

import com.telcox.productcatalog.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByProductId(UUID productId);
}
