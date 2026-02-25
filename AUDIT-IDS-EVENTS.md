# Audit & IDS Events (Security)

This document describes the security audit and IDS event formats used across the hospital platform.

## Configuration

- **Gateway and PHI services:** Set `security.audit.url` to the audit log endpoint to enable sending events. When not set, implementations are no-op (no network calls).
- **Gateway only:** Optionally set `security.ids.url` so that RATE_LIMIT_EXCEEDED and SUSPICIOUS_INPUT are also sent to the IDS.

---

## PHI_DELETED (T6.3)

Emitted by PHI services after a **successful** DELETE of protected health information. No PII/PHI is included in the payload.

| Field         | Type   | Description |
|---------------|--------|-------------|
| `eventType`   | string | Always `"PHI_DELETED"` |
| `timestamp`   | string | ISO-8601 instant (e.g. `2025-02-17T10:30:00.123Z`) |
| `resourceType`| string | One of: `PATIENT`, `MEDICAL_RECORD`, `CONSULTATION`, `APPOINTMENT` |
| `resourceId`  | string | Identifier of the deleted resource (e.g. patient ID or record ID). No PII. |

**Example payload:**

```json
{
  "eventType": "PHI_DELETED",
  "timestamp": "2025-02-17T10:30:00.123Z",
  "resourceType": "MEDICAL_RECORD",
  "resourceId": "100"
}
```

**When it is sent:**

- **medical-record-service:** After successful DELETE by patient (delete of medical record for a patient).
- **consultations-service:** After successful DELETE by patient (delete of all consultations for a patient).
- **appointment-service:** After successful DELETE by patient (delete of all appointments for a patient).
- **patient-service:** After successful deletion of a patient (after cascade and local delete).

Implementations are fire-and-forget; failures to send are logged and do not affect the HTTP response.

---

## Gateway-only events (reference)

These are sent by the gateway when `security.audit.url` is set.

### ACCESS_DENIED

| Field          | Type   | Description |
|----------------|--------|-------------|
| `eventType`    | string | `"ACCESS_DENIED"` |
| `timestamp`    | string | ISO-8601 instant |
| `userId`       | string | User identifier (pseudonymised) |
| `resourceType` | string | Resource from RBAC (e.g. PATIENT, APPOINTMENT) |
| `resourceId`   | string | Resource ID when applicable |
| `action`       | string | Action denied (e.g. READ, DELETE) |
| `reason`       | string | e.g. `"RBAC_DENY"` |

### RATE_LIMIT_EXCEEDED

| Field          | Type   | Description |
|----------------|--------|-------------|
| `eventType`    | string | `"RATE_LIMIT_EXCEEDED"` |
| `timestamp`    | string | ISO-8601 instant |
| `keyType`      | string | `"IP"` or `"USER"` |
| `key`          | string | Identifier (no PII) |
| `limit`        | number | Configured limit |
| `windowSeconds`| number | Time window in seconds |

### SUSPICIOUS_INPUT

| Field       | Type   | Description |
|-------------|--------|-------------|
| `eventType` | string | `"SUSPICIOUS_INPUT"` |
| `timestamp` | string | ISO-8601 instant |
| `source`    | string | `"query"`, `"header"`, or `"body"` |
| `path`      | string | Request path |
| `method`    | string | HTTP method |
| `category`  | string | `"SQLI"` or `"XSS"` |
