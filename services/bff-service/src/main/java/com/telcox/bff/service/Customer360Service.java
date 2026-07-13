package com.telcox.bff.service;

import com.telcox.bff.model.Customer360Response;
import com.telcox.bff.model.UpstreamResult;
import com.telcox.bff.model.UpstreamStatus;
import com.telcox.bff.model.UserContext;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class Customer360Service {

    private final UpstreamClient upstreamClient;

    public Customer360Service(UpstreamClient upstreamClient) {
        this.upstreamClient = upstreamClient;
    }

    public Customer360Response getCustomer360(UUID customerId, UserContext user, String correlationId) {
        String encodedCustomerId = URLEncoder.encode(customerId.toString(), StandardCharsets.UTF_8);
        List<UpstreamStatus> statuses = new ArrayList<>();
        List<String> hiddenSections = new ArrayList<>();

        UpstreamResult<Map<String, Object>> customer =
                upstreamClient.getMap("customer-service", "/api/v1/customers/" + encodedCustomerId, user, correlationId);
        UpstreamResult<List<Map<String, Object>>> subscriptions =
                upstreamClient.getList("subscription-service", "/api/v1/subscriptions?customerId=" + encodedCustomerId, user, correlationId);
        UpstreamResult<List<Map<String, Object>>> orders =
                upstreamClient.getList("order-service", "/api/v1/orders?customerId=" + encodedCustomerId, user, correlationId);

        statuses.add(customer.status());
        statuses.add(subscriptions.status());
        statuses.add(orders.status());

        List<Map<String, Object>> invoices = List.of();
        if (user.canSeeBilling()) {
            UpstreamResult<List<Map<String, Object>>> result =
                    upstreamClient.getList("billing-service", "/api/v1/invoices?customerId=" + encodedCustomerId, user, correlationId);
            invoices = result.body();
            statuses.add(result.status());
        } else {
            hiddenSections.add("billing");
        }

        List<Map<String, Object>> tickets = List.of();
        if (user.canSeeTickets()) {
            UpstreamResult<List<Map<String, Object>>> result =
                    upstreamClient.getList("ticket-service", "/api/v1/tickets?customerId=" + encodedCustomerId, user, correlationId);
            tickets = result.body();
            statuses.add(result.status());
        } else {
            hiddenSections.add("tickets");
        }

        return new Customer360Response(
                customerId,
                OffsetDateTime.now(),
                user.toResponse(),
                customer.body(),
                subscriptions.body(),
                orders.body(),
                invoices,
                tickets,
                hiddenSections,
                statuses
        );
    }
}
