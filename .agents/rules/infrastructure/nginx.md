---
trigger: always_on
---

# NGINX

## Purpose

Define mandatory standards for using NGINX.

---

## Scope

Applies to all NGINX servers and reverse proxies.

---

# Rules

## MUST

### NGINX-001

Use HTTPS for all public endpoints.

### NGINX-002

Enable HTTP compression when appropriate.

### NGINX-003

Configure request timeouts.

### NGINX-004

Forward client IP headers correctly.

### NGINX-005

Enable access and error logs.

### NGINX-006

Protect sensitive endpoints.

---

## MUST NOT

### NGINX-007

Do not expose internal services publicly.

### NGINX-008

Do not disable TLS verification unnecessarily.

### NGINX-009

Do not hardcode secrets in configuration.

---

# Anti-patterns

- Public Internal Service
- Missing TLS
- No Logging
- Unlimited Timeout

---

# Checklist

- [ ] HTTPS
- [ ] Logging
- [ ] Timeout
- [ ] Proxy Headers
- [ ] Security

---

# References

- NGINX Documentation