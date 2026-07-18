package com.telcox.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator telcoxRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("bff-service", route -> route
                        .path("/api/v1/bff/**")
                        .uri("lb://bff-service"))
                .route("identity-service", route -> route
                        .path("/api/v1/auth/**", "/api/v1/users/**", "/api/v1/identity/**")
                        .uri("lb://identity-service"))
                .route("customer-service", route -> route
                        .path("/api/v1/customers", "/api/v1/customers/**")
                        .uri("lb://customer-service"))
                .route("product-catalog-service", route -> route
                        .path("/api/v1/products", "/api/v1/products/**", "/api/v1/categories", "/api/v1/categories/**")
                        .uri("lb://product-catalog-service"))
                .route("order-service", route -> route
                        .path("/api/v1/orders", "/api/v1/orders/**")
                        .uri("lb://order-service"))
                .route("subscription-service", route -> route
                        .path("/api/v1/subscriptions", "/api/v1/subscriptions/**")
                        .uri("lb://subscription-service"))
                .route("usage-service", route -> route
                        .path("/api/v1/usage/**")
                        .uri("lb://usage-service"))
                .route("billing-service", route -> route
                        .path("/api/v1/invoices", "/api/v1/invoices/**", "/api/v1/billing/**")
                        .uri("lb://billing-service"))
                .route("payment-service", route -> route
                        .path("/api/v1/payments", "/api/v1/payments/**")
                        .uri("lb://payment-service"))
                .route("notification-service", route -> route
                        .path("/api/v1/notifications", "/api/v1/notifications/**")
                        .uri("lb://notification-service"))
                .route("ticket-service", route -> route
                        .path("/api/v1/tickets", "/api/v1/tickets/**")
                        .uri("lb://ticket-service"))
                .build();
    }
}
