---
trigger: always_on
---

# Request Template

## Purpose

Generate request DTOs for API input.

---

## Template

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateXxxRequest {

    @NotBlank
    private String name;

}
```

---

# Required Members

- DTO Fields
- Validation Annotations
- Lombok
- API Documentation (Optional)

---

# Standard Responsibilities

- Receive client input
- Validate request data
- Transfer data to services

---

# Rules

## MUST

### REQUEST-001

Use DTO classes.

### REQUEST-002

Validate fields using Jakarta Validation.

### REQUEST-003

Use meaningful field names.

### REQUEST-004

Keep request objects simple.

### REQUEST-005

Use nested DTOs when appropriate.

---

## MUST NOT

### REQUEST-006

Do not use JPA annotations.

### REQUEST-007

Do not implement business logic.

### REQUEST-008

Do not inject Spring Beans.

### REQUEST-009

Do not expose entities.

---

# Checklist

- [ ] DTO
- [ ] Validation
- [ ] No Entity
- [ ] No Business Logic
- [ ] Simple Structure