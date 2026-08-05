---
description: Develop a complete feature from business requirements to tested code.
---

# Feature Development Workflow

## Purpose

Develop a complete feature from business requirements while following the established project architecture, templates, and coding standards.

The workflow coordinates all previous workflows to ensure consistent implementation.

---

## Input

- Business Requirements
- Existing Project
- Existing Code (Optional)

---

## Preconditions

Business requirements have been clarified.

Architecture and technology are known.

If critical information is missing,

STOP.

Ask the user.

---

## Rule Files

### Read Before Starting (Baseline for entire feature)

- `rules/global/01_architecture.md`
- `rules/global/02_api.md`
- `rules/global/03_database.md`
- `rules/global/04_naming.md`
- `rules/global/06_security.md`
- `rules/global/07_logging.md`
- `rules/global/08_error_handling.md`
- `rules/global/10_testing.md`
- `rules/architecture/clean-architecture.md`
- `rules/templates/java/structure.md`
- `rules/technology/java/core/java.md`
- `rules/technology/java/persistence/persistence.md`

### Loaded by Sub-Workflows (passed through from Step 0)

Each sub-workflow will load its own specific rules as defined in its Rule Files section.

The rules loaded in Step 0 serve as the shared baseline context.

---

## Steps

### Step 0 — Load Baseline Rules

Read all rule files listed in the Rule Files section above.

These rules remain active throughout the entire feature development process.

Every sub-workflow executed below inherits this baseline context.

---

### Step 1 — Understand Requirements

Execute Understand Requirement Workflow (`02_understand_requirement.md`).

That workflow will additionally load

- `rules/docx/java/api-blueprint-generator.md` (if blueprint needed)

Pass baseline rules context to the workflow.

Output: Requirement Summary

---

### Step 1.5 — Check Prerequisites
- Đọc `application.properties` để xác nhận `spring.kafka.bootstrap-servers` tồn tại.
- Kiểm tra bảng `flyfly_schema_history` có migration cho các bảng `storages`, `medias`, `upload_sessions`, `upload_policies`.
- Query bảng `storages` để chắc chắn có ít nhất một bản ghi `is_default = true`.
- Query bảng `upload_policies` để có ít nhất một policy cho `target_type` sẽ được sử dụng.
- Nếu bất kỳ mục nào không thỏa, **STOP** và báo lỗi chi tiết cho người dùng.

### Step 2 — Generate API Blueprint

Execute Generate Blueprint Workflow (`03_generate_blueprint.md`).

That workflow will additionally load

- `rules/docx/java/api-blueprint-generator.md`

Validate the blueprint against `API-BLUEPRINT-xxx` rules before proceeding.

Output: API Blueprint

STOP. Wait for user approval of the blueprint before continuing to Step 3.

---

### Step 3 — Generate Implementation

Execute Generate Code Workflow (`04_generate_code.md`).

That workflow will additionally load

- All `rules/templates/java/*.md` (relevant templates)
- `rules/technology/java/spring/*.md`
- `rules/technology/java/persistence/migration.md`
- `rules/technology/java/libraries/libraries.md`
- `rules/technology/java/build/build.md`

Ensure generated code complies with

- `CA-xxx` (architecture boundaries)
- `STRUCTURE-xxx` (package structure)
- `JAVA-xxx` (Java standards)
- `PERSIST-xxx` (persistence standards)
- `NAME-xxx` (naming conventions)

Output: Java Source Code

---

### Step 4 — Generate Tests

Execute Generate Test Workflow (`08_generate_test.md`).

That workflow will additionally load

- `rules/technology/java/testing/testing.md`

Output: Test Classes

---

### Step 5 — Review Implementation

Execute Review Code Workflow (`05_review_code.md`).

Review the generated code against ALL loaded rules.

Every violation must be reported with its rule ID.

Fix violations before completing.

Output: Review Summary

---

### Step 6 — Generate Documentation (If Requested)

Execute Documentation Workflow (`09_generate_documentation.md`).

That workflow will additionally load

- `rules/global/09_documentation.md`
- `rules/docx/java/api-blueprint-generator.md`

Output: Documentation

---

## Human Approval Gate

STOP before

- Creating New Modules
- Creating New Services
- Changing Database Schema
- Breaking Existing APIs
- Introducing New Infrastructure
- Changing Authentication
- Changing Authorization
- Changing Business Rules

Present

- Proposed Changes
- Expected Impact
- Alternatives

Wait for explicit user approval.

---

## Output

- API Blueprint (validated against `API-BLUEPRINT-xxx`)
- Java Source Code (validated against `CA-xxx`, `STRUCTURE-xxx`, `JAVA-xxx`, `PERSIST-xxx`, `NAME-xxx`)
- Tests (validated against testing rules)
- Documentation (Optional, validated against `09_documentation.md`)
- Review Summary (with rule violation report)

---

## Validation Checklist

- [ ] Baseline rules loaded at Step 0
- [ ] Requirements understood (Step 1)
- [ ] Blueprint generated and validated against `API-BLUEPRINT-xxx` (Step 2)
- [ ] Blueprint approved by user before code generation
- [ ] Code generated following templates and rules (Step 3)
- [ ] Tests generated following testing rules (Step 4)
- [ ] Code reviewed with rule IDs for violations (Step 5)
- [ ] Documentation generated (if requested) (Step 6)
- [ ] No unauthorized changes
- [ ] All rule violations resolved