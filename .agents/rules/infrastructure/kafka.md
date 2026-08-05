---
trigger: always_on
---

# Kafka

## Purpose

Define mandatory standards for using Apache Kafka.

---

## Scope

Applies to all Kafka producers and consumers.

---

# Rules

## MUST

### KAFKA-001

Use meaningful topic names.

### KAFKA-002

Use message keys when ordering is required.

### KAFKA-003

Handle retries explicitly.

### KAFKA-004

Use Dead Letter Queue for failed messages.

### KAFKA-005

Consumers MUST be idempotent.

### KAFKA-006

Monitor consumer lag.

### KAFKA-007

Use Schema Registry for shared events.

### KAFKA-011

Configuration classes defining custom Kafka listener container factories MUST include `@EnableKafka` to ensure Spring Kafka scans and activates `@KafkaListener` consumer beans.

### KAFKA-012

Event DTO classes passed through Kafka topics SHOULD use standard Java classes (with Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) rather than plain Java Records to prevent Jackson deserialization issues when header-less `JsonDeserializer` is configured.

---

## MUST NOT

### KAFKA-008

Do not send large messages.

### KAFKA-009

Do not rely on message ordering across partitions.

### KAFKA-010

Do not lose failed messages.

---

# Anti-patterns

- Huge Messages
- Missing DLQ
- Non-idempotent Consumer
- Infinite Retry

---

# Checklist

- [ ] Topic
- [ ] Key
- [ ] Retry
- [ ] DLQ
- [ ] Idempotent Consumer
- [ ] Monitoring

---

# References

- Apache Kafka Documentation
- Confluent Documentation