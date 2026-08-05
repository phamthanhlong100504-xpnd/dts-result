---
trigger: always_on
---

# PostgreSQL

## Purpose

Define mandatory standards for using PostgreSQL.

---

## Scope

Applies to all services using PostgreSQL.

---

# Rules

## MUST

### POSTGRES-001

Use UUID as the default primary key for main entity aggregates.

### POSTGRES-002

Manage schema changes with migrations.

### POSTGRES-003

Create indexes for frequently queried columns.

### POSTGRES-004

Use transactions for write operations.

### POSTGRES-005

Use UTC for all timestamps.

### POSTGRES-009

When interacting with `JSONB` columns marked `NOT NULL`, application entities MUST initialize fields with a valid JSON string (e.g. `"{}"`) to avoid `DataIntegrityViolationException` from null constraint violations on insert.

---

## MUST NOT

### POSTGRES-006

Do not use SELECT *.

### POSTGRES-007

Do not modify applied migrations.

### POSTGRES-008

Do not store large files in PostgreSQL.

---

# Anti-patterns

- Full Table Scan
- Missing Index
- Long Transaction
- Mutable Migration

---

# Checklist

- [ ] UUID
- [ ] Migration
- [ ] Index
- [ ] Transaction

---

# References

- PostgreSQL Documentation