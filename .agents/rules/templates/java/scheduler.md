---
trigger: always_on
---

# Scheduler Template

## Purpose

Generate scheduled jobs for background and recurring tasks.

---

## Template

```java
@Component
@RequiredArgsConstructor
public class XxxScheduler {

    @Scheduled(cron = "0 */5 * * * *")
    public void execute() {

    }

}
```

---

# Required Members

- @Scheduled
- Scheduling Strategy
- Logging
- Error Handling

---

# Standard Responsibilities

- Execute scheduled tasks
- Trigger recurring jobs
- Perform background processing
- Monitor scheduled execution

---

# Rules

## MUST

### SCHEDULER-001

Use `@Scheduled`.

### SCHEDULER-002

Keep scheduled jobs idempotent.

### SCHEDULER-003

Log execution results.

### SCHEDULER-004

Handle unexpected exceptions.

### SCHEDULER-005

Keep scheduled tasks lightweight.

### SCHEDULER-006

Move business logic to services.

---

## MUST NOT

### SCHEDULER-007

Do not implement complex business logic.

### SCHEDULER-008

Do not access repositories directly.

### SCHEDULER-009

Do not execute long-running blocking operations without proper control.

### SCHEDULER-010

Do not ignore execution failures.

---

# Checklist

- [ ] Scheduled Annotation
- [ ] Idempotent
- [ ] Logging
- [ ] Error Handling
- [ ] Service Delegation