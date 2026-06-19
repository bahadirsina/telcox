package com.telcox.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * SEC-03 — API Gateway'i OAuth2 Resource Server yapar; gelen JWT'yi Keycloak realm'ine
 * karşı doğrular (issuer-uri application.yml'de).
 *
 * Reaktif gateway (gateway-server-webflux) olduğu için SecurityWebFilterChain kullanılır.
 * Keycloak rollerini (realm_access.roles) ROLE_* authority'lerine çevirir.
 *
 * Açık (public) path'ler: login/auth, actuator health/info, swagger.
 * Geri kalan tüm route'lar authenticated.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /** Kimlik doğrulaması gerektirmeyen path'ler. */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",          // login / token / refresh (identity-service)
            "/actuator/health/**",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // CORS Java tarafında CorsConfig ile yönetiliyor (INF-04)
                .cors(cors -> {})
                .authorizeExchange(ex -> ex
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakAuthConverter()))
                );
        return http.build();
    }

    /**
     * Keycloak JWT'sindeki realm_access.roles claim'ini Spring Security authority'lerine
     * (ROLE_*) çevirir. Reaktif zincir için adapter ile sarılır.
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> keycloakAuthConverter() {
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();

        delegate.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map<?, ?> map && map.get("roles") instanceof List<?> roles) {
                for (Object role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
            return authorities;
        });

        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }
}
