---
trigger: always_on
---

# Response Template

## Purpose

Generate response DTOs for API output.

---

## Template

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XxxResponse {

    private UUID id;

    private String name;

}
```

---

# Required Members

- DTO Fields
- Serialization
- Lombok

---

# Standard Responsibilities

- Return API data
- Hide internal implementation
- Expose only necessary fields

---

# Rules

## MUST

### RESPONSE-001

Use DTO classes.

### RESPONSE-002

Return only required fields.

### RESPONSE-003

Keep response immutable whenever possible.

### RESPONSE-004

Use meaningful field names.

### RESPONSE-005

Map entities using Mapper classes.

---

## MUST NOT

### RESPONSE-006

Do not expose entities.

### RESPONSE-007

Do not expose sensitive information.

### RESPONSE-008

Do not implement business logic.

### RESPONSE-009

Do not inject Spring Beans.

---

# Checklist

- [ ] DTO
- [ ] Required Fields Only
- [ ] No Sensitive Data
- [ ] Mapper Used
- [ ] No Business Logic