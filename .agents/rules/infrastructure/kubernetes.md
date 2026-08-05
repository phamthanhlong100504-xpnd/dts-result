---
trigger: always_on
---

# Kubernetes

## Purpose

Define mandatory standards for deploying applications on Kubernetes.

---

## Scope

Applies to all Kubernetes workloads.

---

# Rules

## MUST

### K8S-001

Define resource requests and limits.

### K8S-002

Configure liveness probes.

### K8S-003

Configure readiness probes.

### K8S-004

Store configuration in ConfigMaps.

### K8S-005

Store secrets in Secrets.

### K8S-006

Deploy using rolling updates.

### K8S-007

Use namespaces.

---

## MUST NOT

### K8S-008

Do not run containers as root.

### K8S-009

Do not hardcode configuration.

### K8S-010

Do not store secrets in manifests.

---

# Anti-patterns

- No Resource Limits
- Missing Health Checks
- Hardcoded Secrets
- Default Namespace

---

# Checklist

- [ ] Resource Limits
- [ ] Liveness
- [ ] Readiness
- [ ] ConfigMap
- [ ] Secret
- [ ] Namespace

---

# References

- Kubernetes Documentation