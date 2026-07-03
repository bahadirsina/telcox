package com.telcox.order.projection;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffSnapshotProjectionRepository
        extends JpaRepository<TariffSnapshotProjection, UUID> {
}
