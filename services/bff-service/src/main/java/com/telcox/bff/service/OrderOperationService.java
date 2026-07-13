package com.telcox.bff.service;

import com.telcox.bff.model.OnboardingOrderRequest;
import com.telcox.bff.model.OperationAcceptedResponse;
import com.telcox.bff.model.OperationRecord;
import com.telcox.bff.model.OperationStatusResponse;
import com.telcox.bff.model.UpstreamResult;
import com.telcox.bff.model.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderOperationService {

    private final UpstreamClient upstreamClient;
    private final OperationStore operationStore;

    public OrderOperationService(UpstreamClient upstreamClient, OperationStore operationStore) {
        this.upstreamClient = upstreamClient;
        this.operationStore = operationStore;
    }

    public OperationAcceptedResponse submit(OnboardingOrderRequest request, UserContext user, String correlationId) {
        Map<String, Object> payload = Map.of(
                "customerId", request.customerId(),
                "orderType", request.orderType(),
                "planCode", nullToEmpty(request.planCode()),
                "addonCodes", request.addonCodes() == null ? List.of() : request.addonCodes(),
                "msisdn", nullToEmpty(request.msisdn()),
                "simIccid", nullToEmpty(request.simIccid())
        );
        UpstreamResult<Map<String, Object>> result =
                upstreamClient.postMap("order-service", "/api/v1/orders", payload, user, correlationId);
        if (!result.available()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "order-service unavailable: " + result.detail());
        }

        UUID operationId = UUID.randomUUID();
        UUID orderId = uuidFrom(result.body().get("id"));
        OffsetDateTime now = OffsetDateTime.now();
        OperationRecord record = new OperationRecord(operationId, orderId, "SUBMITTED", now, now, result.body());
        operationStore.save(record);

        return new OperationAcceptedResponse(
                operationId,
                orderId,
                "SUBMITTED",
                "/api/v1/bff/operations/" + operationId,
                "/api/v1/bff/operations/" + operationId + "/events",
                now
        );
    }

    public OperationStatusResponse getStatus(UUID operationId, UserContext user, String correlationId) {
        OperationRecord record = operationStore.find(operationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "operation not found"));
        return loadStatus(record, user, correlationId);
    }

    public OperationStatusResponse getOrderStatus(UUID orderId, UserContext user, String correlationId) {
        OperationRecord record = new OperationRecord(
                null,
                orderId,
                "TRACKING",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                Map.of()
        );
        return loadStatus(record, user, correlationId);
    }

    private OperationStatusResponse loadStatus(OperationRecord record, UserContext user, String correlationId) {
        UpstreamResult<Map<String, Object>> order =
                upstreamClient.getMap("order-service", "/api/v1/orders/" + record.orderId(), user, correlationId);
        Map<String, Object> snapshot = order.available() ? order.body() : record.snapshot();
        String orderStatus = asString(snapshot.get("status"));
        String state = stateFrom(orderStatus, record.state());
        List<Map<String, Object>> saga = listOfMaps(snapshot.get("saga"));

        OperationRecord updated = new OperationRecord(
                record.operationId(),
                record.orderId(),
                state,
                record.submittedAt(),
                OffsetDateTime.now(),
                snapshot
        );
        if (updated.operationId() != null) {
            operationStore.save(updated);
        }

        return new OperationStatusResponse(
                updated.operationId(),
                updated.orderId(),
                state,
                orderStatus,
                asString(snapshot.get("correlationId")),
                updated.updatedAt(),
                saga,
                snapshot,
                List.of(order.status())
        );
    }

    private String stateFrom(String orderStatus, String fallback) {
        if (orderStatus == null || orderStatus.isBlank()) {
            return fallback;
        }
        return switch (orderStatus) {
            case "COMPLETED" -> "COMPLETED";
            case "FAILED", "CANCELLED" -> "FAILED";
            default -> "RUNNING";
        };
    }

    private UUID uuidFrom(Object value) {
        if (value == null) {
            return null;
        }
        return UUID.fromString(value.toString());
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private Object nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }
}
