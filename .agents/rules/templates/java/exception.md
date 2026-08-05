---
trigger: always_on
---

# Exception Template

## Purpose

Generate custom exceptions and global exception handlers.

---

## Template

```java
public class XxxException extends RuntimeException {

    public XxxException(String message) {
        super(message);
    }

}
```

---

# Required Members

- Exception Class
- Error Message
- Error Code (Optional)
- Global Exception Handler

---

# Standard Responsibilities

- Represent business errors
- Represent system errors
- Provide meaningful error messages
- Centralize exception handling

---

# Rules

## MUST

### EXCEPTION-001

Extend `RuntimeException` for business exceptions.

### EXCEPTION-002

Use meaningful exception names.

### EXCEPTION-003

Provide clear error messages.

### EXCEPTION-004

Handle exceptions using `@RestControllerAdvice`.

### EXCEPTION-005

Map exceptions to appropriate HTTP status codes.

### EXCEPTION-006

Keep exception classes lightweight.

---

## MUST NOT

### EXCEPTION-007

Do not catch generic `Exception` unless necessary.

### EXCEPTION-008

Do not swallow exceptions silently.

### EXCEPTION-009

Do not expose stack traces to clients.

### EXCEPTION-010

Do not place business logic inside exception classes.

---

# Checklist

- [ ] Custom Exception
- [ ] Clear Message
- [ ] Proper HTTP Status
- [ ] Global Handler
- [ ] No Business Logic