---
description: Improve code quality while preserving business behavior.
---

# Refactor Code Workflow

## Purpose

Improve the quality, readability, maintainability, and structure of existing code while preserving its behavior.

Refactoring must never change business logic unless explicitly approved.

---

## Input

- Existing Source Code
- User Request
- Existing Tests (Optional)

---

## Preconditions

- Code compiles successfully or the existing issues are known.
- Current business behavior is understood.

If the current behavior cannot be determined,

STOP.

Ask the user before continuing.

---

## Rule Files

### Read Before Starting

- `rules/architecture/clean-architecture.md`
- `rules/templates/java/structure.md`
- `rules/technology/java/core/java.md`
- `rules/technology/java/core/oop.md`
- `rules/technology/java/persistence/persistence.md`
- `rules/global/04_naming.md`

### Read If Related

- `rules/technology/java/spring/spring-boot.md`
- `rules/technology/java/spring/spring-web.md`
- Infrastructure rules matching the project dependencies

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

The refactored code MUST comply with

- Architecture boundaries from `clean-architecture.md` (`CA-xxx`)
- Package structure from `structure.md` (`STRUCTURE-xxx`)
- Java standards from `java.md` (`JAVA-xxx`)
- Naming conventions from `04_naming.md` (`NAME-xxx`)

---

### Step 1 — Understand Existing Implementation

Identify

- Business Purpose
- Execution Flow
- Dependencies

---

### Step 2 — Identify Refactoring Opportunities

Check against loaded rules and identify

- Duplicate Code → violates `JAVA-021`
- Long Methods → violates `JAVA-011`, `JAVA-027`
- Large Classes → violates `JAVA-012`
- Poor Naming → violates `NAME-xxx`
- Tight Coupling → violates `CA-002`
- Business logic in wrong layer → violates `STRUCTURE-016` to `STRUCTURE-019`
- `@Data` on entities → violates `PERSIST-028a`
- Dead Code
- Code Smells

---

### Step 3 — Evaluate Risks

Determine whether the refactoring may affect

- Business Logic
- API Contract
- Database
- Transactions
- Events

---

### Step 4 — Apply Safe Refactoring

Apply refactoring to bring code into compliance with loaded rules.

Examples

- Rename to match `NAME-xxx`
- Extract Method to satisfy `JAVA-011`
- Move class to correct package per `STRUCTURE-xxx`
- Replace `@Data` with `@Getter`/`@Setter` on entities per `PERSIST-028a`
- Simplify Logic
- Improve Readability

---

### Step 5 — Validate Against Rules

Ensure the refactored code satisfies

- [ ] `CA-xxx` — Layer boundaries maintained
- [ ] `STRUCTURE-xxx` — Package structure correct
- [ ] `JAVA-xxx` — Java coding standards met
- [ ] `NAME-xxx` — Naming conventions followed
- [ ] `PERSIST-xxx` — Persistence rules satisfied (if applicable)
- [ ] Behavior unchanged

---

## Human Approval Gate

STOP immediately if refactoring requires

- Business Logic Changes
- API Contract Changes
- Database Changes
- Service Split
- Service Merge
- New Infrastructure
- New Dependencies

Explain the impact.

Wait for user approval.

---

## Output

- Refactored Code
- Refactoring Summary (with rule IDs that motivated each change)
- Risks
- Remaining Issues

---

## Validation Checklist

- [ ] Rules loaded before refactoring
- [ ] Business behavior preserved
- [ ] Architecture rules (`CA-xxx`) followed
- [ ] Structure rules (`STRUCTURE-xxx`) followed
- [ ] Java rules (`JAVA-xxx`) followed
- [ ] Naming rules (`NAME-xxx`) followed
- [ ] Readability improved
- [ ] Maintainability improved
- [ ] No new bugs introduced
- [ ] No unauthorized changes