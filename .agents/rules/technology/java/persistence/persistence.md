---
trigger: always_on
---

# Persistence

## Purpose

Define mandatory standards for designing and accessing persistent data.

---

## Scope

Applies to all persistence layers using JPA, Hibernate, or Spring Data.

---

# Rules

## MUST

### PERSIST-001

Use UUID as the primary key for main entity aggregates. For lookup tables or join tables, standard numeric IDs or composite keys are permitted.

### PERSIST-002

Use repositories only for data access.

### PERSIST-003

Keep business logic out of repositories.

### PERSIST-004

Keep entities focused on domain modeling.

### PERSIST-005

Use DTOs for external communication.

### PERSIST-006

Prefer LAZY fetching by default.

### PERSIST-007

Define indexes for frequently queried columns.

### PERSIST-008

Use transactions for write operations.

### PERSIST-009

Keep transactions short.

### PERSIST-010

Use optimistic locking where concurrent updates are expected.

### PERSIST-011

Use auditing for created and modified timestamps.

### PERSIST-012

Use pagination for large result sets.

### PERSIST-013

Load only required data.

### PERSIST-014

Use projections when full entities are unnecessary.

### PERSIST-015

Use specifications for dynamic queries.

### PERSIST-016

Validate entity constraints before persistence.

### PERSIST-017

Keep entity relationships explicit.

### PERSIST-018

Use cascading only when ownership exists.

### PERSIST-034

When mapping PostgreSQL `JSONB` columns in JPA Entities, MUST annotate the Java field with `@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)` to prevent PostgreSQL type mismatch errors (`character varying` vs `jsonb`).

### PERSIST-035

When application code assigns UUIDs manually prior to `repository.save()`, MUST NOT annotate the `@Id` field with `@GeneratedValue(strategy = GenerationType.UUID)`. Otherwise, Hibernate will generate a new UUID during save and override the application-assigned ID, breaking foreign key relationships across associated entities.

---

## MUST NOT

### PERSIST-019

Do not expose entities through APIs.

### PERSIST-020

Do not use EAGER fetching by default.

### PERSIST-021

Do not create unnecessary entity relationships.

### PERSIST-022

Do not execute N+1 queries.

### PERSIST-023

Do not keep transactions open longer than necessary.

### PERSIST-024

Do not perform business logic inside entity lifecycle callbacks.

### PERSIST-025

Do not use native SQL unless required.

### PERSIST-026

Do not fetch unnecessary columns.

### PERSIST-027

Do not ignore database constraints.

### PERSIST-028

Do not duplicate persistence logic.

### PERSIST-028a

Do not use Lombok's `@Data`, `@ToString`, or `@EqualsAndHashCode` on JPA Entity classes to prevent infinite recursion, StackOverflowError, or accidental lazy loading triggers. Prefer `@Getter`, `@Setter` and manual implementation of `equals`/`hashCode` using the entity's business key or ID.

---

## SHOULD

### PERSIST-029

Prefer EntityGraph over unnecessary eager loading.

### PERSIST-030

Prefer batch operations for bulk processing.

### PERSIST-031

Monitor slow queries.

### PERSIST-032

Review execution plans for critical queries.

### PERSIST-033

Optimize indexes periodically.

---

# Anti-patterns

- N+1 Query
- EAGER Everywhere
- Fat Repository
- Entity Exposure
- Long Transaction
- Missing Index
- Table Scan
- Native SQL Abuse

---

# Checklist

- [ ] UUID
- [ ] Repository Only
- [ ] DTO
- [ ] Lazy Loading
- [ ] Transaction
- [ ] Pagination
- [ ] Index
- [ ] Projection
- [ ] Auditing
- [ ] Optimistic Lock

---

# References

- Jakarta Persistence Specification
- Hibernate Documentation
- Spring Data JPA Documentation
- Effective Java