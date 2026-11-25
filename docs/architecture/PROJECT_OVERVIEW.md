# Project Overview

## 1. Purpose & Problem Statement

NearHelp AI is a community emergency response platform designed to bridge the critical time gap between when a medical or safety crisis occurs and when professional emergency services (such as `108` ambulance, `101` fire, `100` police, or `112` unified emergency in India) arrive on scene.

The system connects a person in distress (**Victim / Broadcaster**) with nearby, available, skill-verified community volunteers (**Responders**), while providing structured first-aid guidance via an **AI Crisis Assistant**, real-time **GPS tracking and chat**, nearby **hospital/AED resource discovery**, and an **Admin Command Center** for credential verification and user moderation.

---

## 2. Implementation Reality Check (Implemented vs. Partial vs. Planned)

Because NearHelp evolved rapidly from an academic 24-module blueprint ([`docs/architecture.md`](../planning/architecture.md), [`docs/todos.md`](../planning/todos.md)) into a working prototype, several planned features were scaled back, replaced, or left partially wired. The table below reflects the **verified state of the codebase**:

| Capability | Status | Evidence & Actual Behavior |
| :--- | :--- | :--- |
| **Email/Password Auth & JWTs** | **Implemented** | `POST /api/v1/auth/register`, `/login`, `/refresh`; bcrypt hashing + HS256 JWTs (`15m` access, `7d` refresh). |
| **Anonymous Guest Session** | **Implemented** | `POST /api/v1/auth/anonymous` creates a disposable `users` row (`is_anonymous=True`) and issues tokens. |
| **Google Sign-In (Firebase/OIDC)** | **Partial** | Backend verifies Firebase/OIDC tokens; Android uses Credential Manager + FirebaseAuth if `google-services.json` exists, falling back to `AccountManager` / mock tokens in dev mode. |
| **Phone OTP Auth** | **Partial (Insecure Dev Stub)** | Backend `/phone/send-otp` is a no-op acknowledgement; `/phone/verify` accepts any `otp_code` >= 4 chars if `id_token` is absent. Removed from Android `Navigation.kt` UI (`PhoneOtpNavKey` routes back to `LoginScreen`). |
| **User Profile & Encrypted Medical ID** | **Implemented** | AES-256-GCM encryption on `medical_conditions`, `known_allergies`, `medical_notes`; up to 5 emergency contacts; avatar upload. |
| **Skill Verification & Trust Score** | **Implemented** | Users submit PDF/image certificates via `/users/me/skills/form`; admins approve/reject via `/admin/verification-queue`, granting `+5.0` trust score and badges. |
| **Smart SOS Creation & Idempotency** | **Implemented** | Android persists pending payload + UUID `Idempotency-Key`; backend deduplicates via deterministic UUIDv5 + payload fingerprint + Redis/memory middleware. |
| **PostGIS Spatial Dispatch & Ranking** | **Implemented** | PostGIS `ST_DWithin` / `ST_Distance` query (with Haversine fallback) + weighted scoring (`0.40*D + 0.35*S + 0.25*R`) and severity-based top-N selection. |
| **Live Tracking WebSocket + Chat** | **Implemented (Single-Worker)** | `/ws/tracking/{incident_id}` streams `connection_ack`, `tracking_snapshot`, `responder_update`, and persists chat in `timeline_events`. Process-local rooms; no Redis Pub/Sub subscriber. |
| **FCM Push Notifications** | **Partial (Unverified on Device)** | Backend `NotificationService` sends high-priority FCM via `firebase-admin`; Android `NearHelpMessagingService` displays notification. Requires `google-services.json` and Firebase credentials absent from Git. |
| **3-Layer Escalation** | **Partial** | `POST /api/v1/sos/{id}/escalate` expands radius (`2x`/`3x` up to 5 km) and sets `auto_call_108_triggered`. **No server scheduler or Android timer invokes it automatically.** |
| **AI Classification & Severity** | **Implemented (Sync Blocking)** | `ai_service` exposes `/classify`, `/severity`, `/taxonomy`, `/transcribe`, `/vision`. Backend `SOSService` **blocks** awaiting severity before creating the incident. |
| **AI Crisis Assistant & Guardrails** | **Implemented (Text-Only on Mobile)** | 5-node LangGraph agent + 8 grounded protocols + regex guardrails in `ai_service`; 27-condition rule engine + Gemini fallback in `backend` & `android`. Voice/photo removed from mobile chat UI (commit `8c9b13f`). |
| **Kolkata Facilities & AED Mesh** | **Implemented (Static Seed)** | `data/regional/kolkata_facilities.json` auto-seeds `facilities` table on startup; `/api/v1/facilities/nearby` queries by Haversine distance. |
| **Nearby Places (Android Map)** | **Implemented (Requires Cloud Key)** | `GoogleNearbyPlacesRepository` uses Google Places SDK (New) within a 3 km cap. Requires Places API (New) enabled on the Maps key. |
| **Rescue Routing & Road Hazards** | **Partial (Demo/Simulated)** | `routing_service.py` constructs hardcoded Kolkata Sector V routes/polylines and stores hazards in process memory; `GOOGLE_MAPS_API_KEY` is missing from backend `Settings`. |
| **Admin Web Dashboard** | **Implemented** | Same-origin Vanilla JS SPA at `/admin` backed by `/api/v1/admin/*` and `user_presence`. Replaced planned Next.js `admin_dashboard/`. |
| **Localization (EN / BN / HI)** | **Partial** | Android UI strings generated via `ui-translations.tsv` for English, Bengali, Hindi; 31 spoken language codes stored in profile. No live participant chat auto-translation. |
| **Guardian Mode** | **Partial** | `GuardianRadarScreen.kt` exists and composes an `ACTION_SENDTO` SMS intent with GPS coordinates to saved emergency contacts. No automated dead-man switch or continuous background tracking. |
| **Continuous Background GPS** | **Planned / Not Implemented** | Android location updates run only while foreground screens (`HomeScreen`, `IncidentScreen`) are active. |
| **Voice SOS / Photo SOS Intake** | **Planned / Removed** | Backend schemas accept `voice_transcript` and `photo_url`; Android SOS creation (`IncidentScreen` / `MedicalSosPromptViewModel`) is medical text/chip-only. |
| **Offline SMS SOS Fallback** | **Planned / Not Implemented** | No SMS gateway or automated offline SOS relay exists. |
| **RAG / Vector DB (ChromaDB)** | **Removed** | Removed in commit `9699a81`; replaced with in-memory `GROUNDED_PROTOCOLS`. |
| **Cloud Run, Prometheus, Grafana, Locust** | **Planned / Not Present** | Documented in `docs/architecture.md` and `README.md`; no deployment manifests, `/metrics` endpoints, or `simulator/` directory exist in the repo. |

---

## 3. Repository Components at a Glance

```
NearHelp/
├── android/       # Native Android app (Kotlin, Jetpack Compose, Navigation 3, Maps/Places)
├── backend/       # FastAPI coordination API (:8000), SQLAlchemy models, Admin Web UI (/admin)
├── ai_service/    # FastAPI AI microservice (:8001), LangGraph agent, classifiers, guardrails
├── data/          # Regional seed dataset (data/regional/kolkata_facilities.json)
└── docs/          # Historical specs, device readiness, diagrams/, and decisions/
```

---

## 4. Regional Focus

The project targets **Kolkata, India** (specifically Salt Lake Sector V / EM Bypass) as its demonstration region:
- Default coordinates across `LocationHelper.kt`, `.env.example`, and routing fallbacks center around `(22.5726, 88.3639)` and `(22.5804, 88.4378)`.
- Statutory emergency numbers follow Indian standards (`108` medical/ambulance, `101` fire/gas, `100` police, `112` unified emergency) and legal disclaimers cite **Section 134A of the Motor Vehicles (Amendment) Act, 2019** and Supreme Court Good Samaritan guidelines.
- `data/regional/kolkata_facilities.json` seeds local hospitals and AED locations.
