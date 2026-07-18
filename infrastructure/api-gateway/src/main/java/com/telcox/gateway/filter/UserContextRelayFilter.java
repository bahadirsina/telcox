package com.telcox.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Relays the gateway-validated user context to downstream services.
 *
 * Spoofable incoming user headers are always removed first; only the authenticated
 * principal observed by Spring Security is propagated.
 */
@Component
public class UserContextRelayFilter implements GlobalFilter, Ordered {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String ROLES_HEADER = "X-Roles";
    // Remove the former header too, preventing mixed clients from spoofing it.
    public static final String LEGACY_ROLES_HEADER = "X-User-Roles";

    public static final String BFF_USER_ID_HEADER = "X-Telcox-User-Id";
    public static final String BFF_USER_NAME_HEADER = "X-Telcox-User-Name";
    public static final String BFF_USER_EMAIL_HEADER = "X-Telcox-User-Email";
    public static final String BFF_ROLES_HEADER = "X-Telcox-User-Roles";

    private final boolean demoUserContextEnabled;
    private final String demoUserId;
    private final String demoUserName;
    private final String demoUserEmail;
    private final String demoUserRoles;

    public UserContextRelayFilter(
            @Value("${telcox.gateway.demo-user-context.enabled:false}") boolean demoUserContextEnabled,
            @Value("${telcox.gateway.demo-user-context.user-id:demo-operator}") String demoUserId,
            @Value("${telcox.gateway.demo-user-context.user-name:Demo Operator}") String demoUserName,
            @Value("${telcox.gateway.demo-user-context.email:demo.operator@telcox.local}") String demoUserEmail,
            @Value("${telcox.gateway.demo-user-context.roles:ADMIN,SUPPORT,BILLING,OPS}") String demoUserRoles) {
        this.demoUserContextEnabled = demoUserContextEnabled;
        this.demoUserId = demoUserId;
        this.demoUserName = demoUserName;
        this.demoUserEmail = demoUserEmail;
        this.demoUserRoles = demoUserRoles;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .flatMap(authentication -> chain.filter(withUserHeaders(exchange, authentication)))
                .switchIfEmpty(chain.filter(
                        demoUserContextEnabled ? withDemoUserHeaders(exchange) : stripUserHeaders(exchange)
                ));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 20;
    }

    private ServerWebExchange withUserHeaders(ServerWebExchange exchange, Authentication authentication) {
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
        builder.headers(headers -> {
            removeUserHeaders(headers);
            String userId = authentication.getName();
            String roles = trustedRoles(authentication);

            if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
                Jwt jwt = jwtAuthentication.getToken();
                userId = firstPresent(jwt.getSubject(),
                        firstPresent(jwt.getClaimAsString("preferred_username"),
                                firstPresent(jwt.getClaimAsString("email"), userId)));
                String username = firstPresent(jwt.getClaimAsString("preferred_username"), userId);
                headers.set(BFF_USER_NAME_HEADER, username);
                String email = jwt.getClaimAsString("email");
                if (email != null && !email.isBlank()) {
                    headers.set(BFF_USER_EMAIL_HEADER, email);
                }
            } else {
                headers.set(BFF_USER_NAME_HEADER, authentication.getName());
            }

            headers.set(USER_ID_HEADER, userId);
            headers.set(ROLES_HEADER, roles);
            headers.set(BFF_USER_ID_HEADER, userId);
            headers.set(BFF_ROLES_HEADER, roles);
        });
        return exchange.mutate().request(builder.build()).build();
    }

    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(this::removeUserHeaders)
                .build();
        return exchange.mutate().request(request).build();
    }

    private ServerWebExchange withDemoUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
        builder.headers(headers -> {
            removeUserHeaders(headers);
            headers.set(USER_ID_HEADER, demoUserId);
            headers.set(ROLES_HEADER, demoUserRoles);
            headers.set(BFF_USER_ID_HEADER, demoUserId);
            headers.set(BFF_USER_NAME_HEADER, demoUserName);
            headers.set(BFF_USER_EMAIL_HEADER, demoUserEmail);
            headers.set(BFF_ROLES_HEADER, demoUserRoles);
        });
        return exchange.mutate().request(builder.build()).build();
    }

    private void removeUserHeaders(org.springframework.http.HttpHeaders headers) {
        headers.remove(USER_ID_HEADER);
        headers.remove(ROLES_HEADER);
        headers.remove(LEGACY_ROLES_HEADER);
        headers.remove(BFF_USER_ID_HEADER);
        headers.remove(BFF_USER_NAME_HEADER);
        headers.remove(BFF_USER_EMAIL_HEADER);
        headers.remove(BFF_ROLES_HEADER);
    }

    private String trustedRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(","));
    }

    private String firstPresent(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }
}
