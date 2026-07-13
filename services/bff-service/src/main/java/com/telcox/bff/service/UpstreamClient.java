package com.telcox.bff.service;

import com.telcox.bff.config.BffProperties;
import com.telcox.bff.model.UpstreamResult;
import com.telcox.bff.model.UserContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class UpstreamClient {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_OF_MAP =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> MAP =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final BffProperties properties;

    public UpstreamClient(RestClient restClient, BffProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public UpstreamResult<List<Map<String, Object>>> getList(
            String service,
            String path,
            UserContext user,
            String correlationId
    ) {
        try {
            List<Map<String, Object>> body = restClient.get()
                    .uri(properties.upstreamUrl(service) + path)
                    .headers(headers -> applyHeaders(headers, user, correlationId))
                    .retrieve()
                    .body(LIST_OF_MAP);
            return UpstreamResult.ok(service, body == null ? List.of() : body);
        } catch (RestClientResponseException e) {
            return UpstreamResult.unavailable(service, "HTTP " + e.getStatusCode().value(), List.of());
        } catch (RestClientException | IllegalStateException e) {
            return UpstreamResult.unavailable(service, compact(e), List.of());
        }
    }

    public UpstreamResult<Map<String, Object>> getMap(
            String service,
            String path,
            UserContext user,
            String correlationId
    ) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(properties.upstreamUrl(service) + path)
                    .headers(headers -> applyHeaders(headers, user, correlationId))
                    .retrieve()
                    .body(MAP);
            return UpstreamResult.ok(service, body == null ? Map.of() : body);
        } catch (RestClientResponseException e) {
            return UpstreamResult.unavailable(service, "HTTP " + e.getStatusCode().value(), Map.of());
        } catch (RestClientException | IllegalStateException e) {
            return UpstreamResult.unavailable(service, compact(e), Map.of());
        }
    }

    public UpstreamResult<Map<String, Object>> postMap(
            String service,
            String path,
            Map<String, Object> request,
            UserContext user,
            String correlationId
    ) {
        try {
            Map<String, Object> body = restClient.post()
                    .uri(properties.upstreamUrl(service) + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> applyHeaders(headers, user, correlationId))
                    .body(request)
                    .retrieve()
                    .body(MAP);
            return UpstreamResult.ok(service, body == null ? Map.of() : body);
        } catch (RestClientResponseException e) {
            return UpstreamResult.unavailable(service, "HTTP " + e.getStatusCode().value(), Map.of());
        } catch (RestClientException | IllegalStateException e) {
            return UpstreamResult.unavailable(service, compact(e), Map.of());
        }
    }

    private void applyHeaders(org.springframework.http.HttpHeaders headers, UserContext user, String correlationId) {
        headers.set("X-Correlation-Id", correlationId);
        headers.set("X-Telcox-User-Id", user.userId());
        headers.set("X-Telcox-User-Name", user.username());
        headers.set("X-Telcox-User-Roles", String.join(",", user.roles()));
        if (!user.email().isBlank()) {
            headers.set("X-Telcox-User-Email", user.email());
        }
    }

    private String compact(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
