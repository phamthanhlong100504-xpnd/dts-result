---
trigger: always_on
---

# Migration

## Purpose

Define mandatory standards for managing database schema changes.

---

## Scope

Applies to all database migrations regardless of migration tool.

---

# Rules

## MUST

### MIGRATION-001

Manage all schema changes through migration scripts.

### MIGRATION-002

Assign a unique version to every migration.

### MIGRATION-003

Keep each migration focused on a single change.

### MIGRATION-004

Make migrations deterministic.

### MIGRATION-005

Test migrations before deployment.

### MIGRATION-006

Maintain forward compatibility whenever possible.

### MIGRATION-007

Use repeatable migrations only for repeatable objects.

### MIGRATION-008

Document destructive changes.

### MIGRATION-009

Keep migration history immutable.

---

## MUST NOT

### MIGRATION-010

Do not modify executed migrations.

### MIGRATION-011

Do not apply manual schema changes.

### MIGRATION-012

Do not combine unrelated changes in one migration.

### MIGRATION-013

Do not remove historical migrations.

### MIGRATION-014

Do not skip migration versions.

---

## SHOULD

### MIGRATION-015

Prefer backward-compatible schema changes.

### MIGRATION-016

Separate schema migration from data migration.

### MIGRATION-017

Review migration performance for large tables.

### MIGRATION-018

Provide rollback strategies for critical changes.

---

# Anti-patterns

- Mutable Migration
- Manual Database Changes
- Giant Migration
- Missing Rollback
- Version Gap

---

# Checklist

- [ ] Versioned
- [ ] Immutable
- [ ] Tested
- [ ] Documented
- [ ] Rollback Planned

---

# References

- Flyway Documentation
- Liquibase Documentation
- Database Refactoring