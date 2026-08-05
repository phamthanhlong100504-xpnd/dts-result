---
trigger: always_on
---

# Performance

## Purpose

Define mandatory standards for building performant, scalable, and efficient systems.

---

## Scope

Applies to all applications, services, APIs, databases, and background jobs.

---

## Principles

- Measure Before Optimize
- Performance by Design
- Scalability
- Efficiency
- Predictability

---

# Rules

## MUST

### PERF-001

Measure performance before applying optimizations.

### PERF-002

Define performance objectives for critical operations.

### PERF-003

Optimize database queries.

### PERF-004

Limit unnecessary network calls.

### PERF-005

Release resources promptly.

### PERF-006

Use pagination for large datasets.

### PERF-007

Use indexes for frequently queried columns.

### PERF-008

Avoid N+1 query problems.

### PERF-009

Configure request timeouts.

### PERF-010

Configure connection timeouts.

### PERF-011

Configure retry policies carefully.

### PERF-012

Use connection pooling.

### PERF-013

Use asynchronous processing for long-running tasks.

### PERF-014

Implement caching only when justified.

### PERF-015

Monitor resource consumption continuously.

### PERF-016

Limit memory usage.

### PERF-017

Minimize disk I/O.

### PERF-018

Minimize serialization overhead.

### PERF-019

Keep transactions short.

### PERF-020

Benchmark critical components.

---

## MUST NOT

### PERF-021

Do not optimize without evidence.

### PERF-022

Do not load unnecessary data.

### PERF-023

Do not query inside loops.

### PERF-024

Do not block threads unnecessarily.

### PERF-025

Do not create excessive objects.

### PERF-026

Do not cache everything.

### PERF-027

Do not keep unnecessary long-lived connections.

### PERF-028

Do not perform expensive work synchronously when asynchronous execution is appropriate.

---

## SHOULD

### PERF-029

Batch operations when possible.

### PERF-030

Reuse expensive resources.

### PERF-031

Compress large payloads.

### PERF-032

Stream large responses.

### PERF-033

Apply lazy loading carefully.

### PERF-034

Profile performance regularly.

---

## MAY

### PERF-035

Use CDN.

### PERF-036

Use distributed cache.

### PERF-037

Use read replicas.

---

# Anti-patterns

- Premature Optimization
- N+1 Query
- Full Table Scan
- Memory Leak
- Blocking I/O
- Excessive Allocation
- Cache Abuse
- Long Transaction
- Chatty API
- Synchronous Bottleneck

---

# Checklist

- [ ] Performance Measured
- [ ] Query Optimized
- [ ] Index Applied
- [ ] Pagination
- [ ] Connection Pooling
- [ ] Timeout Configured
- [ ] Retry Configured
- [ ] Memory Reviewed
- [ ] Cache Reviewed
- [ ] Load Tested

---

# References

- Google SRE Book
- Designing Data-Intensive Applications
- PostgreSQL Documentation
- OpenTelemetry Documentation