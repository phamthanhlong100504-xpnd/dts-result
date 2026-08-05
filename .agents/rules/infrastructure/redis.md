---
trigger: always_on
---

# Redis

## Purpose

Define mandatory standards for using Redis.

---

## Scope

Applies to all services using Redis.

---

# Rules

## MUST

### REDIS-001

Define TTL for cached data.

### REDIS-002

Use meaningful key naming.

### REDIS-003

Handle cache misses correctly.

### REDIS-004

Use Redis only for appropriate workloads.

### REDIS-005

Monitor memory usage.

---

## MUST NOT

### REDIS-006

Do not use Redis as the primary database.

### REDIS-007

Do not store unbounded data.

### REDIS-008

Do not cache sensitive data without protection.

---

# Anti-patterns

- No TTL
- Unlimited Cache
- Cache as Database
- Memory Exhaustion

---

# Checklist

- [ ] TTL
- [ ] Key Naming
- [ ] Memory Monitoring
- [ ] Cache Strategy

---

# References

- Redis Documentation