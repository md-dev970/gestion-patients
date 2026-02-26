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

## DOSSIER_ACCESSED (T6.8)

Emitted by **patient-service** when a patient dossier is successfully **read** (GET `/api/patients/{id}/dossier`) or **exported** (GET `/api/patients/{id}/dossier/export`). No PII/PHI is included in the payload; used for audit trail of access to PHI.

| Field          | Type   | Description |
|----------------|--------|-------------|
| `eventType`    | string | Always `"DOSSIER_ACCESSED"` |
| `timestamp`   | string | ISO-8601 instant |
| `resourceType`| string | Always `"PATIENT_DOSSIER"` |
| `resourceId`  | string | Patient ID (no PII). |
| `action`       | string | `"READ"` for dossier view, `"EXPORT"` for dossier download |

**Example payload (read):**

```json
{
  "eventType": "DOSSIER_ACCESSED",
  "timestamp": "2025-02-17T10:35:00.000Z",
  "resourceType": "PATIENT_DOSSIER",
  "resourceId": "42",
  "action": "READ"
}
```

**When it is sent:**

- **patient-service:** After a successful response for GET `/api/patients/{id}/dossier` (action READ) or GET `/api/patients/{id}/dossier/export` (action EXPORT). Not sent when the patient is not found (404).

Implementations are fire-and-forget; failures to send are logged and do not affect the HTTP response.

---

## PATIENT_SELF_DELETION_REQUESTED (T6.11)

Emitted by the **gateway** when a user with **ROLE_PATIENT** is **allowed** to DELETE their own patient record (`DELETE /api/patients/{id}` where id = user id). Lets the audit log distinguish patient-initiated deletion (right to erasure) from admin-initiated deletion. No PII in the payload.

| Field          | Type   | Description |
|----------------|--------|-------------|
| `eventType`    | string | Always `"PATIENT_SELF_DELETION_REQUESTED"` |
| `timestamp`    | string | ISO-8601 instant |
| `resourceType` | string | Always `"PATIENTS"` |
| `resourceId`   | string | Patient ID from the path (no PII). |

**When it is sent:** Immediately when the gateway allows the request (before proxying to patient-service). Not sent when the requester is ADMIN or when the request is denied (e.g. PATIENT trying to delete another patient).

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
