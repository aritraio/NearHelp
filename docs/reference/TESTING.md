# Testing & Verification

This document inventories all test suites, smoke scripts, CI checks, and **exact execution results observed during this audit**.

---

## 1. Test Suite Inventory

| Component | Framework | Test Files | Test Cases | Persistence / Mocking |
| :--- | :--- | :---: | :---: | :--- |
| **Backend (`backend/tests/`)** | `pytest` + `pytest-asyncio` + `httpx` | 13 | 100 | In-memory SQLite (`aiosqlite`) with SQLite spatial function stubs (`ST_DWithin` -> `1`), in-memory rate-limit/idempotency stores |
| **AI Service (`ai_service/tests/`)** | `pytest` + `pytest-asyncio` + `httpx` | 5 | 57 | Calls `embedding_service.initialize()` (uses local `ClinicalSemanticVectorizer` if `GEMINI_API_KEY` unset) |
| **Android Unit (`android/app/src/test/`)** | JUnit 4 + Robolectric 4.17 + MockWebServer + `kotlinx-coroutines-test` | 17 | 98 | Robolectric + fake/mock repositories & MockWebServer |
| **Android Instrumented (`android/app/src/androidTest/`)** | Compose UI Test + Espresso | 1 | 1 | `MainScreenTest.kt` (leftover starter template test) |
| **Network Smoke (`backend/scripts/device_smoke.py`)** | `httpx` + `websockets` | 1 script | — | Two-client live HTTP + WebSocket flow against running backend |

### Backend Test Files (`backend/tests/`)
- `test_admin.py` (8) — Admin RBAC, user CRUD, presence tracking, pagination, validation, delete FK cleanup, `/admin` delivery, demo purge.
- `test_ai_agent.py` (7) — Protocol catalog, chat dialogue, contraindication alert, handover report, `/ws/ai/chat` WS flow.
- `test_ai_classify.py` (4), `test_ai_severity.py` (3) — Proxy classification and severity endpoints.
- `test_auth.py` (17) — Registration, login, Google/phone/anonymous auth, refresh, rate limiting.
- `test_crypto.py` (6) — AES-256-GCM encryption/decryption and plaintext fallback.
- `test_device_workflow.py` (5) — Two-user SOS creation, idempotency replay/conflict, active feed, acceptance, dual-WS GPS/chat, reconnect snapshot, resolve, cancel, availability freshness, FCM mocking, DB `/health` failure.
- `test_facilities.py` (8) — Facility seeding and nearby filtering.
- `test_profile_medical.py` (6) — Medical ID encryption, contacts limit, language preferences.
- `test_routing.py` (6) — Primary/detour/AED routes and hazard reporting/clearing.
- `test_skill_verification.py` (9) — Certificate upload, claim submission, admin approval/rejection, trust score updates.
- `test_sos_engine.py` (9) — SOS creation, ranking math, escalation layers, resolution.
- `test_tracking_ws.py` (12) — Tracking WebSocket roles, heartbeats, GPS updates, arrival milestone, chat persistence, HTTP fallbacks.

### AI Service Test Files (`ai_service/tests/`)
- `test_agent.py` (10), `test_agent_ws.py` (6), `test_classifier.py` (20), `test_guardrails.py` (6), `test_severity.py` (15).

### Android Unit Test Files (`android/app/src/test/java/com/example/nearhelp/`)
- `ui/incident/`: `IncidentViewModelTest.kt` (10), `IncidentPresentationTest.kt` (6), `MedicalSosPromptViewModelTest.kt` (5)
- `ui/assistant/`: `AiCrisisAssistantViewModelTest.kt` (6)
- `ui/auth/`: `AuthViewModelTest.kt` (8)
- `ui/navigation/`: `RescueNavigationViewModelTest.kt` (9)
- `ui/map/`: `CommunityGeoMapViewModelTest.kt` (6), `NearbyPlacesViewModelTest.kt` (6)
- `ui/profile/`: `ProfileViewModelTest.kt` (6)
- `ui/tracking/`: `LiveTrackingViewModelTest.kt` (7)
- `ui/main/`: `NavigationTransitionTest.kt` (2), `MainScreenViewModelTest.kt` (2)
- `data/`: `AiAgentRepositoryTest.kt` (13), `AuthRepositoryTest.kt` (4), `LiveTrackingWebSocketClientTest.kt` (1)
- `i18n/` & `theme/`: `AppLanguagesTest.kt` (5), `AppThemeTest.kt` (2)

---

## 2. Observed Test Execution Results (2026-11-25)

### Backend (`backend/.venv/bin/pytest -q` from `backend/`)
- **Result:** **97 passed, 3 failed** (19.30s).
- **Failures (all 3 in `tests/test_ai_agent.py`):**
  1. `test_backend_agent_chat_dialogue`
  2. `test_backend_agent_chat_contraindication_alert`
  3. `test_backend_agent_ws_full_dialogue_flow`
- **Root Cause Observed:** `ai_service` was not running on port `8001`, so `AIClient` fell back to `_local_fallback_agent_chat`. Because `backend/.env` contains a `GEMINI_API_KEY`, `_local_fallback_agent_chat` invoked live `gemini-3.5-flash-lite` **before** running its deterministic contraindication and CPR rule checks, returning prose that did not match the exact expected substrings (`"5 to 6 cm"`, `"110"`) or populate `contraindications`.

### AI Service (`ai_service/`)
- **Result:** **Could not be executed** in this environment (`ModuleNotFoundError: No module named 'langgraph'`). No dedicated virtualenv exists under `ai_service/` (and `backend/.venv` only installs `backend/requirements.txt`).

### Android (`./gradlew :app:testDebugUnitTest` from `android/`)
- **Result:** **Could not be executed** in this environment (`Unable to locate a Java Runtime` on PATH).

---

## 3. How to Run All Checks

```sh
# 1. Backend tests (disable GEMINI_API_KEY if testing deterministic fallback without ai_service)
cd backend
GEMINI_API_KEY="" .venv/bin/pytest -q

# 2. AI Service tests (after creating an ai_service venv with requirements.txt)
cd ai_service
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
pytest -q

# 3. Android unit tests, debug APK, and lint (requires JDK 17)
cd android
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug

# 4. Two-client integration smoke test against a running backend + PostGIS
cd backend
python scripts/device_smoke.py --base-url http://127.0.0.1:8000

# 5. Linting (matches .github/workflows/ci.yml)
cd backend && ruff check .
cd ai_service && ruff check .
```

---

## 4. CI & Testing Gaps

1. **GitHub Actions CI (`.github/workflows/ci.yml`) does not run `pytest` or Gradle:** Despite job names `Backend Lint & Unit Tests` and `AI Service Lint & Unit Tests`, those jobs only run `ruff check .`.
2. **SQLite Masks PostGIS Bugs:** `backend/tests/conftest.py` stubs `ST_DWithin` to always return `1`, meaning spatial radius filtering, geography casts, and GIST index usage are **only** tested if you run `device_smoke.py` or manual tests against real PostgreSQL/PostGIS.
3. **Unverified Physical Hardware Flows:** Actual GPS hardware fixes, physical FCM push wakeups on backgrounded phones, Google Places API (New) responses, and Compose layout on physical screens require manual device testing ([docs/DEVICE_READINESS.md](../guides/DEVICE_READINESS.md)).
