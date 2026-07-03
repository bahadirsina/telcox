package com.telcox.payment.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class PaymentIdempotencyGuardTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private PaymentIdempotencyGuard guard;

    @BeforeEach
    void setUp() {
        guard = new PaymentIdempotencyGuard(redisTemplate, Duration.ofHours(24));
    }

    @Test
    void acquiresLeaseAtomicallyWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), eq(Duration.ofHours(24))))
                .thenReturn(true);

        Optional<PaymentIdempotencyGuard.Lease> lease = guard.tryAcquire("payment-request-42");

        assertThat(lease).isPresent();
        assertThat(lease.orElseThrow().redisKey())
                .startsWith(PaymentIdempotencyGuard.KEY_PREFIX)
                .doesNotContain("payment-request-42");
        verify(valueOperations).setIfAbsent(
                eq(lease.orElseThrow().redisKey()),
                eq(lease.orElseThrow().token()),
                eq(Duration.ofHours(24))
        );
    }

    @Test
    void rejectsDuplicateKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
                .thenReturn(false);

        assertThat(guard.tryAcquire("payment-request-42")).isEmpty();
    }

    @Test
    void releasesOnlyWithLeaseToken() {
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                anyList(),
                eq("lease-token")
        )).thenReturn(1L);

        PaymentIdempotencyGuard.Lease lease = new PaymentIdempotencyGuard.Lease(
                PaymentIdempotencyGuard.KEY_PREFIX + "digest",
                "lease-token"
        );

        assertThat(guard.release(lease)).isTrue();
    }

    @Test
    void rejectsBlankIdempotencyKey() {
        assertThatThrownBy(() -> guard.tryAcquire(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("idempotencyKey must not be blank");
    }

    @Test
    void rejectsNonPositiveTtl() {
        assertThatThrownBy(() -> new PaymentIdempotencyGuard(redisTemplate, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("idempotency ttl must be positive");
    }
}
