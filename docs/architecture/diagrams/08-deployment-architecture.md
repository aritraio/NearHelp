# 8. Deployment Architecture

This diagram shows the **implemented local Docker Compose topology** alongside **LAN device testing** and **planned/partially configured cloud components**.

```mermaid
flowchart TB
    subgraph DevHost["Local Host / Docker Compose Network (nearhelp_network)"]
        direction TB
        DB["nearhelp_postgis (postgis/postgis:16-3.4-alpine, :5432, volume: postgres_data)"]
        Redis["nearhelp_redis (redis:7-alpine --appendonly yes, :6379, volume: redis_data)"]
        Backend["nearhelp_backend (Python 3.11-slim, uvicorn :8000 --reload, 1 worker)"]
        AIService["nearhelp_ai_service (Python 3.11-slim, uvicorn :8001 --reload)"]

        Backend -->|"asyncpg"| DB
        Backend -->|"redis.asyncio"| Redis
        Backend -->|"http://ai_service:8001"| AIService
    end

    subgraph MobileBuild["Android Client Build"]
        Gradle["build.gradle.kts (-PnearhelpBaseUrl=...)"]
        DefaultURL["Default BuildConfig.API_BASE_URL (Cloudflare Tunnel URL)"]
        LANBuild["LAN APK (http://HOST_LAN_IP:8000/)"]
    end

    subgraph CloudPlanned["External & Planned Hosting"]
        Firebase["Firebase Cloud Messaging & Auth"]
        Gemini["Google Gemini API"]
        CloudRun["Planned: Google Cloud Run + Prometheus/Grafana (Not configured in repo)"]
    end

    MobileBuild -->|"HTTPS/WSS or LAN HTTP/WS"| Backend
    Backend -->|"Outbound HTTPS"| Firebase
    Backend -->|"Outbound HTTPS"| Gemini
    AIService -->|"Outbound HTTPS"| Gemini
```

## Deployment Notes & Verified Boundaries

- **Single-worker constraint**: `ws_manager` in [`backend/app/core/websocket_manager.py`](../../backend/app/core/websocket_manager.py) stores WebSocket rooms in process memory and `TrackingService._live_telemetry_cache` is process-local. Redis Pub/Sub publishes messages (`channel:tracking:{incident_id}`), but no subscriber loop is implemented. Multi-worker deployments will miss socket broadcasts across workers.
- **Port exposure**: `docker-compose.yml` exposes `:8001` (`ai_service`), `:5432` (`db`), and `:6379` (`redis`) directly to the host, and both Dockerfiles start `uvicorn` with `--reload` (development mode).
- **Android default endpoint**: [`android/app/build.gradle.kts`](../../android/app/build.gradle.kts) defaults `API_BASE_URL` to a `trycloudflare.com` tunnel URL unless overridden via `-PnearhelpBaseUrl=...` or changed in-app via the Login screen's Server Settings dialog.
- **Unimplemented planned infra**: `docs/architecture.md` references Google Cloud Run, Prometheus, Grafana, Sentry, and a Next.js `admin_dashboard/`. None of those manifests or directories exist in this repository (the actual admin panel is served statically by FastAPI at `/admin`).
