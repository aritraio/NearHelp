# Codebase Map

This guide maps every directory and key file in the repository to its exact role, entry points, and responsibilities.

---

## 1. Repository Root

| Path | Role |
| :--- | :--- |
| [`docker-compose.yml`](docker-compose.yml) | Orchestrates 4 local services: `db` (PostGIS 16-3.4), `redis` (Redis 7), `backend` (:8000), `ai_service` (:8001). |
| [`.env.example`](.env.example) | Template of environment variables across DB, Redis, Backend, AI, Firebase, and Maps. |
| [`.pre-commit-config.yaml`](.pre-commit-config.yaml) | Configures trailing-whitespace, YAML/JSON checks, large-file cap (5 MB), and `ruff` / `ruff-format`. |
| [`.github/workflows/ci.yml`](.github/workflows/ci.yml) | CI pipeline running `ruff check .` on `backend/` & `ai_service/` and `docker compose config`. (Does not run `pytest` or Gradle). |
| [`decisions.md`](decisions.md) | 29 consolidated project decisions (`D01`–`D29`) with evidence links. |
| [`flow.md`](flow.md) | Runtime data-journey documentation. |
| [`data/regional/kolkata_facilities.json`](data/regional/kolkata_facilities.json) | Seed JSON of Kolkata hospitals, trauma centers, and AED stations loaded into `facilities`. |

---

## 2. Backend Service (`backend/`)

Entry point: [`backend/app/main.py`](backend/app/main.py)

### `backend/app/core/` — Configuration, Security, Middleware, Sockets
- [`config.py`](backend/app/core/config.py) — `Settings(BaseSettings)` loading `.env` / `../.env`, normalizing `postgres://` URLs to `postgresql+asyncpg://`, defining JWT expirations, CORS, and `INITIAL_ADMIN_*`.
- [`security.py`](backend/app/core/security.py) — `verify_password`, `get_password_hash` (bcrypt, 72-byte truncation), `create_access_token`, `create_refresh_token`, `decode_token`, `verify_token_type` (HS256 via `python-jose`).
- [`crypto.py`](backend/app/core/crypto.py) — `MedicalDataEncryption` (AES-256-GCM using `SHA-256(SECRET_KEY)` and 12-byte random nonces; legacy plaintext fallback on decrypt).
- [`dependencies.py`](backend/app/core/dependencies.py) — FastAPI dependencies: `get_current_user`, `get_current_active_user`, `get_current_admin_user`, `get_optional_current_user`. Calls `presence_service.record_activity`.
- [`middleware.py`](backend/app/core/middleware.py) — `IdempotencyMiddleware` (caches `<500` POST/PUT/PATCH responses when `Idempotency-Key` header is sent), `RateLimitMiddleware` (30 req/min sliding window on `POST .../auth/...`), `get_redis_client` with in-memory fallbacks.
- [`websocket_manager.py`](backend/app/core/websocket_manager.py) — `ConnectionManager` (`ws_manager` singleton) managing in-memory `_rooms` keyed by `incident_id` and publishing to Redis Pub/Sub.

### `backend/app/db/` — Database Engine & Bootstrap
- [`base.py`](backend/app/db/base.py) — SQLAlchemy `DeclarativeBase`.
- [`session.py`](backend/app/db/session.py) — Creates `async_engine` (`AsyncAdaptedQueuePool` for Postgres; `StaticPool` + spatial function stubs for SQLite), `get_db()` dependency, and `init_db()` (enables PostGIS, runs `create_all`, ad-hoc `ALTER TABLE`s, GIST indexes, seeds Kolkata facilities, and auto-bootstraps initial admin).
- [`backend/app/cli.py`](backend/app/cli.py) — CLI commands: `create-admin` and `purge-demo-users`.

### `backend/app/models/` — SQLAlchemy ORM Models
- [`user.py`](backend/app/models/user.py) — `User` (`users` table).
- [`user_presence.py`](backend/app/models/user_presence.py) — `UserPresence` (`user_presence` table).
- [`sos_event.py`](backend/app/models/sos_event.py) — `SOSEvent` (`sos_events` table).
- [`response.py`](backend/app/models/response.py) — `SOSResponse` (`responses` table).
- [`timeline_event.py`](backend/app/models/timeline_event.py) — `TimelineEvent` (`timeline_events` table).
- [`skill_verification.py`](backend/app/models/skill_verification.py) — `SkillVerification` (`skill_verifications` table).
- [`facility.py`](backend/app/models/facility.py) — `Facility` (`facilities` table).

### `backend/app/api/` — Route Handlers
- [`__init__.py`](backend/app/api/__init__.py) — Registers routers under `/api/v1`.
- [`auth.py`](backend/app/api/auth.py) — `/auth/*` registration, login, Google, phone OTP, anonymous, refresh, device, `/me`.
- [`users.py`](backend/app/api/users.py) — `/users/me*` profile, Medical ID, emergency contacts, photo upload, languages, FCM token, skills, location/availability.
- [`sos.py`](backend/app/api/sos.py) — `/sos/*` create, active feed, detail, respond, escalate, resolve, timeline, cancel.
- [`tracking_ws.py`](backend/app/api/tracking_ws.py) — WebSockets `/ws/tracking/{id}` & `/ws/sos/{id}`, plus REST `/api/v1/sos/{id}/tracking` & `/tracking/location`.
- [`ai.py`](backend/app/api/ai.py) & [`ai_ws.py`](backend/app/api/ai_ws.py) — AI proxy endpoints (`/ai/classify`, `/severity`, `/taxonomy`, `/agent/*`) and `/ws/ai/chat`.
- [`facilities.py`](backend/app/api/facilities.py) — `/facilities/*` nearby search, seed, detail, list.
- [`routing.py`](backend/app/api/routing.py) — `/routing/*` directions, detour, road hazards CRUD.
- [`admin.py`](backend/app/api/admin.py) & [`admin_users.py`](backend/app/api/admin_users.py) — Superuser endpoints for verification queue, stats, user CRUD, and demo user purge.

### `backend/app/services/` — Business Logic
- [`auth_service.py`](backend/app/services/auth_service.py), [`firebase_service.py`](backend/app/services/firebase_service.py), [`user_service.py`](backend/app/services/user_service.py), [`skill_service.py`](backend/app/services/skill_service.py), [`presence_service.py`](backend/app/services/presence_service.py)
- [`sos_service.py`](backend/app/services/sos_service.py), [`geo_service.py`](backend/app/services/geo_service.py), [`ranking_service.py`](backend/app/services/ranking_service.py), [`notification_service.py`](backend/app/services/notification_service.py)
- [`tracking_service.py`](backend/app/services/tracking_service.py), [`eta_service.py`](backend/app/services/eta_service.py), [`facility_service.py`](backend/app/services/facility_service.py), [`routing_service.py`](backend/app/services/routing_service.py)
- [`ai_client.py`](backend/app/services/ai_client.py) — HTTP client to `ai_service` + local Gemini/rule fallbacks (1,287 lines).

### `backend/app/admin_web/` & `backend/scripts/`
- [`admin_web/index.html`](backend/app/admin_web/index.html), [`app.js`](backend/app/admin_web/app.js), [`style.css`](backend/app/admin_web/style.css) — Same-origin Admin Command Center UI.
- [`scripts/device_smoke.py`](backend/scripts/device_smoke.py) — End-to-end two-client HTTP + WebSocket smoke script.
- `backend/uploads/` — Local disk store for avatars and skill certificates.

---

## 3. AI Microservice (`ai_service/`)

Entry point: [`ai_service/app/main.py`](ai_service/app/main.py)

| Path | Role |
| :--- | :--- |
| [`core/config.py`](ai_service/app/core/config.py) | `AISettings` (`GEMINI_API_KEY`, `GEMINI_MODEL="gemini-2.0-flash"`, `EMBEDDING_MODEL="models/text-embedding-004"`). |
| [`api/classify.py`](ai_service/app/api/classify.py) | `/classify`, `/taxonomy`, `/transcribe`, `/vision`. |
| [`api/severity.py`](ai_service/app/api/severity.py) | `/severity` triage scoring endpoint. |
| [`api/agent.py`](ai_service/app/api/agent.py) & [`api/agent_ws.py`](ai_service/app/api/agent_ws.py) | `/agent/chat`, `/init`, `/step`, `/protocols`, `/handover`, and `/ws/ai/chat`. |
| [`classifiers/crisis_types.py`](ai_service/app/classifiers/crisis_types.py) | 8 `CLINICAL_CONDITIONS_MATRIX` profiles, 7 `NON_MEDICAL_PROFILES`, and 6 `CRISIS_TYPES_TAXONOMY` categories. |
| [`classifiers/embedding_service.py`](ai_service/app/classifiers/embedding_service.py) | `ClinicalSemanticVectorizer` (384-dim deterministic hash + EN/BN/HI lexicon) & `EmbeddingService` (Gemini embeddings or local fallback). |
| [`classifiers/emergency_classifier.py`](ai_service/app/classifiers/emergency_classifier.py) | Orchestrates text + STT + Vision into `EmbeddingService.match_emergency`. |
| [`classifiers/severity_predictor.py`](ai_service/app/classifiers/severity_predictor.py) | Calls legacy `google.generativeai` or deterministic clinical rules (`0–100` score, Level `1–5`). |
| [`classifiers/speech_service.py`](ai_service/app/classifiers/speech_service.py) & [`vision_service.py`](ai_service/app/classifiers/vision_service.py) | Gemini audio/image analysis with hardcoded test/dev fallbacks. |
| [`agent/gemini_agent.py`](ai_service/app/agent/gemini_agent.py) | 5-node LangGraph `StateGraph` (`triage_node`, `protocol_retriever_node`, `safety_guardrail_node`, `reasoning_and_response_node`, `handover_node`). |
| [`agent/guardrails.py`](ai_service/app/agent/guardrails.py) | `HallucinationGuardrails` regex pre-query interception & post-LLM response sanitization. |
| [`agent/knowledge.py`](ai_service/app/agent/knowledge.py) | `CITATIONS_CATALOG`, `GROUNDED_PROTOCOLS` (8 conditions), `CONTRAINDICATION_RULES`. |
| [`agent/state.py`](ai_service/app/agent/state.py) | `EmergencyAgentState` TypedDict for LangGraph. |

---

## 4. Android Client (`android/app/src/main/java/com/example/nearhelp/`)

| Package / File | Role |
| :--- | :--- |
| [`NearHelpApplication.kt`](android/app/src/main/java/com/example/nearhelp/NearHelpApplication.kt) | Initializes `ThemeManager`, `TokenStorage`, `RetrofitClient`, and singleton repositories (`AuthRepository`, `UserRepository`, `RoutingRepository`, `AiAgentRepository`). |
| [`MainActivity.kt`](android/app/src/main/java/com/example/nearhelp/MainActivity.kt) | Edge-to-edge host, language context wrapper, handles `incident_id` notification deep links. |
| [`Navigation.kt`](android/app/src/main/java/com/example/nearhelp/Navigation.kt) & [`NavigationKeys.kt`](android/app/src/main/java/com/example/nearhelp/NavigationKeys.kt) | Navigation 3 `NavDisplay` back stack, scoped ViewModels, and route keys. |
| `data/api/` | Retrofit interfaces (`AuthApiService`, `UserApiService`, `SosApiService`, `AiAgentApiService`, `RoutingApiService`), `RetrofitClient`, `SessionTokens`, and `ws/LiveTrackingWebSocketClient`. |
| `data/local/` | [`TokenStorage.kt`](android/app/src/main/java/com/example/nearhelp/data/local/TokenStorage.kt) (`EncryptedSharedPreferences` + fallback) storing tokens, cached profile, pending SOS, active incident ID, local incident history, custom server URL, and optional Gemini key. |
| `data/location/` | [`LocationHelper.kt`](android/app/src/main/java/com/example/nearhelp/data/location/LocationHelper.kt) wrapping Android `LocationManager` and `Geocoder`. |
| `data/places/` | [`NearbyPlaces.kt`](android/app/src/main/java/com/example/nearhelp/data/places/NearbyPlaces.kt) & [`GoogleNearbyPlacesRepository.kt`](android/app/src/main/java/com/example/nearhelp/data/places/GoogleNearbyPlacesRepository.kt) (Places SDK New, 3 km radius). |
| `data/repository/` | `AuthRepository`, `UserRepository`, `IncidentRepository` (`AndroidIncidentRepository`), `RoutingRepository`, `AiAgentRepository`. |
| `notifications/` | [`NearHelpMessagingService.kt`](android/app/src/main/java/com/example/nearhelp/notifications/NearHelpMessagingService.kt) (`PushRegistration` + FCM message handler). |
| `i18n/` | [`AppLanguages.kt`](android/app/src/main/java/com/example/nearhelp/i18n/AppLanguages.kt) & `LocalizedText.kt` (EN/BN/HI UI strings + 31 spoken language preferences). |
| `theme/` | `Color.kt`, `Theme.kt`, `ThemeManager.kt`, `Type.kt` (Pure White, Soft White, OLED Teal, OLED Blue). |
| `ui/auth/` | `AuthViewModel`, `GoogleAuthentication.kt` (Credential Manager + FirebaseAuth), `SplashScreen`, `LoginScreen`, `SignUpScreen`. |
| `ui/home/` | [`VictimMainScreen.kt`](android/app/src/main/java/com/example/nearhelp/ui/home/VictimMainScreen.kt) (4-tab shell: Home, Chat, Map, Profile) & [`HomeScreen.kt`](android/app/src/main/java/com/example/nearhelp/ui/home/HomeScreen.kt). |
| `ui/incident/` | [`IncidentScreen.kt`](android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentScreen.kt), [`IncidentViewModel.kt`](android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt), [`MedicalSosPromptViewModel.kt`](android/app/src/main/java/com/example/nearhelp/ui/incident/MedicalSosPromptViewModel.kt), `IncidentPresentation.kt`. |
| `ui/assistant/` | `AiCrisisAssistantScreen`, `AiCrisisAssistantViewModel`, `AssistantAttachments` (currently text-chat focused). |
| `ui/map/` | `CommunityGeoMapScreen`, `CommunityGeoMapViewModel`, `NearbyPlacesViewModel`, `NearHelpGoogleMapView`. |
| `ui/profile/` | `ProfileScreen`, `ProfileViewModel` (Medical ID, contacts, skill certificate upload, appearance/language settings). |
| `ui/design/` & `ui/components/` | `CareComponents.kt` (`CareSosHoldButton`, `CareMap`, etc.), `EmergencyAlertOverlay`, `AIDisclaimerBanner`, and legacy/extra components. |
| `ui/screens/`, `ui/tracking/`, `ui/navigation/`, `ui/main/` | `GuardianRadarScreen`, `CrisisDispatchScreen`, plus legacy/template files (`LiveTrackingViewModel`, `RescueNavigationViewModel`, `MainScreen`). |
