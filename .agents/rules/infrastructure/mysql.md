---
trigger: always_on
---

# MySQL

## Purpose

Define mandatory standards for using MySQL.

---

## Scope

Applies to all services using MySQL.

---

# Rules

## MUST

### MYSQL-001

Use UTF-8 encoding.

### MYSQL-002

Use transactions for write operations.

### MYSQL-003

Create indexes for lookup columns.

### MYSQL-004

Use foreign keys where appropriate.

### MYSQL-005

Manage schema changes with migrations.

---

## MUST NOT

### MYSQL-006

Do not use SELECT *.

### MYSQL-007

Do not store large files in MySQL.

### MYSQL-008

Do not modify applied migrations.

---

# Anti-patterns

- Full Table Scan
- Missing Index
- Long Transaction
- Mutable Migration

---

# Checklist

- [ ] Migration
- [ ] Index
- [ ] Transaction
- [ ] Foreign Key

---

# References

- MySQL Documentation