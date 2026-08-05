---
trigger: always_on
---

# Grafana

## Purpose

Define mandatory standards for monitoring visualization and operational dashboards.

---

## Scope

Applies to all Grafana dashboards, alerts, and data sources.

---

## Principles

- Visibility
- Simplicity
- Actionability
- Consistency
- Reliability

---

# Rules

## MUST

### GRAFANA-001

Every production service MUST have a dashboard.

### GRAFANA-002

Dashboards MUST display key service metrics.

### GRAFANA-003

Dashboards MUST use consistent naming.

### GRAFANA-004

Dashboards MUST display time in UTC unless otherwise required.

### GRAFANA-005

Critical services MUST define alerts.

### GRAFANA-006

Alert thresholds MUST be documented.

### GRAFANA-007

Dashboards MUST identify the monitored service.

### GRAFANA-008

Dashboards MUST use meaningful panel titles.

### GRAFANA-009

Alert notifications MUST reach responsible teams.

### GRAFANA-010

Dashboards MUST be version controlled.

---

## MUST NOT

### GRAFANA-011

Do not create dashboards without ownership.

### GRAFANA-012

Do not create duplicate dashboards.

### GRAFANA-013

Do not expose sensitive information.

### GRAFANA-014

Do not define noisy alerts.

### GRAFANA-015

Do not ignore alert failures.

### GRAFANA-016

Do not use inconsistent units.

---

## SHOULD

### GRAFANA-017

Keep dashboards simple.

### GRAFANA-018

Group related metrics.

### GRAFANA-019

Use variables for reusable dashboards.

### GRAFANA-020

Separate infrastructure metrics from business metrics.

### GRAFANA-021

Review dashboards regularly.

### GRAFANA-022

Review alert quality periodically.

---

## MAY

### GRAFANA-023

Create executive dashboards.

### GRAFANA-024

Create capacity planning dashboards.

### GRAFANA-025

Create SLA or SLO dashboards.

---

# Minimum Dashboard

Every production dashboard SHOULD include

- Request Rate
- Error Rate
- Response Time
- CPU Usage
- Memory Usage
- Disk Usage
- Network Usage
- Active Connections
- Availability

---

# Anti-patterns

- Dashboard Sprawl
- Alert Fatigue
- Duplicate Dashboards
- Missing Ownership
- Missing Alerts
- Missing Business Metrics
- Inconsistent Naming

---

# Checklist

- [ ] Dashboard Created
- [ ] Service Identified
- [ ] Core Metrics
- [ ] Alerts Configured
- [ ] Ownership Defined
- [ ] Version Controlled
- [ ] Variables Used
- [ ] Documentation Updated

---

# References

- Grafana Documentation
- Grafana Alerting Documentation
- Google SRE Workbook
- OpenTelemetry Documentation