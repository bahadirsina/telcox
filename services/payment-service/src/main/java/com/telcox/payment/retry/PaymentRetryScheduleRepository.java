package com.telcox.payment.retry;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRetryScheduleRepository extends JpaRepository<PaymentRetrySchedule, UUID> {
    boolean existsByPaymentId(UUID paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select schedule from PaymentRetrySchedule schedule
            where schedule.status = 'READY' and schedule.nextRetryAt <= :now
            order by schedule.nextRetryAt
            """)
    List<PaymentRetrySchedule> findDue(Instant now, Pageable pageable);
}
