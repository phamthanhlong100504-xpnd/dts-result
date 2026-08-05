---
description: Generate accurate and maintainable technical documentation.
---

# Generate Documentation Workflow

## Purpose

Generate accurate and maintainable technical documentation based on existing code, architecture, or business requirements.

Documentation must always reflect the current implementation and never invent behavior.

---

## Input

- Source Code
- API Blueprint (Optional)
- Business Requirements (Optional)
- Existing Documentation (Optional)

---

## Preconditions

The implementation or requirements are available.

If documentation cannot be generated accurately,

STOP.

Ask the user for additional information.

---

## Rule Files

### Read Before Starting

- `rules/global/09_documentation.md`
- `rules/global/02_api.md`
- `rules/docx/java/api-blueprint-generator.md`
- `rules/architecture/clean-architecture.md`

### Read If Related

- `rules/templates/java/structure.md`
- `rules/technology/java/core/java.md`
- Infrastructure rules matching the documentation scope

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

Understand documentation standards from `09_documentation.md`.

If generating API documentation, follow the format defined in `api-blueprint-generator.md` (`API-BLUEPRINT-xxx`).

---

### Step 1 — Identify Documentation Target

Examples

- API
- Module
- Service
- Architecture
- Feature
- Database
- Workflow

---

### Step 2 — Understand the Implementation

Identify

- Business Purpose
- Components
- Dependencies
- Data Flow

Use `clean-architecture.md` and `structure.md` to understand layer responsibilities.

---

### Step 3 — Generate Documentation

Describe

- Purpose
- Responsibilities
- Flow
- Dependencies
- Inputs
- Outputs
- Limitations

For API documentation, follow `api-blueprint-generator.md` format and validate against `API-xxx` rules.

---

### Step 4 — Validate Documentation

Ensure

- Accurate (matches current implementation)
- Complete (all sections from rules are present)
- Consistent (terminology matches `04_naming.md`)
- Easy to understand

---

## Human Approval Gate

If documentation contradicts the current implementation,

STOP.

Explain the inconsistency.

Ask the user whether the documentation or implementation should be considered the source of truth.

---

## Output

- Technical Documentation (formatted per applicable rules)

---

## Validation Checklist

- [ ] Rules loaded before generation
- [ ] Documentation format follows `09_documentation.md`
- [ ] API docs follow `api-blueprint-generator.md` format (if applicable)
- [ ] Purpose documented
- [ ] Flow documented
- [ ] Dependencies documented
- [ ] No assumptions
- [ ] Matches implementation