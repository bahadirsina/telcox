package com.telcox.usage.event;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageOutboxEventRepository extends JpaRepository<UsageOutboxEvent, UUID> {
}
