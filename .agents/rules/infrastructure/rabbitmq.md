---
trigger: always_on
---

# Prometheus

## Purpose

Define mandatory standards for collecting application metrics.

---

## Scope

Applies to all services exposing Prometheus metrics.

---

# Rules

## MUST

### PROM-001

Expose application metrics.

### PROM-002

Use consistent metric names.

### PROM-003

Monitor latency, throughput, and error rate.

### PROM-004

Protect metrics endpoints.

### PROM-005

Define alert rules for critical metrics.

---

## MUST NOT

### PROM-006

Do not expose sensitive information.

### PROM-007

Do not create high-cardinality metrics.

### PROM-008

Do not collect unused metrics.

---

# Anti-patterns

- Metric Explosion
- High Cardinality
- Missing Alerts
- Exposed Metrics

---

# Checklist

- [ ] Metrics
- [ ] Alerts
- [ ] Naming
- [ ] Security

---

# References

- Prometheus Documentation