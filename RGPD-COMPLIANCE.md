# RGPD / HIPAA Compliance – Modèle de consentement et traçabilité

Ce document décrit le modèle de consentement, les bases légales et la traçabilité pour la conformité RGPD et HIPAA du KIT COMMUN.

---

## 1. Modèle de consentement

### 1.1 Champs du modèle

| Champ | Description | Valeurs |
|-------|-------------|---------|
| `consentGiven` | Indique si le patient a donné son consentement au traitement des données | `true` / `false` |
| `legalBasis` | Base légale du traitement (RGPD Art. 6) | `consent`, `legitimate_interest`, `contract`, `withdrawn`, etc. |

### 1.2 Valeur par défaut à la création

Lors de la création d’un patient, si `legalBasis` n’est pas fourni, la valeur par défaut est **`consent`**. Cela garantit qu’un patient nouvellement créé a une base légale explicite.

### 1.3 Retrait du consentement (T1.19)

Le flux `withdrawConsent` :

1. Met `consentGiven` à `false`
2. Met `legalBasis` à `"withdrawn"`
3. Émet un événement **PHI_ACCESS** (action `UPDATE`) pour l’audit
4. Retourne le patient mis à jour

**Comportement métier après retrait :**

- Les traitements basés uniquement sur le consentement doivent être arrêtés.
- Les données déjà collectées peuvent être conservées si une autre base légale s’applique (ex. obligation légale, intérêt légitime).
- Le droit à l’effacement (Art. 17 RGPD) peut être exercé via `DELETE /api/patients/{id}` (avec RBAC approprié).

---

## 2. Bases légales (RGPD Art. 6)

| Base | Usage typique |
|------|----------------|
| `consent` | Traitement explicite des données de santé avec accord du patient |
| `legitimate_interest` | Intérêt légitime de l’établissement (ex. continuité des soins) |
| `contract` | Exécution d’un contrat (ex. assurance) |
| `legal_obligation` | Obligations légales (déclarations, archivage) |
| `withdrawn` | Consentement retiré – arrêt des traitements basés sur le consentement |

---

## 3. Traçabilité pour le DPO

### 3.1 Événements d’audit

Tous les accès et modifications de PHI émettent des événements d’audit (voir `AUDIT-IDS-EVENTS.md`) :

- **PHI_ACCESS** : lecture, création, modification de PHI
- **PHI_DELETED** : suppression de PHI
- **DOSSIER_ACCESSED** : accès au dossier patient (READ/EXPORT)
- **ACCOUNT_LOCKED** : verrouillage de compte (bruteforce)

Les payloads ne contiennent **aucun PII** (noms, emails, diagnostics) – uniquement des identifiants techniques (patientId, userId, resourceId).

### 3.2 Retrait de consentement

Le retrait de consentement est tracé par :

1. **PHI_ACCESS** (action `UPDATE`) sur la ressource `PATIENT` avec le `patientId`
2. Le champ `legalBasis` passé à `"withdrawn"` dans la base de données

Le DPO peut interroger les logs d’audit pour retrouver :

- Qui a initié le retrait (via `X-User-Id` ou équivalent dans le contexte d’appel)
- Quand le retrait a eu lieu (timestamp de l’événement)
- Quel patient est concerné (resourceId = patientId)

### 3.3 Accès au dossier

Les accès au dossier patient (GET `/api/patients/{id}/dossier` et export) sont tracés via **DOSSIER_ACCESSED** avec l’action `READ` ou `EXPORT`.

---

## 4. Recommandations

- Conserver les événements d’audit selon la politique de rétention (voir `AUDIT-IDS-EVENTS.md` ou `SECURITY-ENCRYPTION.md`).
- Documenter les traitements (registre des activités – Art. 30 RGPD).
- Former les équipes au respect du consentement et des bases légales.
- Mettre en place des processus de réponse aux demandes d’accès, de rectification et d’effacement (Art. 12–17 RGPD).
