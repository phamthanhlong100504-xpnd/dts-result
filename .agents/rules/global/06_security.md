---
trigger: always_on
---

# Security

## Purpose

Define mandatory security standards.

---

## Scope

Applies to all applications, services, APIs, infrastructure, and repositories.

---

## Principles

- Least Privilege
- Defense in Depth
- Secure by Default
- Fail Securely
- Zero Trust

---

# Rules

## MUST

### SEC-001

Use HTTPS for all communications.

### SEC-002

Encrypt sensitive data in transit.

### SEC-003

Encrypt sensitive data at rest.

### SEC-004

Validate all external input.

### SEC-005

Authenticate every protected request.

### SEC-006

Authorize every protected operation.

### SEC-007

Hash passwords using approved algorithms.

### SEC-008

Store secrets outside source code.

### SEC-009

Rotate secrets regularly.

### SEC-010

Log security events.

### SEC-011

Apply the principle of least privilege.

### SEC-012

Sanitize user input.

### SEC-013

Validate uploaded files.

### SEC-014

Protect against injection attacks.

### SEC-015

Protect against broken authentication.

### SEC-016

Protect against insecure deserialization.

### SEC-017

Keep dependencies updated.

### SEC-018

Validate JWT tokens.

### SEC-019

Expire access tokens.

### SEC-020

Audit privileged actions.

---

## MUST NOT

### SEC-021

Do not store plaintext passwords.

### SEC-022

Do not hardcode secrets.

### SEC-023

Do not expose stack traces.

### SEC-024

Do not expose internal configuration.

### SEC-025

Do not trust client input.

### SEC-026

Do not disable security controls.

### SEC-027

Do not use weak cryptography.

### SEC-028

Do not expose sensitive logs.

---

## SHOULD

### SEC-029

Enable rate limiting.

### SEC-030

Enable audit logging.

### SEC-031

Use short-lived tokens.

### SEC-032

Apply Content Security Policy where applicable.

### SEC-033

Review dependencies regularly.

---

## MAY

### SEC-034

Enable Multi-Factor Authentication.

### SEC-035

Enable mutual TLS.

---

# Anti-patterns

- Hardcoded Secret
- Plaintext Password
- SQL Injection
- XSS
- CSRF
- Broken Access Control
- Weak Encryption
- Security by Obscurity

---

# Checklist

- [ ] HTTPS
- [ ] Authentication
- [ ] Authorization
- [ ] Secret Management
- [ ] Encryption
- [ ] Input Validation
- [ ] Output Encoding
- [ ] Dependency Scan
- [ ] Audit Logging
- [ ] Least Privilege

---

# References

- OWASP ASVS
- OWASP Cheat Sheet Series
- NIST Secure Software Development Framework