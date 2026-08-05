---
description: Generate Java Spring Boot source code from an approved API Blueprint.
---

# Generate Code Workflow

## Purpose

Generate Java Spring Boot source code from an approved API Blueprint.

The generated code must strictly follow all project rules and templates.

---

## Input

- API Blueprint
- Existing Project
- Existing Code (Optional)

---

## Preconditions

- API Blueprint has been approved.
- Project architecture is understood.
- Required rules have been loaded.

If the blueprint is incomplete,

STOP.

Ask the user.

---

## Rule Files

### Read Before Starting

- `rules/architecture/clean-architecture.md`
- `rules/templates/java/structure.md`
- `rules/templates/java/package.md`
- `rules/technology/java/core/java.md`
- `rules/technology/java/core/oop.md`
- `rules/technology/java/persistence/persistence.md`
- `rules/technology/java/persistence/migration.md`
- `rules/technology/java/spring/spring-boot.md`
- `rules/technology/java/spring/spring-web.md`
- `rules/technology/java/spring/spring-data.md`
- `rules/technology/java/spring/spring-security.md`
- `rules/technology/java/libraries/libraries.md`
- `rules/technology/java/build/build.md`
- `rules/global/04_naming.md`
- `rules/global/06_security.md`
- `rules/global/07_logging.md`
- `rules/global/08_error_handling.md`

### Read Per Component (only the templates needed)

| Component | Template File |
|---|---|
| Controller | `rules/templates/java/controller.md` |
| Service | `rules/templates/java/service.md` |
| Repository | `rules/templates/java/repository.md` |
| Entity | `rules/templates/java/entity.md` |
| Mapper | `rules/templates/java/mapper.md` |
| Validator | `rules/templates/java/validator.md` |
| Request | `rules/templates/java/request.md` |
| Response | `rules/templates/java/response.md` |
| Exception | `rules/templates/java/exception.md` |
| Configuration | `rules/templates/java/configuration.md` |
| Filter | `rules/templates/java/filter.md` |
| Interceptor | `rules/templates/java/interceptor.md` |
| Event | `rules/templates/java/event.md` |
| Producer | `rules/templates/java/producer.md` |
| Consumer | `rules/templates/java/consumer.md` |
| Scheduler | `rules/templates/java/scheduler.md` |

### Read If Related

- Infrastructure rules matching the project dependencies

---

## Steps

### Step 0 — Load Rules

Read all rule files listed in the Rule Files section above.

Before writing any code, understand

- Layer boundaries from `clean-architecture.md` (rules `CA-001` to `CA-044`)
- Package structure from `structure.md` (rules `STRUCTURE-001` to `STRUCTURE-027`)
- Java coding standards from `java.md` (rules `JAVA-001` to `JAVA-030`)
- Persistence standards from `persistence.md` (rules `PERSIST-001` to `PERSIST-028a`)
- Naming conventions from `04_naming.md` (rules `NAME-001` to `NAME-031`)

---

### Step 0.5 — Determine Base Package (MANDATORY)

Before generating ANY code, determine the project's base package:

1. Read `build.gradle` → extract `group` (e.g., `com.dts`)
2. Read `settings.gradle` → extract `rootProject.name` (e.g., `media`)
3. Base package = `{group}.{rootProject.name}` (e.g., `com.dts.media`)

If `build.gradle` is not found, read `pom.xml` → extract `groupId` + `artifactId`.

**NEVER use `com.example` as the base package.** (rule `STRUCTURE-027`)

Verify by checking the existing main application class location under `src/main/java/`.



### Step 1 — Review API Blueprint

Understand

- Business Flow
- API Contract
- Validation
- Data Interaction

---

### Step 2 — Determine Required Components

Generate only the necessary files.

For each component, read the corresponding template from the table above BEFORE generating code.

Examples

- Controller → read `rules/templates/java/controller.md` first
- Service → read `rules/templates/java/service.md` first
- Repository → read `rules/templates/java/repository.md` first
- Entity → read `rules/templates/java/entity.md` first

---

### Step 3 — Generate Code

For each component, follow this sequence

1. Read the component template.
2. Write the code following the template structure.
3. Verify against applicable rules.

Rules to enforce during generation

- Package placement per `STRUCTURE-xxx`
- Class naming per `NAME-012` to `NAME-016`
- Dependency direction per `CA-002` (inward only)
- No business logic in Controller per `STRUCTURE-021`, `CA-010`
- No business logic in Entity per `STRUCTURE-022`
- No repository access from Controller per `STRUCTURE-023`, `CA-021`
- No entity exposure through API per `STRUCTURE-024`, `CA-022`
- Use `record` for DTOs per `JAVA-004`
- Use `final` fields per `JAVA-003`
- Use LAZY fetching per `PERSIST-006`
- Use UUID for aggregate primary keys per `PERSIST-001`
- No `@Data` on entities per `PERSIST-028a`
- Use `@Getter`, `@Setter` instead per `JAVA-030`
- Return empty collections not null per `JAVA-013`
- Throw specific exceptions per `JAVA-014`

---

### Step 4 — Generate Migration (If Needed)

If new database tables or columns are required

Read `rules/technology/java/persistence/migration.md` (rules `MIGRATION-001` to `MIGRATION-018`).

Ensure

- Each migration has a unique version per `MIGRATION-002`
- Each migration is focused on a single change per `MIGRATION-003`
- Migrations are deterministic per `MIGRATION-004`

---

### Step 5 — Validate Generated Code

Check each generated file against the rules

- [ ] Package placement matches `STRUCTURE-xxx`
- [ ] Layer dependencies are one-directional per `CA-002`
- [ ] Class naming follows `NAME-xxx`
- [ ] Controller is thin (no business logic) per `CA-010`
- [ ] Service contains business logic only per `STRUCTURE-016`
- [ ] Repository contains persistence only per `STRUCTURE-017`
- [ ] Entity uses `@Getter`/`@Setter` not `@Data` per `PERSIST-028a`
- [ ] Transactions applied for write operations per `PERSIST-008`
- [ ] Exception handling follows `08_error_handling.md`
- [ ] Logging follows `07_logging.md`

---

## Human Approval Gate

If code generation requires

- Creating new modules
- Creating new services
- Splitting existing services
- Merging services
- Database schema changes
- API contract changes
- Introducing Kafka
- Introducing Redis
- Introducing RabbitMQ
- Changing authentication
- Breaking backward compatibility

STOP.

Explain why.

Wait for user approval.

---

## Output

- Java Spring Boot Source Code (following all loaded rules and templates)

---

## Validation Checklist

- [ ] Rules loaded before code generation
- [ ] Component templates read before each component
- [ ] Architecture rules (`CA-xxx`) followed
- [ ] Structure rules (`STRUCTURE-xxx`) followed
- [ ] Java rules (`JAVA-xxx`) followed
- [ ] Persistence rules (`PERSIST-xxx`) followed
- [ ] Naming rules (`NAME-xxx`) followed
- [ ] Migration rules (`MIGRATION-xxx`) followed (if applicable)
- [ ] Error handling rules followed
- [ ] Logging rules followed
- [ ] No rule violations in generated code