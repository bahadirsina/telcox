package com.telcox.notification.projection;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPreferenceProjectionRepository
        extends JpaRepository<CustomerPreferenceProjection, UUID> {
}
