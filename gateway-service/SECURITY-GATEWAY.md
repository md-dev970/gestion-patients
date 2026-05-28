# Security Gateway Overview

`gateway-service` is the security gateway and reverse proxy for the hospital patient microservices platform.

## Role

The gateway is the only client-facing API entry point. Clients call:

```text
http://localhost:8080
```

or, when TLS is enabled:

```text
https://localhost:8080
```

The gateway routes requests to backend services through Eureka using `lb://` routes.

## Responsibilities

- Route API requests to backend microservices.
- Verify JWT bearer tokens.
- Forward identity headers to downstream services.
- Apply RBAC before proxying sensitive requests.
- Enforce per-IP and per-user rate limits.
- Block repeated failed login attempts by IP.
- Reject SQLi/XSS-like query and header values.
- Add secure response headers.
- Emit audit and IDS events.
- Support HTTPS/TLS for client-facing traffic.

## Runtime Stack

| Component | Value |
|-----------|-------|
| Framework | Spring Cloud Gateway |
| Port | `8080` |
| Discovery | Eureka via `discovery-service` |
| Docker network | `hospital-network` |
| Main config | `src/main/resources/application.yml` |

## Routes

| Public Path | Backend |
|-------------|---------|
| `/api/auth/**` | `lb://auth-service` |
| `/api/patients/**` | `lb://patient-service` |
| `/api/staff/**` | `lb://staff-service` |
| `/api/appointments/**` | `lb://appointment-service` |
| `/api/medical-records/**` | `lb://medical-record-service` |
| `/api/consultations/**` | `lb://consultations-service` |

## Public Paths

These paths do not require a bearer token:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/refresh`
- `/actuator/health`

All other API paths require:

```http
Authorization: Bearer <accessToken>
```

## Verifying The Gateway

Start the stack:

```bash
docker-compose up -d --build
```

Check gateway health:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health
# Expected: 200
```

Check that protected traffic is intercepted by the gateway:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/patients/1
# Expected: 401 without a token
```

Check a suspicious input rejection:

```bash
curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/patients/search?query=OR%201=1"
# Expected: 400
```

## Security Event Flow

When `security.audit.url` is configured, the gateway sends:

- `ACCESS_DENIED`
- `RATE_LIMIT_EXCEEDED`
- `SUSPICIOUS_INPUT`
- `PATIENT_SELF_DELETION_REQUESTED`

When `security.ids.url` is configured, the gateway also sends IDS-oriented events:

- `RATE_LIMIT_EXCEEDED`
- `SUSPICIOUS_INPUT`

See [../AUDIT-IDS-EVENTS.md](../AUDIT-IDS-EVENTS.md).

## More Detail

For the complete runbook, TLS setup, JWT algorithms, RBAC matrix, rate limiting, validation, headers, and tests, see [GATEWAY-HTTPS.md](GATEWAY-HTTPS.md).
