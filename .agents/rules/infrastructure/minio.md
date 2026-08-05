---
trigger: always_on
---

# MinIO

## Purpose

Define mandatory standards for object storage using MinIO.

---

## Scope

Applies to all services using MinIO.

---

# Rules

## MUST

### MINIO-001

Store only object files.

### MINIO-002

Use unique object keys.

### MINIO-003

Validate uploaded files.

### MINIO-004

Set appropriate bucket policies.

### MINIO-005

Use server-side encryption when required.

### MINIO-006

Store metadata separately when appropriate.

---

## MUST NOT

### MINIO-007

Do not store business data inside objects.

### MINIO-008

Do not expose private buckets publicly.

### MINIO-009

Do not trust uploaded filenames.

---

# Anti-patterns

- Public Bucket
- Duplicate Objects
- Missing Validation
- Business Data in Object Storage

---

# Checklist

- [ ] Unique Object Key
- [ ] Validation
- [ ] Bucket Policy
- [ ] Encryption
- [ ] Metadata

---

# References

- MinIO Documentation
- Amazon S3 Best Practices