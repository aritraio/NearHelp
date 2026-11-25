# System Architecture

## 1. High-Level Architecture

NearHelp AI is structured as a **three-tier system**:

1. **Client Layer:**
   - **Android Mobile App** (`android/`): Native Kotlin + Jetpack Compose application using Navigation 3, ViewModels + StateFlow, Retrofit/OkHttp, Google Maps SDK, Places SDK (New), Firebase Messaging, and AndroidX `EncryptedSharedPreferences`.
   - **Admin Web Panel** (`backend/app/admin_web/`): Single-page Vanilla HTML/CSS/JS dashboard served directly by FastAPI at `/admin`.
2. **Application & AI Services Layer:**
   - **Core Backend Service** (`backend/`, port `8000`): FastAPI monolith handling authentication, profiles, encrypted Medical ID, skill verification, SOS lifecycle, PostGIS spatial queries, responder ranking, FCM push dispatch, WebSocket live tracking, routing estimates, and admin APIs.
   - **AI Microservice** (`ai_service/`, port `8001`): Separate FastAPI service running multimodal emergency classification, severity scoring, and a 5-node LangGraph emergency assistant with clinical guardrails.
3. **Data & External Services Layer:**
   - **PostgreSQL 16 + PostGIS 3.4**: Authoritative relational & spatial store (`users`, `user_presence`, `sos_events`, `responses`, `timeline_events`, `skill_verifications`, `facilities`).
   - **Redis 7**: Ephemeral cache for HTTP `Idempotency-Key` responses and sliding-window authentication rate limits (with automatic in-memory fallback when Redis is unreachable), plus unused Redis Pub/Sub publishing for tracking rooms.
   - **Local Filesystem** (`backend/uploads/`): Stores uploaded avatar images and skill verification certificates served statically under `/uploads`.
   - **External Cloud Services**: Google Gemini API, Firebase Auth & Cloud Messaging (FCM), Google Maps SDK & Places API (New).

See [docs/diagrams/01-system-architecture.md](diagrams/01-system-architecture.md) for the full architecture diagram, [docs/diagrams/02-frontend-backend-communication.md](diagrams/02-frontend-backend-communication.md) for client-server transport details, and the Data Flow Diagrams ([DFD Level 0](diagrams/dfd-level-0-context.md), [DFD Level 1](diagrams/dfd-level-1-system.md), [Level 2 Auth](diagrams/dfd-level-2-authentication.md), [Level 2 Core Workflow](diagrams/dfd-level-2-core-workflow.md), [Level 2 AI](diagrams/dfd-level-2-ai-processing.md)).

---

## 2. Service Boundaries & Ownership

| Service / Component | Port / Entry Point | Owns | Communicates With |
| :--- | :--- | :--- | :--- |
| **Android Client** | `MainActivity.kt`, `NearHelpApplication.kt` | UI state, local token/SOS persistence, foreground GPS (`LocationHelper`), Google Maps & Places UI | Backend (`:8000` REST + WS), Google Maps/Places SDK, Firebase FCM/Auth, Google Gemini REST API (direct branch in `AiAgentRepository`) |
| **Backend API** | `:8000` (`backend/app/main.py`) | Identity, SOS state machine, PostGIS queries, ranking, WS rooms, `/admin` UI, `/uploads` files | PostgreSQL/PostGIS, Redis, AI Service (`:8001`), Firebase Admin SDK, Gemini API (fallback), Google Routes API v2 (unconfigured) |
| **AI Microservice** | `:8001` (`ai_service/app/main.py`) | Classification embeddings, severity scoring, LangGraph agent state, `GROUNDED_PROTOCOLS`, `HallucinationGuardrails` | Backend API (via HTTP), Google Gemini API |
| **PostgreSQL + PostGIS** | `:5432` | 7 relational tables + 3 GiST spatial indexes | Backend API (`asyncpg`) |
| **Redis** | `:6379` | Middleware rate-limit sorted sets, idempotency cache (`24h` TTL) | Backend API (`redis.asyncio`) |

---

## 3. Architectural Divergences from Original Specifications

Inspecting the actual code reveals several important differences from [`docs/architecture.md`](../planning/architecture.md):

1. **AI Severity Blocks SOS Creation:** `docs/architecture.md` specifies that AI processing runs *in parallel* so it never blocks the SOS critical path. In [`backend/app/services/sos_service.py`](../../backend/app/services/sos_service.py), `create_sos_event` **synchronously awaits** `ai_client.predict_severity()` (up to 10s timeout) *before* inserting `SOSEvent` or notifying responders.
2. **Direct-to-Gemini Bypass on Android:** When a user-supplied Gemini API key is stored in `TokenStorage`, [`AiAgentRepository.kt`](../../android/app/src/main/java/com/example/nearhelp/data/repository/AiAgentRepository.kt) calls the Gemini REST endpoint directly from the mobile device *before* calling the backend `/api/v1/ai/agent/chat` proxy. This bypasses server-side LangGraph guardrails (`HallucinationGuardrails`) unless no local key is set or the direct call fails.
3. **Single-Worker WebSocket State:** `ConnectionManager` ([`backend/app/core/websocket_manager.py`](../../backend/app/core/websocket_manager.py)) publishes broadcasts to Redis Pub/Sub (`channel:tracking:{incident_id}`), **but no subscriber reads from Redis**. Rooms and `TrackingService._live_telemetry_cache` exist only in single-process memory.
4. **No `messages` or `ai_summaries` Tables:** Participant chat is stored as `TimelineEvent` rows (`event_type="chat_message"`) in `timeline_events`, and AI triage output is stored in `sos_events.ai_triage_data` JSON.
5. **In-Process Admin Web Instead of Next.js:** The planned `admin_dashboard/` Next.js app does not exist; an admin SPA is served from `backend/app/admin_web/` at `/admin`.
6. **RAG / Vector DB Removed:** ChromaDB and document ingestion were removed in commit `9699a81`; AI protocols live in Python dictionaries (`GROUNDED_PROTOCOLS` in [`ai_service/app/agent/knowledge.py`](../../ai_service/app/agent/knowledge.py)).
7. **Dual API Prefixes:** Backend mounts both `/api/v1/*` and `/api/*` compatibility aliases in [`backend/app/main.py`](../../backend/app/main.py); AI service mounts both `/api/v1/*` and root `/` aliases in [`ai_service/app/main.py`](../../ai_service/app/main.py).

---

## 4. Communication Patterns

- **REST (JSON over HTTP/HTTPS):** Used for authentication, profile CRUD, SOS creation/acceptance/resolution/cancellation, facility queries, routing, admin management, and AI triage/chat.
- **WebSockets:**
  - `/ws/tracking/{incident_id}` (and alias `/ws/sos/{incident_id}`): Bidirectional live GPS telemetry, responder status transitions, incident action logs, and persisted participant chat.
  - `/ws/ai/chat` (and `/api/v1/ws/ai/chat`): Exists on both backend and AI service for streaming AI assistant chat, step toggles, and 110 BPM metronome sync; **however, the Android app currently uses HTTP `POST /api/v1/ai/agent/chat` rather than this WebSocket**.
- **Firebase Cloud Messaging (FCM):** High-priority data messages wake responder devices and deep-link `MainActivity` to `LiveTrackingNavKey(incidentId)`.
- **Client Polling Fallback:** Android's `IncidentViewModel` polls `GET /api/v1/sos/{id}` and `/tracking` every **5 seconds** while an incident is open, polls `GET /api/v1/sos/active` every **10 seconds** when browsing the feed, and `HomeScreen` polls `GET /api/v1/sos/active` every **4 seconds** for the incoming emergency alert overlay.
