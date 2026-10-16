# SilkyRoad backend

Spring Boot 4.1.1 REST API for the Angular app in the parent directory.
Stateless JWT auth, Postgres in Docker. The application always runs against
Postgres; H2 is a test-only dependency used to boot the context during tests.

## Run with Docker (recommended)

Starts Postgres and the API together. Needs only Docker - no JDK, no Maven.
Run from the repository root, where `docker-compose.yml` lives:

```bash
docker compose up --build
```

- API: <http://localhost:8080>
- Postgres: `localhost:5432`, database/user/password all `silkyroad`

Data lives in the `db-data` volume and survives `docker compose down`. To wipe
it, add `-v`.

## Run without Docker

Needs a JDK 21 or newer and a reachable Postgres. Maven is **not** required -
`mvnw` downloads it. The easiest Postgres is the Compose one: `docker compose up
db` from the repository root.

```bash
cd backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/silkyroad \
SPRING_DATASOURCE_USERNAME=silkyroad \
SPRING_DATASOURCE_PASSWORD=silkyroad \
./mvnw spring-boot:run        # PowerShell: set the vars with $env:NAME first
```

The database is seeded with one account: `admin` / `admin123`.

## Database configuration

The application has no datasource defaults and always expects Postgres through
the `SPRING_DATASOURCE_*` environment variables (Compose sets them). There are
no Spring profiles to juggle.

## Test

```bash
./mvnw test
```

Tests boot the full context against an in-memory H2 database, auto-configured
from the test-scoped H2 dependency, so they need no running database. H2 is not
part of the shipped application.

## Endpoints

| Method | Path                | Auth   | Purpose                                 |
| ------ | ------------------- | ------ | --------------------------------------- |
| POST   | `/api/users`        | public | Register                                |
| POST   | `/api/users/login`  | public | Returns `{ token, expiresIn, user }`    |
| GET    | `/api/users/me`     | bearer | The token owner; restores a session     |
| GET    | `/api/users`        | bearer | List accounts                           |
| GET    | `/api/users/{id}`   | bearer | One account                             |
| GET    | `/api/items`        | bearer | List items, `?creatorId=` or `?status=` |
| GET    | `/api/items/{id}`   | bearer | One item                                |
| POST   | `/api/items`        | bearer | Create; owner comes from the token      |
| PUT    | `/api/items/{id}`   | bearer | Replace editable fields; owner only     |
| DELETE | `/api/items/{id}`   | bearer | Delete; owner only                      |

Send the token as `Authorization: Bearer <token>`. Everything except register
and login returns 401 without one, and 403 when you touch another user's
listing.

## Security notes

Current state, as a baseline to harden against later:

- Passwords are stored only as BCrypt hashes, in `users.password_hash`. The
  plain password is never persisted and never returned by any endpoint.
- Tokens are HS256 JWTs signed with `silkyroad.jwt.secret`, valid 12 hours.
  The subject is the user id. Override the secret outside development.
- Login returns one generic message for both an unknown username and a wrong
  password, so responses cannot be used to enumerate accounts.
- Item ownership is taken from the token, not the request body, so a client
  cannot create or edit a listing as another user.
- Stateless: no server session, so CSRF protection is disabled. That is safe
  only because the token travels in a header a cross-site form cannot set.

Not yet addressed: no refresh tokens or revocation, no rate limiting on login,
no HTTPS enforcement, no pagination, and `ddl-auto=update` instead of managed
migrations.

## Layout

Packages are grouped by feature, not by layer:

```
user/         User entity, repository, service, controller, mapper, TokenService, dto/
marketplace/  MarketplaceItem entity, status enum, repository, service, controller, mapper, dto/
common/       ApiError, the four domain exceptions, GlobalExceptionHandler
config/       SecurityConfig, JwtConfig, PasswordConfig, DevDataSeeder
```
