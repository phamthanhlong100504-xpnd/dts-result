---
trigger: always_on
---

# Clean Architecture

## Purpose

Define mandatory standards for designing maintainable, testable, and framework-independent software using Clean Architecture.

---

## Scope

Applies to all applications unless another architecture is explicitly approved.

---

## Principles

- Independent of Frameworks
- Independent of UI
- Independent of Database
- Independent of Infrastructure
- Independent of External Systems
- Dependency Inversion
- Separation of Concerns
- Single Responsibility

---

# Rules

## MUST

### CA-001

Organize the system into architectural layers.

### CA-002

Dependencies MUST point inward.

### CA-003

Business rules MUST be independent of frameworks.

### CA-004

Business rules MUST be independent of databases.

### CA-005

Business rules MUST be independent of transport protocols.

### CA-006

Business rules MUST be testable without infrastructure.

### CA-007

Application logic MUST orchestrate use cases only.

### CA-008

Infrastructure MUST implement abstractions defined by inner layers.

### CA-009

Presentation MUST delegate business execution.

### CA-010

Controllers MUST NOT contain business logic.

### CA-011

Repositories MUST NOT contain business rules.

### CA-012

Entities MUST encapsulate business behavior.

### CA-013

Business validation MUST reside in the domain or application layer.

### CA-014

Dependencies MUST be injected.

### CA-015

Interfaces MUST belong to the consuming layer.

### CA-016

Infrastructure MUST be replaceable.

### CA-017

Application services MUST expose use cases.

### CA-018

External systems MUST be accessed through ports.

### CA-019

Framework annotations SHOULD remain outside the domain layer.

### CA-020

Domain objects MUST remain persistence-agnostic.

---

## MUST NOT

### CA-021

Do not access the database directly from controllers.

### CA-022

Do not expose ORM entities through APIs.

### CA-023

Do not place business logic inside controllers.

### CA-024

Do not place business logic inside repositories.

### CA-025

Do not inject infrastructure services into domain objects.

### CA-026

Do not reference framework classes inside the domain layer.

### CA-027

Do not introduce circular dependencies.

### CA-028

Do not expose infrastructure details across layer boundaries.

### CA-029

Do not bypass application services.

### CA-030

Do not couple business rules to implementation details.

---

## SHOULD

### CA-031

Keep use cases small and focused.

### CA-032

Keep controllers thin.

### CA-033

Keep repositories focused on persistence.

### CA-034

Keep domain models rich.

### CA-035

Use immutable value objects.

### CA-036

Keep interfaces minimal.

### CA-037

Prefer constructor injection.

### CA-038

Use dependency inversion everywhere.

### CA-039

Keep modules independently testable.

### CA-040

Separate read and write models when complexity justifies it.

---

## MAY

### CA-041

Apply CQRS.

### CA-042

Apply Event-Driven Architecture.

### CA-043

Apply Hexagonal Architecture.

### CA-044

Apply DDD tactical patterns.

---

# Layer Responsibilities

## Presentation

Responsible for:

- HTTP
- REST
- GraphQL
- gRPC
- CLI
- Validation
- Authentication
- Request Mapping
- Response Mapping

Must NOT contain:

- Business Rules
- Persistence Logic

---

## Application

Responsible for:

- Use Cases
- Transactions
- Authorization
- Workflow
- Coordination

Must NOT contain:

- Database Implementation
- Framework Logic

---

## Domain

Responsible for:

- Business Rules
- Business Policies
- Aggregates
- Entities
- Value Objects
- Domain Services
- Domain Events

Must NOT contain:

- Spring
- ASP.NET
- Hibernate
- EF Core
- SQL
- HTTP

---

## Infrastructure

Responsible for:

- Database
- Messaging
- Cache
- Email
- File Storage
- External APIs
- Framework Integration

Must NOT contain:

- Business Rules

---

# Dependency Rule

Allowed

```
Presentation
        ↓
Application
        ↓
Domain
```

```
Infrastructure
        ↓
Application
```

```
Infrastructure
        ↓
Domain Interface
```

Forbidden

```
Domain
      ↓
Infrastructure
```

```
Application
      ↓
Controller
```

```
Domain
      ↓
Framework
```

```
Controller
      ↓
Repository
```

---

# Ports and Adapters

Inbound Ports

- Use Cases

Outbound Ports

- Repository
- Message Broker
- Cache
- Storage
- External API

Adapters

- REST Controller
- Kafka Consumer
- PostgreSQL Repository
- Redis Repository
- S3 Storage Adapter

---

# Dependency Injection

Allowed

```
Controller

↓

Use Case

↓

Repository Interface

↓

Repository Implementation
```

Forbidden

```
Controller

↓

Repository Implementation
```

---

# Entity Rules

Entities MUST

- Protect invariants
- Validate state
- Contain behavior
- Hide internal state
- Avoid setters unless required

Entities MUST NOT

- Access repositories
- Call HTTP APIs
- Access databases
- Read configuration
- Send emails
- Publish infrastructure events directly

---

# Repository Rules

Repositories MUST

- Persist aggregates
- Retrieve aggregates
- Hide persistence implementation

Repositories MUST NOT

- Implement business logic
- Validate business rules
- Call external systems

---

# Controller Rules

Controllers MUST

- Validate request format
- Authenticate requests
- Delegate execution
- Map responses

Controllers MUST NOT

- Execute business rules
- Access repositories
- Manage transactions

---

# Application Service Rules

Application Services MUST

- Execute one use case
- Coordinate domain objects
- Manage transactions
- Publish domain events when required

Application Services MUST NOT

- Contain persistence logic
- Contain HTTP logic
- Contain serialization logic

---

# Domain Rules

Domain MUST

- Own business rules
- Protect consistency
- Be framework independent
- Be infrastructure independent

---

# Anti-patterns

- Fat Controller
- Fat Repository
- Anemic Domain Model
- God Service
- Service Locator
- Static Dependency
- Circular Dependency
- Infrastructure Leakage
- Active Record
- Transaction Script
- Shared Database
- Business Logic in DTO
- Business Logic in Entity Framework Model

---

# Checklist

- [ ] Layered Architecture
- [ ] Dependency Inversion
- [ ] Thin Controller
- [ ] Thin Repository
- [ ] Rich Domain
- [ ] Independent Domain
- [ ] Constructor Injection
- [ ] Replaceable Infrastructure
- [ ] Business Logic Isolated
- [ ] Independently Testable

---

# References

- Clean Architecture — Robert C. Martin
- The Clean Architecture Blog Series
- Domain-Driven Design — Eric Evans
- Implementing Domain-Driven Design — Vaughn Vernon
- Microsoft .NET Architecture Guide
- Spring Framework Reference Documentation