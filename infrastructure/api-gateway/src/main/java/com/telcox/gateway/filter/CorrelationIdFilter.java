package com.telcox.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * INF-04 — Correlation-Id propagasyonu.
 *
 * Gelen istekte X-Correlation-Id yoksa üretir; downstream servislere header olarak iletir
 * ve response'a geri yazar. Bu id event envelope'larına (ADR-0003 correlationId) ve
 * distributed tracing'e taşınır.
 *
 * En erken sırada çalışır ki tüm filtreler/loglar aynı id'yi görsün.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        final String cid = correlationId;

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(HEADER, cid)
                .build();

        exchange.getResponse().getHeaders().set(HEADER, cid);

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
