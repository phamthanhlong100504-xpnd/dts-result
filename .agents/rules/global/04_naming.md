---
trigger: always_on
---

# Naming

## Purpose

Define mandatory naming conventions across the entire system.

---

## Scope

Applies to source code, APIs, databases, infrastructure, repositories, and documentation.

---

## Principles

- Consistency
- Readability
- Explicitness
- Predictability
- Simplicity

---

# Rules

## MUST

### NAME-001

Use meaningful names.

### NAME-002

Names MUST describe intent.

### NAME-003

Use consistent terminology across the system.

### NAME-004

Use the domain language.

### NAME-005

Avoid abbreviations unless universally accepted.

### NAME-006

Keep names concise.

### NAME-007

Use singular names for classes.

### NAME-008

Use plural names for collections.

### NAME-009

Database tables MUST use snake_case.

### NAME-010

Database columns MUST use snake_case.

### NAME-011

REST resources MUST use lowercase plural nouns.

### NAME-012

Repository names MUST end with `Repository`.

### NAME-013

Service names MUST end with `Service`.

### NAME-014

Controller names MUST end with `Controller`.

### NAME-015

Exception names MUST end with `Exception`.

### NAME-016

DTO names MUST clearly indicate their purpose.

### NAME-017

Enum names MUST represent business concepts.

### NAME-018

Boolean variables MUST read naturally.

### NAME-019

Constants MUST use UPPER_SNAKE_CASE.

### NAME-020

Packages MUST use lowercase.

---

## MUST NOT

### NAME-021

Do not use Hungarian notation.

### NAME-022

Do not use meaningless names.

Bad

```
a
b
obj
temp
data
```

### NAME-023

Do not encode types into names.

### NAME-024

Do not use inconsistent abbreviations.

### NAME-025

Do not mix naming conventions.

### NAME-026

Do not use reserved keywords.

---

## SHOULD

### NAME-027

Prefer business terminology.

### NAME-028

Prefer complete words.

### NAME-029

Keep identifiers stable.

### NAME-030

Align names across services.

---

## MAY

### NAME-031

Use common abbreviations.

Examples

```
id
url
uri
uuid
http
json
xml
dto
api
sql
jwt
```

---

# Examples

Good

```
UserService

UserRepository

UserController

CreateUserRequest

UserResponse

MediaFile

OrderItem

PaymentStatus
```

Bad

```
Manager

Helper

Util

Common

Data

Info

Object

Bean

Temp

Misc
```

---

# Boolean Naming

Good

```
is_active

is_enabled

has_permission

can_publish

should_retry
```

Bad

```
flag

status

check

value
```

---

# Method Naming

Good

```
findById

createUser

updateProfile

deleteMedia

publishEvent

calculateScore
```

Bad

```
doStuff

handle

process

run

executeLogic

testMethod
```

---

# REST Naming

Good

```
/users

/orders

/media-files

/exam-sessions
```

Bad

```
/getUsers

/createUser

/doLogin

/deleteOrder
```

---

# Anti-patterns

- Generic Names
- Ambiguous Names
- Type Prefixes
- Inconsistent Naming
- Mixed Languages
- Magic Abbreviations
- Utility Everything

---

# Checklist

- [ ] Meaningful
- [ ] Consistent
- [ ] Domain Language
- [ ] No Abbreviation
- [ ] Readable
- [ ] Predictable
- [ ] Correct Convention
- [ ] Business Focused

---

# References

- Google Java Style Guide
- Clean Code
- Domain-Driven Design
- Microsoft Naming Guidelines