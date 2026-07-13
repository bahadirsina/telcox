package com.telcox.gateway.filter;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void propagatesExistingCorrelationIdToRequestResponseAndMdc() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/bff/dashboard")
                .header(CorrelationIdFilter.HEADER, "gateway-corr-1")
                .build());
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        GatewayFilterChain chain = forwardedExchange -> {
            forwarded.set(forwardedExchange);
            mdcValueInsideChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(forwarded.get().getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER))
                .isEqualTo("gateway-corr-1");
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER))
                .isEqualTo("gateway-corr-1");
        assertThat(mdcValueInsideChain).hasValue("gateway-corr-1");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void createsCorrelationIdWhenMissing() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/bff/me").build());
        AtomicReference<String> forwardedCorrelationId = new AtomicReference<>();
        GatewayFilterChain chain = forwardedExchange -> {
            forwardedCorrelationId.set(forwardedExchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(forwardedCorrelationId.get()).isNotBlank();
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER))
                .isEqualTo(forwardedCorrelationId.get());
    }
}
