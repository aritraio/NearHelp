# Deployment & Configuration Guide

See [docs/diagrams/08-deployment-architecture.md](../architecture/diagrams/08-deployment-architecture.md) for the deployment topology diagram.

---

## 1. Local Docker Compose Stack

Source: [`docker-compose.yml`](../../docker-compose.yml), [`backend/Dockerfile`](../../backend/Dockerfile), [`ai_service/Dockerfile`](../../ai_service/Dockerfile)

| Service | Container Name | Image / Base | Host Port | Healthcheck / Startup |
| :--- | :--- | :--- | :---: | :--- |
| `db` | `nearhelp_postgis` | `postgis/postgis:16-3.4-alpine` | `5432` | `pg_isready` (volume: `postgres_data`) |
| `redis` | `nearhelp_redis` | `redis:7-alpine` (`--appendonly yes`) | `6379` | `redis-cli ping` (volume: `redis_data`) |
| `backend` | `nearhelp_backend` | `python:3.11-slim` + `libpq-dev`, `libgeos-dev`, `libgdal-dev` | `8000` | `uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload` |
| `ai_service` | `nearhelp_ai_service` | `python:3.11-slim` | `8001` | `uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload` |

### Running the Stack

```sh
cp .env.example .env
# Edit .env: set GEMINI_API_KEY and a secure SECRET_KEY
docker compose up -d --build
curl http://localhost:8000/health
curl http://localhost:8001/health
```

> **Critical Scaling Constraint:** Keep `backend` at **1 worker / 1 replica**. WebSocket rooms (`ws_manager._rooms`) and live telemetry (`TrackingService._live_telemetry_cache`) are stored in process memory, and no Redis Pub/Sub subscriber is implemented.

---

## 2. Environment Variables Inventory

Compiled from [`.env.example`](../../.env.example), [`backend/app/core/config.py`](../../backend/app/core/config.py), [`ai_service/app/core/config.py`](../../ai_service/app/core/config.py), and [`docker-compose.yml`](../../docker-compose.yml):

### Shared / Database / Cache
- `ENVIRONMENT` (`development`), `DEBUG` (`True`), `PROJECT_NAME`, `API_V1_STR` (`/api/v1`), `CORS_ORIGINS`
- `POSTGRES_SERVER`, `POSTGRES_PORT` (`5432`), `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` (`nearhelp_db`)
- `DATABASE_URL` (`postgresql+asyncpg://...`), `DATABASE_SYNC_URL` (`postgresql://...`)
- `REDIS_HOST`, `REDIS_PORT` (`6379`), `REDIS_DB` (`0`), `REDIS_PASSWORD`, `REDIS_URL`

### Backend Security, Admin & Integrations
- `BACKEND_HOST` (`0.0.0.0`), `BACKEND_PORT` (`8000`)
- `SECRET_KEY` (signs HS256 JWTs **and** derives AES-256-GCM medical encryption key), `ALGORITHM` (`HS256`)
- `ACCESS_TOKEN_EXPIRE_MINUTES` (`15` in `config.py`, `60` in `.env.example`), `REFRESH_TOKEN_EXPIRE_DAYS` (`7`), `ANONYMOUS_TOKEN_EXPIRE_MINUTES` (`60` in `config.py`, currently unused)
- `RATE_LIMIT_AUTH_PER_MINUTE` (`30`), `RATE_LIMIT_DEFAULT_PER_MINUTE` (`100`), `IDEMPOTENCY_EXPIRE_SECONDS` (`86400`)
- `AI_SERVICE_URL` (`http://localhost:8001` locally, `http://ai_service:8001` in Docker Compose)
- `GEMINI_API_KEY`, `GEMINI_MODEL` (`gemini-3.5-flash-lite` in backend `config.py`)
- `FIREBASE_PROJECT_ID`, `FIREBASE_CREDENTIALS_PATH`, `FIREBASE_CREDENTIALS_JSON`, `FCM_SERVER_KEY`
- `INITIAL_ADMIN_EMAIL`, `INITIAL_ADMIN_PASSWORD`, `INITIAL_ADMIN_NAME` (auto-bootstrapped in `init_db()` — override or clear in non-dev environments!)
- **Note:** `routing_service.py` reads `GOOGLE_MAPS_API_KEY` via `getattr(settings, "GOOGLE_MAPS_API_KEY", "")`, which is present in `.env.example` **but not declared in `backend/app/core/config.py` `Settings`**, so `Settings` ignores it.

### AI Service
- `AI_SERVICE_HOST`, `AI_SERVICE_PORT` (`8001`)
- `GEMINI_API_KEY`, `GEMINI_MODEL` (`gemini-2.0-flash` in `ai_service/app/core/config.py`), `EMBEDDING_MODEL` (`models/text-embedding-004`)
- `GOOGLE_CLOUD_PROJECT`, `GOOGLE_APPLICATION_CREDENTIALS`

---

## 3. Android Build & Device Deployment

Source: [`android/app/build.gradle.kts`](../../android/app/build.gradle.kts), [`docs/DEVICE_READINESS.md`](../guides/DEVICE_READINESS.md)

- **SDK Targets:** `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24`, Java `17`.
- **Backend URL Injection:** `BuildConfig.API_BASE_URL` defaults to a `trycloudflare.com` URL in `build.gradle.kts`. Override per build with `-PnearhelpBaseUrl=...`:
  ```sh
  cd android
  # Same Wi-Fi LAN testing:
  ./gradlew :app:assembleDebug -PnearhelpBaseUrl=http://YOUR_LAN_IP:8000/
  # Android Emulator:
  ./gradlew :app:assembleDebug -PnearhelpBaseUrl=http://10.0.2.2:8000/
  # Hosted HTTPS backend:
  ./gradlew :app:assembleDebug -PnearhelpBaseUrl=https://YOUR_BACKEND_HOST/
  ```
- **Firebase & Google Maps / Places:**
  - Place `google-services.json` in `android/app/` to activate `com.google.gms.google-services` (enabling FCM and Firebase Google Sign-In).
  - Ensure **Places API (New)** and **Maps SDK for Android** are enabled on the Google Cloud project key in `strings.xml`.

---

## 4. Production Readiness Gaps

- **Dev Commands in Dockerfiles:** Both `Dockerfile`s run `uvicorn ... --reload` and bind-mount source directories in `docker-compose.yml`.
- **Exposed Internal Ports:** `db` (`5432`), `redis` (`6379`), and `ai_service` (`8001`) are published on host ports.
- **No Versioned Migrations:** `init_db()` uses `create_all` instead of Alembic migrations.
- **Ephemeral Local Uploads:** `backend/uploads/` is stored on container/host local disk rather than S3/GCS.
- **Unimplemented Cloud Targets:** `docs/architecture.md` mentions Google Cloud Run, Prometheus `/metrics`, Grafana, and Sentry; none are implemented in the repository.
