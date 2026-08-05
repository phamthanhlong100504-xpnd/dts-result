---
trigger: always_on
---

# Database

## Purpose

Define mandatory standards for database design, schema evolution, and data integrity.

---

## Scope

Applies to all relational databases unless explicitly overridden.

---

## Principles

- Data Integrity
- Consistency
- Explicit Relationships
- Backward Compatibility
- Performance by Design
- Minimize Redundancy

---

# Rules

## MUST

### DB-001

Every table MUST have a primary key.

### DB-002

Primary keys SHOULD use UUID unless another strategy is explicitly justified.

### DB-003

Every table MUST have a descriptive name.

### DB-004

Table names MUST use snake_case.

### DB-005

Column names MUST use snake_case.

### DB-006

Every foreign key MUST be explicitly defined.

### DB-007

Every table MUST define appropriate indexes.

### DB-008

Every schema change MUST be managed by migration.

### DB-009

Migration files MUST be immutable.

### DB-010

Every table MUST define NOT NULL whenever possible.

### DB-011

Every column MUST use the smallest appropriate data type.

### DB-012

Every timestamp MUST be stored in UTC.

### DB-013

Every table SHOULD contain created_at.

### DB-014

Every table SHOULD contain updated_at.

### DB-015

Soft delete MUST be explicitly designed.

### DB-016

Unique business constraints MUST be enforced by UNIQUE indexes.

### DB-017

Database constraints MUST enforce business invariants whenever applicable.

### DB-018

Transactions MUST guarantee consistency.

### DB-019

Queries MUST use indexes for lookup operations.

### DB-020

Large objects SHOULD NOT be stored inside the relational database unless required.

---

## MUST NOT

### DB-021

Do not use SELECT *.

### DB-022

Do not duplicate data without justification.

### DB-023

Do not store business logic inside triggers unless explicitly required.

### DB-024

Do not expose database schema directly to API consumers.

### DB-025

Do not use nullable columns by default.

### DB-026

Do not create orphan records.

### DB-027

Do not modify historical migration files.

### DB-028

Do not use reserved keywords as identifiers.

### DB-029

Do not use inconsistent naming conventions.

### DB-030

Do not use database-specific features unless portability is intentionally sacrificed.

---

## SHOULD

### DB-031

Prefer normalization.

### DB-032

Denormalize only when justified by performance.

### DB-033

Index foreign keys.

### DB-034

Index frequently queried columns.

### DB-035

Keep transactions short.

### DB-036

Use optimistic locking where appropriate.

### DB-037

Archive historical data instead of deleting it.

---

## MAY

### DB-038

Partition large tables.

### DB-039

Use materialized views.

### DB-040

Use read replicas.

---

# Naming

Tables

```
users
orders
payments
media_files
exam_sessions
```

Columns

```
created_at
updated_at
deleted_at
user_id
media_id
```

---

# Migration

Preferred

```
V001__create_users.sql

V002__create_roles.sql

V003__add_user_email.sql
```

Migration MUST be repeatable only when explicitly supported.

Migration MUST never be edited after production deployment.

---

# Indexes

Index

- Primary Key
- Foreign Key
- Unique Constraint
- Frequently Filtered Columns
- Frequently Sorted Columns

Avoid unnecessary indexes.

---

# Transactions

Transactions MUST be atomic.

Transactions SHOULD be short.

Transactions MUST NOT include external network calls.

---

# Anti-patterns

- Missing Primary Key
- Missing Foreign Key
- Missing Index
- SELECT *
- Long Transaction
- Over-normalization
- Under-normalization
- Business Logic in Trigger
- Mutable Migration
- Database as Message Queue

---

# Checklist

- [ ] Primary Key
- [ ] Foreign Key
- [ ] Index
- [ ] Migration
- [ ] NOT NULL
- [ ] Data Type
- [ ] Unique Constraint
- [ ] Audit Fields
- [ ] UTC Timestamp
- [ ] Transaction Safe

---

# References

- PostgreSQL Documentation
- Flyway Documentation
- Designing Data-Intensive Applications
- SQL Style Guide