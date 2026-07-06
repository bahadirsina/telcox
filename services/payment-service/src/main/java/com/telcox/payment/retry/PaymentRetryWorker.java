package com.telcox.payment.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentRetryWorker {
    private final PaymentRetryProcessor processor;
    private final int batchSize;

    public PaymentRetryWorker(
            PaymentRetryProcessor processor,
            @Value("${telcox.payment.retry.batch-size:100}") int batchSize) {
        this.processor = processor;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${telcox.payment.retry.poll-delay:60000}")
    public void poll() {
        processor.processDue(batchSize);
    }
}
