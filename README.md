# User service

Accounts, authentication, dietary restrictions, and favourites (recipes +
per-recipe ingredient substitutions).

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![Build](https://img.shields.io/badge/build-Gradle-blue)
![DB](https://img.shields.io/badge/MySQL-8.4-blue)
![Port](https://img.shields.io/badge/port-8084-lightgrey)

## Model

- **`Account`** — `id`, `username` (unique), `email` (unique), `displayName`,
  BCrypt `passwordHash`, `Set<String> restrictions` (free-text codes),
  `List<Long> favoriteRecipeIds`, timestamps.
- **`FavoriteAlternative`** — per `(accountId, recipeId, ingredientId)`, a
  preferred `replacementIngredientId`.
- **`Restriction`** — the seeded advisory catalogue (`code`, `label`, `kind`,
  `description`). Accounts store codes as **free text**; the catalogue only
  populates pickers.

## API

### `/api/accounts`
| | |
|---|---|
| `POST /` | register (`username`, `email`, `displayName`, `password`) → `AccountResponse` |
| `GET /{id}` | one account |
| `PUT /{id}` | update `email` / `displayName` |
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
cd .. && docker compose up --build user

docker compose up -d mysql       # local — schema `user_service`
./gradlew bootRun
```

## Configuration (env)

| Var | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3309/user_service` |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | `myuser` / `secret` |
| `SERVER_PORT` | `8084` |
| `ZIPKIN_ENDPOINT` | `http://localhost:9411/api/v2/spans` |

Flyway migrations; `ddl-auto=validate`.

## Tests

```bash
./gradlew test   # Testcontainers MySQL: account flow, auth, restrictions,
                 # favourites, repository slice, OpenAPI contract, ops
```

## Security ⚠️

`SecurityConfig` is `anyRequest().permitAll()` — **the security starter is on
the classpath only to force BCrypt**, not to gate anything. Passwords are
BCrypt and `currentPassword` is verified on change, **but every other endpoint
is unauthenticated**: anyone who can reach `:8084` can read any account's PII,
change its email/display name, overwrite its restrictions, or delete it. IDs are
sequential.

This is **finding C1** in `../IngredientCatalogue/SECURITY.md` and the top
open item. Intended fix: the BFF forwards a verified identity and this service
enforces `caller == {id}`. Until then, bind `:8084` to `127.0.0.1`.

## Status

**v1 complete.** Consumed directly by the UI (profile / restrictions /
favourites) and by the BFF (`/bff/login`, composition).
