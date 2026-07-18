package com.telcox.bff.api;

import com.telcox.bff.model.Customer360Response;
import com.telcox.bff.model.DashboardSummaryResponse;
import com.telcox.bff.model.OnboardingOrderRequest;
import com.telcox.bff.model.OperationAcceptedResponse;
import com.telcox.bff.model.OperationStatusResponse;
import com.telcox.bff.model.PlatformOpsResponse;
import com.telcox.bff.model.UserContext;
import com.telcox.bff.model.UserContextResponse;
import com.telcox.bff.service.Customer360Service;
import com.telcox.bff.service.DashboardService;
import com.telcox.bff.service.OrderOperationService;
import com.telcox.bff.service.PlatformOpsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bff")
public class BffController {

    private final DashboardService dashboardService;
    private final Customer360Service customer360Service;
    private final OrderOperationService orderOperationService;
    private final PlatformOpsService platformOpsService;

    public BffController(DashboardService dashboardService,
                         Customer360Service customer360Service,
                         OrderOperationService orderOperationService,
                         PlatformOpsService platformOpsService) {
        this.dashboardService = dashboardService;
        this.customer360Service = customer360Service;
        this.orderOperationService = orderOperationService;
        this.platformOpsService = platformOpsService;
    }

    @GetMapping("/me")
    public UserContextResponse me(HttpServletRequest request) {
        return UserContext.from(request).toResponse();
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummaryResponse dashboardSummary(
            HttpServletRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false, defaultValue = "bff-dashboard") String correlationId
    ) {
        return dashboardService.summary(UserContext.from(request), correlationId);
    }

    @GetMapping("/platform/ops")
    public PlatformOpsResponse platformOps(
            HttpServletRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false, defaultValue = "bff-platform-ops") String correlationId
    ) {
        return platformOpsService.snapshot(UserContext.from(request), correlationId);
    }

    @GetMapping("/customers/{customerId}/360")
    public Customer360Response customer360(
            @PathVariable UUID customerId,
            HttpServletRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false, defaultValue = "bff-customer360") String correlationId
    ) {
        return customer360Service.getCustomer360(customerId, UserContext.from(request), correlationId);
    }

    @PostMapping("/orders/onboarding")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OperationAcceptedResponse submitOnboardingOrder(
            @Valid @RequestBody OnboardingOrderRequest requestBody,
            HttpServletRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false, defaultValue = "bff-order-onboarding") String correlationId
    ) {
        return orderOperationService.submit(requestBody, UserContext.from(request), correlationId);
    }

    @GetMapping("/operations/{operationId}")
    public OperationStatusResponse operationStatus(
            @PathVariable UUID operationId,
            HttpServletRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false, defaultValue = "bff-operation-status") String correlationId
    ) {
        return orderOperationService.getStatus(operationId, UserContext.from(request), correlationId);
    }

    @GetMapping("/orders/{orderId}/status")
    public OperationStatusResponse orderStatus(
            @PathVariable UUID orderId,
            HttpServletRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false, defaultValue = "bff-order-status") String correlationId
    ) {
        return orderOperationService.getOrderStatus(orderId, UserContext.from(request), correlationId);
    }

    @GetMapping("/operations/{operationId}/events")
    public SseEmitter operationEvents(
            @PathVariable UUID operationId,
            HttpServletRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false, defaultValue = "bff-operation-events") String correlationId
    ) {
        SseEmitter emitter = new SseEmitter(30_000L);
        UserContext user = UserContext.from(request);
        Thread.startVirtualThread(() -> streamStatus(operationId, user, correlationId, emitter));
        return emitter;
    }

    private void streamStatus(UUID operationId, UserContext user, String correlationId, SseEmitter emitter) {
        try {
            for (int i = 0; i < 15; i++) {
                OperationStatusResponse status = orderOperationService.getStatus(operationId, user, correlationId);
                emitter.send(SseEmitter.event().name("status").data(status));
                if ("COMPLETED".equals(status.state()) || "FAILED".equals(status.state())) {
                    break;
                }
                Thread.sleep(2_000L);
            }
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            emitter.completeWithError(e);
        } catch (RuntimeException e) {
            emitter.completeWithError(e);
        }
    }
}
