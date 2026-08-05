---
trigger: always_on
---

# Repository Template

## Purpose

Generate repository interfaces for data persistence.

---

## Template

```java
@Repository
public interface XxxRepository extends JpaRepository<XxxEntity, UUID> {

}
```

---

# Required Members

- @Repository (Optional)
- JpaRepository
- Custom Query Methods
- Specifications (Optional)

---

# Standard Responsibilities

- CRUD operations
- Database queries
- Pagination
- Sorting
- Custom query execution

---

# Rules

## MUST

### REPOSITORY-001

Extend `JpaRepository` whenever possible.

### REPOSITORY-002

Keep repositories focused on persistence.

### REPOSITORY-003

Use derived query methods when appropriate.

### REPOSITORY-004

Use `@Query` only when necessary.

### REPOSITORY-005

Return Optional for single nullable results.

---

## MUST NOT

### REPOSITORY-006

Do not implement business logic.

### REPOSITORY-007

Do not call services.

### REPOSITORY-008

Do not publish events.

### REPOSITORY-009

Do not perform object mapping.

---

# Checklist

- [ ] JpaRepository
- [ ] Query Methods
- [ ] Pagination Support
- [ ] No Business Logic
- [ ] Persistence Only