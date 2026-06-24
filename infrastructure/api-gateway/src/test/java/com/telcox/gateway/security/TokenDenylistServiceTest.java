package com.telcox.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenDenylistServiceTest {

    @Test
    void rejectsTokenWhenRedisIsUnavailable() {
        ReactiveStringRedisTemplate redis = mock(ReactiveStringRedisTemplate.class);
        when(redis.hasKey(anyString())).thenReturn(Mono.error(
                new RedisConnectionFailureException("Redis unavailable")));

        TokenDenylistService service = new TokenDenylistService(redis);

        assertThat(service.isDenied(jwtWithId()).block()).isTrue();
    }

    @Test
    void allowsTokenWhenRedisDoesNotContainItsId() {
        ReactiveStringRedisTemplate redis = mock(ReactiveStringRedisTemplate.class);
        when(redis.hasKey(anyString())).thenReturn(Mono.just(false));

        TokenDenylistService service = new TokenDenylistService(redis);

        assertThat(service.isDenied(jwtWithId()).block()).isFalse();
    }

    private Jwt jwtWithId() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("jti", "test-jti")
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
