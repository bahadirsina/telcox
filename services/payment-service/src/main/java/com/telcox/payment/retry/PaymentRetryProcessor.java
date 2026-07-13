package com.telcox.payment.retry;

import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentRetryProcessor {
    private final PaymentRetryScheduleRepository schedules;
    private final PaymentRetryExecutor executor;
    private final Clock clock;

    @Autowired
    public PaymentRetryProcessor(PaymentRetryScheduleRepository schedules, PaymentRetryExecutor executor) {
        this(schedules, executor, Clock.systemUTC());
    }

    PaymentRetryProcessor(
            PaymentRetryScheduleRepository schedules, PaymentRetryExecutor executor, Clock clock) {
        this.schedules = schedules;
        this.executor = executor;
        this.clock = clock;
    }

    @Transactional
    public int processDue(int batchSize) {
        Instant now = clock.instant();
        var dueSchedules = schedules.findDue(now, PageRequest.of(0, batchSize));
        for (PaymentRetrySchedule schedule : dueSchedules) {
            schedule.markProcessing(now);
            PaymentRetryExecutor.RetryResult result = executor.retry(schedule.getPaymentId());
            if (result.successful()) {
                schedule.complete(now);
            } else {
                schedule.fail(result.error(), now);
            }
        }
        return dueSchedules.size();
    }
}
