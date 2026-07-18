package com.telcox.bff.service;

import com.telcox.bff.config.BffProperties;
import com.telcox.bff.model.PlatformOpsResponse;
import com.telcox.bff.model.UpstreamResult;
import com.telcox.bff.model.UserContext;
import org.springframework.core.env.Environment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class PlatformOpsService {

    private static final String CACHE_PREFIX = "bff:platform-ops:";
    private static final ParameterizedTypeReference<List<String>> LIST_OF_STRING =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> MAP =
            new ParameterizedTypeReference<>() {};
    private static final List<ServiceProbe> SERVICE_PROBES = List.of(
            new ServiceProbe("api-gateway", "API Gateway", "edge", null),
            new ServiceProbe("bff-service", "BFF Service", "experience", null),
            new ServiceProbe("identity-service", "Identity Service", "core", null),
            new ServiceProbe("customer-service", "Customer Service", "business", "/api/v1/customers"),
            new ServiceProbe("product-catalog-service", "Product Catalog", "business", "/api/v1/products"),
            new ServiceProbe("order-service", "Order Service", "business", "/api/v1/orders"),
            new ServiceProbe("subscription-service", "Subscription Service", "business", "/api/v1/subscriptions"),
            new ServiceProbe("usage-service", "Usage Service", "business", "/api/v1/usage/subscriptions/22222222-2222-4222-8222-000000010482/quotas"),
            new ServiceProbe("billing-service", "Billing Service", "business", "/api/v1/invoices"),
            new ServiceProbe("payment-service", "Payment Service", "business", null),
            new ServiceProbe("notification-service", "Notification Service", "business", null),
            new ServiceProbe("ticket-service", "Ticket Service", "business", "/api/v1/tickets")
    );

    private final UpstreamClient upstreamClient;
    private final RestClient restClient;
    private final BffCacheService cacheService;
    private final BffProperties properties;
    private final Environment environment;

    public PlatformOpsService(
            UpstreamClient upstreamClient,
            RestClient restClient,
            BffCacheService cacheService,
            BffProperties properties,
            Environment environment
    ) {
        this.upstreamClient = upstreamClient;
        this.restClient = restClient;
        this.cacheService = cacheService;
        this.properties = properties;
        this.environment = environment;
    }

    public PlatformOpsResponse snapshot(UserContext user, String correlationId) {
        String key = CACHE_PREFIX + user.cacheScope();
        return cacheService.get(key, PlatformOpsPayload.class)
                .map(payload -> payload.toResponse(true))
                .orElseGet(() -> loadAndCache(key, user, correlationId));
    }

    private PlatformOpsResponse loadAndCache(String key, UserContext user, String correlationId) {
        OffsetDateTime now = OffsetDateTime.now();
        List<PlatformOpsResponse.ServiceStatus> services = SERVICE_PROBES.stream()
                .map(probe -> probeService(probe, user, correlationId))
                .sorted(Comparator.comparing(PlatformOpsResponse.ServiceStatus::service))
                .toList();
        List<PlatformOpsResponse.ConnectorStatus> connectors = loadConnectors();
        PlatformOpsResponse.PrometheusStatus prometheus = loadPrometheus();

        int healthyServices = (int) services.stream().filter(service -> "UP".equals(service.status())).count();
        int degradedServices = (int) services.stream().filter(service -> "DEGRADED".equals(service.status())).count();
        int downServices = (int) services.stream().filter(service -> "DOWN".equals(service.status())).count();
        int totalRecords = services.stream().mapToInt(PlatformOpsResponse.ServiceStatus::records).sum();
        int connectorsRunning = (int) connectors.stream().filter(PlatformOpsResponse.ConnectorStatus::available).count();
        PlatformOpsResponse.PlatformPulse pulse = new PlatformOpsResponse.PlatformPulse(
                services.size(),
                healthyServices,
                degradedServices,
                downServices,
                totalRecords,
                prometheus.targetsUp(),
                prometheus.targetsTotal(),
                connectorsRunning,
                connectors.size()
        );

        PlatformOpsPayload payload = new PlatformOpsPayload(
                now,
                profile(),
                overallStatus(downServices, degradedServices, prometheus, connectors),
                pulse,
                services,
                connectors,
                prometheus,
                events(now, services, connectors, prometheus)
        );
        cacheService.put(key, payload, properties.getPlatformCacheTtl());
        return payload.toResponse(false);
    }

    private PlatformOpsResponse.ServiceStatus probeService(ServiceProbe probe, UserContext user, String correlationId) {
        long started = System.nanoTime();
        UpstreamResult<Map<String, Object>> health =
                upstreamClient.getMap(probe.service(), "/actuator/health", user, correlationId);
        long latencyMs = Math.max(1L, (System.nanoTime() - started) / 1_000_000L);
        boolean healthUp = health.available() && "UP".equalsIgnoreCase(String.valueOf(health.body().get("status")));
        int records = 0;
        boolean dataAvailable = probe.dataPath() == null;
        String detail = health.available() ? String.valueOf(health.body().getOrDefault("status", "OK")) : health.detail();

        if (probe.dataPath() != null && healthUp) {
            UpstreamResult<List<Map<String, Object>>> data =
                    upstreamClient.getList(probe.service(), probe.dataPath(), user, correlationId);
            records = data.body().size();
            dataAvailable = data.available();
            if (!data.available()) {
                detail = data.detail();
            }
        }

        String status = healthUp && dataAvailable ? "UP" : healthUp ? "DEGRADED" : "DOWN";
        return new PlatformOpsResponse.ServiceStatus(
                probe.service(),
                probe.label(),
                probe.group(),
                status,
                healthUp,
                dataAvailable,
                latencyMs,
                records,
                detail
        );
    }

    private List<PlatformOpsResponse.ConnectorStatus> loadConnectors() {
        String baseUrl = trimTrailingSlash(properties.getPlatform().getKafkaConnectUrl());
        try {
            List<String> connectorNames = restClient.get()
                    .uri(baseUrl + "/connectors")
                    .retrieve()
                    .body(LIST_OF_STRING);
            if (connectorNames == null || connectorNames.isEmpty()) {
                return List.of(new PlatformOpsResponse.ConnectorStatus(
                        "kafka-connect",
                        "UP",
                        true,
                        0,
                        0,
                        "Worker reachable, no connectors registered"
                ));
            }
            return connectorNames.stream()
                    .filter(Objects::nonNull)
                    .sorted()
                    .map(name -> connectorStatus(baseUrl, name))
                    .toList();
        } catch (RestClientResponseException e) {
            return List.of(new PlatformOpsResponse.ConnectorStatus(
                    "kafka-connect",
                    "DOWN",
                    false,
                    0,
                    0,
                    "HTTP " + e.getStatusCode().value()
            ));
        } catch (RestClientException | IllegalStateException e) {
            return List.of(new PlatformOpsResponse.ConnectorStatus(
                    "kafka-connect",
                    "DOWN",
                    false,
                    0,
                    0,
                    compact(e)
            ));
        }
    }

    @SuppressWarnings("unchecked")
    private PlatformOpsResponse.ConnectorStatus connectorStatus(String baseUrl, String name) {
        try {
            Map<String, Object> status = restClient.get()
                    .uri(baseUrl + "/connectors/" + name + "/status")
                    .retrieve()
                    .body(MAP);
            String connectorState = String.valueOf(status == null ? "UNKNOWN" : status.getOrDefault("connector", Map.of()));
            Object connector = status == null ? null : status.get("connector");
            if (connector instanceof Map<?, ?> connectorMap) {
                Object state = connectorMap.get("state");
                connectorState = state == null ? "UNKNOWN" : String.valueOf(state);
            }
            List<Map<String, Object>> tasks = status != null && status.get("tasks") instanceof List<?> rawTasks
                    ? rawTasks.stream()
                            .filter(Map.class::isInstance)
                            .map(task -> (Map<String, Object>) task)
                            .toList()
                    : List.of();
            int runningTasks = (int) tasks.stream()
                    .filter(task -> "RUNNING".equalsIgnoreCase(String.valueOf(task.get("state"))))
                    .count();
            boolean running = "RUNNING".equalsIgnoreCase(connectorState) && runningTasks == tasks.size();
            return new PlatformOpsResponse.ConnectorStatus(
                    name,
                    running ? "UP" : "DEGRADED",
                    running,
                    runningTasks,
                    tasks.size(),
                    "Connector " + connectorState
            );
        } catch (RestClientException e) {
            return new PlatformOpsResponse.ConnectorStatus(name, "DOWN", false, 0, 0, compact(e));
        }
    }

    @SuppressWarnings("unchecked")
    private PlatformOpsResponse.PrometheusStatus loadPrometheus() {
        String baseUrl = trimTrailingSlash(properties.getPlatform().getPrometheusUrl());
        try {
            Map<String, Object> response = restClient.get()
                    .uri(baseUrl + "/api/v1/query?query=up")
                    .retrieve()
                    .body(MAP);
            Map<String, Object> data = response != null && response.get("data") instanceof Map<?, ?> rawData
                    ? (Map<String, Object>) rawData
                    : Map.of();
            List<Map<String, Object>> result = data.get("result") instanceof List<?> rawResult
                    ? rawResult.stream()
                            .filter(Map.class::isInstance)
                            .map(item -> (Map<String, Object>) item)
                            .toList()
                    : List.of();
            int targetsTotal = result.size();
            int targetsUp = (int) result.stream().filter(this::prometheusTargetUp).count();
            return new PlatformOpsResponse.PrometheusStatus(
                    true,
                    targetsUp,
                    targetsTotal,
                    targetsTotal == 0 ? "Prometheus reachable, no up targets returned" : "OK"
            );
        } catch (RestClientResponseException e) {
            return new PlatformOpsResponse.PrometheusStatus(false, 0, 0, "HTTP " + e.getStatusCode().value());
        } catch (RestClientException | IllegalStateException e) {
            return new PlatformOpsResponse.PrometheusStatus(false, 0, 0, compact(e));
        }
    }

    private boolean prometheusTargetUp(Map<String, Object> result) {
        if (!(result.get("value") instanceof List<?> value) || value.size() < 2) {
            return false;
        }
        return "1".equals(String.valueOf(value.get(1)));
    }

    private String overallStatus(
            int downServices,
            int degradedServices,
            PlatformOpsResponse.PrometheusStatus prometheus,
            List<PlatformOpsResponse.ConnectorStatus> connectors
    ) {
        if (downServices > 0) {
            return "DOWN";
        }
        boolean connectorIssue = connectors.stream().anyMatch(connector -> !connector.available());
        if (degradedServices > 0 || !prometheus.available() || connectorIssue) {
            return "DEGRADED";
        }
        return "UP";
    }

    private List<PlatformOpsResponse.PlatformEvent> events(
            OffsetDateTime now,
            List<PlatformOpsResponse.ServiceStatus> services,
            List<PlatformOpsResponse.ConnectorStatus> connectors,
            PlatformOpsResponse.PrometheusStatus prometheus
    ) {
        List<PlatformOpsResponse.PlatformEvent> events = new ArrayList<>();
        services.stream()
                .filter(service -> !"UP".equals(service.status()))
                .limit(4)
                .forEach(service -> events.add(new PlatformOpsResponse.PlatformEvent(
                        "ServiceProbe",
                        service.service(),
                        service.status(),
                        service.detail(),
                        now
                )));
        connectors.stream()
                .filter(connector -> !connector.available())
                .limit(2)
                .forEach(connector -> events.add(new PlatformOpsResponse.PlatformEvent(
                        "ConnectorProbe",
                        connector.name(),
                        connector.status(),
                        connector.detail(),
                        now
                )));
        events.add(new PlatformOpsResponse.PlatformEvent(
                "PrometheusScrape",
                "prometheus",
                prometheus.available() ? "UP" : "DOWN",
                prometheus.targetsUp() + "/" + prometheus.targetsTotal() + " targets up",
                now
        ));
        return events;
    }

    private String profile() {
        String profile = environment.getProperty("telcox.profile", "prod");
        return profile.toUpperCase(Locale.ROOT);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String compact(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }

    private record ServiceProbe(String service, String label, String group, String dataPath) {
    }

    public record PlatformOpsPayload(
            OffsetDateTime generatedAt,
            String environment,
            String overallStatus,
            PlatformOpsResponse.PlatformPulse pulse,
            List<PlatformOpsResponse.ServiceStatus> services,
            List<PlatformOpsResponse.ConnectorStatus> connectors,
            PlatformOpsResponse.PrometheusStatus prometheus,
            List<PlatformOpsResponse.PlatformEvent> events
    ) {
        PlatformOpsResponse toResponse(boolean cached) {
            return new PlatformOpsResponse(
                    generatedAt,
                    cached,
                    environment,
                    overallStatus,
                    pulse,
                    services,
                    connectors,
                    prometheus,
                    events
            );
        }
    }
}
