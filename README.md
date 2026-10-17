# SilkyRoad

A small marketplace web app: list items, add pictures, and comment on other
people's listings. Angular frontend, Spring Boot backend, PostgreSQL database.

## Run it

You only need Docker Desktop. From this folder:

```bash
cp .env.example .env      # Windows PowerShell: copy .env.example .env
docker compose up --build
```

Then open <http://localhost:8081>.

The first time, click **Create one** on the login page to register an account.

To stop it: `docker compose down` (add `-v` to also wipe the database).

## What runs where

| Part     | URL / Access                          |
| -------- | ------------------------------------- |
| Frontend | <http://localhost:8081>               |
| API      | <http://localhost:8081/api>           |
| Database | inside Docker only (not exposed)      |

Look at the database with: `docker compose exec db psql -U silkyroad silkyroad`

## Notes

- `.env` holds the JWT secret and is git-ignored. Copy it from `.env.example`;
  the app will not start without `SILKYROAD_JWT_SECRET` set.
- Frontend lives in this folder, the backend in `backend/` (see `backend/README.md`).
