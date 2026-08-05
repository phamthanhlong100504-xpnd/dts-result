---
trigger: always_on
---

# Testing

## Purpose

Define mandatory standards for software testing.

---

## Scope

Applies to all automated tests.

---

# Rules

## MUST

### TEST-001

Write tests for business logic.

### TEST-002

Keep tests independent.

### TEST-003

Use descriptive test names.

### TEST-004

Follow the Arrange-Act-Assert pattern.

### TEST-005

Test observable behavior, not implementation details.

### TEST-006

Write integration tests for external dependencies.

### TEST-007

Mock only external systems.

### TEST-008

Keep tests deterministic.

### TEST-009

Run tests automatically in CI.

### TEST-010

Fix failing tests before merging.

---

## MUST NOT

### TEST-011

Do not ignore failing tests.

### TEST-012

Do not rely on execution order.

### TEST-013

Do not duplicate test cases.

### TEST-014

Do not test framework behavior.

### TEST-015

Do not use production data in tests.

---

## SHOULD

### TEST-016

Keep tests fast.

### TEST-017

Prefer Testcontainers for integration tests.

### TEST-018

Prefer real infrastructure over in-memory replacements when feasible.

### TEST-019

Maintain meaningful test coverage.

---

# Anti-patterns

- Fragile Test
- Flaky Test
- Slow Test
- Duplicate Test
- Over Mocking

---

# Checklist

- [ ] Independent
- [ ] Deterministic
- [ ] Readable
- [ ] Automated
- [ ] Fast
- [ ] CI Enabled

---

# References

- JUnit 5 User Guide
- Mockito Documentation
- Testcontainers Documentation
- Martin Fowler - Test Pyramid