package com.telcox.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * INF-04 — Gateway CORS altyapısı.
 *
 * İzin verilen origin'ler env/config'den gelir (varsayılan: lokal Vite ve compose frontend).
 * Frontend (Signal Atlas) buradan geçtiği için origin listesi onun adresleriyle uyumlu.
 */
@Configuration
public class CorsConfig {

    @Value("${telcox.gateway.cors.allowed-origins:http://localhost:5173,http://localhost:15173}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        // Downstream'e relay edilen / correlation header'larını client görebilsin
        cfg.setExposedHeaders(List.of("X-Correlation-Id"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
