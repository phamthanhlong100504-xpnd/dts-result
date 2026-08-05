---
trigger: always_on
---

# Build

## Purpose

Define mandatory standards for project build and dependency management.

---

## Scope

Applies to all build configurations.

---

# Rules

## MUST

### BUILD-001

Builds MUST be reproducible.

### BUILD-002

Pin dependency versions.

### BUILD-003

Use a single build tool per project.

### BUILD-004

Separate production and test dependencies.

### BUILD-005

Fail builds on compilation errors.

### BUILD-006

Run tests during CI builds.

### BUILD-007

Use dependency management.

### BUILD-008

Keep build configuration under version control.

---

## MUST NOT

### BUILD-009

Do not use dynamic dependency versions.

### BUILD-010

Do not commit generated artifacts.

### BUILD-011

Do not skip required build steps.

### BUILD-012

Do not ignore build warnings without justification.

---

## SHOULD

### BUILD-013

Keep builds fast.

### BUILD-014

Use build cache when appropriate.

### BUILD-015

Automate release builds.

---

# Anti-patterns

- Unstable Build
- Dynamic Version
- Manual Build
- Slow Build

---

# Checklist

- [ ] Reproducible
- [ ] Version Pinned
- [ ] Automated
- [ ] Tested
- [ ] Version Controlled

---

# References

- Gradle Documentation
- Maven Documentation