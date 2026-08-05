---
trigger: always_on
---

# Spring Boot

## Purpose

Define mandatory standards for developing Spring Boot applications.

---

## Scope

Applies to all Spring Boot applications.

---

# Rules

## MUST

### SPRING-001

Use constructor injection.

### SPRING-002

Keep beans stateless unless state is required.

### SPRING-003

Use `@ConfigurationProperties` for configuration.

### SPRING-004

Use Profiles for environment-specific configuration.

### SPRING-005

Validate configuration at startup.

### SPRING-006

Keep configuration externalized.

### SPRING-007

Use Spring Boot auto-configuration whenever possible.

### SPRING-008

Register only necessary beans.

---

## MUST NOT

### SPRING-009

Do not use field injection.

### SPRING-010

Do not hardcode configuration.

### SPRING-011

Do not create unnecessary beans.

### SPRING-012

Do not place business logic inside configuration classes.

---

## SHOULD

### SPRING-013

Prefer constructor binding.

### SPRING-014

Keep configuration modular.

### SPRING-015

Enable Actuator in production.

---

# Anti-patterns

- Field Injection
- Hardcoded Configuration
- Bean Explosion
- Stateful Singleton

---

# Checklist

- [ ] Constructor Injection
- [ ] External Configuration
- [ ] Profiles
- [ ] ConfigurationProperties
- [ ] Actuator

---

# References

- Spring Boot Documentation