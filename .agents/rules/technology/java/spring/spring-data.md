---
trigger: always_on
---

# Spring Data

## Purpose

Define mandatory standards for data access using Spring Data.

---

## Scope

Applies to all Spring Data repositories.

---

# Rules

## MUST

### DATA-001

Keep repositories responsible only for persistence.

### DATA-002

Use transactions for write operations.

### DATA-003

Use pagination for large result sets.

### DATA-004

Prefer derived queries for simple lookups.

### DATA-005

Use custom queries only when necessary.

### DATA-006

Keep transactions short.

---

## MUST NOT

### DATA-007

Do not place business logic in repositories.

### DATA-008

Do not return entities directly to clients.

### DATA-009

Do not execute unnecessary queries.

### DATA-010

Do not load large object graphs unnecessarily.

---

## SHOULD

### DATA-011

Prefer projections when only partial data is required.

### DATA-012

Optimize fetch strategies.

### DATA-013

Monitor slow queries.

---

# Anti-patterns

- Fat Repository
- N+1 Query
- Long Transaction
- Entity Leakage

---

# Checklist

- [ ] Repository Only
- [ ] Transaction
- [ ] Pagination
- [ ] Optimized Query
- [ ] DTO

---

# References

- Spring Data Documentation
- Spring Data JPA Documentation