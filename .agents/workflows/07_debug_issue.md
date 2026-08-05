---
description: Identify the root cause of defects, exceptions, or unexpected behavior.
---

# Debug Issue Workflow

## Purpose

Identify the root cause of defects, exceptions, unexpected behavior, or system failures.

Focus on identifying the real cause rather than only fixing symptoms.

---

## Input

- Error Message
- Stack Trace
- Source Code
- Logs
- User Description

---

## Preconditions

Enough information is available to reproduce or analyze the issue.

If insufficient,

STOP.

Ask the user for

- Logs
- Stack Trace
- Source Code
- Configuration
- Steps to Reproduce

---

## Rule Files

### Read Before Starting

- `rules/technology/java/core/java.md`
- `rules/technology/java/persistence/persistence.md`
- `rules/global/08_error_handling.md`
- `rules/architecture/clean-architecture.md`
- `rules/templates/java/structure.md`

### Read If Related

- `rules/technology/java/spring/spring-boot.md`
- `rules/technology/java/spring/spring-data.md`
- `rules/technology/java/spring/spring-security.md`
- Infrastructure rules matching the error context

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

These rules help identify whether the bug is caused by a rule violation.

Common rule-related bugs

- N+1 query → violation of `PERSIST-022`
- `@Data` on entity causing StackOverflow → violation of `PERSIST-028a`
- EAGER fetching causing performance issues → violation of `PERSIST-020`
- Business logic in controller → violation of `STRUCTURE-021`, `CA-010`
- Missing transaction → violation of `PERSIST-008`
- Null collection return → violation of `JAVA-013`, `JAVA-018`
- Ignored exception → violation of `JAVA-019`

---

### Step 1 — Understand the Reported Issue

Determine

- Expected Behavior
- Actual Behavior

---

### Step 2 — Locate the Failure

Identify which component contains the bug

- Controller
- Service
- Repository
- External System
- Infrastructure

Verify the component is in the correct layer per `STRUCTURE-xxx`.

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

External System

↓

Response

Check if dependency direction is violated per `CA-002`.

---

### Step 4 — Identify Root Cause

Examples

- Null Pointer
- Invalid Configuration
- Missing Transaction (check `PERSIST-008`)
- Concurrency Issue (check `PERSIST-010`)
- Database Constraint
- Network Failure
- Serialization Error
- Lazy Loading issue (check `PERSIST-006`, `PERSIST-022`)

If the root cause is a rule violation, note the rule ID.

---

### Step 5 — Evaluate Impact

Determine affected

- APIs
- Features
- Modules
- Users

---

### Step 6 — Propose Fixes

Rank

- Recommended Fix (must comply with loaded rules)
- Alternative Fix
- Temporary Workaround

Each fix proposal must NOT introduce new rule violations.

---

## Human Approval Gate

Never modify source code automatically.

If fixing the issue requires

- Database Migration
- Business Logic Changes
- API Changes
- Service Split
- Infrastructure Changes

STOP.

Explain why.

Wait for user approval.

---

## Output

- Root Cause Analysis (with rule ID if violation-related)
- Affected Components
- Recommended Solution (rule-compliant)
- Alternative Solutions
- Risks

---

## Validation Checklist

- [ ] Rules loaded before analysis
- [ ] Root cause identified
- [ ] Rule violation checked (if applicable, note rule ID)
- [ ] Impact assessed
- [ ] Fix proposed (rule-compliant)
- [ ] Risks explained
- [ ] No assumptions made