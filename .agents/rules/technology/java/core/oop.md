---
trigger: always_on
---

# Object-Oriented Programming

## Purpose

Define mandatory object-oriented design principles.

---

## Scope

Applies to all business and application code.

---

# Rules

## MUST

### OOP-001

Follow SOLID principles.

### OOP-002

Prefer composition over inheritance.

### OOP-003

Program to interfaces.

### OOP-004

Encapsulate internal state.

### OOP-005

Keep classes highly cohesive.

### OOP-006

Keep coupling low.

### OOP-007

Model behavior, not data.

### OOP-008

Keep objects responsible for their own state.

### OOP-009

Prefer immutable Value Objects.

### OOP-010

Expose only necessary APIs.

### OOP-011

Keep inheritance shallow.

### OOP-012

Separate domain logic from infrastructure.

---

## MUST NOT

### OOP-013

Do not create God Objects.

### OOP-014

Do not expose internal implementation.

### OOP-015

Do not inherit for code reuse alone.

### OOP-016

Do not create Anemic Domain Models.

### OOP-017

Do not mix unrelated responsibilities.

### OOP-018

Do not violate encapsulation.

### OOP-019

Do not depend on concrete implementations.

---

## SHOULD

### OOP-020

Keep classes small.

### OOP-021

Favor domain behavior over utility methods.

### OOP-022

Prefer explicit collaboration between objects.

### OOP-023

Minimize public APIs.

---

# Anti-patterns

- God Object
- Anemic Domain Model
- Feature Envy
- Tight Coupling
- Deep Inheritance
- Data Class

---

# Checklist

- [ ] SOLID
- [ ] Composition
- [ ] Low Coupling
- [ ] High Cohesion
- [ ] Encapsulation
- [ ] Behavior-Oriented Design

---

# References

- Clean Code
- Effective Java
- Domain-Driven Design
- Clean Architecture