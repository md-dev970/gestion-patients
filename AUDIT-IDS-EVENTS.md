# Audit And IDS Event Reference

This document defines the audit and intrusion-detection event payloads used by the platform. Events are designed for security monitoring and privacy traceability while avoiding raw PII/PHI in payloads.

## Configuration

| Setting | Used By | Description |
|---------|---------|-------------|
| `security.audit.url` | Gateway and PHI services | HTTP endpoint that receives audit events. When unset, senders are no-op. |
| `security.ids.url` | Gateway | Optional IDS endpoint. Receives IDS-oriented events such as `RATE_LIMIT_EXCEEDED` and `SUSPICIOUS_INPUT`. |

Events are sent as JSON with fire-and-forget behavior. Delivery failures are logged and do not change the client response.

## Privacy Rules

Events must not contain:

- Patient names, addresses, phone numbers, or emails.
- Diagnoses, prescriptions, notes, or medical details.
- Raw request bodies.
- Raw suspicious input strings.
- Passwords, tokens, refresh tokens, or secrets.

Events may contain:

- Technical user IDs.
- Resource IDs.
- Patient IDs.
- HTTP method/path metadata.
- Action names.
- Event categories.
- Timestamps.

## PHI_ACCESS

Emitted after a successful read, create, or update of protected health information.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `PHI_ACCESS` |
| `timestamp` | string | ISO-8601 instant |
| `resourceType` | string | `PATIENT`, `MEDICAL_RECORD`, `CONSULTATION`, or `APPOINTMENT` |
| `resourceId` | string | Technical resource identifier |
| `action` | string | `READ`, `CREATE`, or `UPDATE` |

Example:

```json
{
  "eventType": "PHI_ACCESS",
  "timestamp": "2026-05-28T10:40:00.000Z",
  "resourceType": "PATIENT",
  "resourceId": "42",
  "action": "READ"
}
```

Typical emitters:

- `patient-service`
- `medical-record-service`
- `consultations-service`
- `appointment-service`

## PHI_DELETED

Emitted after successful deletion of protected health information.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `PHI_DELETED` |
| `timestamp` | string | ISO-8601 instant |
| `resourceType` | string | `PATIENT`, `MEDICAL_RECORD`, `CONSULTATION`, or `APPOINTMENT` |
| `resourceId` | string | Deleted resource identifier |

Example:

```json
{
  "eventType": "PHI_DELETED",
  "timestamp": "2026-05-28T10:30:00.123Z",
  "resourceType": "MEDICAL_RECORD",
  "resourceId": "100"
}
```

## DOSSIER_ACCESSED

Emitted by `patient-service` after a successful dossier read or export.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `DOSSIER_ACCESSED` |
| `timestamp` | string | ISO-8601 instant |
| `resourceType` | string | Always `PATIENT_DOSSIER` |
| `resourceId` | string | Patient ID |
| `action` | string | `READ` or `EXPORT` |

Example:

```json
{
  "eventType": "DOSSIER_ACCESSED",
  "timestamp": "2026-05-28T10:35:00.000Z",
  "resourceType": "PATIENT_DOSSIER",
  "resourceId": "42",
  "action": "EXPORT"
}
```

## PATIENT_SELF_DELETION_REQUESTED

Emitted by the gateway when a `ROLE_PATIENT` user is allowed to delete their own patient record.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `PATIENT_SELF_DELETION_REQUESTED` |
| `timestamp` | string | ISO-8601 instant |
| `resourceType` | string | Always `PATIENTS` |
| `resourceId` | string | Patient ID from the request path |

This event is not emitted for admin deletion and is not emitted when self-deletion is denied.

## RETENTION_PURGE

Emitted by `patient-service` after a scheduled retention purge run.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `RETENTION_PURGE` |
| `timestamp` | string | ISO-8601 instant |
| `resourceType` | string | Always `PATIENT` |
| `purgedCount` | number | Number of patient records purged |

Example:

```json
{
  "eventType": "RETENTION_PURGE",
  "timestamp": "2026-05-28T02:00:00.000Z",
  "resourceType": "PATIENT",
  "purgedCount": 3
}
```

## ACCOUNT_LOCKED

Emitted by `auth-service` when an account is temporarily locked after repeated failed login attempts.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `ACCOUNT_LOCKED` |
| `timestamp` | string | ISO-8601 instant |
| `userId` | string | Technical user ID |
| `username` | string | Login identifier |
| `reason` | string | Usually `BRUTEFORCE` |

## ACCESS_DENIED

Emitted by the gateway when RBAC denies a request.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `ACCESS_DENIED` |
| `timestamp` | string | ISO-8601 instant |
| `userId` | string | Technical user ID from JWT |
| `resourceType` | string | `PATIENTS`, `MEDICAL_RECORDS`, `CONSULTATIONS`, or `APPOINTMENTS` |
| `resourceId` | string | Resource ID from the path when available |
| `action` | string | `READ`, `CREATE`, `UPDATE`, or `DELETE` |
| `reason` | string | Usually `RBAC_DENY` |

Example:

```json
{
  "eventType": "ACCESS_DENIED",
  "timestamp": "2026-05-28T12:00:00Z",
  "userId": "42",
  "resourceType": "PATIENTS",
  "resourceId": "1",
  "action": "DELETE",
  "reason": "RBAC_DENY"
}
```

## RATE_LIMIT_EXCEEDED

Emitted by the gateway when per-IP, per-user, or login-abuse limits are exceeded.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `RATE_LIMIT_EXCEEDED` |
| `timestamp` | string | ISO-8601 instant |
| `keyType` | string | `IP`, `USER`, or `BRUTEFORCE_IP` |
| `key` | string | Client IP or technical user ID |
| `limit` | number | Configured limit |
| `windowSeconds` | number | Window or lockout duration in seconds |

Example:

```json
{
  "eventType": "RATE_LIMIT_EXCEEDED",
  "timestamp": "2026-05-28T12:00:00Z",
  "keyType": "BRUTEFORCE_IP",
  "key": "192.168.1.1",
  "limit": 5,
  "windowSeconds": 900
}
```

## SUSPICIOUS_INPUT

Emitted by the gateway when query parameters or headers match SQLi/XSS-like patterns.

| Field | Type | Description |
|-------|------|-------------|
| `eventType` | string | Always `SUSPICIOUS_INPUT` |
| `timestamp` | string | ISO-8601 instant |
| `source` | string | `query` or `header` |
| `path` | string | Request path |
| `method` | string | HTTP method |
| `category` | string | `SQLI` or `XSS` |

Example:

```json
{
  "eventType": "SUSPICIOUS_INPUT",
  "timestamp": "2026-05-28T12:00:00Z",
  "source": "query",
  "path": "/api/patients/search",
  "method": "GET",
  "category": "SQLI"
}
```

## Retention Recommendations

| Event Type | Suggested Retention | After Retention |
|------------|---------------------|-----------------|
| `PHI_ACCESS`, `PHI_DELETED`, `DOSSIER_ACCESSED` | 1 year or policy-defined healthcare audit period | Archive, anonymize, or purge |
| `RETENTION_PURGE` | 1 year or policy-defined compliance period | Archive or purge |
| `ACCOUNT_LOCKED` | 1 year | Purge or archive |
| `ACCESS_DENIED`, `RATE_LIMIT_EXCEEDED`, `SUSPICIOUS_INPUT` | 90 days | Purge |

In ELK/OpenSearch, implement this with Index Lifecycle Management. In Splunk or a database-backed audit service, configure equivalent retention policies.
