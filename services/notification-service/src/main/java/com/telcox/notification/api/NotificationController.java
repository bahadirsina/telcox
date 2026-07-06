package com.telcox.notification.api;

import com.telcox.notification.service.NotificationDeliveryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationDeliveryService deliveryService;

    public NotificationController(NotificationDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationDeliveryResponse send(
            @Valid @RequestBody SendNotificationRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return deliveryService.send(request, correlationId);
    }

    @GetMapping
    public List<NotificationDeliveryResponse> list(@RequestParam(required = false) UUID customerId) {
        return deliveryService.list(customerId);
    }
}
