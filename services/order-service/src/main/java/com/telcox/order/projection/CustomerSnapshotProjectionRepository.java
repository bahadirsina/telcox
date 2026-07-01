package com.telcox.order.projection;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSnapshotProjectionRepository
        extends JpaRepository<CustomerSnapshotProjection, UUID> {
}
