---
trigger: always_on
---

# Filter Template

## Purpose

Generate Servlet filters for processing HTTP requests and responses.

---

## Template

```java
@Component
public class XxxFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) {

    }

}
```

---

# Required Members

- Filter Implementation
- Request Processing
- Response Processing
- Filter Chain

---

# Standard Responsibilities

- Authentication
- Logging
- Request Validation
- Correlation ID
- Request/Response Pre-processing

---

# Rules

## MUST

### FILTER-001

Extend `OncePerRequestFilter`.

### FILTER-002

Always continue the filter chain unless blocking the request.

### FILTER-003

Keep filter execution lightweight.

### FILTER-004

Handle unexpected exceptions gracefully.

### FILTER-005

Log important request information when necessary.

---

## MUST NOT

### FILTER-006

Do not implement business logic.

### FILTER-007

Do not access repositories.

### FILTER-008

Do not call services unless absolutely necessary.

### FILTER-009

Do not modify request payloads unnecessarily.

---

# Checklist

- [ ] OncePerRequestFilter
- [ ] Filter Chain Continued
- [ ] Lightweight
- [ ] Logging
- [ ] No Business Logic