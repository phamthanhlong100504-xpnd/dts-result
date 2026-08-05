---
trigger: always_on
---

# Structure Template

## Purpose

Generate projects following the standard package structure and architectural boundaries.

---

# Standard Project Structure

```text
src/main/java
└── {group}.{artifact}          ← Xác định từ build.gradle/pom.xml
    ├── api
    │   ├── controller
    │   ├── form
    │   ├── response
    │   └── view
    │
    ├── application
    │   ├── dto
    │   ├── enums
    │   ├── exception
    │   ├── model
    │   ├── service
    │   ├── utils
    │   └── Constants.java
    │
    ├── config
    │
    ├── domain
    │   ├── entity
    │   └── repository
    │
    ├── infrastructure
    │   ├── configuration
    │   └── utils
    │
    └── XxxApplication.java
```

> **Cách xác định `{group}.{artifact}`**:
> - Gradle: `group` trong `build.gradle` + `rootProject.name` trong `settings.gradle`
> - Maven: `groupId` + `artifactId` trong `pom.xml`
> - Ví dụ: `group = 'com.dts'`, `rootProject.name = 'media'` → base package = `com.dts.media`

---

# Package Responsibilities

## api

Responsible for exposing application interfaces.

Contains:

- Controller
- Request(Form)
- Response
- View

Responsibilities

- Receive HTTP requests
- Validate input
- Return API responses
- Delegate business logic

---

## application

Responsible for application use cases.

Contains

- Service
- DTO
- Model
- Exception
- Enum
- Utility
- Constants

Responsibilities

- Implement business logic
- Coordinate repositories
- Publish events
- Execute use cases

---

## domain

Responsible for domain models.

Contains

- Entity
- Repository

Responsibilities

- Business entities
- Repository contracts
- Domain model

---

## infrastructure

Responsible for technical implementation.

Contains

- Configuration
- Infrastructure utilities

Responsibilities

- Spring Configuration
- External libraries
- Technical concerns

---

## config

Responsible for application configuration.

Contains

- Security
- Swagger
- WebMvc
- Jackson
- Bean Configuration

---

# Dependency Rules

## Allowed

```text
api
    ↓
application
    ↓
domain

application
    ↓
infrastructure

config
    ↓
application
```

---

## Forbidden

### STRUCTURE-001

api → repository

### STRUCTURE-002

api → entity

### STRUCTURE-003

repository → controller

### STRUCTURE-004

entity → service

### STRUCTURE-005

entity → controller

### STRUCTURE-006

controller → database

### STRUCTURE-007

controller → kafka

### STRUCTURE-008

controller → redis

### STRUCTURE-009

response → entity

### STRUCTURE-010

request → entity

---

# Package Naming

Use lowercase package names.

Examples

```text
controller

service

repository

entity

mapper

validator

configuration
```

---

# Class Naming

Controller

```
UserController
```

Service

```
UserService
```

Repository

```
UserRepository
```

Entity

```
UserEntity
```

Mapper

```
UserMapper
```

Validator

```
UserValidator
```

Configuration

```
RedisConfiguration
```

Producer

```
UserProducer
```

Consumer

```
UserConsumer
```

---

# Rules

## MUST

### STRUCTURE-011

Separate API, Business, Domain and Infrastructure layers.

### STRUCTURE-012

Each class has a single responsibility.

### STRUCTURE-013

Follow package conventions.

### STRUCTURE-014

Keep dependencies one-directional.

### STRUCTURE-015

Keep infrastructure independent from business logic.

### STRUCTURE-016

Business logic belongs only to Service.

### STRUCTURE-017

Persistence belongs only to Repository.

### STRUCTURE-018

HTTP logic belongs only to Controller.

### STRUCTURE-019

Object conversion belongs only to Mapper.

---

## MUST NOT

### STRUCTURE-020

Do not create cyclic dependencies.

### STRUCTURE-021

Do not place business logic inside Controllers.

### STRUCTURE-022

Do not place business logic inside Entities.

### STRUCTURE-023

Do not access repositories from Controllers.

### STRUCTURE-024

Do not expose Entities through APIs.

### STRUCTURE-025

Do not mix infrastructure code with business logic.

### STRUCTURE-026

MUST read `build.gradle` (or `pom.xml`) and `settings.gradle` to determine the base package BEFORE generating any code. The base package is `{group}.{artifact}` (e.g., `com.dts.media`).

### STRUCTURE-027

MUST NOT use `com.example` as the base package. Always derive the real base package from the project build file.

---

# Checklist

- [ ] Standard Package Structure
- [ ] Layer Separation
- [ ] Dependency Direction
- [ ] Single Responsibility
- [ ] No Cyclic Dependency
- [ ] No Business Logic in Controller
- [ ] No Business Logic in Entity
- [ ] Repository for Persistence Only
- [ ] Service for Business Logic Only
- [ ] Mapper for Object Conversion Only