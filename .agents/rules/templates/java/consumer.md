---
trigger: always_on
---

# Consumer Template

## Purpose

Generate message consumers for asynchronous event processing.

---

## Template

```java
@Component
@RequiredArgsConstructor
public class XxxConsumer {

    private final XxxService xxxService;

    @KafkaListener(topics = "xxx-topic")
    public void consume(XxxEvent event) {

    }

}
```

---

# Required Members

- Message Listener
- Service Dependency
- Event Handling
- Logging
- Error Handling

---

# Standard Responsibilities

- Receive messages
- Validate incoming events
- Delegate processing to services
- Handle processing failures
- Log important events

---

# Rules

## MUST

### CONSUMER-001

Use the appropriate message listener annotation.

### CONSUMER-002

Delegate business logic to services.

### CONSUMER-003

Process messages asynchronously.

### CONSUMER-004

Handle duplicate messages safely (Idempotent).

### CONSUMER-005

Log message processing results.

### CONSUMER-006

Handle failures using retry or dead-letter mechanisms when supported.

### CONSUMER-007

Validate incoming events before processing.

---

## MUST NOT

### CONSUMER-008

Do not access repositories directly.

### CONSUMER-009

Do not implement complex business logic.

### CONSUMER-010

Do not block processing with unnecessary operations.

### CONSUMER-011

Do not silently ignore processing failures.

---

# Checklist

- [ ] Listener Configured
- [ ] Event Validated
- [ ] Service Only
- [ ] Idempotent
- [ ] Retry Strategy
- [ ] Logging