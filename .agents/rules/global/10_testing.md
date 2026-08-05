---
trigger: always_on
---

# Testing

## Purpose

Define mandatory software testing standards.

---

## Scope

Applies to all applications, services, libraries, and APIs.

---

## Principles

- Test Early
- Test Automatically
- Repeatability
- Reliability
- Fast Feedback

---

# Rules

## MUST

### TEST-001

Every production feature MUST be testable.

### TEST-002

Automated tests MUST be executable without manual intervention.

### TEST-003

Every bug fix MUST include a regression test.

### TEST-004

Unit tests MUST be isolated.

### TEST-005

Integration tests MUST verify external integration.

### TEST-006

Tests MUST be deterministic.

### TEST-007

Tests MUST be repeatable.

### TEST-008

Test data MUST be isolated.

### TEST-009

Failing tests MUST block release.

### TEST-010

Tests MUST have clear assertions.

### TEST-011

Tests MUST have meaningful names.

### TEST-012

Tests MUST clean up created resources.

### TEST-013

Critical business logic MUST be covered by automated tests.

### TEST-014

Security-sensitive functionality MUST be tested.

### TEST-015

API contracts MUST be verified.

---

## MUST NOT

### TEST-016

Do not ignore failing tests.

### TEST-017

Do not depend on execution order.

### TEST-018

Do not share mutable state between tests.

### TEST-019

Do not write flaky tests.

### TEST-020

Do not use production data.

### TEST-021

Do not disable tests permanently.

---

## SHOULD

### TEST-022

Keep tests independent.

### TEST-023

Keep tests fast.

### TEST-024

Prefer integration tests for critical workflows.

### TEST-025

Mock only external dependencies.

### TEST-026

Continuously execute automated tests.

---

## MAY

### TEST-027

Perform load testing.

### TEST-028

Perform chaos testing.

### TEST-029

Perform mutation testing.

---

# Test Categories

- Unit Test
- Integration Test
- Contract Test
- End-to-End Test
- Performance Test
- Security Test

---

# Anti-patterns

- Flaky Test
- Slow Test Suite
- Shared State
- Manual Verification
- Missing Regression Test
- Assertion-less Test
- Disabled Test

---

# Checklist

- [ ] Unit Test
- [ ] Integration Test
- [ ] Regression Test
- [ ] Repeatable
- [ ] Independent
- [ ] Deterministic
- [ ] Fast
- [ ] Clear Assertions

---

# References

- Google Testing Blog
- Martin Fowler – Test Pyramid
- OWASP Testing Guide
- Testing on the Toilet