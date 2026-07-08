package com.telcox.productcatalog.repository;

import com.telcox.productcatalog.domain.PlanFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, UUID> {
    List<PlanFeature> findByPlanId(UUID planId);
}
