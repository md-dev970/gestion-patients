# Format des événements audit et IDS (T1.9)

Le gateway émet des événements vers **security-audit-log** et **ids-service** via API (POST JSON). Format défini ci-dessous. Aucun PII/PHI dans les payloads.

---

## Configuration

| Propriété | Description |
|-----------|-------------|
| `security.audit.url` | URL de l’API security-audit-log (ex. `http://security-audit-log:8080/api/events`). Si renseignée, tous les événements sont envoyés en POST. |
| `security.ids.url` | (Optionnel) URL de l’API ids-service. Si renseignée, les événements de type IDS (RATE_LIMIT_EXCEEDED, SUSPICIOUS_INPUT) sont aussi envoyés à cette URL. |

Si aucune URL n’est configurée, l’implémentation no-op est utilisée (aucun envoi, log DEBUG possible).

---

## 1. ACCESS_DENIED (audit)

Émis lorsqu’un accès est refusé par le RBAC (403).

**Méthode** : `POST`  
**Body** : `application/json`

| Champ | Type | Description |
|-------|------|-------------|
| `eventType` | string | `"ACCESS_DENIED"` |
| `timestamp` | string | ISO-8601 (ex. `2025-02-17T12:00:00Z`) |
| `userId` | string | Identifiant technique (JWT), pas de nom/email |
| `resourceType` | string | `PATIENTS` \| `MEDICAL_RECORDS` \| `CONSULTATIONS` |
| `resourceId` | string | ID extrait du path si pertinent, sinon `""` |
| `action` | string | `READ` \| `CREATE` \| `UPDATE` \| `DELETE` |
| `reason` | string | ex. `"RBAC_DENY"` |

**Exemple** :
```json
{
  "eventType": "ACCESS_DENIED",
  "timestamp": "2025-02-17T12:00:00Z",
  "userId": "42",
  "resourceType": "PATIENTS",
  "resourceId": "1",
  "action": "DELETE",
  "reason": "RBAC_DENY"
}
```

---

## 2. RATE_LIMIT_EXCEEDED (audit + IDS)

Émis lorsque la limite de débit (par IP ou par utilisateur) ou le blocage anti-bruteforce par IP est dépassé (429 / 423).

**Méthode** : `POST`  
**Body** : `application/json`

| Champ | Type | Description |
|-------|------|-------------|
| `eventType` | string | `"RATE_LIMIT_EXCEEDED"` |
| `timestamp` | string | ISO-8601 |
| `keyType` | string | `IP` \| `USER` \| `BRUTEFORCE_IP` |
| `key` | string | Adresse IP ou identifiant utilisateur |
| `limit` | number | Limite (ex. requêtes par fenêtre ou N tentatives) |
| `windowSeconds` | number | Fenêtre en secondes (ex. 60 ou durée de blocage) |

**Exemple** :
```json
{
  "eventType": "RATE_LIMIT_EXCEEDED",
  "timestamp": "2025-02-17T12:00:00Z",
  "keyType": "BRUTEFORCE_IP",
  "key": "192.168.1.1",
  "limit": 5,
  "windowSeconds": 900
}
```

---

## 3. SUSPICIOUS_INPUT (audit + IDS)

Émis lorsqu’une entrée (query ou header) correspond à un motif d’injection (SQLi/XSS).

**Méthode** : `POST`  
**Body** : `application/json`

| Champ | Type | Description |
|-------|------|-------------|
| `eventType` | string | `"SUSPICIOUS_INPUT"` |
| `timestamp` | string | ISO-8601 |
| `source` | string | `query` \| `header` |
| `path` | string | Chemin de la requête (ex. `/api/patients/search`) |
| `method` | string | Méthode HTTP (ex. `GET`) |
| `category` | string | `SQLI` \| `XSS` (pas de valeur brute ni PII) |

**Exemple** :
```json
{
  "eventType": "SUSPICIOUS_INPUT",
  "timestamp": "2025-02-17T12:00:00Z",
  "source": "query",
  "path": "/api/patients/search",
  "method": "GET",
  "category": "SQLI"
}
```

---

## Envoi (API)

- **security-audit-log** : tous les événements sont envoyés à `security.audit.url` (si configuré).
- **ids-service** : les événements **RATE_LIMIT_EXCEEDED** et **SUSPICIOUS_INPUT** sont en outre envoyés à `security.ids.url` (si configuré).
- Envoi **non bloquant** (fire-and-forget). En cas d’erreur HTTP, un log est émis et la réponse au client n’est pas modifiée.
