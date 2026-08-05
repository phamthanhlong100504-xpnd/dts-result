---
trigger: always_on
---

# Observability

## Purpose

Define mandatory standards for monitoring, diagnostics, and operational visibility.

---

## Scope

Applies to all applications, services, APIs, infrastructure, and background jobs.

---

## Principles

- Visibility
- Traceability
- Reliability
- Actionability
- Continuous Monitoring

---

# Rules

## MUST

### OBS-001

Expose application health status.

### OBS-002

Collect application metrics.

### OBS-003

Collect structured logs.

### OBS-004

Collect distributed traces.

### OBS-005

Propagate trace identifiers.

### OBS-006

Propagate request identifiers.

### OBS-007

Monitor application availability.

### OBS-008

Monitor latency.

### OBS-009

Monitor throughput.

### OBS-010

Monitor error rates.

### OBS-011

Monitor resource utilization.

### OBS-012

Record deployment events.

### OBS-013

Define service ownership.

### OBS-014

Define alert thresholds.

### OBS-015

Ensure observability data is retained according to policy.

---

## MUST NOT

### OBS-016

Do not rely solely on logs.

### OBS-017

Do not emit inconsistent metrics.

### OBS-018

Do not lose trace context.

### OBS-019

Do not expose sensitive information through telemetry.

### OBS-020

Do not create unnecessary high-cardinality metrics.

### OBS-021

Do not ignore critical alerts.

---

## SHOULD

### OBS-022

Adopt OpenTelemetry.

### OBS-023

Standardize metric names.

### OBS-024

Use dashboards for critical services.

### OBS-025

Monitor dependency health.

### OBS-026

Track business metrics.

### OBS-027

Monitor SLA and SLO compliance.

### OBS-028

Continuously review alert quality.

---

## MAY

### OBS-029

Implement synthetic monitoring.

### OBS-030

Implement anomaly detection.

### OBS-031

Implement distributed profiling.

---

# Standard Signals

- Logs
- Metrics
- Traces
- Health Checks
- Events

---

# Minimum Metrics

- Request Count
- Request Duration
- Error Rate
- CPU Usage
- Memory Usage
- Disk Usage
- Network Usage
- Active Connections
- Queue Length
- Thread Count

---

# Health Checks

Every service SHOULD expose:

- Liveness
- Readiness
- Startup

---

# Anti-patterns

- Log Only Monitoring
- Missing Trace Context
- Alert Fatigue
- High Cardinality Metrics
- Silent Failure
- Unowned Alerts
- Missing Dashboards

---

# Checklist

- [ ] Health Check
- [ ] Metrics
- [ ] Logs
- [ ] Traces
- [ ] Trace ID
- [ ] Request ID
- [ ] Dashboards
- [ ] Alerts
- [ ] SLO Defined
- [ ] Telemetry Reviewed

---

# References

- OpenTelemetry Specification
- Google SRE Book
- Prometheus Documentation
- Grafana Documentation
- CNCF Observability Whitepaper