package com.telcox.payment.retry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRetryConfiguration {
    @Bean
    @ConditionalOnMissingBean(PaymentRetryExecutor.class)
    PaymentRetryExecutor paymentRetryExecutor() {
        return paymentId -> PaymentRetryExecutor.RetryResult.failure("Payment provider unavailable");
    }
}
