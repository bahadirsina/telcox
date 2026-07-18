package com.telcox.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextRelayFilterTest {

    private final UserContextRelayFilter filter = new UserContextRelayFilter(
            false,
            "demo-operator",
            "Demo Operator",
            "demo.operator@telcox.local",
            "ADMIN,SUPPORT,BILLING,OPS"
    );

    @Test
    void replacesClientSuppliedHeadersWithTrustedJwtContext() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/customers")
                .header(UserContextRelayFilter.USER_ID_HEADER, "spoofed-user")
                .header(UserContextRelayFilter.ROLES_HEADER, "ADMIN")
                .header(UserContextRelayFilter.LEGACY_ROLES_HEADER, "ADMIN")
                .build());
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        GatewayFilterChain chain = forwardedExchange -> {
            forwarded.set(forwardedExchange);
            return Mono.empty();
        };
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                java.util.Map.of("alg", "none"), java.util.Map.of("sub", "user-42"));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(
                new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                new SimpleGrantedAuthority("ROLE_AGENT"),
                new SimpleGrantedAuthority("SCOPE_openid")
        ));

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                .block();

        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContextRelayFilter.USER_ID_HEADER))
                .isEqualTo("user-42");
        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContextRelayFilter.ROLES_HEADER))
                .isEqualTo("AGENT,CUSTOMER");
        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContextRelayFilter.LEGACY_ROLES_HEADER))
                .isNull();
    }

    @Test
    void addsDemoUserHeadersWhenDemoContextIsEnabledWithoutAuthentication() {
        UserContextRelayFilter demoFilter = new UserContextRelayFilter(
                true,
                "demo-operator",
                "Demo Operator",
                "demo.operator@telcox.local",
                "ADMIN,SUPPORT,BILLING,OPS"
        );
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/bff/dashboard/summary")
                .header(UserContextRelayFilter.USER_ID_HEADER, "spoofed-user")
                .build());
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        GatewayFilterChain chain = forwardedExchange -> {
            forwarded.set(forwardedExchange);
            return Mono.empty();
        };

        demoFilter.filter(exchange, chain).block();

        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContextRelayFilter.USER_ID_HEADER))
                .isEqualTo("demo-operator");
        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContextRelayFilter.BFF_USER_NAME_HEADER))
                .isEqualTo("Demo Operator");
        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContextRelayFilter.BFF_ROLES_HEADER))
                .isEqualTo("ADMIN,SUPPORT,BILLING,OPS");
    }
}
