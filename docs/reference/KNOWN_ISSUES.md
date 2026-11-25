# Known Issues, Technical Debt & Unverified Gaps

Every item below was directly observed in the repository source code, configuration, or test output.

---

## 1. Security & Credential Issues

1. **Direct-to-Gemini Bypass in Android (`AiAgentRepository.kt:101`):** When `tokenStorage?.getGeminiApiKey()` is populated on-device, `chatWithAgent()` calls the Gemini REST API directly from the phone before reaching the backend `/api/v1/ai/agent/chat` proxy, bypassing server-side LangGraph guardrails.
2. **Committed Google Maps API Key (`android/app/src/main/res/values/strings.xml:3`):** `google_maps_key` is stored directly in version-controlled XML.
3. **Default Admin Auto-Bootstrap (`backend/app/core/config.py:62-64`, `backend/app/db/session.py:167-194`):** `init_db()` automatically creates/promotes a superuser using default `INITIAL_ADMIN_EMAIL` / `INITIAL_ADMIN_PASSWORD` values unless explicitly cleared or overridden, contradicting `README.md`.
4. **Dev Firebase Auth Bypass (`backend/app/services/firebase_service.py`):** When `ENVIRONMENT` is `development`/`test`/`testing`, `/api/v1/auth/google` accepts raw emails, `mock_*` strings, and unverified JWT claims.
5. **No-Op Phone OTP (`backend/app/api/auth.py`, `backend/app/services/auth_service.py`):** `/phone/send-otp` does not send an SMS/OTP, and `/phone/verify` accepts any `otp_code` of length `>= 4` without verification.
6. **Unauthenticated Static File Access (`backend/app/main.py:94`):** `/uploads` exposes all uploaded user avatars and skill certificates without authorization checks.
7. **Unauthenticated Read/Mutation Routes:**
   - `GET /api/v1/sos/{id}`, `/timeline`, `/tracking`, and tokenless `WS /ws/tracking/{id}` expose incident details, coordinates, and chat to anyone holding an incident UUID.
   - `POST /api/v1/sos/{id}/escalate`, `POST /api/v1/facilities/seed`, `POST|DELETE /api/v1/routing/hazards`, and `/api/v1/ai/*` lack auth dependencies.
8. **Encryption Key Coupled to `SECRET_KEY` (`backend/app/core/crypto.py`):** Changing `SECRET_KEY` breaks decryption of stored medical conditions/allergies/notes. Additionally, `blood_group`, `emergency_contacts`, and `fcm_token` are unencrypted, and `TokenStorage.kt` silently falls back to unencrypted `SharedPreferences` on KeyStore failure.
9. **Permissive CORS & Exception Exposure:** `CORS_ORIGINS` includes `"*"` with `allow_credentials=True`; `global_exception_handler` returns `str(exc)` to clients.

---

## 2. Functional Bugs & Schema Mismatches

1. **3 Failing Backend Tests (`backend/tests/test_ai_agent.py`):** When `ai_service` is offline and `GEMINI_API_KEY` is present in `backend/.env`, `AIClient._local_fallback_agent_chat` calls live Gemini **before** checking deterministic contraindication and CPR-depth rules, causing 3 test failures (`97 passed, 3 failed`) and bypassing `ContraindicationAlert` payloads.
2. **Dropped Fields in `SOSService` -> `SeverityRequest` (`backend/app/services/sos_service.py:108-114`):** `SOSService.create_sos_event` instantiates `SeverityRequest(crisis_type=..., sub_type=..., text=..., symptoms=..., photo_url=...)`, but `backend/app/schemas/ai.py` defines `emergency_type` and `detected_symptoms` (with no `crisis_type`, `symptoms`, or `photo_url` fields). Pydantic silently drops those 3 fields.
3. **Android `UserRepository` Bypasses `SessionTokens` (`UserRepository.kt:21-24`):** Unlike `AndroidIncidentRepository` and `ProfileViewModel.submitQualification`, `UserRepository` reads `tokenStorage.getAccessToken()` directly instead of calling `SessionTokens.bearer()`, so expired 15-minute access tokens are not auto-refreshed on profile/medical/contact calls.
4. **Hardcoded Demo Data in Production Code Paths:**
   - `handover_node` (`ai_service/app/agent/gemini_agent.py:378-401`) and `AIClient._local_fallback_handover` return hardcoded patient `"Rajesh Sengupta"`, address `"Godrej Waterside..."`, and `"AMRI Hospital..."`.
   - `routing_service.py` generates hardcoded Sector V street names (`"Ring Rd"`, `"Godrej Waterside Access Rd"`) and synthetic 4-point polylines for all coordinates, and `GOOGLE_MAPS_API_KEY` is missing from backend `Settings` so Google Routes API v2 is never invoked.
   - `speech_service.py` and `vision_service.py` return hardcoded mock cardiac/fire strings when Gemini is unavailable.
5. **Unpopulated `sos_events.location` Geometry:** `SOSService.create_sos_event` writes `latitude` and `longitude` floats, leaving `sos_events.location` (`Geometry(POINT, 4326)`) `NULL`.
6. **PostGIS `::geography` Cast vs. `geometry` GiST Index:** `GeoService` queries `ST_DWithin(location::geography, ...)`, which may not utilize `idx_users_location` created on `Geometry` without a functional geography index.
7. **Synchronous AI Blocking on SOS Creation:** `SOSService.create_sos_event` awaits `ai_client.predict_severity()` (up to 10s timeout) before saving the SOS or dispatching FCM alerts, contradicting ADR-6.
8. **Anonymous Incident Ownership Dead-End:** Creating an SOS with `is_anonymous=True` sets `broadcaster_id = None`, preventing that user from resolving or cancelling it (`403`). (Android's `SosCreateRequest` currently omits `is_anonymous`, mitigating this for mobile guest logins.)
9. **No Automatic Escalation Scheduler:** `escalate_sos` only runs if an external client explicitly calls `POST /api/v1/sos/{id}/escalate`.

---

## 3. Architecture, Scaling & Repo Hygiene Debt

1. **Single-Worker WebSocket & Cache Lock-In:** `ws_manager._rooms`, `TrackingService._live_telemetry_cache`, `routing_service._hazards`, `EmergencyAgentService.sessions`, and `ai_client._SESSION_MEMORY` are all process-local. Redis Pub/Sub publishes tracking messages with no subscriber.
2. **Missing Alembic Migrations:** `alembic` is in `requirements.txt`, but no migrations exist; schema changes rely on `create_all` and 3 hardcoded `ALTER TABLE` statements in `init_db()`.
3. **CI Pipeline Blind Spots (`.github/workflows/ci.yml`):** Only runs `ruff check .` and `docker compose config`—never runs `pytest` or Gradle tests.
4. **Model Version Drift:** Backend `config.py` defaults `GEMINI_MODEL` to `"gemini-3.5-flash-lite"`, `ai_service` defaults to `"gemini-2.0-flash"`, `AiAgentRepository.kt` tries `"gemini-3.5-flash-lite"` / `"gemini-3.8-flash"`, and docs say `"Gemini 2.5"`.
5. **Local Runtime Artifacts in Working Tree:**
   - `backend/nearhelp.db` and `backend/uploads/` sample files are present in the working tree (gitignored).
6. **Unverified Components in This Environment:**
   - `ai_service/tests/` (57 tests) could not run (no `langgraph` venv).
   - Android unit tests (98 tests) and APK build could not run (no JDK on shell PATH).
   - Real PostGIS queries, physical FCM notifications, Google Sign-In, Google Places (New), and Cloud Run deployment remain unverified in this workspace.
