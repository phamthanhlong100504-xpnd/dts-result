---
trigger: always_on
---

# Java

## Purpose

Define mandatory standards for writing Java code.

---

## Scope

Applies to all Java source code.

---

# Rules

## MUST

### JAVA-001

Use Java 21 or later unless otherwise required.

### JAVA-002

Prefer immutable objects.

### JAVA-003

Declare variables and fields as `final` whenever possible.

### JAVA-004

Use `record` for immutable DTOs and simple value carriers.

### JAVA-005

Use `enum` instead of constant values.

### JAVA-006

Use `Optional` only as a return type.

### JAVA-007

Use try-with-resources for all `AutoCloseable` resources.

### JAVA-008

Prefer Stream API for collection transformations.

### JAVA-009

Prefer enhanced switch expressions.

### JAVA-010

Prefer meaningful names over abbreviations.

### JAVA-011

Keep methods focused on a single responsibility.

### JAVA-012

Keep classes cohesive.

### JAVA-013

Return empty collections instead of `null`.

### JAVA-014

Throw specific exceptions.

### JAVA-015

Keep business logic independent of utility classes.

---

## MUST NOT

### JAVA-016

Do not use wildcard imports.

### JAVA-017

Do not use raw types.

### JAVA-018

Do not return `null` collections.

### JAVA-019

Do not ignore exceptions.

### JAVA-020

Do not expose mutable internal state.

### JAVA-021

Do not duplicate logic.

### JAVA-022

Do not use reflection unless required.

### JAVA-023

Do not use mutable static fields.

### JAVA-024

Do not suppress warnings without justification.

---

## SHOULD

### JAVA-025

Prefer immutable collections.

### JAVA-026

Prefer local variable type inference when the type is obvious.

### JAVA-027

Keep methods short and readable.

### JAVA-028

Minimize object creation inside loops.

### JAVA-029

Use constants instead of magic numbers.

### JAVA-030

Prefer Lombok's `@Getter`, `@Setter`, and `@Slf4j` to reduce boilerplate, but avoid `@Data` on complex domain models or entities.

---

# Anti-patterns

- Long Method
- Duplicate Code
- Magic Number
- Mutable Shared State
- Null Return
- Reflection Abuse

---

# Checklist

- [ ] Immutable
- [ ] Small Methods
- [ ] Specific Exception
- [ ] No Raw Types
- [ ] No Wildcard Import
- [ ] No Null Collection

---

# References

- Effective Java
- Java Language Specification