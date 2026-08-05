---
trigger: always_on
---

# API

## Purpose

Define mandatory standards for designing, implementing, and maintaining HTTP APIs.

---

## Scope

Applies to all public, internal, and service-to-service APIs.

---

## Principles

- Consistency
- Predictability
- Simplicity
- Backward Compatibility
- Explicit Contracts
- Stateless Communication

---

# Rules

## MUST

### API-001

Use REST as the default API style.

### API-002

Use HTTP methods according to their semantic meaning.

### API-003

Use nouns for resource names.

### API-004

Use plural resource names.

### API-005

Version every public API.

### API-006

Return appropriate HTTP status codes.

### API-007

Validate every client request.

### API-008

Return a consistent response format.

### API-009

Return machine-readable error responses.

### API-010

Document every endpoint.

### API-011

Use UTC for all timestamps.

### API-012

Use ISO-8601 date and time format.

### API-013

Support pagination for collection resources.

### API-014

Support filtering using query parameters.

### API-015

Support sorting using query parameters.

### API-016

Support idempotency where applicable.

### API-017

Protect every endpoint using authentication unless explicitly public.

### API-018

Use HTTPS only.

### API-019

Return explicit validation errors.

### API-020

Treat API contracts as backward compatible.

---

## MUST NOT

### API-021

Do not expose database entities.

### API-022

Do not expose internal exceptions.

### API-023

Do not leak stack traces.

### API-024

Do not return inconsistent response structures.

### API-025

Do not use verbs in resource names.

Bad

```
GET /getUsers
POST /createOrder
```

Good

```
GET /api/v1/user-service/users
POST /api/v1/order-service/orders
```

---

### API-026

Do not change existing response contracts without versioning.

### API-027

Do not use HTTP 200 for business failures.

### API-028

Do not place business logic inside controllers.

---

## SHOULD

### API-029

Keep endpoints resource-oriented.

### API-030

Keep URLs short.

### API-031

Prefer PATCH over PUT for partial updates.

### API-032

Use OpenAPI.

### API-033

Support request tracing.

### API-034

Support request id propagation.

### API-035

Keep responses deterministic.

### API-036

Prefer cursor pagination for large datasets.

---

## MAY

### API-037

Support HATEOAS when required.

### API-038

Support GraphQL where REST is insufficient.

### API-039

Support gRPC for internal communication.

---

# Resource Naming

All API endpoints MUST follow the format: `/api/{version}/{service}/{object}`

Good

```
/api/v1/user-service/users
/api/v1/order-service/orders
/api/v1/media-service/media
/api/v1/content-builder/questions
/api/v1/content-builder/learning-programs
```

Bad

```
/getUser
/deleteUser
/createExam
/doLogin
```

---

# HTTP Methods

GET

Read resources.

POST

Create resources.

PUT

Replace resources.

PATCH

Partial update.

DELETE

Remove resources.

---

# Status Codes

200 OK

Successful request.

201 Created

Resource created.

202 Accepted

Async accepted.

204 No Content

Successful without body.

400 Bad Request

Client request invalid.

401 Unauthorized

Authentication required.

403 Forbidden

Permission denied.

404 Not Found

Resource not found.

409 Conflict

Resource conflict.

422 Unprocessable Entity

Validation failed.

429 Too Many Requests

Rate limit exceeded.

500 Internal Server Error

Unexpected server error.

503 Service Unavailable

Temporary unavailable.

---

# Pagination

Preferred

```
GET /users?page=1&size=20
```

or

```
GET /users?cursor=abc123
```

---

# Filtering

Preferred

```
GET /users?status=ACTIVE
```

---

# Sorting

Preferred

```
GET /users?sort=name,asc
```

---

# Versioning

Preferred

```
/api/v1/content-builder/questions
```

---

# Error Response

Preferred

```
{
  "code": "...",
  "message": "...",
  "details": [],
  "traceId": "..."
}
```

---

# Anti-patterns

- RPC Style APIs
- Inconsistent URLs
- Mixed Naming Conventions
- Leaking Internal Models
- Returning Stack Traces
- Returning HTML Errors
- Business Logic in Controller
- Missing Validation
- Missing Versioning
- Missing Documentation
- Breaking Backward Compatibility

---

# Checklist

- [ ] RESTful
- [ ] Versioned
- [ ] Documented
- [ ] Authentication
- [ ] Authorization
- [ ] Validation
- [ ] Consistent Response
- [ ] Consistent Error
- [ ] Pagination
- [ ] Filtering
- [ ] Sorting
- [ ] Trace ID
- [ ] Proper Status Code
- [ ] Backward Compatible

---

# References

- RFC 9110 HTTP Semantics
- RFC 7807 Problem Details
- Microsoft REST API Guidelines
- Zalando RESTful API Guidelines
- OpenAPI Specification