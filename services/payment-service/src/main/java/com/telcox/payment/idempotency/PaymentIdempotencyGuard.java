package com.telcox.payment.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class PaymentIdempotencyGuard {

    static final String KEY_PREFIX = "payment:idempotency:";
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] "
                    + "then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public PaymentIdempotencyGuard(
            StringRedisTemplate redisTemplate,
            @Value("${telcox.payment.idempotency.ttl:PT24H}") Duration ttl
    ) {
        this.redisTemplate = redisTemplate;
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("idempotency ttl must be positive");
        }
        this.ttl = ttl;
    }

    /**
     * Acquires a best-effort processing lease. Keep the key after a successful
     * payment; call release only when processing fails before persistence.
     */
    public Optional<Lease> tryAcquire(String idempotencyKey) {
        requireText(idempotencyKey);
        String redisKey = KEY_PREFIX + sha256(idempotencyKey.trim());
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, token, ttl);
        return Boolean.TRUE.equals(acquired)
                ? Optional.of(new Lease(redisKey, token))
                : Optional.empty();
    }

    public boolean release(Lease lease) {
        Objects.requireNonNull(lease, "lease must not be null");
        Long released = redisTemplate.execute(
                RELEASE_SCRIPT,
                List.of(lease.redisKey()),
                lease.token()
        );
        return Long.valueOf(1L).equals(released);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void requireText(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must not be blank");
        }
    }

    public record Lease(String redisKey, String token) {
        public Lease {
            requireText(redisKey);
            requireText(token);
            if (!redisKey.startsWith(KEY_PREFIX)) {
                throw new IllegalArgumentException("lease key must use payment idempotency prefix");
            }
        }
    }
}
