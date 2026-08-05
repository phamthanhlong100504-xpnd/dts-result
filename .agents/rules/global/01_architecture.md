---
trigger: always_on
---

# Architecture

## Purpose

Define mandatory architectural standards for all projects.

---

## Scope

Applies to every repository, service, library, and application.

---

## Principles

- Separation of Concerns
- Single Responsibility
- High Cohesion
- Low Coupling
- Explicit Dependencies
- Composition over Inheritance
- Convention over Configuration

---

# Rules

## MUST

- Follow a layered or explicitly defined architecture.
- Keep business logic independent from transport and persistence.
- Define clear module boundaries.
- Keep dependencies unidirectional.
- Design for maintainability before optimization.
- Keep components focused on a single responsibility.
- Isolate infrastructure concerns.
- Make dependencies explicit.
- Use dependency injection.
- Keep implementations replaceable.
- Prefer composition over inheritance.
- Prefer immutable objects when possible.
- Design modules for independent testing.
- Keep public interfaces minimal.
- Keep domain logic framework-independent whenever possible.

---

## MUST NOT

- Mix business logic with transport logic.
- Mix business logic with persistence logic.
- Access the database directly from controllers.
- Introduce circular dependencies.
- Expose infrastructure objects to business layers.
- Create God Objects.
- Create God Services.
- Use hidden dependencies.
- Share mutable global state.
- Place unrelated responsibilities in the same component.

---

## SHOULD

- Use DDD when business complexity justifies it.
- Keep modules loosely coupled.
- Favor interfaces over implementations.
- Organize projects by feature rather than technical type when appropriate.
- Keep services stateless.
- Prefer asynchronous communication where appropriate.
- Prefer event-driven integration for cross-service communication.
- Keep configuration externalized.
- Minimize shared libraries between services.

---

## MAY

- Apply CQRS when read/write workloads differ significantly.
- Apply Event Sourcing only when justified.
- Apply Hexagonal Architecture.
- Apply Clean Architecture.
- Apply Modular Monolith where Microservices are unnecessary.

---

# Dependency Direction

Allowed

```
Presentation
↓

Application
↓

Domain
↓

Infrastructure
```

Forbidden

```
Infrastructure
↓

Domain
```

```
Repository
↓

Controller
```

```
Entity
↓

Controller
```

---

# Module Rules

Every module MUST have a clearly defined responsibility.

Every module MUST expose only necessary APIs.

Every module MUST hide internal implementation.

Every module SHOULD be independently testable.

---

# Service Rules

Every service MUST own its data.

Every service MUST expose stable contracts.

Every service MUST be independently deployable.

Every service SHOULD avoid synchronous dependencies.

---

# Anti-patterns

- God Object
- God Service
- Fat Controller
- Anemic Domain Model
- Shared Database
- Circular Dependency
- Hidden Dependency
- Utility Class Abuse
- Distributed Monolith
- Business Logic in Controller
- Business Logic in Repository
- Infrastructure Leakage
- Tight Coupling

---

# Checklist

- [ ] Single responsibility
- [ ] Clear module boundary
- [ ] No circular dependency
- [ ] Business logic isolated
- [ ] Infrastructure isolated
- [ ] Dependencies explicit
- [ ] Dependency injection
- [ ] Interfaces minimized
- [ ] Public API documented
- [ ] Independently testable

---

# References

- Domain-Driven Design — Eric Evans
- Implementing Domain-Driven Design — Vaughn Vernon
- Clean Architecture — Robert C. Martin
- Building Microservices — Sam Newman
- ThoughtWorks Technology Radar