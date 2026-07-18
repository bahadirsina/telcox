package com.telcox.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

/**
 * INF-04 — Rate limit anahtarı çözümleyici.
 *
 * Kimliği doğrulanmış istekte kullanıcı (JWT 'sub'), aksi halde client IP baz alınır.
 * application.yaml'daki RequestRateLimiter filtresi bu bean'i "#{@userOrIpKeyResolver}"
 * ile referanslar. Sayaçlar Redis'te tutulur (ADR-0004 sınırlarına uygun).
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userOrIpKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    String name = ctx.getAuthentication() != null ? ctx.getAuthentication().getName() : null;
                    return name == null || name.isBlank() ? Mono.empty() : Mono.just(name);
                })
                .switchIfEmpty(Mono.defer(() -> Mono.just(ipKey(exchange))));
    }

    private String ipKey(org.springframework.web.server.ServerWebExchange exchange) {
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return "ip:" + ip;
    }
}
