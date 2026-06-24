package com.telcox.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Comparator;

/**
 * Relays a trusted identity context from the gateway to downstream services.
 * Client-supplied identity headers are always removed first, so downstream
 * services only receive values derived from a validated JWT.
 */
@Component
public class UserContextRelayFilter implements GlobalFilter, Ordered {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String ROLES_HEADER = "X-Roles";
    // Remove the former header too, preventing mixed clients from spoofing it.
    public static final String LEGACY_ROLES_HEADER = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(this::trustedContext)
                .defaultIfEmpty(TrustedContext.empty())
                .map(context -> removeUntrustedHeaders(exchange, context))
                .flatMap(chain::filter);
    }

    private TrustedContext trustedContext(JwtAuthenticationToken authentication) {
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .distinct()
                .sorted(Comparator.naturalOrder())
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        return new TrustedContext(authentication.getToken().getSubject(), roles);
    }

    private ServerWebExchange removeUntrustedHeaders(ServerWebExchange exchange, TrustedContext context) {
        return exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(ROLES_HEADER);
                    headers.remove(LEGACY_ROLES_HEADER);
                    if (context.userId() != null && !context.userId().isBlank()) {
                        headers.set(USER_ID_HEADER, context.userId());
                        headers.set(ROLES_HEADER, context.roles());
                    }
                }))
                .build();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

    private record TrustedContext(String userId, String roles) {
        static TrustedContext empty() {
            return new TrustedContext(null, "");
        }
    }
}
