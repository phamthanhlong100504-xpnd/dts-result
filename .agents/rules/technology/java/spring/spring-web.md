---
trigger: always_on
---

# Spring Web

## Purpose

Define mandatory standards for REST API development.

---

## Scope

Applies to all REST controllers.

---

# Rules

## MUST

### WEB-001

Controllers MUST contain no business logic.

### WEB-002

Validate all incoming requests.

### WEB-003

Use DTOs for request and response models.

### WEB-004

Return appropriate HTTP status codes.

### WEB-005

Use global exception handling.

### WEB-006

Keep APIs RESTful.

### WEB-007

Document public APIs.

### WEB-008

Support pagination for list endpoints.

---

## MUST NOT

### WEB-009

Do not expose entities.

### WEB-010

Do not catch exceptions in controllers.

### WEB-011

Do not return stack traces.

### WEB-012

Do not use GET for state-changing operations.

---

## SHOULD

### WEB-013

Use ResponseEntity when needed.

### WEB-014

Version public APIs.

### WEB-015

Keep endpoints resource-oriented.

---

# Anti-patterns

- Fat Controller
- Entity Exposure
- Inconsistent Response
- RPC Style API

---

# Checklist

- [ ] DTO
- [ ] Validation
- [ ] Global Exception
- [ ] RESTful
- [ ] Pagination

---

# References

- Spring MVC Documentation
- REST API Design Guidelines