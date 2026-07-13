# ADR-0006: Frontend BFF Service Boundary

## Status

Accepted

## Context

Signal Atlas needs frontend-oriented aggregation for dashboard, customer 360,
order onboarding, and long-running saga status views. The gateway already owns
edge concerns such as JWT validation, CORS, rate limiting, and correlation-id
propagation. Putting aggregation logic in the gateway would mix routing/security
with product-specific response shaping.

## Decision

Telcox uses a separate Spring Boot `bff-service` for frontend aggregation.

The API Gateway remains the authenticated edge and relays trusted user context
headers to the BFF and downstream services. The BFF uses these headers for
role-aware response shaping, Redis-backed short TTL dashboard cache, customer
360 aggregation, order onboarding command submission, polling, and SSE operation
status.

The BFF does not own domain data. It calls documented service APIs and reports
upstream availability in responses when an optional downstream endpoint is not
ready yet.

## Consequences

- Frontend contracts can evolve without adding product logic to the gateway.
- Dashboard and operation status can use Redis without leaking cache concerns
  into domain services.
- Gateway and BFF must keep the trusted user context header contract aligned.
- Missing downstream REST endpoints are visible as degraded upstream statuses
  instead of failing the whole BFF response.
