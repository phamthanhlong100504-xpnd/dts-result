---
description: Generate a complete PostgreSQL database schema from business requirements.
---

# Generate DB Schema Workflow

## Purpose

Generate a complete PostgreSQL database schema file (`.sql`) from business requirements.

The generated schema will serve as the database design specification for the project.

The schema describes table structure, columns, data types, constraints, indexes, and the business rationale behind every design decision.

---

## Input

- Business Requirements
- Requirement Summary (Optional)
- Existing Database Schema (Optional)

---

## Preconditions

- Business requirements are complete.
- Entities and their relationships are understood.
- Missing information has been clarified.

If any required information is missing,

STOP.

Ask the user before continuing.

---

## Rule Files

### Read Before Starting

- `rules/docx/java/db-schema-generator.md`
- `rules/global/03_database.md`
- `rules/global/04_naming.md`

### Read If Related

- `rules/global/06_security.md` (if sensitive data is involved)
- `rules/architecture/clean-architecture.md` (if clarifying service boundaries)
- Infrastructure rules matching the database engine

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

The schema MUST follow the exact format defined in `db-schema-generator.md` (rules `DB-SCHEMA-001` to `DB-SCHEMA-030`).

Key constraints from `db-schema-generator.md`

- `DB-SCHEMA-001`: Use UUID for primary keys.
- `DB-SCHEMA-010`: Every table MUST have an `id` column.
- `DB-SCHEMA-011`: Every table MUST have `created_at`.
- `DB-SCHEMA-012`: Every table MUST have `updated_at`.
- `DB-SCHEMA-019`: Header comment MUST explain design rationale.
- `DB-SCHEMA-024`: Do not generate Java code.
- `DB-SCHEMA-025`: Do not generate migration files.
- `DB-SCHEMA-028`: Do not skip any section.

---

### Step 1 — Review Business Requirements

Ensure all functional requirements are understood.

Identify

- Domain Entities (the core business objects)
- Entity Relationships (one-to-one, one-to-many, many-to-many)
- Business Invariants (uniqueness rules, status transitions, time windows)
- Ownership (which service owns this table)
- Multi-tenancy requirements

---

### Step 2 — Design Table Structure

For each identified entity, determine

- Table Name (snake_case, plural — per `NAME-009`)
- Column List (per `DB-SCHEMA-010` to `DB-SCHEMA-018`)
- Data Types (per `DB-SCHEMA-001` to `DB-SCHEMA-009`)
- Nullability (justify every nullable column)
- Default Values
- Status Columns (list all valid states)

Column ordering MUST follow the convention defined in `db-schema-generator.md`

1. Primary key
2. Foreign keys
3. Business identity columns
4. Business data columns
5. Status and lifecycle columns
6. Metadata (JSONB)
7. Audit columns

---

### Step 3 — Design Constraints

Define

- Primary Key (`pk_<table_name>`)
- CHECK constraints for status/enum columns
- CHECK constraints for business invariants (e.g., `end_time > start_time`)
- Unique constraints for business uniqueness rules

Cross-service references

- Do NOT use database-level FK constraints (`DB-SCHEMA-029`)
- Document the target service in the column comment (`DB-SCHEMA-023`)

---

### Step 4 — Design Indexes

For each table, identify

- Hot-path query patterns (the most frequent lookups)
- Administrative query patterns (reports, audit trails)
- Scheduled job patterns (expiry scanners, cleanup jobs)

Create indexes following the naming convention

- `uq_<table_name>_<purpose>` for unique indexes
- `ix_<table_name>_<purpose>` for regular indexes

Every index MUST have a preceding comment explaining the use-case (`DB-SCHEMA-021`).

---

### Step 5 — Generate SQL File

Generate the complete `.sql` file following all sections defined in `db-schema-generator.md`

1. Header Comment Block (Section 0)
2. CREATE TABLE with inline comments (Section 1)
3. ALTER TABLE with constraints (Section 2)
4. COMMENT ON COLUMN for every column (Section 3)
5. Indexes with use-case comments (Section 4)

---

### Step 6 — Validate Schema Against Rules

Cross-check

- Table naming against `NAME-009`
- Column naming against `NAME-010`
- Primary key against `DB-001`, `DB-002`
- NOT NULL usage against `DB-010`
- Data types against `DB-011`
- Timestamps against `DB-012`
- Audit fields against `DB-013`, `DB-014`
- Soft delete against `DB-015`
- Unique constraints against `DB-016`
- Business constraints against `DB-017`
- Index coverage against `DB-007`, `DB-033`, `DB-034`

---

### Step 7 — Save Output

Save the generated SQL file to `docs/db/<table_name>.sql`.

One table per file.

If multiple tables are generated, create one file per table.

---

## Human Approval Gate

If generating the schema requires

- New database tables
- Schema modifications to existing tables
- Cross-service foreign key references
- New database engine or extension
- Partitioning or sharding decisions
- Sensitive data columns (PII, credentials)

STOP.

Explain the impact.

Wait for user approval.

---

## Output

- SQL Schema File(s) (formatted per `db-schema-generator.md`)
- Saved to `docs/db/<table_name>.sql`

---

## Validation Checklist

- [ ] Rules loaded before generation
- [ ] `db-schema-generator.md` format followed exactly
- [ ] Business requirements fully analyzed (Step 1)
- [ ] Table structure designed with correct column ordering (Step 2)
- [ ] Constraints defined with proper naming (Step 3)
- [ ] Indexes designed with use-case comments (Step 4)
- [ ] SQL file generated with all required sections (Step 5)
- [ ] Schema validated against `DB-xxx` and `NAME-xxx` rules (Step 6)
- [ ] Output saved to `docs/db/` (Step 7)
- [ ] No Java code generated (`DB-SCHEMA-024`)
- [ ] No migration files generated (`DB-SCHEMA-025`)
- [ ] No missing sections (`DB-SCHEMA-028`)
- [ ] Cross-service references documented without FK (`DB-SCHEMA-029`)
- [ ] User approval obtained (if required)
