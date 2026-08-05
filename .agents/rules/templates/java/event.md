---
trigger: always_on
---

# Event Template

## Purpose

Generate domain or integration events for asynchronous communication.

---

## Template

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XxxEvent {

    private UUID id;

}
```

---

# Required Members

- Event Name
- Event Payload
- Event Metadata
- Timestamp

---

# Standard Responsibilities

- Represent business events
- Transfer event data
- Enable asynchronous communication
- Decouple services

---

# Rules

## MUST

### EVENT-001

Events should be immutable whenever possible.

### EVENT-002

Include only required event data.

### EVENT-003

Use meaningful event names.

### EVENT-004

Keep events serializable.

### EVENT-005

Include metadata when necessary.

### EVENT-006

Represent facts that already happened.

---

## MUST NOT

### EVENT-007

Do not implement business logic.

### EVENT-008

Do not reference repositories or services.

### EVENT-009

Do not expose entities directly.

### EVENT-010

Do not include unnecessary data.

---

# Checklist

- [ ] Immutable
- [ ] Serializable
- [ ] Minimal Payload
- [ ] Meaningful Name
- [ ] No Business Logic