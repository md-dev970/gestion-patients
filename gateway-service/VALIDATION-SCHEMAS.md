# Validation schemas (T1.7)

This document defines the **validation schemas** and the **middleware** that enforce them. Invalid requests are **rejected** (400 Bad Request or 422 when applicable) before or at the backend.

---

## 1. Schema types (équivalent JSON Schema)

### 1.1 Injection detection schema (gateway)

**Scope**: Query parameter values and header values (except `Authorization`).

**Definition**: A **blocklist** of pattern categories. Each string value is matched against compiled regex patterns; if any match, the request is considered **invalid**.

| Category | Description | Example patterns (conceptual) |
|----------|-------------|-------------------------------|
| **SQLI** | SQL injection–like sequences | `OR 1=1`, `UNION SELECT`, `; DROP`, `INSERT INTO`, `DELETE FROM`, `--`, `' OR '1'='1`, `exec(`, `char(`, etc. |
| **XSS** | Cross-site scripting–like sequences | `<script`, `</script>`, `javascript:`, `onerror=`, `onload=`, `onclick=`, `<iframe`, `vbscript:`, `data:text/html`, etc. |

**Implementation**: `InjectionPatterns` (gateway) holds the compiled patterns; see `com.hospital.gateway.validation.InjectionPatterns`. Patterns are applied case-insensitively. No PII or raw payload is logged; only category (SQLI/XSS), source (query/header), path, and method are sent to the audit/IDS.

**Rejection**: **400 Bad Request**, body `{"error":"Invalid or suspicious input"}`. Request is **not** proxied.

---

### 1.2 Structural validation schema (services)

**Scope**: Request **body** (JSON) and path/query parameters validated by each microservice.

**Definition**: **Bean Validation** (JSR 380) on DTOs: `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@Valid`, etc. Each service defines its own constraints on its request DTOs (e.g. `PatientCreateRequest`, `LoginRequest`).

**Implementation**: Controllers use `@Valid` on request bodies and parameters; `MethodArgumentNotValidException` is handled by `GlobalExceptionHandler` (or equivalent) in each service → **400 Bad Request** with validation error details (field-level errors can be returned to the client for form/API consumption).

**Rejection**: **400 Bad Request** (or **422 Unprocessable Entity** if the project standard reserves 422 for semantic/validation errors). Invalid requests are rejected by the service; the gateway does not inspect body content (no body buffering in gateway).

---

## 2. Validation middleware (T1.7)

| Layer | Component | Order | Applies schema | Rejection |
|-------|-----------|--------|----------------|-----------|
| **Gateway** | **InputValidationFilter** | -80 | Injection detection (query + headers) | 400, request not proxied |
| **Services** | `@Valid` + exception handlers | N/A | Structural (body, path, query) | 400/422 |

- **InputValidationFilter** is the gateway **validation middleware** that enforces the injection detection schema on every non-excluded request. It runs after `RateLimitFilter` (-90) and before `RbacAuthorizationFilter` (-50).
- **Excluded paths**: e.g. `/actuator/health`; configurable via `input-validation.excluded-paths`.
- **Configuration**: `input-validation.enabled` (default `true`), `input-validation.excluded-paths`. See `application.yml` and GATEWAY-HTTPS.md §7.

---

## 3. Rejet des requêtes invalides (T1.7)

- **Requêtes invalides** (query/header contenant des motifs SQLi/XSS) : rejetées par le **gateway** avec **400** ; la requête n’est pas transmise au backend.
- **Requêtes invalides** (body ou paramètres ne respectant pas les contraintes métier) : rejetées par le **service** avec **400** (ou **422**) après parsing et validation Bean Validation.

Aucune requête ne contourne la validation du gateway pour les entrées query/headers ; les bodies sont validés par les services.
