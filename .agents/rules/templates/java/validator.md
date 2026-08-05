---
trigger: always_on
---

# Validator Template

## Purpose

Generate validator classes for validating business rules.

---

## Template

```java
@Component
@RequiredArgsConstructor
public class XxxValidator {

    public void validate(...) {

    }

}
```

---

# Required Members

- @Component
- Validation Methods
- Exception Throwing
- Dependency Injection (Optional)

---

# Standard Responsibilities

- Validate business rules
- Validate domain constraints
- Validate cross-field conditions
- Throw business exceptions

---

# Rules

## MUST

### VALIDATOR-001

Use `@Component`.

### VALIDATOR-002

Validate business rules only.

### VALIDATOR-003

Throw meaningful business exceptions.

### VALIDATOR-004

Keep validation methods reusable.

### VALIDATOR-005

Separate validation from business execution.

---

## MUST NOT

### VALIDATOR-006

Do not implement business workflows.

### VALIDATOR-007

Do not expose REST endpoints.

### VALIDATOR-008

Do not perform object mapping.

### VALIDATOR-009

Do not modify application state.

### VALIDATOR-010

Do not execute persistence operations unless required for validation.

---

# Checklist

- [ ] Business Validation
- [ ] Reusable Methods
- [ ] Clear Exceptions
- [ ] No Business Logic
- [ ] No State Modification