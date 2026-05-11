# Spring Security JWT Template

Template d'application Spring Boot avec Spring Security, authentification JWT, Spring Data JPA et une base PostgreSQL lancable avec Docker.

## Stack

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA / Hibernate
- JWT avec JJWT
- PostgreSQL en local via Docker Compose
- Features d'auth optionnelles, desactivees par defaut
- H2 uniquement pour les tests
- Maven Wrapper inclus (`mvnw.cmd`)

## Structure utile

```text
.
+-- BDD/
|   +-- docker-compose.yml
+-- src/main/resources/application.properties
+-- src/test/resources/application.properties
+-- .env.dev
+-- .env.prod
+-- pom.xml
+-- README.md
```

Le dossier `BDD` contient la configuration Docker Compose pour demarrer PostgreSQL.

Les fichiers `.env.dev` et `.env.prod` donnent des templates de variables d'environnement. Le fichier `.env.dev` est utilise par Docker Compose en local. Spring lit les memes variables via `application.properties`.

## Variables d'environnement

Les principales variables sont:

```env
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/spring_security_template
SPRING_DATASOURCE_USERNAME=spring_user
SPRING_DATASOURCE_PASSWORD=spring_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_SQL_INIT_PLATFORM=postgresql
SPRING_JPA_HIBERNATE_DDL_AUTO=update
TOKEN_SIGNING_KEY=bXktdmVyeS1zZWNyZXQta2V5LTEyMzQ1Njc4OTAxMjM0NTY=

POSTGRES_DB=spring_security_template
POSTGRES_USER=spring_user
POSTGRES_PASSWORD=spring_password
POSTGRES_HOST_PORT=5432
POSTGRES_CONTAINER_PORT=5432
POSTGRES_DATA_VOLUME=postgres_data_dev

SECURITY_FEATURE_REFRESH_TOKEN_ENABLED=false
SECURITY_FEATURE_REFRESH_TOKEN_EXPIRATION_MS=604800000
SECURITY_FEATURE_EMAIL_VERIFICATION_ENABLED=false
SECURITY_FEATURE_EMAIL_VERIFICATION_EXPIRATION_MS=86400000
SECURITY_FEATURE_PASSWORD_RESET_ENABLED=false
SECURITY_FEATURE_PASSWORD_RESET_EXPIRATION_MS=900000
SECURITY_FEATURE_AUDIT_LOG_ENABLED=false
SECURITY_FEATURE_RATE_LIMIT_ENABLED=false
SECURITY_FEATURE_RATE_LIMIT_MAX_ATTEMPTS=5
SECURITY_FEATURE_RATE_LIMIT_WINDOW_MS=60000
```

En local, les valeurs par defaut de `application.properties` correspondent a `.env.dev`.

En production, ne gardez pas les valeurs `change_me` de `.env.prod`. Remplacez aussi `TOKEN_SIGNING_KEY` par une vraie cle secrete en Base64, suffisamment longue pour l'algorithme HMAC utilise par JWT.

## Lancer la base PostgreSQL

Depuis la racine du projet:

```powershell
cd BDD
docker compose up -d
```

Docker Compose lit `../.env.dev` et cree:

- un conteneur PostgreSQL nomme `spring-security-template-postgres`
- une base `spring_security_template`
- un utilisateur `spring_user`
- un volume Docker persistant `postgres_data_dev`
- une exposition locale sur le port `5432`

Verifier que la base tourne:

```powershell
docker compose ps
```

Voir les logs:

```powershell
docker compose logs -f postgres
```

Arreter la base:

```powershell
docker compose down
```

Arreter et supprimer aussi les donnees locales:

```powershell
docker compose down -v
```

Si PostgreSQL affiche une erreur sur `/var/lib/postgresql/data` apres un changement d'image ou de montage, supprimez le volume local puis relancez la base:

```powershell
docker compose down -v
docker compose up -d
```

Cette commande supprime les donnees locales de developpement stockees dans le volume Docker.

## Lancer l'application Spring

Dans un nouveau terminal, placez-vous a la racine du projet:

```powershell
cd C:\Users\basil\Documents\ideaProject\Spring3.2SecurityTemplate
```

Puis lancez l'application:

```powershell
.\mvnw.cmd spring-boot:run
```

Par defaut, l'application demarre sur:

```text
http://localhost:8080
```

La configuration Spring est dans `src/main/resources/application.properties`. Elle utilise les variables d'environnement si elles existent, sinon elle reprend les valeurs par defaut compatibles avec `.env.dev`.

## Endpoints disponibles

Base path d'authentification:

```text
/api/v1/auth
```

Inscription:

```http
POST /api/v1/auth/signup
```

Connexion:

```http
POST /api/v1/auth/signin
```

Ressource protegee:

```http
GET /api/v1/resource
Authorization: Bearer <token>
```

## Features d'auth optionnelles

Le template contient plusieurs modules utiles en authentification. Ils sont desactives par defaut pour ne pas imposer un comportement au projet final.

Activation depuis `.env.dev` ou `.env.prod`:

```env
SECURITY_FEATURE_REFRESH_TOKEN_ENABLED=true
SECURITY_FEATURE_EMAIL_VERIFICATION_ENABLED=true
SECURITY_FEATURE_PASSWORD_RESET_ENABLED=true
SECURITY_FEATURE_AUDIT_LOG_ENABLED=true
SECURITY_FEATURE_RATE_LIMIT_ENABLED=true
```

### Refresh tokens

Flag:

```env
SECURITY_FEATURE_REFRESH_TOKEN_ENABLED=true
```

Quand cette feature est activee, `/signup` et `/signin` retournent aussi un `refreshToken`.

Renouveler une session:

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "token"
}
```

Le refresh token est stocke en base sous forme de hash SHA-256. Lors d'un refresh, l'ancien token est revoque et un nouveau refresh token est genere.

### Verification d'email

Flag:

```env
SECURITY_FEATURE_EMAIL_VERIFICATION_ENABLED=true
```

Quand cette feature est activee, un nouvel utilisateur est cree avec `emailVerified=false`. Il ne peut pas se connecter tant que son email n'est pas verifie.

Verifier un email:

```http
POST /api/v1/auth/verify-email
Content-Type: application/json

{
  "token": "token"
}
```

Dans ce template, le token est retourne dans la reponse de `/signup` pour faciliter le developpement. En production, il faut l'envoyer par email et ne pas le retourner dans la reponse HTTP.

### Reset password

Flag:

```env
SECURITY_FEATURE_PASSWORD_RESET_ENABLED=true
```

Demander un reset:

```http
POST /api/v1/auth/password-reset/request
Content-Type: application/json

{
  "email": "user@example.com"
}
```

Confirmer le reset:

```http
POST /api/v1/auth/password-reset/confirm
Content-Type: application/json

{
  "token": "token",
  "newPassword": "new-password"
}
```

Le token de reset est stocke en base sous forme de hash SHA-256. Apres changement du mot de passe, les refresh tokens de l'utilisateur sont revoques si la feature refresh token est activee.

Comme pour la verification d'email, le token est retourne par l'endpoint de demande pour faciliter le developpement. En production, il faut l'envoyer par email.

### Audit log

Flag:

```env
SECURITY_FEATURE_AUDIT_LOG_ENABLED=true
```

Quand cette feature est activee, les evenements d'authentification sont historises en base:

- signup
- signin reussi
- signin echoue

Les informations stockees incluent l'email, le succes ou l'echec, l'adresse IP, le user-agent, un message et la date.

### Rate limiting

Flag:

```env
SECURITY_FEATURE_RATE_LIMIT_ENABLED=true
```

Parametres:

```env
SECURITY_FEATURE_RATE_LIMIT_MAX_ATTEMPTS=5
SECURITY_FEATURE_RATE_LIMIT_WINDOW_MS=60000
```

Le rate limiting protege les endpoints `signup` et `signin` contre les tentatives rapides. L'implementation fournie est en memoire, donc adaptee au developpement ou a une application simple. Pour une application distribuee, remplacez-la par Redis, Bucket4j ou une protection au niveau gateway/reverse proxy.

## Tests

Les tests n'utilisent pas PostgreSQL. Ils utilisent H2 via:

```text
src/test/resources/application.properties
```

Lancer les tests:

```powershell
.\mvnw.cmd test
```

## Utilisation de `.env.prod`

`.env.prod` sert de template pour un environnement de production. Il ne doit pas etre utilise tel quel sans remplacer les secrets.

Exemples de points a adapter:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `TOKEN_SIGNING_KEY`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`

En production, `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` est plus prudent que `update`, car l'application valide le schema sans modifier automatiquement la base.

## Commandes rapides

Depuis la racine:

```powershell
cd BDD
docker compose up -d
cd ..
.\mvnw.cmd spring-boot:run
```

Tests:

```powershell
.\mvnw.cmd test
```
