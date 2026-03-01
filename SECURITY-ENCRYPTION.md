# Chiffrement et disposition des données (T1.21)

Ce document décrit les exigences et mesures relatives au **chiffrement des données** (en transit et au repos) et à la **disposition sécurisée** après purge.

---

## 1. Chiffrement en transit (HTTPS)

- **Exigence** : Tout trafic entre les clients et l’API doit transiter en HTTPS (TLS) pour éviter l’écoute et la modification des données.
- **Mise en œuvre** :
  - Le **gateway** (security-gateway) est le point d’entrée unique. La TLS est activée sur le gateway (voir `gateway-service/GATEWAY-HTTPS.md` et `docker-compose.tls.yml`).
  - En production, un certificat valide (CA) doit être utilisé. En développement, un keystore auto-signé est généré via `gateway-service/scripts/generate-dev-keystore.bat` (ou `.sh`).
  - Les appels entre microservices (Feign, Eureka) peuvent rester en HTTP sur un réseau interne de confiance ; en environnement non sécurisé, TLS entre services est recommandé (mTLS ou TLS terminate at gateway).
- **Vérification** : Accéder à l’API via `https://localhost:8080` (ou l’URL du gateway) et vérifier que le navigateur affiche une connexion sécurisée.

---

## 2. Chiffrement au repos (base de données)

- **Exigence** : Les données sensibles (PHI, mots de passe) stockées en base doivent être protégées au repos (chiffrement des données ou du stockage).
- **Mise en œuvre** :
  - **Mots de passe** : Les mots de passe des utilisateurs (auth-service) ne sont **jamais** stockés en clair ; ils sont hashés avec un algorithme adapté (ex. BCrypt) via `PasswordEncoder`.
  - **Bases de données** : Chaque microservice utilise sa propre base PostgreSQL. Le chiffrement au repos dépend de la configuration du serveur PostgreSQL et du système de fichiers :
    - Utiliser le chiffrement de volume (ex. LUKS, BitLocker) ou le chiffrement natif de la base (ex. PostgreSQL TDE si disponible) en production.
    - En développement, les données sont souvent non chiffrées au repos ; les environnements de prod doivent activer le chiffrement au niveau stockage ou base.
- **Bonnes pratiques** : Restreindre l’accès réseau aux bases, utiliser des identifiants forts et les stocker dans un gestionnaire de secrets (ex. Vault, variables d’environnement sécurisées).

---

## 3. Disposition sécurisée après purge

- **Exigence** : Lorsqu’une donnée est purgée (rétention dépassée ou effacement demandé), sa disposition doit être sécurisée (suppression définitive, pas de récupération).
- **Mise en œuvre** :
  - **Purge planifiée (T1.18)** : Le job `RetentionPurgeJob` (patient-service) supprime les patients dont la date `retention_until` est dépassée. La suppression est en cascade (medical-record, consultations, appointments) via `PatientService.deletePatient`. Les enregistrements sont **supprimés** de la base (DELETE), pas seulement marqués comme supprimés.
  - **Suppression à la demande** : Les endpoints de suppression (ex. `DELETE /api/patients/{id}`) effectuent une suppression réelle en base.
  - **Comptes utilisateur (T1.20)** : L’anonymisation remplace les données identifiantes (username, email, mot de passe) par des valeurs non réversibles ; la suppression du compte supprime la ligne en base.
- **Audit** : Les événements `PHI_DELETED` et `RETENTION_PURGE` sont envoyés au système d’audit (si `security.audit.url` est configuré) pour tracer les purges.
- **Bonnes pratiques** : En environnement très sensible, envisager le shredding ou le chiffrement des backups avant destruction, et documenter la procédure de disposition dans la politique de sécurité.

---

## 4. Résumé des configurations

| Élément                    | Configuration / Fichier                          |
|---------------------------|---------------------------------------------------|
| TLS sur le gateway        | `application.yml` (ssl.*), `docker-compose.tls.yml` |
| Keystore dev              | `gateway-service/scripts/generate-dev-keystore.*` |
| Rétention et purge        | `retention.*` dans chaque service, `RetentionPurgeJob` |
| Audit des purges          | `security.audit.url`, événements PHI_DELETED / RETENTION_PURGE |
