---
description: Determine and load the minimum required rule set before performing any task.
---

# Select Rules Workflow

## Purpose

Determine and load the minimum required rule set before performing any task.

Prevent unnecessary context loading and ensure all subsequent workflows follow the correct standards.

---

## Input

- User Request
- Business Requirement
- Existing Project (Optional)

---

## Preconditions

- User request is clearly identified.
- Project language and framework are known.
- Target task can be classified.

If any of the above cannot be determined,

STOP.

Ask the user for clarification.

---

## Steps

### Step 1 — Identify Task

Identify the primary task.

Examples

- Read Code
- Understand Requirement
- Generate Blueprint
- Generate Code
- Review Code
- Refactor Code
- Debug Issue
- Generate Test
- Generate Documentation
- Design Architecture
- Feature Development
- Bug Fix

---

### Step 2 — Identify Technology

Determine the project technology.

Examples

- Java Spring Boot

If the technology cannot be identified,

STOP.

Ask the user.

---

### Step 3 — Load Global Rules (Always)

Read ALL files in `rules/global/` directory.

These files define the baseline standards for every task.

Files

- `rules/global/01_architecture.md`
- `rules/global/02_api.md`
- `rules/global/03_database.md`
- `rules/global/04_naming.md`
- `rules/global/05_git.md`
- `rules/global/06_security.md`
- `rules/global/07_logging.md`
- `rules/global/08_error_handling.md`
- `rules/global/09_documentation.md`
- `rules/global/10_testing.md`
- `rules/global/11_performance.md`
- `rules/global/12_observability.md`

---

### Step 4 — Load Task-Specific Rules

Based on the identified task, read the corresponding rule files.

Use the mapping table below.

#### Task → Rule Files Mapping

| Task | Rule Files to Read |
|---|---|
| Read Code | `rules/architecture/clean-architecture.md`, `rules/templates/java/structure.md` |
| Understand Requirement | `rules/global/02_api.md`, `rules/global/03_database.md`, `rules/global/04_naming.md` |
| Generate Blueprint | `rules/docx/java/api-blueprint-generator.md`, `rules/global/02_api.md` |
| Generate Code | `rules/architecture/clean-architecture.md`, `rules/templates/java/structure.md`, `rules/templates/java/*.md` (relevant templates), `rules/technology/java/core/java.md`, `rules/technology/java/core/oop.md`, `rules/technology/java/persistence/persistence.md`, `rules/technology/java/persistence/migration.md`, `rules/technology/java/spring/*.md`, `rules/technology/java/libraries/libraries.md`, `rules/technology/java/build/build.md`, `rules/global/04_naming.md` |
| Review Code | `rules/architecture/clean-architecture.md`, `rules/templates/java/structure.md`, `rules/technology/java/core/java.md`, `rules/technology/java/persistence/persistence.md`, `rules/global/04_naming.md` |
| Refactor Code | `rules/architecture/clean-architecture.md`, `rules/templates/java/structure.md`, `rules/technology/java/core/java.md`, `rules/global/04_naming.md` |
| Debug Issue | `rules/technology/java/core/java.md`, `rules/technology/java/persistence/persistence.md`, `rules/global/08_error_handling.md` |
| Generate Test | `rules/technology/java/testing/testing.md`, `rules/global/10_testing.md` |
| Generate Documentation | `rules/global/09_documentation.md`, `rules/docx/java/api-blueprint-generator.md` |
| Design Architecture | `rules/architecture/clean-architecture.md`, `rules/architecture/ddd.md`, `rules/architecture/event-driven.md`, `rules/architecture/microservice.md`, `rules/architecture/modular-monolith.md`, `rules/global/01_architecture.md` |
| Feature Development | ALL rules from Generate Blueprint + Generate Code + Generate Test + Review Code |
| Bug Fix | `rules/global/08_error_handling.md`, `rules/technology/java/core/java.md`, `rules/technology/java/persistence/persistence.md`, `rules/architecture/clean-architecture.md`, `rules/templates/java/structure.md` |

---

### Step 5 — Load Infrastructure Rules (Only If Required)

Read infrastructure rule files ONLY if the task involves the corresponding technology.

| Technology | Rule File |
|---|---|
| PostgreSQL | `rules/infrastructure/postgres.md` |
| MySQL | `rules/infrastructure/mysql.md` |
| MongoDB | `rules/infrastructure/mongodb.md` |
| Redis | `rules/infrastructure/redis.md` |
| Kafka | `rules/infrastructure/kafka.md` |
| RabbitMQ | `rules/infrastructure/rabbitmq.md` |
| Docker | `rules/infrastructure/docker.md` |
| Kubernetes | `rules/infrastructure/kubernetes.md` |
| Nginx | `rules/infrastructure/nginx.md` |
| MinIO | `rules/infrastructure/minio.md` |
| Prometheus | `rules/infrastructure/prometheus.md` |
| Grafana | `rules/infrastructure/grafana.md` |

Do not load unrelated infrastructure rules.

---

### Step 6 — Validate Rule Set

Remove duplicated rules.

Ensure every required rule file exists and was read successfully.

If a rule file is missing, report to the user.

---

## Human Approval Gate

If the task requires assumptions about

- Architecture
- Technology
- Framework
- Business Logic

STOP.

Ask the user before continuing.

Never guess.

---

## Output

- Selected Rule Files (with paths)
- Loading Order
- Missing Rules (if any)

---

## Validation Checklist

- [ ] Task identified
- [ ] Technology identified
- [ ] Global rules read
- [ ] Task-specific rules read
- [ ] Infrastructure rules loaded only if needed
- [ ] No duplicated rules
- [ ] No missing rule files
- [ ] User confirmation obtained when necessary