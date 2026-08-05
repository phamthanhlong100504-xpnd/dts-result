---
trigger: always_on
---

# Docker

## Purpose

Define mandatory standards for building, packaging, and running applications with Docker.

---

## Scope

Applies to all Docker images, Dockerfiles, and containers.

---

## Principles

- Reproducibility
- Security
- Portability
- Minimalism
- Immutability

---

# Rules

## MUST

### DOCKER-001

Use official or trusted base images.

### DOCKER-002

Pin image versions explicitly.

### DOCKER-003

Use multi-stage builds whenever possible.

### DOCKER-004

Run containers as a non-root user.

### DOCKER-005

Use `.dockerignore`.

### DOCKER-006

Define a HEALTHCHECK when applicable.

### DOCKER-007

Keep images immutable.

### DOCKER-008

Minimize image size.

### DOCKER-009

Use environment variables for configuration.

### DOCKER-010

Store secrets outside Docker images.

### DOCKER-011

Expose only required ports.

### DOCKER-012

Use one process per container unless explicitly justified.

### DOCKER-013

Keep Dockerfiles deterministic.

### DOCKER-014

Scan images for vulnerabilities.

### DOCKER-015

Tag images consistently.

---

## MUST NOT

### DOCKER-016

Do not use the `latest` tag.

### DOCKER-017

Do not run containers as root.

### DOCKER-018

Do not store credentials inside images.

### DOCKER-019

Do not install unnecessary packages.

### DOCKER-020

Do not copy unnecessary files.

### DOCKER-021

Do not hardcode configuration.

### DOCKER-022

Do not modify running containers manually.

### DOCKER-023

Do not build images from untrusted sources.

---

## SHOULD

### DOCKER-024

Keep layers minimal.

### DOCKER-025

Order Dockerfile instructions to maximize cache reuse.

### DOCKER-026

Use lightweight base images.

### DOCKER-027

Keep build context small.

### DOCKER-028

Label images with metadata.

### DOCKER-029

Use read-only filesystems when possible.

### DOCKER-030

Limit container resources.

---

## MAY

### DOCKER-031

Sign container images.

### DOCKER-032

Generate SBOM for images.

### DOCKER-033

Use Distroless images.

---

# Anti-patterns

- Using latest tag
- Root Container
- Fat Image
- Mutable Container
- Secret in Image
- Manual Container Changes
- Huge Build Context
- Single Dockerfile for Multiple Environments

---

# Checklist

- [ ] Multi-stage Build
- [ ] Non-root User
- [ ] Image Version Pinned
- [ ] HEALTHCHECK
- [ ] .dockerignore
- [ ] Small Image
- [ ] No Secret
- [ ] Security Scan
- [ ] Environment Variables
- [ ] Immutable Image

---

# References

- Docker Documentation
- Dockerfile Best Practices
- OCI Image Specification
- CIS Docker Benchmark