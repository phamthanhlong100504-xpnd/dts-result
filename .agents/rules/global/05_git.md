---
trigger: always_on
---

# Git

## Purpose

Define mandatory standards for version control and repository management.

---

## Scope

Applies to all repositories.

---

## Principles

- Traceability
- Small Changes
- Reviewability
- Reproducibility

---

# Rules

## MUST

### GIT-001

Every change MUST be committed.

### GIT-002

Every commit MUST represent a single logical change.

### GIT-003

Commit messages MUST be meaningful.

### GIT-004

Use feature branches for development.

### GIT-005

Pull before pushing.

### GIT-006

Resolve conflicts before merging.

### GIT-007

Keep commit history clean.

### GIT-008

Review code before merging.

### GIT-009

Protect the main branch.

### GIT-010

Tag every release.

### GIT-011

Ignore generated files.

### GIT-012

Keep .gitignore updated.

### GIT-013

Store source code only.

### GIT-014

Review every Pull Request.

### GIT-015

Delete merged branches.

---

## MUST NOT

### GIT-016

Do not commit secrets.

### GIT-017

Do not commit credentials.

### GIT-018

Do not commit build artifacts.

### GIT-019

Do not rewrite published history.

### GIT-020

Do not force push to protected branches.

### GIT-021

Do not commit temporary files.

### GIT-022

Do not merge unreviewed code.

---

## SHOULD

### GIT-023

Squash trivial commits.

### GIT-024

Keep branches short-lived.

### GIT-025

Use semantic versioning.

### GIT-026

Keep commits atomic.

### GIT-027

Reference issues in commits.

---

## MAY

### GIT-028

Sign commits.

### GIT-029

Use Conventional Commits.

---

# Anti-patterns

- Huge Commit
- Force Push
- Secret Leak
- Broken Main Branch
- Long-lived Branch
- Mixed Changes
- Binary Repository

---

# Checklist

- [ ] Atomic Commit
- [ ] Meaningful Message
- [ ] Reviewed
- [ ] Tested
- [ ] No Secret
- [ ] No Build Artifact
- [ ] Clean History
- [ ] Merge Conflict Resolved

---

# References

- Git Documentation
- Conventional Commits
- Semantic Versioning