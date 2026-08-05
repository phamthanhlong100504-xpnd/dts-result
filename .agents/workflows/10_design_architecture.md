---
description: Design or improve software architecture based on business requirements.
---

# Design Architecture Workflow

## Purpose

Design or improve software architecture based on business requirements while ensuring scalability, maintainability, and consistency.

Architecture decisions must never be assumed without user approval.

---

## Input

- Business Requirements
- Existing Architecture (Optional)
- Non-functional Requirements

---

## Preconditions

Business goals are clearly understood.

If architectural constraints are unknown,

STOP.

Ask the user.

---

## Rule Files

### Read Before Starting

- `rules/global/01_architecture.md`
- `rules/architecture/clean-architecture.md`
- `rules/architecture/ddd.md`
- `rules/architecture/event-driven.md`
- `rules/architecture/microservice.md`
- `rules/architecture/modular-monolith.md`
- `rules/templates/java/structure.md`
- `rules/templates/java/package.md`

### Read If Related

- `rules/global/02_api.md`
- `rules/global/03_database.md`
- `rules/global/06_security.md`
- `rules/global/11_performance.md`
- Infrastructure rules matching the architecture scope

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

Understand

- Architectural principles from `01_architecture.md`
- Clean Architecture layers and boundaries from `clean-architecture.md` (`CA-001` to `CA-044`)
- DDD patterns from `ddd.md`
- Event-Driven patterns from `event-driven.md`
- Microservice patterns from `microservice.md`
- Modular Monolith patterns from `modular-monolith.md`
- Standard package structure from `structure.md` (`STRUCTURE-xxx`)

These rules define the constraints and options for architectural decisions.

---

### Step 1 — Understand Business Domain

Identify

- Core Features
- Modules
- Boundaries

---

### Step 2 — Identify Architecture Requirements

Examples

- Scalability
- Performance (check `11_performance.md`)
- Security (check `06_security.md`)
- Availability
- Maintainability

---

### Step 3 — Select Architecture Pattern

Choose from loaded architecture rules

- Clean Architecture (`clean-architecture.md`)
- DDD (`ddd.md`)
- Event-Driven (`event-driven.md`)
- Microservices (`microservice.md`)
- Modular Monolith (`modular-monolith.md`)

Justify the selection based on business requirements and rule constraints.

---

### Step 4 — Design the Architecture

Define

- Modules (following `STRUCTURE-xxx`)
- Services
- Communication patterns
- Storage
- External Systems

Ensure dependency direction follows `CA-002`.

---

### Step 5 — Validate the Architecture

Review against loaded rules

- Layer Separation (`CA-001`, `STRUCTURE-011`)
- Dependency Direction (`CA-002`, `STRUCTURE-014`)
- Technology Selection
- Scalability
- Security (`06_security.md`)
- Anti-patterns from `01_architecture.md` (God Object, Circular Dependency, etc.)

---

## Human Approval Gate

STOP if the proposed architecture requires

- Splitting Services
- Merging Services
- Introducing New Infrastructure
- Database Redesign
- New Communication Protocols
- Breaking Existing Contracts

Present

- Benefits
- Risks
- Trade-offs

Wait for explicit user approval.

---

## Output

- Architecture Design (referencing applicable rules)
- Component Diagram (Description)
- Technology Decisions
- Risks
- Recommendations

---

## Validation Checklist

- [ ] All architecture rules loaded before design
- [ ] Architecture pattern selected with justification
- [ ] Layer boundaries follow `CA-xxx`
- [ ] Package structure follows `STRUCTURE-xxx`
- [ ] Dependency direction validated
- [ ] Anti-patterns avoided
- [ ] Business requirements satisfied
- [ ] Dependencies valid
- [ ] Risks identified
- [ ] User approval obtained when required