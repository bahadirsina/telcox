package com.telcox.bff.service;

import com.sun.net.httpserver.HttpServer;
import com.telcox.bff.config.BffProperties;
import com.telcox.bff.model.PlatformOpsResponse;
import com.telcox.bff.model.UpstreamResult;
import com.telcox.bff.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlatformOpsServiceTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void aggregatesServiceConnectAndPrometheusState() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/connectors", exchange -> {
            String response = exchange.getRequestURI().getPath().endsWith("/status")
                    ? "{\"connector\":{\"state\":\"RUNNING\"},\"tasks\":[{\"state\":\"RUNNING\"}]}"
                    : "[\"dbz-customer-pg\"]";
            writeJson(exchange, response);
        });
        server.createContext("/api/v1/query", exchange -> writeJson(exchange,
                "{\"status\":\"success\",\"data\":{\"result\":[{\"value\":[0,\"1\"]},{\"value\":[0,\"0\"]}]}}"));
        server.start();

        UpstreamClient upstreamClient = mock(UpstreamClient.class);
        UserContext user = new UserContext("u1", "Ada", "", Set.of("ADMIN", "OPS"));
        for (String service : List.of(
                "api-gateway", "bff-service", "identity-service", "customer-service",
                "product-catalog-service", "order-service", "subscription-service", "usage-service",
                "billing-service", "payment-service", "notification-service", "ticket-service"
        )) {
            when(upstreamClient.getMap(service, "/actuator/health", user, "corr"))
                    .thenReturn(UpstreamResult.ok(service, Map.of("status", "UP")));
        }
        when(upstreamClient.getList("customer-service", "/api/v1/customers", user, "corr"))
                .thenReturn(UpstreamResult.ok("customer-service", List.of(Map.of("id", "c1"), Map.of("id", "c2"))));
        when(upstreamClient.getList("product-catalog-service", "/api/v1/products", user, "corr"))
                .thenReturn(UpstreamResult.ok("product-catalog-service", List.of(Map.of("id", "p1"))));
        when(upstreamClient.getList("order-service", "/api/v1/orders", user, "corr"))
                .thenReturn(UpstreamResult.ok("order-service", List.of(Map.of("id", "o1"))));
        when(upstreamClient.getList("subscription-service", "/api/v1/subscriptions", user, "corr"))
                .thenReturn(UpstreamResult.ok("subscription-service", List.of(Map.of("id", "s1"))));
        when(upstreamClient.getList("usage-service", "/api/v1/usage/subscriptions/22222222-2222-4222-8222-000000010482/quotas", user, "corr"))
                .thenReturn(UpstreamResult.ok("usage-service", List.of(Map.of("type", "DATA_MB"))));
        when(upstreamClient.getList("billing-service", "/api/v1/invoices", user, "corr"))
                .thenReturn(UpstreamResult.ok("billing-service", List.of(Map.of("id", "i1"))));
        when(upstreamClient.getList("ticket-service", "/api/v1/tickets", user, "corr"))
                .thenReturn(UpstreamResult.ok("ticket-service", List.of(Map.of("id", "t1"))));

        BffProperties properties = new BffProperties();
        properties.setPlatformCacheTtl(Duration.ofSeconds(1));
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        properties.getPlatform().setKafkaConnectUrl(baseUrl);
        properties.getPlatform().setPrometheusUrl(baseUrl);
        Environment environment = mock(Environment.class);
        when(environment.getProperty("telcox.profile", "prod")).thenReturn("test");

        PlatformOpsService service = new PlatformOpsService(
                upstreamClient,
                RestClient.create(),
                new NoopBffCacheService(),
                properties,
                environment
        );

        PlatformOpsResponse response = service.snapshot(user, "corr");

        assertThat(response.cached()).isFalse();
        assertThat(response.environment()).isEqualTo("TEST");
        assertThat(response.overallStatus()).isEqualTo("UP");
        assertThat(response.pulse().healthyServices()).isEqualTo(12);
        assertThat(response.pulse().totalRecords()).isEqualTo(8);
        assertThat(response.connectors()).singleElement()
                .satisfies(connector -> assertThat(connector.available()).isTrue());
        assertThat(response.prometheus().targetsUp()).isEqualTo(1);
        assertThat(response.prometheus().targetsTotal()).isEqualTo(2);
    }

    private void writeJson(com.sun.net.httpserver.HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    static class NoopBffCacheService extends BffCacheService {
        NoopBffCacheService() {
            super(null, null);
        }

        @Override
        public <T> Optional<T> get(String key, Class<T> type) {
            return Optional.empty();
        }

        @Override
        public void put(String key, Object value, Duration ttl) {
        }
    }
}
