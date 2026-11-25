# AGENTS.md — Guide for AI Coding Agents & Maintainers

This file provides repository-specific rules, architecture realities, verification commands, and pitfalls for any AI agent or developer modifying **NearHelp AI**.

---

## 1. Ground Rules & Source-of-Truth Hierarchy

When documentation disagrees, trust sources in this exact order:
1. **Executable Source Code** (`backend/app/`, `ai_service/app/`, `android/app/src/`)
2. **Current Architecture & Audit Docs** (`docs/architecture/`, `docs/reference/`, `docs/guides/DEVICE_READINESS.md`)
3. **Historical Planning Specs** (`docs/planning/architecture.md`, `docs/planning/design.md`, `docs/planning/todos.md`, `docs/planning/proposal.md`) — *these describe an earlier 24-module vision (e.g., Hilt DI, Next.js `admin_dashboard/`, ChromaDB RAG, `messages` table, RS256 JWTs) that does not match the current codebase.*

---

## 2. Critical Realities Before Editing

### Backend (`backend/`)
- **Dual Route Mounts:** Endpoints are mounted at both `/api/v1/*` and `/api/*` (`backend/app/main.py`). Keep compatibility if modifying router prefixes.
- **Single-Worker Assumption:** `ws_manager` (`websocket_manager.py`) and `TrackingService._live_telemetry_cache` are in-memory. Do not deploy multiple Uvicorn workers without implementing a Redis Pub/Sub subscriber loop.
- **Database Schema:** No Alembic migrations exist. Adding columns requires updating both the SQLAlchemy model and `init_db()` (`backend/app/db/session.py`) for existing Postgres databases.
- **SQLite Test Caveat:** `backend/tests/conftest.py` stubs `ST_DWithin` to always return `1`. Changes to PostGIS SQL in `geo_service.py` must be verified against a real PostGIS container (e.g., `docker compose up -d db` + `backend/scripts/device_smoke.py`).
- **AI Client Schema Bug:** Note that `sos_service.py` passes `crisis_type`/`symptoms` to `backend/app/schemas/ai.py`'s `SeverityRequest`, which expects `emergency_type`/`detected_symptoms`.

### AI Service (`ai_service/`)
- **No Vector DB / RAG:** RAG was removed (`9699a81`). Clinical knowledge lives in `ai_service/app/agent/knowledge.py` (`GROUNDED_PROTOCOLS`, `CONTRAINDICATION_RULES`) and `classifiers/crisis_types.py`.
- **Two Gemini SDKs in Use:** `gemini_agent.py`, `embedding_service.py`, and `vision_service.py` import `google.genai`, while `severity_predictor.py` and `speech_service.py` import legacy `google.generativeai`.
- **Fallback Drift:** Clinical rule logic is duplicated across `ai_service`, `backend/app/services/ai_client.py`, and `android/app/src/main/java/com/example/nearhelp/data/repository/AiAgentRepository.kt`. Updating a protocol in one place does not automatically update the other two.

### Android (`android/`)
- **Navigation & ViewModel Scoping:** Uses **Navigation 3** (`NavDisplay` + `rememberViewModelStoreNavEntryDecorator`). All SOS creation, browsing, tracking, and chat now share `IncidentScreen` + `IncidentViewModel`. Do not reintroduce hardcoded demo data into `IncidentViewModel` or `HomeScreen`.
- **No Hilt:** Dependencies are wired manually in `NearHelpApplication.kt` and composable `viewModel { ... }` factories.
- **Localization Generator:** Never hand-edit `values*/ui_strings.xml`; edit `android/localization/ui-translations.tsv` and run `python3 android/tools/generate_ui_strings.py`.
- **Token Refresh:** Use `SessionTokens.bearer()` for authenticated Retrofit calls so expired 15-minute access tokens refresh automatically.
- **Never Commit Secrets:** Do not hardcode API keys in Kotlin (`AiAgentRepository.kt`) or XML (`strings.xml`), and never commit `google-services.json` or `.env`.

---

## 3. Standard Verification Commands

```sh
# Backend linting & unit tests (unset GEMINI_API_KEY for deterministic AI fallback tests)
cd backend
ruff check .
GEMINI_API_KEY="" .venv/bin/pytest -q

# AI Service linting & unit tests (requires ai_service venv)
cd ai_service
ruff check .
pytest -q

# Android unit tests, debug APK, and lint (requires JDK 17)
cd android
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug

# Full Docker Compose validation + two-client smoke test
docker compose config
docker compose up -d --build
cd backend && python scripts/device_smoke.py --base-url http://127.0.0.1:8000
```

---

## 4. Safe Modification Checklist

- [ ] Preserve `Idempotency-Key` + `pending_sos_*` deduplication semantics when touching SOS creation.
- [ ] Preserve `with_for_update()` locking and owner checks when touching `resolve_sos` / `cancel_sos`.
- [ ] Run `HallucinationGuardrails` / contraindication checks *before* any LLM call in any fallback or agent path.
- [ ] Keep `docs/decisions/` and `docs/architecture/DECISIONS.md` updated whenever architectural choices or external providers change.
