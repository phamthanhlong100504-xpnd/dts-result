---
description: Understand the existing codebase before making any modification.
---

# Read Code Workflow

## Purpose

Understand the existing codebase before making any modification.

The objective is to understand the current implementation, not to improve it.

---

## Input

- Source Code
- Project Structure
- User Request

---

## Preconditions

The required source files are available.

If important files are missing,

STOP.

Ask the user to provide them.

---

## Rule Files

### Read Before Starting

- `rules/global/01_architecture.md`
- `rules/global/04_naming.md`
- `rules/architecture/clean-architecture.md`
- `rules/templates/java/structure.md`

### Read If Related

- `rules/technology/java/core/java.md`
- `rules/technology/java/persistence/persistence.md`
- `rules/technology/java/spring/spring-boot.md`
- Infrastructure rules matching the project dependencies

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

Understand the expected architecture pattern from `clean-architecture.md` (rules `CA-001` to `CA-044`).

Understand the expected package structure from `structure.md` (rules `STRUCTURE-001` to `STRUCTURE-025`).

Understand the naming conventions from `04_naming.md` (rules `NAME-001` to `NAME-031`).

---

### Step 1 — Understand Project Architecture

Identify

- Architecture Pattern (compare against `CA-xxx` rules)
- Module Structure (compare against `STRUCTURE-xxx` rules)
- Layer Responsibilities (compare against `STRUCTURE-011` to `STRUCTURE-019`)

---

### Step 2 — Locate Related Feature

Identify

- Controller
- Service
- Repository
- Entity
- Mapper
- Validator

Verify each component is in the correct package according to `STRUCTURE-xxx`.

---

### Step 3 — Trace Execution Flow

Request

↓

Controller

↓

Service

↓

Repository

↓

Database

↓

Response

Verify dependency direction follows `CA-002` (dependencies point inward).

---

### Step 4 — Identify Dependencies

Examples

- Kafka
- Redis
- External APIs
- File Storage

---

### Step 5 — Identify Business Rules

Only describe existing behavior.

Do not infer missing requirements.

---

### Step 6 — Evaluate Against Rules

Compare the existing implementation against the loaded rules.

Note any deviations from

- Architecture rules (`CA-xxx`)
- Structure rules (`STRUCTURE-xxx`)
- Naming rules (`NAME-xxx`)
- Java rules (`JAVA-xxx`)
- Persistence rules (`PERSIST-xxx`)

Do NOT fix anything. Only document deviations.

---

### Step 7 — Summarize Findings

Include

- Current behavior
- Existing architecture
- Rule compliance status
- Potential risks
- Unknown areas

---

## Human Approval Gate

If the existing implementation appears

- Incorrect
- Inconsistent
- Poorly designed
- Violates rules

DO NOT modify it.

DO NOT suggest automatic fixes.

Summarize the findings.

Ask the user whether changes are desired.

---

If business logic cannot be determined,

STOP.

Ask the user.

---

## Output

- Architecture Summary
- Business Flow
- Related Components
- Rule Compliance Report (with rule IDs)
- Risk Analysis
- Questions (if any)

---

## Validation Checklist

- [ ] Rules loaded before analysis
- [ ] Architecture understood
- [ ] Flow identified
- [ ] Dependencies identified
- [ ] Business logic documented
- [ ] Rule deviations documented with IDs
- [ ] No assumptions made