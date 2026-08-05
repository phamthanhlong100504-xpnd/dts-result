---
description: Analyze business requirements and convert them into a complete implementation-ready requirement summary.
---

# Understand Requirement Workflow

## Purpose

Analyze business requirements and convert them into a complete implementation-ready requirement summary.

The objective is understanding, not designing.

---

## Input

- Business Requirement
- User Description
- Existing Documentation (Optional)

---

## Preconditions

Business requirements are sufficiently described.

If the requirements are ambiguous,

STOP.

Ask the user for clarification.

---

## Rule Files

### Read Before Starting

- `rules/global/01_architecture.md`
- `rules/global/02_api.md`
- `rules/global/03_database.md`
- `rules/global/04_naming.md`

### Read If Related

- `rules/architecture/clean-architecture.md`
- `rules/docx/java/api-blueprint-generator.md`
- Infrastructure rules matching the requirements

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

Understand API naming from `02_api.md` (rules `API-001` to `API-039`).

Understand database standards from `03_database.md`.

Understand naming conventions from `04_naming.md` (rules `NAME-001` to `NAME-031`).

These rules define the vocabulary and constraints for analyzing the requirements.

---

### Step 1 — Identify Business Objective

Describe the goal of the feature or change.

---

### Step 2 — Identify Actors

Examples

- Administrator
- Student
- Teacher
- System

---

### Step 3 — Identify Business Rules

List every explicit business rule.

Never invent missing rules.

---

### Step 4 — Identify Entities

Identify domain entities.

Validate naming against `NAME-007` (singular names for classes) and `NAME-017` (enum names represent business concepts).

---

### Step 5 — Identify Required APIs

For each API, verify alignment with

- `API-003` (nouns for resource names)
- `API-004` (plural resource names)
- `API-005` (version every public API)
- `NAME-011` (REST resources use lowercase plural nouns)

---

### Step 6 — Identify Database Interactions

Examples

- Create
- Update
- Delete
- Query

Validate against `03_database.md` standards.

---

### Step 7 — Identify External Systems

Examples

- Kafka
- Redis
- Notification
- REST API
- Email
- File Storage

---

### Step 8 — Identify Missing Information

Generate a list of clarification questions.

Cross-check against the rules to find gaps (e.g., missing validation rules, missing error handling, missing pagination for list APIs per `API-013`).

---

## Human Approval Gate

If any of the following are unclear,

STOP.

Ask the user.

- Business Rules
- Permission Rules
- Database Design
- API Contract
- Event Flow
- Validation Rules
- Transaction Rules

---

If the requested implementation would require

- Creating new database tables
- Modifying existing schemas
- Splitting services
- Merging services
- Changing architecture
- Changing API contracts
- Adding new external systems
- Introducing caching
- Introducing messaging
- Changing authentication or authorization

STOP.

Explain the impact.

Wait for explicit user approval before proceeding.

---

## Output

- Requirement Summary
- Actors
- Business Rules
- Entities (validated against naming rules)
- Required APIs (validated against API rules)
- Database Operations
- External Dependencies
- Clarification Questions

---

## Validation Checklist

- [ ] Rules loaded before analysis
- [ ] Business objective identified
- [ ] Actors identified
- [ ] Business rules extracted
- [ ] Entities identified and names validated against `NAME-xxx`
- [ ] APIs identified and validated against `API-xxx`
- [ ] Database operations identified
- [ ] External systems identified
- [ ] No assumptions made
- [ ] User approval requested when required