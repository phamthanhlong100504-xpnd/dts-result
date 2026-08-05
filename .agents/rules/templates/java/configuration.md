---
trigger: always_on
---

# Configuration Template

## Purpose

Generate Spring Boot configuration classes.

---

## Template

```java
@Configuration
@RequiredArgsConstructor
public class XxxConfiguration {

    @Bean
    public Xxx xxx() {
        return new Xxx();
    }

}
```

---

# Required Members

- @Configuration
- Bean Definitions
- Configuration Properties
- Externalized Configuration

---

# Standard Responsibilities

- Register Spring Beans
- Configure Third-party Libraries
- Configure Infrastructure Components
- Bind Application Properties

---

# Rules

## MUST

### CONFIGURATION-001

Use `@Configuration`.

### CONFIGURATION-002

Register beans using `@Bean` when necessary.

### CONFIGURATION-003

Use constructor injection when dependencies are required.

### CONFIGURATION-004

Keep configuration classes focused on a single concern.

### CONFIGURATION-005

Read configuration from application properties.

### CONFIGURATION-006

Use `@ConfigurationProperties` for grouped settings when appropriate.

### CONFIGURATION-007

Configuration classes should be stateless.

---

## MUST NOT

### CONFIGURATION-008

Do not implement business logic.

### CONFIGURATION-009

Do not access repositories.

### CONFIGURATION-010

Do not expose REST endpoints.

### CONFIGURATION-011

Do not execute long-running tasks during bean initialization.

---

# Checklist

- [ ] Single Responsibility
- [ ] Stateless
- [ ] Bean Configuration Only
- [ ] No Business Logic
- [ ] Externalized Configuration