package com.telcox.usage.cdr;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedCdrEventRepository extends JpaRepository<ProcessedCdrEvent, UUID> {
    boolean existsByEventId(UUID eventId);
}
