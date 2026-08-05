---
trigger: always_on
---

# Entity Template

## Purpose

Generate JPA entities for database persistence.

---

## Template

```java
@Entity
@Table(name = "xxx")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XxxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

}
```

---

# Required Members

- @Entity
- @Table
- Primary Key
- Column Mapping
- Relationships
- Audit Fields

---

# Standard Responsibilities

- Represent database tables
- Define column mappings
- Define entity relationships
- Store persistent data

---

# Rules

## MUST

### ENTITY-001

Use `@Entity`.

### ENTITY-002

Use `@Table`.

### ENTITY-003

Define a primary key.

### ENTITY-004

Map database columns explicitly when necessary.

### ENTITY-005

Use JPA relationship annotations correctly.

### ENTITY-006

Keep entities persistence-focused.

### ENTITY-007

Include audit fields when required.

---

## MUST NOT

### ENTITY-008

Do not implement business logic.

### ENTITY-009

Do not expose entities through APIs.

### ENTITY-010

Do not inject Spring Beans.

### ENTITY-011

Do not access repositories or services.

### ENTITY-012

Do not use entities as Request or Response objects.

---

# Checklist

- [ ] Entity Annotation
- [ ] Table Mapping
- [ ] Primary Key
- [ ] Relationships
- [ ] Audit Fields
- [ ] No Business Logic