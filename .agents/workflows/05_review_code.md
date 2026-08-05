---
description: Review existing code for correctness, maintainability, architecture compliance, and rule violations.
---

# Review Code Workflow

## Purpose

Review existing code for correctness, maintainability, architecture compliance, and rule violations.

The objective is to identify issues and provide recommendations without modifying the code automatically.

---

## Input

- Source Code
- Project Structure
- User Request (Optional)

---

## Preconditions

- Relevant source code is available.
- Project structure can be analyzed.

If required files are missing,

STOP.

Ask the user.

---

## Rule Files

### Read Before Starting

- `rules/global/01_architecture.md`
- `rules/global/02_api.md`
- `rules/global/04_naming.md`
- `rules/global/06_security.md`
- `rules/global/07_logging.md`
- `rules/global/08_error_handling.md`
- `rules/global/11_performance.md`
- `rules/architecture/clean-architecture.md`
- `rules/templates/java/structure.md`
- `rules/technology/java/core/java.md`
- `rules/technology/java/persistence/persistence.md`

### Read If Related

- `rules/technology/java/spring/spring-boot.md`
- `rules/technology/java/spring/spring-security.md`
- `rules/technology/java/testing/testing.md`
- Infrastructure rules matching the project dependencies

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

These rules define the standards against which the code will be evaluated.

Every violation found in the review MUST reference its rule ID.

---

### Step 1 — Understand Code Context

Identify

- Feature
- Business Purpose
- Related Components

---

### Step 2 — Review Architecture

Check against `clean-architecture.md` and `structure.md`

- Layer Separation (`STRUCTURE-011`)
- Dependency Direction (`CA-002`, `STRUCTURE-014`)
- Package Structure (`STRUCTURE-001` to `STRUCTURE-010`)
- No business logic in Controller (`STRUCTURE-021`, `CA-010`)
- No business logic in Entity (`STRUCTURE-022`)
- No repository access from Controller (`STRUCTURE-023`, `CA-021`)
- No entity exposure through API (`STRUCTURE-024`, `CA-022`)

---

### Step 3 — Review Implementation

Check against `java.md` and `persistence.md`

- Immutability (`JAVA-002`, `JAVA-003`)
- `record` for DTOs (`JAVA-004`)
- No wildcard imports (`JAVA-016`)
- No raw types (`JAVA-017`)
- No null collections (`JAVA-013`, `JAVA-018`)
- Specific exceptions (`JAVA-014`)
- No `@Data` on entities (`PERSIST-028a`)
- LAZY fetching default (`PERSIST-006`)
- UUID for aggregates (`PERSIST-001`)
- Transactions for writes (`PERSIST-008`)
- Pagination for lists (`PERSIST-012`)

Check naming against `04_naming.md`

- Meaningful names (`NAME-001`, `NAME-002`)
- Class suffixes (`NAME-012` to `NAME-015`)
- Boolean naming (`NAME-018`)
- REST resource naming (`NAME-011`)

---

### Step 4 — Review Quality

Check

- Readability
- Maintainability
- Reusability
- Complexity

---

### Step 5 — Review Non-functional Concerns

Check against global rules

- Performance (`11_performance.md`)
- Security (`06_security.md`)
- Logging (`07_logging.md`)
- Error Handling (`08_error_handling.md`)

---

### Step 6 — Summarize Findings

For each finding, provide

- Severity (Critical / High / Medium / Low / Suggestion)
- Description
- **Rule ID violated** (e.g., `PERSIST-028a`, `CA-010`, `NAME-012`)
- Recommendation

---

## Human Approval Gate

Never modify code automatically.

If fixes require

- Business logic changes
- API changes
- Database changes
- Service restructuring
- Architectural changes

STOP.

Present the findings.

Wait for user approval before suggesting or generating fixes.

---

## Output

- Review Report
- Rule Violations (with rule IDs)
- Risks
- Improvement Suggestions

---

## Validation Checklist

- [ ] Rules loaded before review
- [ ] Architecture reviewed against `CA-xxx` and `STRUCTURE-xxx`
- [ ] Implementation reviewed against `JAVA-xxx` and `PERSIST-xxx`
- [ ] Naming reviewed against `NAME-xxx`
- [ ] Security reviewed against `06_security.md`
- [ ] Performance reviewed against `11_performance.md`
- [ ] Every violation has a rule ID
- [ ] Findings classified by severity
- [ ] No automatic modifications