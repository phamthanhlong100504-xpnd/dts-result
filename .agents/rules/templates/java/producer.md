---
trigger: always_on
---

# Producer Template

## Purpose

Generate message producers for publishing asynchronous events.

---

## Template

```java
@Component
@RequiredArgsConstructor
public class XxxProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(XxxEvent event) {

    }

}
```

---

# Required Members

- Message Publisher
- Event Payload
- Topic
- Logging

---

# Standard Responsibilities

- Publish events
- Serialize messages
- Log publishing results
- Delegate message delivery

---

# Rules

## MUST

### PRODUCER-001

Publish immutable events.

### PRODUCER-002

Publish only completed business events.

### PRODUCER-003

Use strongly typed event classes.

### PRODUCER-004

Log publish failures.

### PRODUCER-005

Keep producer lightweight.

---

## MUST NOT

### PRODUCER-006

Do not implement business logic.

### PRODUCER-007

Do not access repositories.

### PRODUCER-008

Do not call external APIs unrelated to messaging.

### PRODUCER-009

Do not modify event payloads before publishing.

---

# Checklist

- [ ] Event Published
- [ ] Logging
- [ ] Strongly Typed Event
- [ ] No Business Logic