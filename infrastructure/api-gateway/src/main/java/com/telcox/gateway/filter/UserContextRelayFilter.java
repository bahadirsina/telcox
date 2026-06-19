package com.telcox.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * SEC-04 (komşu task — bu modüle yakın olduğu için iskeleti burada).
 *
 * Doğrulanmış JWT'den kullanıcı kimliğini ve rollerini çıkarıp downstream servislere
 * X-User-Id ve X-User-Roles header'larıyla relay eder. Böylece backend servisler JWT'yi
 * yeniden parse etmeden kullanıcı bağlamına erişir.
 *
 * NOT: Bu task SEC-04 sahibine ait olabilir. Sahibi netleşince taşınır/silinir;
 * INF-04 + SEC-03 ile aynı filtre zincirinde durduğu için iskelet olarak eklendi.
 */
@Component
public class UserContextRelayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(auth -> {
                    Jwt jwt = auth.getToken();
                    String userId = jwt.getSubject();
                    String roles = auth.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .filter(a -> a.startsWith("ROLE_"))
                            .map(a -> a.substring("ROLE_".length()))
                            .collect(Collectors.joining(","));

                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r
                                    .header("X-User-Id", userId)
                                    .header("X-User-Roles", roles))
                            .build();
                    return mutated;
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // Security'den sonra, route'lama sırasında çalışmalı
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
