---
trigger: always_on
---

# Spring Security

## Purpose

Define mandatory standards for securing Spring applications.

---

## Scope

Applies to all authentication and authorization components.

---

# Rules

## MUST

### SECURITY-001

Authenticate every protected request.

### SECURITY-002

Authorize using roles or permissions.

### SECURITY-003

Hash passwords using BCrypt or stronger algorithms.

### SECURITY-004

Validate JWT before processing requests.

### SECURITY-005

Use HTTPS for all production environments.

### SECURITY-006

Apply least privilege.

---

## MUST NOT

### SECURITY-007

Do not store plain-text passwords.

### SECURITY-008

Do not disable security for convenience.

### SECURITY-009

Do not trust client-provided roles.

### SECURITY-010

Do not expose sensitive information.

---

## SHOULD

### SECURITY-011

Use method-level authorization.

### SECURITY-012

Rotate secrets regularly.

### SECURITY-013

Audit authentication failures.

---

# Anti-patterns

- Plain Password
- Hardcoded Secret
- Disabled Security
- Overprivileged User

---

# Checklist

- [ ] Authentication
- [ ] Authorization
- [ ] Password Hashing
- [ ] HTTPS
- [ ] JWT Validation

---

# References

- Spring Security Documentation
- OWASP ASVS