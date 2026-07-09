package com.telcox.usage.cdr;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {
    boolean existsByExternalCdrId(String externalCdrId);
}
