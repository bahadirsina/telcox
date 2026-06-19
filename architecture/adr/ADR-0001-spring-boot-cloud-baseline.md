# ADR-0001: Spring Boot / Spring Cloud version baseline

## Status

Accepted

## Context

TelcoX is a multi-module Spring microservices project. The project baseline
must stay aligned with the version set already used by the team and the current
repository dependency model.

## Decision

Use the following baseline for all backend modules:

- Java 21
- Spring Boot 4.0.6
- Spring Cloud 2025.1.1
- Resilience4j Spring Boot 4 integration

The parent `pom.xml` is the single source of truth for these versions. Service
modules must not override Spring Boot or Spring Cloud versions independently.

## Consequences

- All services share the same dependency compatibility matrix.
- New dependencies must be checked against Spring Boot 4.0.x and Spring Cloud
  2025.1.x before being added.
- Spring Boot 3 specific starters are not allowed in service modules.
