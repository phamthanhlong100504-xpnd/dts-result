---
trigger: always_on
---

# Controller Template

## Purpose

Generate REST controllers.

---

## Template

```java
@RestController
@RequestMapping(...)
@RequiredArgsConstructor
public class XxxController {

    private final XxxService xxxService;

}
```

---

# Required Members

- Constructor Injection
- Request Mapping
- Service Dependency
- Request Validation
- Response Mapping

---

# Standard Endpoints

- create()
- update()
- delete()
- getById()
- getPage()

---

# Rules

## MUST

### CONTROLLER-001

Use `@RestController`.

### CONTROLLER-002

Use constructor injection.

### CONTROLLER-003

Validate request objects.

### CONTROLLER-004

Return DTOs only.

### CONTROLLER-005

Delegate business logic to services.

### CONTROLLER-006

Return appropriate HTTP status codes.

### CONTROLLER-007

Keep controllers thin.

---

## MUST NOT

### CONTROLLER-008

Do not access repositories directly.

### CONTROLLER-009

Do not implement business logic.

### CONTROLLER-010

Do not expose entities.

### CONTROLLER-011

Do not perform manual object mapping.

---

# Checklist

- [ ] Thin Controller
- [ ] DTO
- [ ] Validation
- [ ] Service Only