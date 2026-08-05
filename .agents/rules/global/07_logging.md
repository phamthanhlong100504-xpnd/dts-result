---
trigger: always_on
---

# Logging

## Purpose

Define mandatory logging standards.

---

## Scope

Applies to all applications and services.

---

## Principles

- Structured
- Consistent
- Actionable
- Secure
- Traceable

---

# Rules

## MUST

### LOG-001

Use structured logging.

### LOG-002

Log every application startup.

### LOG-003

Log every application shutdown.

### LOG-004

Log unexpected failures.

### LOG-005

Log external service failures.

### LOG-006

Include timestamp.

### LOG-007

Include log level.

### LOG-008

Include service name.

### LOG-009

Include trace identifier.

### LOG-010

Include request identifier when available.

### LOG-011

Use appropriate log levels.

### LOG-012

Write meaningful log messages.

### LOG-013

Keep log format consistent.

### LOG-014

Log retry attempts.

### LOG-015

Log security events.

---

## MUST NOT

### LOG-016

Do not log passwords.

### LOG-017

Do not log secrets.

### LOG-018

Do not log access tokens.

### LOG-019

Do not log private keys.

### LOG-020

Do not log personal data unless required.

### LOG-021

Do not log duplicate messages.

### LOG-022

Do not use logs for business auditing.

---

## SHOULD

### LOG-023

Use JSON logs.

### LOG-024

Separate application logs from audit logs.

### LOG-025

Use correlation identifiers.

### LOG-026

Log slow operations.

### LOG-027

Log resource usage for diagnostics.

---

## MAY

### LOG-028

Mask sensitive values.

### LOG-029

Log business events when valuable.

---

# Log Levels

ERROR

Unexpected failures.

WARN

Recoverable problems.

INFO

Business and lifecycle events.

DEBUG

Development diagnostics.

TRACE

Detailed execution flow.

---

# Anti-patterns

- Sensitive Logging
- Console Debugging
- Duplicate Logs
- Missing Context
- Generic Messages
- Stack Trace Spam

---

# Checklist

- [ ] Structured
- [ ] Trace ID
- [ ] Request ID
- [ ] Correct Level
- [ ] No Secret
- [ ] No Password
- [ ] Consistent Format
- [ ] Actionable Message

---

# References

- OpenTelemetry Logs
- OWASP Logging Cheat Sheet
- Google SRE Workbook