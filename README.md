# User service

Accounts, authentication, dietary restrictions, and favourites (recipes +
per-recipe ingredient substitutions).

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![Build](https://img.shields.io/badge/build-Gradle-blue)
![DB](https://img.shields.io/badge/MySQL-8.4-blue)
![Port](https://img.shields.io/badge/port-8084-lightgrey)

## Model

- **`Account`** — `id`, `username` (unique), `email` (**optional**, unique when
  set — nothing is sent to it), `displayName`, BCrypt `passwordHash`,
  `Set<String> restrictions` (free-text codes), `List<Long> favoriteRecipeIds`,
  timestamps.
- **`FavoriteAlternative`** — per `(accountId, recipeId, ingredientId)`, a
  preferred `replacementIngredientId`.
- **`Restriction`** — the seeded advisory catalogue (`code`, `label`, `kind`,
  `description`). Accounts store codes as **free text**; the catalogue only
  populates pickers.

## API

### `/api/accounts`
| | |
|---|---|
| `POST /` | register (`username`, `password`, `displayName`; `email` optional) → `AccountResponse` |
| `GET /{id}` | one account |
| `PUT /{id}` | update `displayName` (and `email`; blank clears it) |
| `PUT /{id}/password` | `{currentPassword, newPassword}` — current is verified (401 if wrong) |
| `GET\|PUT /{id}/restrictions` | read / replace the code set |
| `GET /{id}/favorites/recipes`, `POST\|DELETE /{id}/favorites/recipes/{recipeId}` | favourite recipes |
| `GET\|PUT /{id}/favorites/alternatives`, `DELETE /{id}/favorites/alternatives/{altId}` | favourite substitutions |
| `DELETE /{id}` | delete account |

### `/api/authenticate`
`POST` `{identifier, password}` → `{accountId, username, displayName}` (200) or 401.

### `/api/restrictions`
`GET /` (`?kind=allergen\|diet\|religious\|lifestyle`), `GET /{code}`.

## Run

```bash
# once: DB passwords + the HMAC secret live in a git-ignored .env, never in the repo
cp .env.example .env             # fill in MYSQL_ROOT_PASSWORD, MYSQL_PASSWORD, INTERNAL_AUTH_SECRET (openssl rand -hex 24 each)

cd .. && docker compose up --build user    # root stack: reads ../.env

docker compose up -d mysql       # local — schema `user_service`
./gradlew bootRun                # Spring Boot's docker-compose support reads .env
```

`compose.yaml` refuses to start while a password or the secret is unset or empty
(findings H4, N1). `bootRun` also reads `INTERNAL_AUTH_SECRET` from `.env` (via
`spring.config.import`), so one file serves both.
If a container from before this change still exists, its old password survives
the new `.env` — `docker compose down -v` first (this wipes that dev database).

## Configuration (env)

| Var | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3309/user_service` |
| `SPRING_DATASOURCE_USERNAME` | `myuser` |
| `SPRING_DATASOURCE_PASSWORD` | **none** — set from `MYSQL_PASSWORD` (`.env`); the app fails to connect without it |
| `INTERNAL_AUTH_SECRET` | **none** — the HMAC secret every service shares (at least 32 characters), from `.env`; the service refuses to start without it (finding N1) |
| `SERVER_PORT` | `8084` |
| `ZIPKIN_ENDPOINT` | `http://localhost:9411/api/v2/spans` |

Flyway migrations: `V1` schema, `V2` seed restrictions, `V3` make `email`
nullable. `ddl-auto=validate`.

## Tests

```bash
./gradlew test   # Testcontainers MySQL: account flow, auth, restrictions,
                 # favourites, repository slice, OpenAPI contract, ops; plus the
                 # shared-secret check (plain unit tests)
```

## Security ⚠️

`SecurityConfig` is `anyRequest().permitAll()` at the Spring Security layer — the
starter is there for BCrypt. Access control is `UserInternalAuthFilter` (finding
C1, fixed 2026-09-09): **every `/api/**` call needs a valid HMAC
`X-Internal-Auth` header** from an internal caller, and for
`/api/accounts/{id}` (and everything under it) the acting user in that header
must equal `{id}` → 403 otherwise. Passwords are BCrypt, `currentPassword` is
verified on change, and `authenticate` runs one BCrypt verify whether or not the
account exists (finding M2, so timing doesn't reveal usernames).

The shared secret (`INTERNAL_AUTH_SECRET`) has **no default** (finding N1, fixed
2026-11-02): the service refuses to start without a random value of at least 32
characters, from `.env` locally or the environment in a container. Keep `:8084`
bound to `127.0.0.1`. See `../IngredientCatalogue/SECURITY.md`.

**Limits** (finding N4; `Dto/Limits`): ≤ 100 restrictions of ≤ 100 chars per
account (the `PUT …/restrictions` body is now actually validated — it wasn't
before), ≤ 200 favourite substitutions per call, ≤ 1 000 favourite recipes per
account. Over a limit → 400.

## Status

**v1 complete.** Consumed directly by the UI (profile / restrictions /
favourites) and by the BFF (`/bff/login`, composition).
