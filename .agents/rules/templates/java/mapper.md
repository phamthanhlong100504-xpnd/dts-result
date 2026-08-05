---
trigger: always_on
---

# Mapper Template

## Purpose

Generate object mappers for converting between Entities, DTOs, Requests, Responses, and Events.

---

## Template

```java
@Mapper(componentModel = "spring")
public interface XxxMapper {

    XxxResponse toResponse(XxxEntity entity);

    XxxEntity toEntity(CreateXxxRequest request);

}
```

---

# Required Members

- @Mapper
- Mapping Methods
- Entity Mapping
- DTO Mapping

---

# Standard Responsibilities

- Convert Request to Entity
- Convert Entity to Response
- Convert Entity to Event
- Convert DTOs between layers

---

# Rules

## MUST

### MAPPER-001

Use MapStruct whenever possible.

### MAPPER-002

Keep mapping logic simple and deterministic.

### MAPPER-003

Map only object fields.

### MAPPER-004

Reuse mapper methods when appropriate.

### MAPPER-005

Return DTOs instead of entities.

---

## MUST NOT

### MAPPER-006

Do not implement business logic.

### MAPPER-007

Do not access repositories.

### MAPPER-008

Do not call services.

### MAPPER-009

Do not perform database queries.

---

# Checklist

- [ ] MapStruct
- [ ] Request Mapping
- [ ] Response Mapping
- [ ] Entity Mapping
- [ ] No Business Logic