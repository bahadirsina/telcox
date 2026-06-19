# ADR-0002: Remove OpenFeign from core service communication

## Status

Accepted

## Context

The platform uses database-per-service and already models business consistency
through Kafka, outbox, inbox, and idempotent consumers. Keeping OpenFeign as the
default service-to-service mechanism encourages synchronous coupling between
core domains and makes failure boundaries less clear.

## Decision

OpenFeign is removed from core service communication. Core business flows must
use Kafka events and local read models where possible. Synchronous calls are
allowed only at the API edge or for explicitly documented query use cases, and
they should use Spring clients plus Resilience4j policies rather than Feign.

## Consequences

- Service modules must not add `spring-cloud-starter-openfeign`.
- `@EnableFeignClients` must not be used in application classes.
- Cross-domain write flows must publish events through the outbox backbone.
- Read-side needs should be solved with projections, cached read models, or
  explicitly documented query APIs.
