---
trigger: always_on
---

# Microservices

## Purpose

Define mandatory standards for designing and operating microservices.

---

## Scope

Applies to all services deployed as independent microservices.

---

## Principles

- Single Responsibility
- Independent Deployment
- Loose Coupling
- High Cohesion
- Autonomous Teams
- Decentralized Data

---

# Rules

## MUST

### MS-001

Each service MUST own a single business capability.

### MS-002

Each service MUST own its database.

### MS-003

Each service MUST be independently deployable.

### MS-004

Each service MUST expose explicit APIs.

### MS-005

Service communication MUST use well-defined contracts.

### MS-006

Services MUST remain stateless whenever possible.

### MS-007

Services MUST support health checks.

### MS-008

Services MUST emit logs, metrics, and traces.

### MS-009

Services MUST tolerate transient failures.

### MS-010

Services MUST be independently testable.

---

## MUST NOT

### MS-011

Do not share databases.

### MS-012

Do not access another service's database.

### MS-013

Do not share business logic across services.

### MS-014

Do not create cyclic service dependencies.

### MS-015

Do not introduce synchronous communication unless necessary.

### MS-016

Do not expose internal implementation details.

---

## SHOULD

### MS-017

Prefer asynchronous communication.

### MS-018

Prefer event-driven integration.

### MS-019

Keep APIs backward compatible.

### MS-020

Keep services small and focused.

### MS-021

Design for failure.

---

## MAY

### MS-022

Apply Saga.

### MS-023

Apply Outbox Pattern.

### MS-024

Apply CQRS.

---

# Anti-patterns

- Shared Database
- Distributed Monolith
- Chatty Services
- God Service
- Tight Coupling
- Shared Business Logic
- Synchronous Chain Calls

---

# Checklist

- [ ] Single Responsibility
- [ ] Own Database
- [ ] Independent Deployment
- [ ] Health Check
- [ ] Metrics
- [ ] Logs
- [ ] Traces
- [ ] Stable API
- [ ] No Shared Database
- [ ] No Cyclic Dependency

---

# References

- Building Microservices — Sam Newman
- Microservices.io
- CNCF Cloud Native Principles