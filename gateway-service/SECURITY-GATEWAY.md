# Security Gateway (reverse proxy) – T1.1

Ce document décrit le **projet / stack** du security-gateway (reverse proxy) pour le KIT COMMUN.

## Rôle

- **Security-gateway** = service `gateway-service` (Spring Cloud Gateway).
- **Reverse proxy** : point d’entrée unique pour tout le trafic HTTP(S) ; les clients ne parlent qu’au gateway (port 8080). Le gateway route les requêtes vers les microservices backend via Eureka (`lb://`) et applique les contrôles de sécurité (JWT, RBAC, rate limit, validation des entrées, headers de sécurité).

## Stack

- **Technologie** : Spring Cloud Gateway (réactif).
- **Port** : 8080 (seul service exposé sur l’hôte pour l’API).
- **Dépendances** : Eureka (discovery-service) pour la résolution des backends.
- **Déploiement** : défini dans `docker-compose.yml` ; ordre de démarrage : Discovery → Gateway → backends.

## Vérifier que le reverse proxy est opérationnel

1. Démarrer le stack : `docker-compose up -d --build`, attendre que le gateway soit healthy (ou quelques secondes).
2. Santé du gateway :  
   `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health`  
   → attendu **200**.
3. Requête via le proxy (sans token) :  
   `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/patients/1`  
   → attendu **401** (la requête a bien transité par le gateway).

Voir aussi [GATEWAY-HTTPS.md](GATEWAY-HTTPS.md) pour le détail du routage, de l’authentification et des options HTTPS.
