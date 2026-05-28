# Gateway HTTPS, Routing, And Security Runbook

This document explains how `gateway-service` acts as the single API entry point, how to run it with HTTPS, and how its security filters behave.

## 1. Gateway Role

`gateway-service` is a Spring Cloud Gateway reverse proxy.

It is responsible for:

- Receiving all client API traffic.
- Routing requests to backend services through Eureka.
- Verifying JWT bearer tokens.
- Applying RBAC before proxying sensitive requests.
- Enforcing rate limits and login-abuse protection.
- Rejecting suspicious query/header values.
- Adding secure response headers.
- Emitting audit and IDS events.

Default URL:

```text
http://localhost:8080
```

HTTPS URL when TLS is enabled:

```text
https://localhost:8080
```

## 2. Routing

Routes are configured in `src/main/resources/application.yml`:

| Path | Backend |
|------|---------|
| `/api/auth/**` | `lb://auth-service` |
| `/api/patients/**` | `lb://patient-service` |
| `/api/staff/**` | `lb://staff-service` |
| `/api/appointments/**` | `lb://appointment-service` |
| `/api/medical-records/**` | `lb://medical-record-service` |
| `/api/consultations/**` | `lb://consultations-service` |

In Docker Compose, backend microservices are not exposed to the host. External clients reach the platform through the gateway only.

## 3. Basic Verification

Start the stack:

```bash
docker-compose up -d --build
```

Check gateway health:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health
# Expected: 200
```

Check that protected routes are intercepted:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/patients/1
# Expected: 401 without a token
```

## 4. HTTPS / TLS

TLS is configured with Spring Boot `server.ssl` properties:

```yaml
server:
  port: 8080
  ssl:
    enabled: ${SERVER_SSL_ENABLED:false}
    key-store: ${SERVER_SSL_KEY_STORE:}
    key-store-password: ${SERVER_SSL_KEY_STORE_PASSWORD:}
    key-store-type: ${SERVER_SSL_KEY_STORE_TYPE:PKCS12}
    key-alias: ${SERVER_SSL_KEY_ALIAS:gateway}
```

By default, `SERVER_SSL_ENABLED=false` and the gateway runs on HTTP.

### Development Keystore

Windows:

```powershell
cd gateway-service
.\scripts\generate-dev-keystore.bat
cd ..
```

Linux or macOS:

```bash
cd gateway-service
./scripts/generate-dev-keystore.sh
cd ..
```

Start with the TLS override:

```bash
docker-compose -f docker-compose.yml -f docker-compose.tls.yml up -d --build
```

Verify:

```bash
curl -k https://localhost:8080/actuator/health
# Expected: 200
```

Use `-k` only for local self-signed certificates.

### Production TLS

For production:

- Use a certificate issued by a trusted CA.
- Mount the keystore as a secret or secure volume.
- Set `SERVER_SSL_ENABLED=true`.
- Enable the TLS profile so HSTS is sent.
- Prefer port 443 behind a load balancer or reverse proxy.

Spring Cloud Gateway runs a single listener by default. If you need HTTP-to-HTTPS redirect on port 80, place a load balancer or reverse proxy in front of the gateway.

## 5. JWT Verification

Public paths do not require a token:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/refresh`
- `/actuator/health`

All other paths require:

```http
Authorization: Bearer <accessToken>
```

After successful verification, the gateway forwards:

```http
X-User-Id: <user id>
X-Username: <username>
X-User-Roles: ROLE_ADMIN,ROLE_DOCTOR
```

### Algorithms

| Mode | Use | Configuration |
|------|-----|---------------|
| RS256 | Recommended for production | `JWT_PRIVATE_KEY` or `JWT_PRIVATE_KEY_LOCATION` in auth service; `JWT_PUBLIC_KEY` or `JWT_PUBLIC_KEY_LOCATION` in gateway |
| HS256 | Development fallback | Same `JWT_SECRET` in auth service and gateway |

Generate development RS256 keys:

Windows:

```powershell
gateway-service\scripts\generate-jwt-rs256-keys.bat
```

Linux or macOS:

```bash
./gateway-service/scripts/generate-jwt-rs256-keys.sh
```

Do not commit private keys.

## 6. RBAC

RBAC is enforced by the gateway before the request is proxied.

Actions are derived from HTTP methods:

| Method | Action |
|--------|--------|
| GET | `READ` |
| POST | `CREATE` |
| PUT / PATCH | `UPDATE` |
| DELETE | `DELETE` |

Core policy:

| Resource | READ | CREATE | UPDATE | DELETE |
|----------|------|--------|--------|--------|
| Patients | Admin, doctors, nurses, receptionist, lab tech | Admin, doctors, nurses, receptionist | Admin, doctors, nurses | Admin; patient self-delete when own ID |
| Medical records | Admin, doctors, nurses, lab tech | Admin, doctors, nurses, lab tech | Admin, doctors, nurses | Admin |
| Consultations | Admin, doctors, nurses | Admin, doctors, nurses | Admin, doctors, nurses | Admin |
| Appointments | Admin, doctors, nurses, receptionist | Admin, doctors, nurses, receptionist | Admin, doctors, nurses, receptionist | Admin |

Role names include:

- `ROLE_ADMIN`
- `ROLE_MEDECIN`
- `ROLE_DOCTOR`
- `ROLE_INFIRMIER`
- `ROLE_NURSE`
- `ROLE_RECEPTIONIST`
- `ROLE_LAB_TECH`
- `ROLE_PATIENT`

Denied requests:

- Return `403 Forbidden`.
- Do not call the backend service.
- Emit `ACCESS_DENIED` when audit is configured.

## 7. Rate Limiting

The gateway applies in-memory rate limiting.

| Property | Default | Environment Variable |
|----------|---------|----------------------|
| `rate-limit.requests-per-minute-per-ip` | `100` | `RATE_LIMIT_REQUESTS_PER_MINUTE_PER_IP` |
| `rate-limit.requests-per-minute-per-user` | `100` | `RATE_LIMIT_REQUESTS_PER_MINUTE_PER_USER` |
| `rate-limit.window-seconds` | `60` | `RATE_LIMIT_WINDOW_SECONDS` |

When exceeded:

- The gateway returns `429 Too Many Requests`.
- The backend is not called.
- `RATE_LIMIT_EXCEEDED` is emitted when audit/IDS is configured.

The current store is in-memory. With multiple gateway instances, limits are per instance unless replaced by a shared store.

## 8. Login Anti-Bruteforce By IP

The gateway tracks failed `POST /api/auth/login` responses by client IP.

| Property | Default | Environment Variable |
|----------|---------|----------------------|
| `bruteforce-ip.max-failed-attempts` | `5` | `BRUTEFORCE_IP_MAX_FAILED_ATTEMPTS` |
| `bruteforce-ip.lockout-duration-minutes` | `15` | `BRUTEFORCE_IP_LOCKOUT_DURATION_MINUTES` |
| `bruteforce-ip.login-path` | `/api/auth/login` | `BRUTEFORCE_IP_LOGIN_PATH` |

When the IP is blocked:

- The gateway returns `423 Locked`.
- The auth service is not called.
- A `RATE_LIMIT_EXCEEDED` event is emitted with `keyType=BRUTEFORCE_IP`.

`auth-service` also implements per-account anti-bruteforce. See [../auth-service/AUTH-SECURITY.md](../auth-service/AUTH-SECURITY.md).

## 9. Input Validation

`InputValidationFilter` scans query parameters and headers, excluding `Authorization`.

Suspicious SQLi/XSS-like values:

- Return `400 Bad Request`.
- Do not reach the backend.
- Emit `SUSPICIOUS_INPUT` when audit/IDS is configured.

Configuration:

| Property | Default |
|----------|---------|
| `input-validation.enabled` | `true` |
| `input-validation.excluded-paths` | `/actuator/health` |

See [VALIDATION-SCHEMAS.md](VALIDATION-SCHEMAS.md).

## 10. Secure Headers

The gateway uses Spring Cloud Gateway `SecureHeaders`.

Typical headers:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Referrer-Policy`
- `Content-Security-Policy`
- `X-Download-Options`
- `X-Permitted-Cross-Domain-Policies`
- `Strict-Transport-Security` when HTTPS/HSTS is enabled

Verify:

```bash
curl -I http://localhost:8080/api/patients/1
```

## 11. Audit And IDS Events

Configure:

```yaml
security:
  audit:
    url: http://security-audit-log:8080/api/events
  ids:
    url: http://ids-service:8080/api/events
```

Gateway events:

- `ACCESS_DENIED`
- `RATE_LIMIT_EXCEEDED`
- `SUSPICIOUS_INPUT`
- `PATIENT_SELF_DELETION_REQUESTED`

See [../AUDIT-IDS-EVENTS.md](../AUDIT-IDS-EVENTS.md).

## 12. Filter Order

| Filter | Order |
|--------|-------|
| `AuthenticationFilter` | `-100` |
| `BruteforceByIpFilter` | `-92` |
| `RateLimitFilter` | `-90` |
| `InputValidationFilter` | `-80` |
| `RbacAuthorizationFilter` | `-50` |

## 13. Tests

Run gateway tests:

```bash
cd gateway-service
mvn test
```

Important test areas:

- Authentication filter.
- JWT verification with HS256 and RS256.
- RBAC service and filter.
- Rate-limit store and filter.
- IP anti-bruteforce store and filter.
- Input validation patterns and filter.
- Audit sender implementations.
- Route configuration integration test.

## 14. End-To-End Checks

Without token:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/patients/1
# Expected: 401
```

With token:

```bash
curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/patients/1
```

Suspicious query:

```bash
curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/patients/search?query=OR%201=1"
# Expected: 400
```

HTTPS:

```bash
curl -k https://localhost:8080/actuator/health
# Expected: 200
```
