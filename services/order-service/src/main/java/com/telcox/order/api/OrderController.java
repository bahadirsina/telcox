package com.telcox.order.api;

import com.telcox.order.service.OrderOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderOrchestrationService orderOrchestrationService;

    public OrderController(OrderOrchestrationService orderOrchestrationService) {
        this.orderOrchestrationService = orderOrchestrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request,
                                     @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return orderOrchestrationService.createOrder(request, correlationId);
    }

    @GetMapping
    public List<OrderResponse> listOrders(@RequestParam(required = false) UUID customerId) {
        return orderOrchestrationService.listOrders(customerId);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable UUID orderId) {
        return orderOrchestrationService.getOrder(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(@PathVariable UUID orderId, @Valid @RequestBody CancelOrderRequest request) {
        return orderOrchestrationService.cancelOrder(orderId, request);
    }
}
