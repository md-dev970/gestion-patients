# Validation Schemas

This document defines the validation rules used by the platform and where they are enforced.

## Validation Layers

| Layer | Component | Scope | Failure Response |
|-------|-----------|-------|------------------|
| Gateway | `InputValidationFilter` | Query parameter values and header values except `Authorization` | `400 Bad Request` |
| Services | Bean Validation via `@Valid` | JSON bodies, DTO fields, some path/query parameters | `400 Bad Request` |

The gateway does not buffer or inspect request bodies. Body validation is handled inside each microservice.

## Gateway Injection Detection

The gateway scans:

- Query parameter values.
- Header values, excluding `Authorization`.

It rejects values that match SQL injection or XSS-like patterns.

| Category | Examples |
|----------|----------|
| `SQLI` | `OR 1=1`, `UNION SELECT`, `; DROP`, `INSERT INTO`, `DELETE FROM`, SQL comments, `exec(`, `char(` |
| `XSS` | `<script`, `</script>`, `javascript:`, `onerror=`, `onload=`, `<iframe`, `vbscript:`, `data:text/html` |

Implementation:

```text
gateway-service/src/main/java/com/hospital/gateway/validation/InjectionPatterns.java
```

Patterns are matched case-insensitively.

## Gateway Rejection Behavior

When a query or header value is suspicious:

- The gateway returns `400 Bad Request`.
- The response body is:

  ```json
  {"error":"Invalid or suspicious input"}
  ```

- The request is not proxied to the backend.
- A `SUSPICIOUS_INPUT` event is emitted when audit/IDS configuration is enabled.
- The response does not expose which exact pattern matched.
- The event does not include the raw input value.

## Configuration

| Property | Default | Purpose |
|----------|---------|---------|
| `input-validation.enabled` | `true` | Enables the gateway validation filter |
| `input-validation.excluded-paths` | `/actuator/health` | Path prefixes skipped by the filter |

Environment variable:

```text
INPUT_VALIDATION_ENABLED
```

## Service Structural Validation

Microservices use Bean Validation annotations on DTOs and controller arguments, such as:

- `@Valid`
- `@NotNull`
- `@NotBlank`
- `@Size`
- `@Email`
- `@Pattern`

Invalid JSON bodies or DTO fields are rejected by service-level exception handlers with `400 Bad Request`.

Examples:

- `PatientCreateRequest`
- `LoginRequest`
- `RegisterRequest`
- `AppointmentCreateRequest`
- `MedicalEntryDTO`
- `ConsultationCreateRequest`

## Filter Order

The gateway validation filter runs after rate limiting and before RBAC:

| Filter | Order |
|--------|-------|
| `AuthenticationFilter` | `-100` |
| `BruteforceByIpFilter` | `-92` |
| `RateLimitFilter` | `-90` |
| `InputValidationFilter` | `-80` |
| `RbacAuthorizationFilter` | `-50` |

This means invalid input is rejected before protected backend business logic is reached.
