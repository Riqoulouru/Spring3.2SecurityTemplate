# Spring Security JWT Template

Template d'application Spring Boot avec Spring Security, authentification JWT, Spring Data JPA et une base PostgreSQL lancable avec Docker.

## Stack

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA / Hibernate
- JWT avec JJWT
- PostgreSQL en local via Docker Compose
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
