# NearHelp AI

> **AI-Powered Community Emergency Response Network**  
> *Connecting People. Coordinating Rescue. Powered by AI.*

NearHelp AI is a multi-tier emergency response platform consisting of a **native Android client** (Kotlin + Jetpack Compose), a **core coordination backend** (FastAPI + PostgreSQL/PostGIS + Redis + same-origin Admin Web UI), and an **AI microservice** (FastAPI + LangGraph + Google Gemini + curated clinical protocols).

---

## Documentation Suite

### Reverse-Engineered Technical Documentation (Repository Root)

| Document | Purpose | Link |
| :--- | :--- | :--- |
| **Project Overview** | Vision, implemented vs. planned feature matrix, technology stack, and regional scope | [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) |
| **Architecture** | Three-tier runtime topology, service boundaries, and divergence from early specs | [ARCHITECTURE.md](ARCHITECTURE.md) |
| **Codebase Map** | Annotated directory-by-directory map of Backend, AI Service, Android, and Data | [CODEBASE_MAP.md](CODEBASE_MAP.md) |
| **System Design** | Deep dive into SOS dispatch, ranking math, tracking/WS state, routing, and caching | [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) |
| **Database** | SQLAlchemy models, PostGIS spatial indexes, encryption at rest, and SQLite test stubs | [DATABASE.md](DATABASE.md) |
| **API Reference** | Complete REST and WebSocket endpoint reference derived from FastAPI routers | [API_REFERENCE.md](API_REFERENCE.md) |
| **User Flows** | Step-by-step journeys for Victims, Responders, AI Assistant, and Admins | [USER_FLOWS.md](USER_FLOWS.md) |
| **AI System** | Classifiers, severity predictor, LangGraph 5-node agent, guardrails, and fallbacks | [AI_SYSTEM.md](AI_SYSTEM.md) |
| **Security** | Auth flows, JWTs, AES-256-GCM medical ID, input validation, and exposed risks | [SECURITY.md](SECURITY.md) |
| **Testing** | Pytest suites, Android unit/Robolectric tests, smoke script, and observed results | [TESTING.md](TESTING.md) |
| **Deployment** | Docker Compose stack, environment variables, Android APK build, and runtime limits | [DEPLOYMENT.md](DEPLOYMENT.md) |
| **Known Issues** | Verified bugs, schema mismatches, security gaps, dead/template code, and debt | [KNOWN_ISSUES.md](KNOWN_ISSUES.md) |
| **Agents Guide** | Guide for AI coding agents and human maintainers working in this repository | [AGENTS.md](AGENTS.md) |

### Diagrams, Decisions & Historical Specs

| Resource | Description | Link |
| :--- | :--- | :--- |
| **Mermaid Diagrams** | 8 architecture, communication, ERD, auth, workflow, AI, and deployment diagrams | [docs/diagrams/README.md](docs/diagrams/README.md) |
| **Architecture Decisions (ADRs)** | 10 ADRs comparing original design choices with actual implementation | [docs/decisions/README.md](docs/decisions/README.md) |
| **Project Decision Register** | Consolidated engineering decision log | [decisions.md](decisions.md) |
| **Application & Data Flow** | Detailed runtime data-journey reference | [flow.md](flow.md) |
| **Two-Phone Readiness** | End-to-end physical device setup and acceptance checklist | [docs/DEVICE_READINESS.md](docs/DEVICE_READINESS.md) |
| **Android Redesign Notes** | Compose Care design system, localization, and Places API setup | [android/REDESIGN.md](android/REDESIGN.md) |
| **Original Reference Specs** | Pre-implementation blueprint, proposal, design system, and master TODO list | [docs/architecture.md](docs/architecture.md), [docs/design.md](docs/design.md), [docs/proposal.md](docs/proposal.md), [docs/todos.md](docs/todos.md) |

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

> **Important Security Note:** By default, [`backend/app/core/config.py`](backend/app/core/config.py) and [`backend/app/db/session.py`](backend/app/db/session.py) also auto-bootstrap an initial administrator from `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD` during startup if those variables are non-empty (and they currently carry hardcoded development defaults). Override or clear these in production; see [SECURITY.md](SECURITY.md) and [KNOWN_ISSUES.md](KNOWN_ISSUES.md).

### Building the Android App for Local Testing

From `android/` (requires JDK 17):

```sh
./gradlew :app:assembleDebug -PnearhelpBaseUrl=http://YOUR_LAN_IP:8000/
```

The debug APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`. See [DEPLOYMENT.md](DEPLOYMENT.md) and [docs/DEVICE_READINESS.md](docs/DEVICE_READINESS.md) for emulator URLs (`http://10.0.2.2:8000/`), Firebase push setup (`google-services.json`), and Google Places API (New) requirements.

---

## Technology Stack Summary (Verified from Source)

| Layer | Technology in Codebase |
| :--- | :--- |
| **Android App** | Kotlin `2.3.20`, Jetpack Compose BOM `2026.03.01`, Material 3, Navigation 3 `1.0.1`, Retrofit `2.11.0`, OkHttp `4.12.0`, Google Maps Compose `4.4.1`, Places SDK `6.0.2`, Firebase Auth/Messaging, AndroidX Security Crypto |
| **Backend API** | Python 3.11+, FastAPI `>=0.111.0`, SQLAlchemy `>=2.0.30` (asyncpg + GeoAlchemy2), `python-jose` (HS256 JWT), `bcrypt`, `cryptography` (AES-256-GCM), `firebase-admin`, `httpx` |
| **AI Microservice** | Python 3.11+, FastAPI, LangGraph `>=0.1.0`, `google-genai`, `google-generativeai`, deterministic 384-dim clinical vectorizer |
| **Database & Cache** | PostgreSQL 16 + PostGIS 3.4 (`postgis/postgis:16-3.4-alpine`), Redis 7 (`redis:7-alpine`) with in-memory fallback |
| **Admin UI** | Same-origin static HTML/CSS/Vanilla JS served by FastAPI at `/admin` |
