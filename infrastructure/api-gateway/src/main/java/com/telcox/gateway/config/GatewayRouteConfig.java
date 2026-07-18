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
                        .uri("http://bff-service:9011"))
                .route("identity-service", route -> route
                        .path("/api/v1/auth/**", "/api/v1/users/**", "/api/v1/identity/**")
                        .uri("http://identity-service:9001"))
                .route("customer-service", route -> route
                        .path("/api/v1/customers", "/api/v1/customers/**")
                        .uri("http://customer-service:9002"))
                .route("product-catalog-service", route -> route
                        .path("/api/v1/products", "/api/v1/products/**", "/api/v1/categories", "/api/v1/categories/**")
                        .uri("http://product-catalog-service:9003"))
                .route("order-service", route -> route
                        .path("/api/v1/orders", "/api/v1/orders/**")
                        .uri("http://order-service:9004"))
                .route("subscription-service", route -> route
                        .path("/api/v1/subscriptions", "/api/v1/subscriptions/**")
                        .uri("http://subscription-service:9005"))
                .route("usage-service", route -> route
                        .path("/api/v1/usage/**")
                        .uri("http://usage-service:9006"))
                .route("billing-service", route -> route
                        .path("/api/v1/invoices", "/api/v1/invoices/**", "/api/v1/billing/**")
                        .uri("http://billing-service:9007"))
                .route("payment-service", route -> route
                        .path("/api/v1/payments", "/api/v1/payments/**")
                        .uri("http://payment-service:9008"))
                .route("notification-service", route -> route
                        .path("/api/v1/notifications", "/api/v1/notifications/**")
                        .uri("http://notification-service:9009"))
                .route("ticket-service", route -> route
                        .path("/api/v1/tickets", "/api/v1/tickets/**")
                        .uri("http://ticket-service:9010"))
                .build();
    }
}
