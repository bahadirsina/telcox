package com.telcox.gateway.security;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Short-lived, gateway-owned token revocation helper backed by Redis.
 * Redis ulaşılamazsa doğrulanmış token da fail-closed olarak reddedilir.
 */
@Service
public class TokenDenylistService {

    private static final String PREFIX = "gateway:denylist:jti:";
    private final ReactiveStringRedisTemplate redis;

    public TokenDenylistService(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }

    public Mono<Boolean> isDenied(Jwt jwt) {
        if (jwt.getId() == null || jwt.getId().isBlank()) {
            return Mono.just(false);
        }
        return redis.hasKey(key(jwt.getId())).onErrorReturn(true);
    }

    /** Denies a token only until its natural expiry; no token material is stored. */
    public Mono<Boolean> deny(Jwt jwt) {
        if (jwt.getId() == null || jwt.getId().isBlank() || jwt.getExpiresAt() == null) {
            return Mono.just(false);
        }
        Duration ttl = Duration.between(Instant.now(), jwt.getExpiresAt());
        if (ttl.isZero() || ttl.isNegative()) {
            return Mono.just(false);
        }
        return redis.opsForValue().set(key(jwt.getId()), "revoked", ttl);
    }

    private String key(String tokenId) {
        return PREFIX + tokenId;
    }
}
