---
trigger: always_on
---

# Interceptor Template

## Purpose

Generate Spring MVC interceptors for processing requests before and after controller execution.

---

## Template

```java
@Component
public class XxxInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        return true;
    }

}
```

---

# Required Members

- HandlerInterceptor
- Pre Handle
- Post Handle
- After Completion

---

# Standard Responsibilities

- Authorization
- Request Logging
- Audit
- Request Tracking
- Header Validation

---

# Rules

## MUST

### INTERCEPTOR-001

Implement `HandlerInterceptor`.

### INTERCEPTOR-002

Return `true` to continue processing unless blocking the request.

### INTERCEPTOR-003

Keep interceptor execution lightweight.

### INTERCEPTOR-004

Use interceptors only for cross-cutting concerns.

### INTERCEPTOR-005

Log important request lifecycle events when necessary.

---

## MUST NOT

### INTERCEPTOR-006

Do not implement business logic.

### INTERCEPTOR-007

Do not access repositories.

### INTERCEPTOR-008

Do not perform long-running operations.

### INTERCEPTOR-009

Do not manipulate domain objects.

---

# Checklist

- [ ] HandlerInterceptor
- [ ] Lightweight
- [ ] Cross-cutting Concern
- [ ] Logging
- [ ] No Business Logic