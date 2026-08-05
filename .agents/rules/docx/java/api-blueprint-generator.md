---
trigger: always_on
---

# API Blueprint Generation Rule

## Purpose

Generate a complete API Blueprint document from business requirements.

The generated blueprint serves as the single source of truth for implementing Java Spring Boot APIs.

The blueprint describes WHAT the API should do, not HOW it should be implemented.

---

# Output Format

Generate Markdown only.

Never generate Java code.

Never generate SQL.

Never generate OpenAPI.

Never generate implementation details.

---

# Required Sections

Every generated API Blueprint MUST contain the following sections in the exact order.

---

## Part 0 — Classification & Identity

Describe the API identity.

Required fields

- API Name
- API Type (Public / Internal)
- Module
- Feature
- Description
- Related Tables
- Related Services (if applicable)

---

## Part 1 — API Contract

### Endpoint

Describe

- HTTP Method
- URL
- Content Type

---

### Request

Describe every input.

Include

- Path Variables
- Query Parameters
- Headers
- Request Body

Every field must contain

- Name
- Type
- Required
- Description
- Validation Rules

---

### Response

Describe

- Success Status
- Response Body

Every response field must contain

- Name
- Type
- Description

---

### Error Codes

Every possible error must be listed.

Each error contains

- Error Code
- HTTP Status
- Business Meaning
- Client Message

---

## Part 2 — Processing Specification

Describe the complete business flow.

The flow must be sequential.

Use numbered steps.

Must include

### Controller Layer

Responsibilities

Input Validation

Request Mapping

---

### Service Layer

Business Rules

Business Workflow

Permission Validation

Transaction Boundary

---

### Repository Layer

Database Operations

Expected Queries

Persistence Operations

---

### External Interaction

Describe interactions with

- Kafka
- Redis
- REST APIs
- gRPC
- File Storage
- Notification
- Email

If none

Write

None

---

### Validation

Describe

- Request Validation
- Business Validation
- Permission Validation

---

## Part 3 — Data Interaction

Describe every database operation.

Each operation contains

- Operation Type

    - SELECT
    - INSERT
    - UPDATE
    - DELETE

- Target Table

- Conditions

- Expected Result

- Performance Notes (Optional)

---

## Part 4 — Operational Notes

Describe

- Idempotency
- Tenant Isolation
- Retry Strategy
- Audit Logging
- Monitoring
- Metrics
- Tracing

---

# Writing Rules

## MUST

### API-BLUEPRINT-001

Generate every required section.

### API-BLUEPRINT-002

Use the exact section order.

### API-BLUEPRINT-003

Describe business behavior instead of implementation.

### API-BLUEPRINT-004

Use numbered steps for processing flow.

### API-BLUEPRINT-005

List every possible validation rule.

### API-BLUEPRINT-006

List every possible business error.

### API-BLUEPRINT-007

Describe every database interaction.

### API-BLUEPRINT-008

Describe every external interaction.

### API-BLUEPRINT-009

Use implementation-independent language.

### API-BLUEPRINT-010

The document must be detailed enough that another AI can generate the complete Java Spring Boot implementation without additional clarification.

---

## MUST NOT

### API-BLUEPRINT-011

Do not generate Java code.

### API-BLUEPRINT-012

Do not generate SQL.

### API-BLUEPRINT-013

Do not generate Entity classes.

### API-BLUEPRINT-014

Do not generate Controller, Service or Repository implementations.

### API-BLUEPRINT-015

Do not skip sections.

### API-BLUEPRINT-016

Do not assume business rules that are not implied by the requirements.

### API-BLUEPRINT-017

Do not describe framework-specific implementation details.

---

# Missing Information

If the business requirements do not provide enough information for a section

DO NOT omit the section.

Instead write

- Not Specified
- Not Applicable

depending on the situation.

---

# Output Style

Use Markdown.

Use headings exactly as defined.

Use tables whenever appropriate.

Use numbered lists for processing flow.

Use bullet lists for business rules.

Keep the blueprint implementation-independent.

The blueprint should focus on business behavior, API contracts, validations, processing flow, data interactions, and operational requirements.

---

# Checklist

- [ ] Identity Completed
- [ ] API Contract Defined
- [ ] Request Specified
- [ ] Response Specified
- [ ] Error Codes Listed
- [ ] Processing Flow Completed
- [ ] Validation Rules Defined
- [ ] Data Interactions Listed
- [ ] External Interactions Listed
- [ ] Operational Notes Completed
- [ ] No Java Code
- [ ] No SQL
- [ ] No Implementation Details