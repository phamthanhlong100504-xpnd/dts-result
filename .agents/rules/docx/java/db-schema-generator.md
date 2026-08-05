---
trigger: always_on
---

# DB Schema Generation Rule

## Purpose

Generate a complete PostgreSQL database schema file (`.sql`) from business requirements.

The generated schema serves as the single source of truth for database table design.

The schema describes the table structure, constraints, indexes, and business rationale for every design decision.

---

# Output Format

Generate PostgreSQL SQL only.

Never generate Java code.

Never generate API specifications.

Never generate migration files (migrations are separate).

---

# Required Sections

Every generated SQL schema file MUST contain the following sections in the exact order.

---

## Section 0 — Header Comment Block

A multi-line SQL comment block at the top of the file.

Required fields

- Table Name
- Service Name (the microservice that owns this table)
- Entities Mapped (the domain entity name)
- Engine (database engine, default: PostgreSQL)
- Description (detailed multi-line description explaining the table's purpose, design rationale, data model decisions, and how the table fits into the overall system)

Format

```sql
-- Table: <table_name>
-- Service: <service_name>
-- Entities mapped: <entity_name>
-- Engine: <engine>
-- Mô tả: <detailed_description>
--
-- <additional_design_notes>
```

The description MUST explain

- What business question this table answers
- How identifiers are structured (if non-trivial)
- Inheritance or hierarchy rules (if applicable)
- Lifecycle management (how records transition between states)
- Audit strategy

---

## Section 1 — CREATE TABLE

The `CREATE TABLE` statement with all columns.

Every column MUST have an inline comment explaining its business purpose.

Format

```sql
CREATE TABLE <table_name> (
    <column_name>    <DATA_TYPE>    [NOT NULL]    [DEFAULT <value>],    -- <business_description>
    ...
);
```

Column ordering rules

1. Primary key column first
2. Foreign key columns next
3. Business identity columns (codes, names, types)
4. Business data columns
5. Status and lifecycle columns
6. Metadata (JSONB) column
7. Audit columns last (`created_by`, `created_at`, `updated_by`, `updated_at`, `deleted_at`)

---

## Section 2 — ALTER TABLE (Constraints)

Constraints MUST be defined in a separate `ALTER TABLE` block after the `CREATE TABLE`.

Required constraints

- Primary Key (`pk_<table_name>`)
- CHECK constraints for status/enum columns (`ck_<table_name>_<column>`)
- CHECK constraints for business invariants (`ck_<table_name>_<description>`)

Format

```sql
ALTER TABLE <table_name>
    ADD CONSTRAINT pk_<table_name> PRIMARY KEY (id),
    ADD CONSTRAINT ck_<table_name>_<column> CHECK (<condition>);
```

---

## Section 3 — COMMENT ON COLUMN

Every column MUST have a `COMMENT ON COLUMN` statement.

The comment MUST restate and expand the inline comment from the `CREATE TABLE`.

Format

```sql
COMMENT ON COLUMN <table_name>.<column_name> IS '<detailed_description>';
```

---

## Section 4 — Indexes

Every index MUST have a preceding SQL comment explaining its use-case.

Index types to consider

- Unique indexes for business invariants (partial unique for conditional uniqueness)
- Composite indexes for hot-path queries
- Single-column indexes for foreign keys and frequently filtered columns
- Partial indexes for status-based filtering

Naming convention

- Primary Key: `pk_<table_name>`
- Unique Index: `uq_<table_name>_<purpose>`
- Regular Index: `ix_<table_name>_<purpose>`

Format

```sql
-- <business_use_case_description>
CREATE [UNIQUE] INDEX <index_name> ON <table_name> (<columns>) [WHERE <condition>];
```

---

# Data Type Rules

## MUST

### DB-SCHEMA-001

Use `UUID` for primary keys (prefer UUIDv7 for time-sortable ordering).

### DB-SCHEMA-002

Use `TEXT` for variable-length strings without a strict business limit.

### DB-SCHEMA-003

Use `VARCHAR(n)` only when a strict maximum length is a business requirement.

### DB-SCHEMA-004

Use `TIMESTAMPTZ` for all timestamp columns (UTC storage).

### DB-SCHEMA-005

Use `JSONB` for metadata and extensible attribute columns.

### DB-SCHEMA-006

Use `BOOLEAN` with explicit `DEFAULT` for flag columns.

### DB-SCHEMA-007

Use `BIGINT` for counters and sizes (byte counts, file sizes).

### DB-SCHEMA-008

Use `INET` for IP address columns.

### DB-SCHEMA-009

Use `gen_random_uuid()` as default for UUID primary keys.

---

# Column Design Rules

## MUST

### DB-SCHEMA-010

Every table MUST have an `id` column as primary key.

### DB-SCHEMA-011

Every table MUST have `created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`.

### DB-SCHEMA-012

Every table MUST have `updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP` with a trigger or application-level update.

### DB-SCHEMA-013

Tables with soft delete MUST use `deleted_at TIMESTAMPTZ NULL` (NULL = not deleted).

### DB-SCHEMA-014

Tables with user tracking MUST have `created_by UUID` and `updated_by UUID`.

### DB-SCHEMA-015

Status columns MUST use `VARCHAR(30)` or `TEXT` with CHECK constraint listing all valid values.

### DB-SCHEMA-016

Status columns MUST have an explicit `DEFAULT` value.

### DB-SCHEMA-017

Nullable columns MUST justify why NULL is a valid business state in the inline comment.

### DB-SCHEMA-018

Foreign key references to other services MUST NOT use database-level FK constraints. Use application-level validation instead. Document the cross-service reference in the column comment.

---

# Comment Rules

## MUST

### DB-SCHEMA-019

The header comment MUST explain design rationale, not just restate the table name.

### DB-SCHEMA-020

Inline column comments MUST explain the business purpose, not just the data type.

### DB-SCHEMA-021

Index comments MUST explain the query pattern or use-case that the index supports.

### DB-SCHEMA-022

CHECK constraint comments MUST list all valid values explicitly.

### DB-SCHEMA-023

Cross-service references MUST be documented in column comments with the target service name.

---

## MUST NOT

### DB-SCHEMA-024

Do not generate Java code.

### DB-SCHEMA-025

Do not generate migration files (Flyway/Liquibase format).

### DB-SCHEMA-026

Do not generate Entity classes.

### DB-SCHEMA-027

Do not generate Repository interfaces.

### DB-SCHEMA-028

Do not skip any section.

### DB-SCHEMA-029

Do not use database-level FK constraints for cross-service references.

### DB-SCHEMA-030

Do not use generic column comments that merely restate the column name.

---

# Trigger Handling

If the table requires `updated_at` auto-update

```sql
-- Trigger: auto-update updated_at on row modification
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_<table_name>_updated_at
    BEFORE UPDATE ON <table_name>
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_updated_at();
```

Note: Only generate the function once per schema. If the function already exists, only generate the `CREATE TRIGGER` statement and add a comment referencing the existing function.

---

# File Naming

Output file name: `<table_name>.sql`

Output location: `docs/db/`

One table per file.

---

# Missing Information

If the business requirements do not provide enough information for a column or constraint

DO NOT omit the column.

Instead

- Add a `-- TODO:` comment explaining what information is needed
- Use a reasonable default based on common patterns
- Flag it in the header comment as requiring clarification

---

# Checklist

- [ ] Header Comment Block with rationale
- [ ] CREATE TABLE with inline comments
- [ ] Column ordering follows convention
- [ ] ALTER TABLE with PK and CHECK constraints
- [ ] COMMENT ON COLUMN for every column
- [ ] Indexes with use-case comments
- [ ] Constraint naming follows convention
- [ ] Index naming follows convention
- [ ] Cross-service references documented (no FK)
- [ ] Status columns have CHECK and DEFAULT
- [ ] Audit columns present (created_at, updated_at)
- [ ] No Java code generated
- [ ] No migration files generated
- [ ] Compliant with DB-001 to DB-040
- [ ] Compliant with NAME-009 and NAME-010
