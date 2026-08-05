---
description: Generate comprehensive tests for Java Spring Boot applications.
---

# Generate Test Workflow

## Purpose

Generate comprehensive tests for Java Spring Boot applications.

Tests must verify correctness without changing production code.

---

## Input

- Source Code
- API Blueprint (Optional)
- Existing Tests (Optional)

---

## Preconditions

The target code is available.

If the expected behavior cannot be determined,

STOP.

Ask the user.

---

## Rule Files

### Read Before Starting

- `rules/global/10_testing.md`
- `rules/technology/java/testing/testing.md`
- `rules/technology/java/core/java.md`
- `rules/templates/java/structure.md`

### Read If Related

- `rules/technology/java/persistence/persistence.md`
- `rules/technology/java/spring/spring-boot.md`
- `rules/technology/java/spring/spring-data.md`
- Infrastructure rules matching the test scope

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

Understand testing standards from `10_testing.md` and `testing.md`.

Understand the project structure from `structure.md` to place test classes in the correct packages.

---

### Step 1 — Understand the Target

Identify

- Public Methods
- Business Rules
- Validation Rules

---

### Step 2 — Identify Test Scenarios

Generate

- Happy Path
- Validation failures
- Exception cases
- Edge Cases
- Boundary Cases

---

### Step 3 — Determine Test Type

Based on the target component and `structure.md`

- Unit Test (Service, Validator, Mapper)
- Integration Test (Repository, external integrations)
- Controller Test (API endpoint testing)

---

### Step 4 — Generate Test Cases

Follow testing rules from `testing.md`.

Ensure

- Independent
- Repeatable
- Readable
- No `@Data` on test fixtures with entities per `PERSIST-028a`
- Test naming follows `NAME-xxx` conventions

---

### Step 5 — Review Coverage

Check

- Business Rules covered
- Validation covered
- Exceptions covered
- Branches covered
- Critical Paths covered

---

## Human Approval Gate

STOP if generating tests requires

- Changing Production Code
- Changing Business Logic
- Modifying APIs
- Changing Database Schema

Ask the user before proceeding.

---

## Output

- Test Classes
- Test Cases
- Coverage Summary
- Untested Areas

---

## Validation Checklist

- [ ] Rules loaded before test generation
- [ ] Testing rules from `testing.md` followed
- [ ] Test classes placed in correct packages per `structure.md`
- [ ] Happy path covered
- [ ] Validation covered
- [ ] Exceptions covered
- [ ] Edge cases covered
- [ ] Critical paths covered
- [ ] Production code unchanged