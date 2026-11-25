# NearHelp AI

> **AI-Powered Community Emergency Response Network**  
> *Connecting People. Coordinating Rescue. Powered by AI.*

NearHelp AI is a multi-tier emergency response platform consisting of a **native Android client** (Kotlin + Jetpack Compose), a **core coordination backend** (FastAPI + PostgreSQL/PostGIS + Redis + same-origin Admin Web UI), and an **AI microservice** (FastAPI + LangGraph + Google Gemini + curated clinical protocols).

---

## Repository Structure

```text
NearHelp/
├── android/       # Native Android app (Kotlin, Jetpack Compose, Navigation 3, Maps/Places)
├── backend/       # FastAPI coordination API (:8000), SQLAlchemy models, Admin Web UI (/admin)
├── ai_service/    # FastAPI AI microservice (:8001), LangGraph agent, classifiers, guardrails
├── data/          # Regional seed datasets (Kolkata facilities, police, fire stations, blood banks)
├── docs/          # Organized architecture, API reference, guides, ADRs, and planning archives
├── AGENTS.md      # Guide for AI coding agents and maintainers
├── CONTRIBUTING.md
├── docker-compose.yml
└── render.yaml
```

---

## Documentation Suite

Browse the [Documentation Hub](docs/README.md) for the full reading order, or jump directly to a section:

### Architecture & System Design (`docs/architecture/`)

| Document | Purpose | Link |
| :--- | :--- | :--- |
| **Project Overview** | Vision, implemented vs. planned feature matrix, technology stack, and regional scope | [PROJECT_OVERVIEW.md](docs/architecture/PROJECT_OVERVIEW.md) |
| **Architecture** | Three-tier runtime topology, service boundaries, and divergence from early specs | [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) |
| **Codebase Map** | Annotated directory-by-directory map of Backend, AI Service, Android, and Data | [CODEBASE_MAP.md](docs/architecture/CODEBASE_MAP.md) |
| **System Design** | Deep dive into SOS dispatch, ranking math, tracking/WS state, routing, and caching | [SYSTEM_DESIGN.md](docs/architecture/SYSTEM_DESIGN.md) |
| **Database** | SQLAlchemy models, PostGIS spatial indexes, encryption at rest, and SQLite test stubs | [DATABASE.md](docs/architecture/DATABASE.md) |
| **AI System** | Classifiers, severity predictor, LangGraph 5-node agent, guardrails, and fallbacks | [AI_SYSTEM.md](docs/architecture/AI_SYSTEM.md) |
| **Data Flow** | Detailed runtime data-journey reference | [DATA_FLOW.md](docs/architecture/DATA_FLOW.md) |
| **Decision Register** | Consolidated engineering decision log | [DECISIONS.md](docs/architecture/DECISIONS.md) |
| **Mermaid Diagrams** | Architecture, communication, ERD, auth, workflow, AI, DFD, and deployment diagrams | [diagrams/README.md](docs/architecture/diagrams/README.md) |

### Reference, Operations & Guides (`docs/reference/`, `docs/guides/`, `docs/decisions/`)

| Document | Purpose | Link |
| :--- | :--- | :--- |
| **API Reference** | Complete REST and WebSocket endpoint reference derived from FastAPI routers | [API_REFERENCE.md](docs/reference/API_REFERENCE.md) |
| **User Flows** | Step-by-step journeys for Victims, Responders, AI Assistant, and Admins | [USER_FLOWS.md](docs/reference/USER_FLOWS.md) |
| **Security** | Auth flows, JWTs, AES-256-GCM medical ID, input validation, and exposed risks | [SECURITY.md](docs/reference/SECURITY.md) |
| **Testing** | Pytest suites, Android unit/Robolectric tests, smoke script, and observed results | [TESTING.md](docs/reference/TESTING.md) |
| **Deployment** | Docker Compose stack, environment variables, Android APK build, and runtime limits | [DEPLOYMENT.md](docs/reference/DEPLOYMENT.md) |
| **Known Issues** | Verified bugs, schema mismatches, security gaps, and technical debt | [KNOWN_ISSUES.md](docs/reference/KNOWN_ISSUES.md) |
| **Architecture Decisions (ADRs)** | 10 ADRs comparing original design choices with actual implementation | [docs/decisions/README.md](docs/decisions/README.md) |
| **Two-Phone Readiness** | End-to-end physical device setup and acceptance checklist | [DEVICE_READINESS.md](docs/guides/DEVICE_READINESS.md) |
| **UI Guidance** | Compose UI design guidance | [UI_GUIDANCE.md](docs/guides/UI_GUIDANCE.md) |
| **Android Redesign Notes** | Compose Care design system, localization, and Places API setup | [ANDROID_REDESIGN.md](docs/guides/ANDROID_REDESIGN.md) |
| **Historical Planning Specs** | Pre-implementation blueprint, proposal, design system, and master TODO list | [docs/planning/](docs/planning/) |

---

## Quick Start (Local Docker Stack)

```sh
# 1. Copy environment template and set your GEMINI_API_KEY (and SECRET_KEY for non-local use)
cp .env.example .env

# 2. Start PostgreSQL/PostGIS, Redis, Backend (:8000), and AI Service (:8001)
docker compose up -d --build

# 3. Verify backend health (checks real DB & PostGIS round trip)
curl http://localhost:8000/health
curl http://localhost:8001/health
```

- **Backend Swagger UI:** `http://localhost:8000/docs`
- **AI Service Swagger UI:** `http://localhost:8001/docs`
- **Admin Command Center:** `http://localhost:8000/admin`

### Creating / Managing an Administrator

The backend CLI in [`backend/app/cli.py`](backend/app/cli.py) provisions or promotes an administrator interactively:

```sh
# With Docker Compose:
docker compose exec backend python -m app.cli create-admin --email you@example.com --name "Administrator"

# Or locally from backend/:
python -m app.cli create-admin --email you@example.com --name "Administrator"
```

> **Important Security Note:** By default, [`backend/app/core/config.py`](backend/app/core/config.py) and [`backend/app/db/session.py`](backend/app/db/session.py) also auto-bootstrap an initial administrator from `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD` during startup if those variables are non-empty (and they currently carry hardcoded development defaults). Override or clear these in production; see [SECURITY.md](docs/reference/SECURITY.md) and [KNOWN_ISSUES.md](docs/reference/KNOWN_ISSUES.md).

### Building the Android App for Local Testing

From `android/` (requires JDK 17):

```sh
./gradlew :app:assembleDebug -PnearhelpBaseUrl=http://YOUR_LAN_IP:8000/
```

The debug APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`. See [DEPLOYMENT.md](docs/reference/DEPLOYMENT.md) and [DEVICE_READINESS.md](docs/guides/DEVICE_READINESS.md) for emulator URLs (`http://10.0.2.2:8000/`), Firebase push setup (`google-services.json`), and Google Places API (New) requirements.

---

## Technology Stack Summary (Verified from Source)

| Layer | Technology in Codebase |
| :--- | :--- |
| **Android App** | Kotlin `2.3.20`, Jetpack Compose BOM `2026.03.01`, Material 3, Navigation 3 `1.0.1`, Retrofit `2.11.0`, OkHttp `4.12.0`, Google Maps Compose `4.4.1`, Places SDK `6.0.2`, Firebase Auth/Messaging, AndroidX Security Crypto |
| **Backend API** | Python 3.11+, FastAPI `>=0.111.0`, SQLAlchemy `>=2.0.30` (asyncpg + GeoAlchemy2), `python-jose` (HS256 JWT), `bcrypt`, `cryptography` (AES-256-GCM), `firebase-admin`, `httpx` |
| **AI Microservice** | Python 3.11+, FastAPI, LangGraph `>=0.1.0`, `google-genai`, `google-generativeai`, deterministic 384-dim clinical vectorizer |
| **Database & Cache** | PostgreSQL 16 + PostGIS 3.4 (`postgis/postgis:16-3.4-alpine`), Redis 7 (`redis:7-alpine`) with in-memory fallback |
| **Admin UI** | Same-origin static HTML/CSS/Vanilla JS served by FastAPI at `/admin` |
