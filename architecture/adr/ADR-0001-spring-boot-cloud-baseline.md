# ADR-0001: Spring Boot / Spring Cloud version baseline

## Status

Accepted

## Context

TelcoX is a multi-module Spring microservices project. The repo previously
declared Spring Boot 4.0.6 and Spring Cloud 2025.1.1, which makes the platform
baseline harder to stabilize for current service work and team collaboration.

## Decision

Use the following baseline for all backend modules:

- Java 21
- Spring Boot 3.5.0
- Spring Cloud 2025.0.0
- Resilience4j Spring Boot 3 integration

The parent `pom.xml` is the single source of truth for these versions. Service
modules must not override Spring Boot or Spring Cloud versions independently.

## Consequences

- All services share the same dependency compatibility matrix.
- New dependencies must be checked against Spring Boot 3.5.x and Spring Cloud
  2025.0.x before being added.
- Spring Boot 4 specific starters are not allowed in service modules.
