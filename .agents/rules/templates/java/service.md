---
trigger: always_on
---

# Service Template

## Purpose

Generate service classes for implementing business logic.

---

## Template

```java
@Service
@RequiredArgsConstructor
@Transactional
public class XxxService {

    private final XxxRepository xxxRepository;
    private final XxxMapper xxxMapper;

}
```

---

# Required Members

- @Service
- Constructor Injection
- Repository Dependency
- Mapper Dependency
- Transaction Management

---

# Standard Responsibilities

- Implement business logic
- Validate business rules
- Coordinate multiple repositories
- Publish domain events
- Delegate persistence to repositories
- Return DTOs or domain objects

---

# Rules

## MUST

### SERVICE-001

Use `@Service`.

### SERVICE-002

Use constructor injection.

### SERVICE-003

Delegate persistence to repositories.

### SERVICE-004

Use mappers for object conversion.

### SERVICE-005

Use transactions when modifying data.

### SERVICE-006

Keep business logic cohesive.

### SERVICE-007

Validate business rules before persistence.

### SERVICE-008

Publish events only after successful business operations.

---

## MUST NOT

### SERVICE-009

Do not expose entities to controllers.

### SERVICE-010

Do not implement HTTP-specific logic.

### SERVICE-011

Do not access HttpServletRequest or HttpServletResponse.

### SERVICE-012

Do not implement SQL directly.

### SERVICE-013

Do not perform manual object mapping.

---

# Checklist

- [ ] Business Logic
- [ ] Transaction Management
- [ ] Repository Delegation
- [ ] Mapper Used
- [ ] Business Validation
- [ ] No HTTP Logic