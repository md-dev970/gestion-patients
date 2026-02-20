# 🏥 Hospital Management System - Kit Commun

## 📋 Vue d'ensemble

Ce projet est une implémentation complète du **Kit Commun** pour le système de gestion des données patients hospitalier. Il inclut tous les microservices obligatoires selon le cahier des charges.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTS                                         │
│                    (Web App, Mobile App, API Clients)                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY (Port 8080)                              │
│              Routing, Load Balancing, Authentication Filter                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         EUREKA DISCOVERY (Port 8761)                         │
│                    Service Registry & Discovery                               │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
        ┌───────────▼──────────┐ ┌───▼──────────┐ ┌────▼──────────┐
        │   AUTH SERVICE       │ │ PATIENT      │ │ STAFF         │
        │   Port: 8085         │ │ SERVICE      │ │ SERVICE       │
        │   JWT, Users, Roles  │ │ Port: 8081   │ │ Port: 8082    │
        └──────────────────────┘ └──────────────┘ └───────────────┘
                    │                 │                 │
        ┌───────────▼──────────┐ ┌───▼──────────┐ ┌────▼──────────┐
        │ APPOINTMENT SERVICE  │ │ MEDICAL      │ │ CONSULTATIONS │
        │ Port: 8083           │ │ RECORD       │ │ SERVICE       │
        │                      │ │ SERVICE      │ │ Port: 8086    │
        │                      │ │ Port: 8084   │ │               │
        └──────────────────────┘ └──────────────┘ └───────────────┘
      ▼             ▼               ▼                   ▼              ▼
┌───────────┐ ┌───────────┐ ┌─────────────────┐ ┌───────────────┐ ┌─────────┐
│PostgreSQL │ │PostgreSQL │ │   PostgreSQL    │ │  PostgreSQL   │ │PostgreSQL│
│  :5432    │ │   :5433   │ │     :5437       │ │    :5436      │ │  :5434  │
└───────────┘ └───────────┘ └─────────────────┘ └───────────────┘ └─────────┘
```

## 📁 Microservices du Kit Commun

| Service | Port | Description |
|---------|------|-------------|
| **auth-service** | 8085 | Authentification JWT, gestion des utilisateurs et rôles ; protection anti-bruteforce (US1.5, voir auth-service/AUTH-SECURITY.md) |
| **patient-service** | 8081 | CRUD patients, recherche |
| **consultations-service** | 8086 | Gestion des consultations médicales |
| **staff-service** | 8082 | Gestion du personnel (médecins, infirmiers) |
| **appointment-service** | 8083 | Gestion des rendez-vous |
| **gateway-service** | 8080 | Point d'entrée unique, routage, validation stricte des entrées (query/headers) et événements IDS SQLi/XSS (US1.6, voir gateway-service/GATEWAY-HTTPS.md) |
| **discovery-service** | 8761 | Eureka Server, découverte de services |

## 🚀 Démarrage Rapide

### Prérequis

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Option 1: Développement Local (Recommandé)

```bash
# 1. Démarrer les bases de données
docker-compose -f docker-compose.dev.yml up -d

# 2. Démarrer Discovery Service (PREMIER!)
cd discovery-service
mvn spring-boot:run

# 3. Démarrer Gateway Service
cd gateway-service
mvn spring-boot:run

# 4. Démarrer Auth Service
cd auth-service
mvn spring-boot:run

# 5. Démarrer les autres services...
cd patient-service && mvn spring-boot:run
cd consultations-service && mvn spring-boot:run
# etc.
```

### Option 2: Docker Compose (Production-like)

```bash
# Construire et démarrer tous les services
docker-compose up -d --build

# Voir les logs
docker-compose logs -f

# Arrêter
docker-compose down
```

## 🧪 Tests

### Exécuter les tests localement

```bash
# Tous les services
mvn test

# Service spécifique
cd appointment-service && mvn test

# Avec rapport détaillé
mvn test -Dtest=AppointmentServiceImplTest
```

### Tests dans Docker Build

Les tests sont automatiquement exécutés lors de la construction des images Docker. Si un test échoue, le build échoue également.

**Vérifier les résultats des tests :**

#### Option 1: Voir les logs de build Docker Compose
```bash
# Voir les logs de build pour un service spécifique
docker-compose build appointment-service 2>&1 | grep -A 5 "Tests run"

# Voir tous les logs de build
docker-compose build 2>&1 | grep -A 5 "Tests run"

# Voir les logs complets d'un service (inclut le build)
docker-compose logs appointment-service | grep -A 10 "Tests run"
```

#### Option 2: Build manuel avec sortie visible
```bash
# Build un service avec sortie complète
cd appointment-service
docker build --progress=plain -t appointment-service . 2>&1 | tee build.log

# Chercher les résultats de tests dans le log
grep -A 3 "Tests run" build.log
```

#### Option 3: Extraire les rapports de test depuis l'image
```bash
# Créer un conteneur temporaire depuis le stage de build
docker build --target builder -t temp-builder ./appointment-service

# Extraire les rapports de test
docker create --name temp-test temp-builder
docker cp temp-test:/app/target/surefire-reports ./test-reports
docker rm temp-test

# Lire les rapports
cat test-reports/*.txt
```

#### Option 4: Vérifier si le build a réussi
Si `docker-compose up -d --build` se termine sans erreur, **tous les tests ont réussi**. 
Le build échoue automatiquement si un test échoue.

**Exemple de sortie réussie :**
```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## 📡 Endpoints API

### Auth Service (Port 8085)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Inscription utilisateur |
| POST | `/api/auth/login` | Connexion, retourne JWT |
| POST | `/api/auth/refresh` | Rafraîchir le token |

### Patient Service (Port 8081)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/patients` | Créer un patient |
| GET | `/api/patients/{id}` | Obtenir un patient |
| PUT | `/api/patients/{id}` | Mettre à jour un patient |
| GET | `/api/patients` | Lister tous les patients |
| GET | `/api/patients/search?q={term}` | Rechercher des patients |

### Staff Service (Port 8082)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/staff` | Créer un membre du personnel |
| GET | `/api/staff/{id}` | Obtenir un membre du personnel |
| PUT | `/api/staff/{id}` | Mettre à jour un membre du personnel |
| GET | `/api/staff` | Lister tout le personnel |
| GET | `/api/staff/role/{role}` | Filtrer par rôle |
| GET | `/api/staff/specialty/{specialty}` | Filtrer par spécialité |

### Appointment Service (Port 8083)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/appointments` | Créer un rendez-vous |
| GET | `/api/appointments/{id}` | Obtenir un rendez-vous |
| PUT | `/api/appointments/{id}` | Mettre à jour un rendez-vous |
| PUT | `/api/appointments/{id}/status` | Mettre à jour le statut |
| DELETE | `/api/appointments/{id}` | Annuler un rendez-vous |
| GET | `/api/appointments/patient/{patientId}` | Rendez-vous d'un patient |
| GET | `/api/appointments/doctor/{doctorId}` | Rendez-vous d'un médecin |

### Consultations Service (Port 8086)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/consultations` | Créer une consultation |
| GET | `/api/consultations/{id}` | Obtenir une consultation |
| PUT | `/api/consultations/{id}` | Mettre à jour une consultation |
| GET | `/api/consultations/patient/{patientId}` | Historique d'un patient |

### Medical Record Service (Port 8084)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/medical-records` | Créer un dossier médical |
| GET | `/api/medical-records/{id}` | Obtenir un dossier médical |
| GET | `/api/medical-records/patient/{patientId}` | Dossier d'un patient |
| PUT | `/api/medical-records/{id}` | Mettre à jour un dossier |
| POST | `/api/medical-records/{patientId}/entries` | Ajouter une entrée |

## 🔧 Configuration

### Variables d'environnement

Chaque service a son propre `application.yml` avec :
- Configuration de la base de données
- Port du service
- Configuration Eureka
- JWT secrets (pour auth-service)

### Validation des entrées (US1.6)

- **Gateway** : les paramètres de requête (query) et les en-têtes (sauf `Authorization`) sont analysés pour détecter des motifs d’injection (SQLi, XSS). Une requête suspecte (ex. `?query=OR 1=1`) reçoit **400 Bad Request** et un événement **SUSPICIOUS_INPUT** est envoyé vers l’IDS/audit si `security.audit.url` est configuré. Détails : `gateway-service/GATEWAY-HTTPS.md` section 7.
- **Services** : la structure des corps de requête est validée via Bean Validation (`@Valid`) sur les DTOs ; les erreurs de validation renvoient **400**.

### Bases de données

Chaque microservice a sa propre base de données PostgreSQL :
- `auth-service` → Port 5432
- `patient-service` → Port 5433
- `staff-service` → Port 5434
- `appointment-service` → Port 5435
- `medical-record-service` → Port 5436
- `consultations-service` → Port 5437

## 📝 Notes de développement

- Les tests unitaires sont exécutés automatiquement lors du build Docker
- Utilisez `mvn test` pour exécuter les tests localement avant de build
- Les images Docker ne sont créées que si tous les tests passent
- Consultez les logs Docker pour voir les résultats détaillés des tests
