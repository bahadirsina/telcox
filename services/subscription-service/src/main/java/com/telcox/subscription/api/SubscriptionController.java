package com.telcox.subscription.api;

import com.telcox.subscription.service.SubscriptionLifecycleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionLifecycleService subscriptionLifecycleService;

    public SubscriptionController(SubscriptionLifecycleService subscriptionLifecycleService) {
        this.subscriptionLifecycleService = subscriptionLifecycleService;
    }

    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse activate(@Valid @RequestBody ActivationRequest request,
                                         @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return subscriptionLifecycleService.activate(request, correlationId);
    }

    @GetMapping
    public List<SubscriptionResponse> list(@RequestParam(required = false) UUID customerId) {
        return subscriptionLifecycleService.list(customerId);
    }

    @GetMapping("/{subscriptionId}")
    public SubscriptionResponse get(@PathVariable UUID subscriptionId) {
        return subscriptionLifecycleService.get(subscriptionId);
    }

    @PostMapping("/{subscriptionId}/suspend")
    public SubscriptionResponse suspend(@PathVariable UUID subscriptionId, @Valid @RequestBody SubscriptionActionRequest request) {
        return subscriptionLifecycleService.suspend(subscriptionId, request);
    }

    @PostMapping("/{subscriptionId}/reactivate")
    public SubscriptionResponse reactivate(@PathVariable UUID subscriptionId, @Valid @RequestBody SubscriptionActionRequest request) {
        return subscriptionLifecycleService.reactivate(subscriptionId, request);
    }

    @PostMapping("/{subscriptionId}/terminate")
    public SubscriptionResponse terminate(@PathVariable UUID subscriptionId, @Valid @RequestBody SubscriptionActionRequest request) {
        return subscriptionLifecycleService.terminate(subscriptionId, request);
    }

    @PostMapping("/{subscriptionId}/addons")
    public SubscriptionResponse addAddon(@PathVariable UUID subscriptionId, @Valid @RequestBody AddonRequest request) {
        return subscriptionLifecycleService.addAddon(subscriptionId, request);
    }

    @DeleteMapping("/{subscriptionId}/addons/{addonCode}")
    public SubscriptionResponse removeAddon(@PathVariable UUID subscriptionId, @PathVariable String addonCode) {
        return subscriptionLifecycleService.removeAddon(subscriptionId, addonCode);
    }

    @PostMapping("/mnp-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public MnpResponse createMnpRequest(@Valid @RequestBody CreateMnpRequest request,
                                        @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return subscriptionLifecycleService.createMnpRequest(request, correlationId);
    }

    @GetMapping("/mnp-requests")
    public List<MnpResponse> listMnpRequests(@RequestParam(required = false) UUID customerId) {
        return subscriptionLifecycleService.listMnpRequests(customerId);
    }

    @GetMapping("/mnp-requests/{requestId}")
    public MnpResponse getMnpRequest(@PathVariable UUID requestId) {
        return subscriptionLifecycleService.getMnpRequest(requestId);
    }

    @PostMapping("/mnp-requests/{requestId}/advance")
    public MnpResponse advanceMnpRequest(@PathVariable UUID requestId, @Valid @RequestBody AdvanceMnpRequest request) {
        return subscriptionLifecycleService.advanceMnpRequest(requestId, request);
    }
}
