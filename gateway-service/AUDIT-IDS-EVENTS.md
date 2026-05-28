# Gateway Audit And IDS Events

This gateway-specific reference mirrors the root [AUDIT-IDS-EVENTS.md](../AUDIT-IDS-EVENTS.md) file and focuses on events emitted by `gateway-service`.

## Configuration

| Property | Description |
|----------|-------------|
| `security.audit.url` | Audit endpoint that receives all gateway security events |
| `security.ids.url` | Optional IDS endpoint that receives IDS-oriented events |

If no URL is configured, the gateway uses a no-op sender.

## ACCESS_DENIED

Emitted when gateway RBAC returns `403 Forbidden`.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | `ACCESS_DENIED` |
| `timestamp` | string | ISO-8601 instant |
| `userId` | string | Technical user ID from JWT |
| `resourceType` | string | Protected resource category |
| `resourceId` | string | ID extracted from the path when available |
| `action` | string | `READ`, `CREATE`, `UPDATE`, or `DELETE` |
| `reason` | string | Usually `RBAC_DENY` |

## RATE_LIMIT_EXCEEDED

Emitted when per-IP, per-user, or login-abuse limits are exceeded.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | `RATE_LIMIT_EXCEEDED` |
| `timestamp` | string | ISO-8601 instant |
| `keyType` | string | `IP`, `USER`, or `BRUTEFORCE_IP` |
| `key` | string | IP address or technical user ID |
| `limit` | number | Configured threshold |
| `windowSeconds` | number | Window or lockout duration |

## SUSPICIOUS_INPUT

Emitted when query parameters or headers match SQLi/XSS-like patterns.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | `SUSPICIOUS_INPUT` |
| `timestamp` | string | ISO-8601 instant |
| `source` | string | `query` or `header` |
| `path` | string | Request path |
| `method` | string | HTTP method |
| `category` | string | `SQLI` or `XSS` |

## PATIENT_SELF_DELETION_REQUESTED

Emitted when a `ROLE_PATIENT` user is allowed to delete their own patient record.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | `PATIENT_SELF_DELETION_REQUESTED` |
| `timestamp` | string | ISO-8601 instant |
| `resourceType` | string | `PATIENTS` |
| `resourceId` | string | Patient ID from the path |

## Delivery Behavior

- Events are sent as JSON with fire-and-forget semantics.
- HTTP failures are logged.
- Client responses are not changed by event-delivery failures.
- Raw PII, PHI, tokens, and suspicious input values must not be included in event payloads.
