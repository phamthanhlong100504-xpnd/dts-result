---
description: Resolve software defects safely while preserving existing business behavior.
---

# Bug Fix Workflow

## Purpose

Resolve software defects safely while preserving existing business behavior and system stability.

The workflow focuses on identifying, fixing, validating, and reviewing bugs before completion.

---

## Input

- Bug Report
- Error Message
- Stack Trace (Optional)
- Logs (Optional)
- Source Code
- Existing Tests (Optional)

---

## Preconditions

The bug has been clearly described.

The affected source code is available.

If the bug cannot be reproduced or understood,

STOP.

Ask the user for

- Steps to Reproduce
- Expected Behavior
- Actual Behavior
- Logs
- Stack Trace
- Environment Information

---

## Rule Files

### Read Before Starting

- `rules/global/08_error_handling.md`
- `rules/global/07_logging.md`
- `rules/architecture/clean-architecture.md`
- `rules/templates/java/structure.md`
- `rules/technology/java/core/java.md`
- `rules/technology/java/persistence/persistence.md`
- `rules/global/04_naming.md`

### Read If Related

- `rules/technology/java/spring/spring-boot.md`
- `rules/technology/java/spring/spring-data.md`
- `rules/technology/java/spring/spring-security.md`
- `rules/technology/java/testing/testing.md`
- Infrastructure rules matching the bug context

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

These rules serve two purposes

1. Help identify if the bug was caused by a rule violation.
2. Ensure the fix does not introduce new rule violations.

---

### Step 1 — Understand the Reported Issue

Identify

- Expected Behavior
- Actual Behavior
- Impact
- Severity

---

### Step 2 — Locate Affected Components

Identify

- Controller
- Service
- Repository
- Entity
- Mapper
- Validator
- External Systems

Verify each component is in the correct layer per `STRUCTURE-xxx`.

---

### Step 3 — Debug Root Cause

Execute Debug Issue Workflow (`07_debug_issue.md`).

Identify

- Root Cause
- Affected Modules
- Dependencies
- Whether root cause is a rule violation (note rule ID)

Common rule-violation bugs

- N+1 query → `PERSIST-022`
- `@Data` StackOverflow → `PERSIST-028a`
- Missing transaction → `PERSIST-008`
- Business logic in controller → `STRUCTURE-021`
- Null collection return → `JAVA-013`
- Ignored exception → `JAVA-019`

---

### Step 4 — Design the Fix

Ensure

- Minimal code changes
- Preserve business behavior
- Preserve API compatibility
- Preserve database compatibility
- Fix complies with ALL loaded rules

---

### Step 5 — Implement the Fix

Modify only the necessary code.

Avoid unrelated changes.

Verify the fix against

- `CA-xxx` (no architecture violations)
- `STRUCTURE-xxx` (correct layer placement)
- `JAVA-xxx` (coding standards)
- `PERSIST-xxx` (persistence standards)
- `NAME-xxx` (naming conventions)

---

### Step 6 — Generate or Update Tests

Cover

- Bug Scenario (prove the fix works)
- Happy Path (prove existing behavior preserved)
- Regression Cases (prove no new bugs)

Follow testing rules from `testing.md`.

---

### Step 7 — Review the Implementation

Verify

- Coding Standards (`JAVA-xxx`)
- Architecture Rules (`CA-xxx`, `STRUCTURE-xxx`)
- Business Logic preserved
- Performance (`11_performance.md`)
- Security (`06_security.md`)
- No new rule violations introduced

---

### Step 8 — Validate Final Result

Ensure

- Bug resolved
- No regression
- Tests pass
- Existing functionality preserved
- No new rule violations

---

## Human Approval Gate

STOP immediately if fixing the bug requires

- Business Logic Changes
- Database Schema Changes
- API Contract Changes
- Service Split
- Service Merge
- New Infrastructure
- New Dependencies
- Authentication Changes
- Authorization Changes
- Breaking Backward Compatibility

Present

- Root Cause (with rule ID if violation-related)
- Proposed Solution (rule-compliant)
- Risks
- Alternatives
- Expected Impact

Wait for explicit user approval before continuing.

---

## Output

- Root Cause Analysis (with rule IDs if applicable)
- Fixed Source Code (rule-compliant)
- Updated Tests
- Review Summary (with rule compliance report)
- Risk Assessment

---

## Validation Checklist

- [ ] Rules loaded before analysis
- [ ] Bug reproduced or understood
- [ ] Root cause identified (with rule ID if applicable)
- [ ] Minimal fix applied
- [ ] Fix complies with `CA-xxx`
- [ ] Fix complies with `STRUCTURE-xxx`
- [ ] Fix complies with `JAVA-xxx`
- [ ] Fix complies with `PERSIST-xxx`
- [ ] Fix complies with `NAME-xxx`
- [ ] Business behavior preserved
- [ ] No unrelated changes
- [ ] Tests updated
- [ ] Regression tested
- [ ] No new rule violations
- [ ] User approval obtained (if required)