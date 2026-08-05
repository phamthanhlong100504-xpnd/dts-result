---
description: Generate a complete API Blueprint from business requirements.
---

# Generate Blueprint Workflow

## Purpose

Generate a complete API Blueprint from business requirements.

The generated blueprint will serve as the implementation specification for generating Java Spring Boot code.

The blueprint describes business behavior, API contracts, processing flow, validations, data interactions, and operational requirements.

---

## Input

- Business Requirement
- Requirement Summary
- Existing APIs (Optional)

---

## Preconditions

- Business requirements are complete.
- Business rules are understood.
- Missing information has been clarified.

If any required information is missing,

STOP.

Ask the user before continuing.

---

## Rule Files

### Read Before Starting

- `rules/docx/java/api-blueprint-generator.md`
- `rules/global/02_api.md`
- `rules/global/03_database.md`
- `rules/global/04_naming.md`
- `rules/architecture/clean-architecture.md`

### Read If Related

- `rules/technology/java/persistence/persistence.md`
- Infrastructure rules matching the requirements

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

The blueprint MUST follow the exact format defined in `api-blueprint-generator.md` (rules `API-BLUEPRINT-001` to `API-BLUEPRINT-017`).

Key constraints from `api-blueprint-generator.md`

- `API-BLUEPRINT-001`: Generate every required section.
- `API-BLUEPRINT-002`: Use the exact section order.
- `API-BLUEPRINT-003`: Describe business behavior instead of implementation.
- `API-BLUEPRINT-009`: Use implementation-independent language.
- `API-BLUEPRINT-010`: Detailed enough for another AI to generate complete code.
- `API-BLUEPRINT-011`: Do not generate Java code.
- `API-BLUEPRINT-012`: Do not generate SQL.

---

### Step 1 — Review Business Requirements

Ensure all functional requirements are understood.

---

### Step 2 — Identify API Purpose

Determine

- API Name
- API Type (Public / Internal)
- Module
- Feature
- Description
- Related Tables
- Related Services

Following the Part 0 format from `api-blueprint-generator.md`.

---

### Step 3 — Design API Contract

Generate

- Endpoint (validate against `API-003`, `API-004`, `API-025` — no verbs in URLs)
- HTTP Method (validate against `API-002`)
- Request (every field: Name, Type, Required, Description, Validation Rules)
- Response (every field: Name, Type, Description)
- Error Codes (every error: Code, HTTP Status, Business Meaning, Client Message)

Following the Part 1 format from `api-blueprint-generator.md`.

---

### Step 4 — Describe Processing Flow

Include

- Controller Responsibilities (validate input, delegate — per `CA-010`, `CA-070`)
- Service Responsibilities (business logic, transactions — per `CA-007`, `CA-017`)
- Repository Responsibilities (persistence only — per `CA-011`)
- External Interactions (Kafka, Redis, REST APIs, etc.)

Following the Part 2 format from `api-blueprint-generator.md`.

---

### Step 5 — Describe Validations

Include

- Request Validation
- Business Validation
- Permission Validation

---

### Step 6 — Describe Data Interactions

Include

- Operation Type (SELECT, INSERT, UPDATE, DELETE)
- Target Table
- Conditions
- Expected Result
- Performance Notes (Optional)

Following the Part 3 format from `api-blueprint-generator.md`.

---

### Step 7 — Describe Operational Notes

Include

- Idempotency (per `API-016`)
- Tenant Isolation
- Retry Strategy
- Audit Logging
- Monitoring
- Metrics
- Tracing (per `API-033`, `API-034`)

If not applicable, write Not Applicable.

Following the Part 4 format from `api-blueprint-generator.md`.

---

### Step 8 — Validate Blueprint Against Rules

Ensure every required section from `api-blueprint-generator.md` exists.

Cross-check

- Endpoint naming against `API-xxx` and `NAME-011`
- Error response format against `API-008`, `API-009`
- Layer responsibilities against `CA-xxx`
- Database interactions against `03_database.md`

---

## Human Approval Gate

If generating the blueprint requires

- New APIs
- New database tables
- Schema modifications
- Service splitting
- New external integrations
- Business rule assumptions

STOP.

Explain the impact.

Wait for user approval.

---

## Output

- API Blueprint (formatted per `api-blueprint-generator.md`)

---

## Validation Checklist

- [ ] Rules loaded before generation
- [ ] `api-blueprint-generator.md` format followed exactly
- [ ] API Identity (Part 0) completed
- [ ] API Contract (Part 1) defined with all fields
- [ ] Processing Flow (Part 2) completed with layer responsibilities
- [ ] Validation Rules (Part 2) defined
- [ ] Data Interactions (Part 3) listed
- [ ] Operational Notes (Part 4) completed
- [ ] No Java code generated (`API-BLUEPRINT-011`)
- [ ] No SQL generated (`API-BLUEPRINT-012`)
- [ ] No missing sections (`API-BLUEPRINT-015`)
- [ ] Endpoint names validated against `API-xxx` and `NAME-xxx`
- [ ] User approval obtained (if required)