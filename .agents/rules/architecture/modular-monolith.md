---
trigger: always_on
---

# Modular Monolith

## Purpose

Define mandatory standards for designing maintainable, modular, and evolvable monolithic applications.

---

## Scope

Applies to all applications adopting the Modular Monolith architecture.

---

## Principles

- High Cohesion
- Low Coupling
- Explicit Boundaries
- Independent Modules
- Domain-Oriented Design
- Encapsulation
- Replaceability

---

# Rules

## MUST

### MM-001

Organize the application into business modules.

### MM-002

Each module MUST own a single business capability.

### MM-003

Each module MUST define clear public interfaces.

### MM-004

Internal implementation MUST remain private.

### MM-005

Modules MUST communicate through public contracts only.

### MM-006

Each module MUST own its application services.

### MM-007

Each module MUST own its domain model.

### MM-008

Each module MUST own its persistence layer.

### MM-009

Each module MUST own its infrastructure components.

### MM-010

Dependencies between modules MUST be explicit.

### MM-011

Dependencies MUST be unidirectional.

### MM-012

Modules MUST be independently testable.

### MM-013

Modules MUST hide implementation details.

### MM-014

Cross-module communication MUST occur through interfaces or application services.

### MM-015

Business rules MUST remain inside the owning module.

### MM-016

Modules MUST expose only required APIs.

### MM-017

Every module MUST have a clearly defined owner.

### MM-018

Each module SHOULD be independently deployable in the future.

### MM-019

Module boundaries MUST align with business boundaries.

### MM-020

Shared code MUST remain minimal.

---

## MUST NOT

### MM-021

Do not access another module's database objects directly.

### MM-022

Do not access another module's internal classes.

### MM-023

Do not bypass module interfaces.

### MM-024

Do not share business logic across modules.

### MM-025

Do not create cyclic module dependencies.

### MM-026

Do not create generic utility modules containing business logic.

### MM-027

Do not expose internal entities.

### MM-028

Do not couple modules through implementation details.

### MM-029

Do not allow one module to manage another module's lifecycle.

### MM-030

Do not create a Shared Domain.

---

## SHOULD

### MM-031

Organize modules around bounded contexts.

### MM-032

Minimize synchronous dependencies.

### MM-033

Publish domain events for cross-module communication when appropriate.

### MM-034

Keep module APIs stable.

### MM-035

Keep modules independently understandable.

### MM-036

Keep modules independently testable.

### MM-037

Separate read and write responsibilities when complexity increases.

### MM-038

Keep shared libraries infrastructure-focused.

### MM-039

Keep module startup independent.

### MM-040

Keep module configuration isolated.

---

## MAY

### MM-041

Apply CQRS within individual modules.

### MM-042

Use asynchronous messaging between modules.

### MM-043

Extract modules into microservices when justified.

---

# Module Structure

Every module SHOULD contain:

```
module/

    api/

    application/

    domain/

    infrastructure/

    configuration/
```

---

# Responsibilities

## api

Responsible for:

- Public Interfaces
- DTO
- Events
- Commands
- Queries

Must NOT contain:

- Business Logic

---

## application

Responsible for:

- Use Cases
- Transactions
- Workflow
- Authorization

Must NOT contain:

- Persistence Implementation

---

## domain

Responsible for:

- Entity
- Aggregate
- Value Object
- Domain Service
- Domain Event

Must NOT contain:

- Framework
- Database
- HTTP

---

## infrastructure

Responsible for:

- Repository
- Database
- Messaging
- External APIs
- Cache
- File Storage

Must NOT contain:

- Business Rules

---

# Module Communication

Allowed

```
Module A

↓

Module B API
```

```
Module A

↓

Module B Application Service
```

```
Module A

↓

Module B Published Event
```

Forbidden

```
Module A

↓

Module B Repository
```

```
Module A

↓

Module B Entity
```

```
Module A

↓

Module B Internal Service
```

```
Module A

↓

Module B Database
```

---

# Shared Components

Shared components SHOULD contain only:

- Common Infrastructure
- Security
- Logging
- Configuration
- Utilities without business rules

Shared components MUST NOT contain:

- Business Logic
- Business Entity
- Aggregate
- Repository
- Use Case

---

# Dependency Rule

Allowed

```
Module A

↓

Module B Public API
```

Forbidden

```
Module A

↓

Module B Internal Package
```

---

# Extraction Readiness

Modules SHOULD be designed so they can be extracted into separate services with minimal refactoring.

Extraction SHOULD require changing infrastructure only.

Business logic SHOULD remain unchanged.

---

# Anti-patterns

- Big Ball of Mud
- Shared Database Logic
- Shared Domain
- Cyclic Dependency
- God Module
- Generic Common Module
- Direct Repository Access
- Cross-Module Entity Reference
- Cross-Module Transaction
- Hidden Dependency
- Feature Scattering

---

# Checklist

- [ ] Business-Oriented Modules
- [ ] Clear Module Boundaries
- [ ] Public API Only
- [ ] Independent Domain
- [ ] Independent Persistence
- [ ] No Cyclic Dependency
- [ ] No Shared Business Logic
- [ ] Minimal Shared Library
- [ ] Independent Testing
- [ ] Extraction Ready

---

# References

- Modular Monolith — Simon Brown
- Domain-Driven Design — Eric Evans
- Implementing Domain-Driven Design — Vaughn Vernon
- Architecture Modernization — O'Reilly
- Spring Modulith Documentation
- Microsoft Modular Monolith Architecture Guide