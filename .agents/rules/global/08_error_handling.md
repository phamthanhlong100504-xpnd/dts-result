---
trigger: always_on
---

# Error Handling

## Purpose

Define mandatory standards for error handling and exception management.

---

## Scope

Applies to all applications, services, APIs, libraries, and background jobs.

---

## Principles

- Fail Fast
- Fail Securely
- Explicit Errors
- Consistent Responses
- Recover Only When Possible

---

# Rules

## MUST

### ERR-001

Handle every expected error.

### ERR-002

Return consistent error responses.

### ERR-003

Log unexpected exceptions.

### ERR-004

Use domain-specific exceptions for business errors.

### ERR-005

Use appropriate HTTP status codes for API errors.

### ERR-006

Provide actionable error messages.

### ERR-007

Include a unique error code.

### ERR-008

Include a trace identifier when available.

### ERR-009

Catch exceptions only when recovery is possible.

### ERR-010

Allow unrecoverable exceptions to propagate.

### ERR-011

Validate all external input.

### ERR-012

Convert infrastructure exceptions into application-specific exceptions.

### ERR-013

Handle timeout errors explicitly.

### ERR-014

Handle retry exhaustion explicitly.

### ERR-015

Ensure failed operations leave the system in a consistent state.

---

## MUST NOT

### ERR-016

Do not swallow exceptions.

### ERR-017

Do not catch generic exceptions unnecessarily.

### ERR-018

Do not expose stack traces.

### ERR-019

Do not expose internal implementation details.

### ERR-020

Do not use exceptions for normal control flow.

### ERR-021

Do not ignore interrupted execution.

### ERR-022

Do not duplicate exception logging.

---

## SHOULD

### ERR-023

Use centralized exception handling.

### ERR-024

Keep exception hierarchies simple.

### ERR-025

Prefer checked validation over runtime failures where practical.

### ERR-026

Provide meaningful validation errors.

### ERR-027

Retry only transient failures.

---

## MAY

### ERR-028

Support localized error messages.

### ERR-029

Support machine-readable error details.

---

# Anti-patterns

- Empty Catch Block
- Silent Failure
- Generic Exception
- Duplicate Logging
- Hidden Failure
- Exception Driven Logic
- Information Leakage

---

# Checklist

- [ ] Consistent Error Response
- [ ] Error Code
- [ ] Trace ID
- [ ] Proper Logging
- [ ] No Stack Trace Exposure
- [ ] No Silent Failure
- [ ] Correct Status Code
- [ ] Recovery Strategy

---

# References

- RFC 7807 Problem Details
- OWASP Error Handling Cheat Sheet
- Effective Java