# NearHelp — Project Decision Register

Last reviewed: **2 November 2026**

This document records the project's product, architecture, technology, data, and delivery decisions, with their reasons and consequences. See [flow.md](flow.md) for the corresponding data journeys.

## How to read this register

- **Implemented** means the choice is visible in this checkout. It does not establish production readiness or successful deployment.
- **Partial** means some supporting code exists, with integration or operational work outstanding.
- **Planned** means the choice appears in project specifications but is not established as working here.
- **Documented rationale** comes from the existing specifications. **Inferred rationale** explains the apparent purpose of an implementation where no historical decision record exists; it is not a claim about a discussion the team had.

The code and [device readiness review](docs/DEVICE_READINESS.md) take precedence when older planning documents describe different behavior. This is a consolidated register of decisions evidenced by the repository, rather than a reconstruction of undocumented meetings.

## 1. Product and scope

### D01 — Coordinate nearby community help alongside professional services

**Status:** Implemented core workflow; broader product scope remains partial.

**Choice:** Build NearHelp around a person raising an SOS, nearby volunteers responding, and participants coordinating until resolution. Include first-aid guidance and professional-service escalation in the design.

**Why — documented:** The [proposal](docs/proposal.md) identifies the interval before professional help arrives as the problem to address. Nearby people with useful skills may be able to assist during that interval.

**Consequences:** Location, responder availability, skills, incident state, and timely communication are central data. An escalation flag or generated handover does not establish that a professional service has actually been contacted.

### D02 — Deliver in three MoSCoW phases

**Status:** Planned prioritization, with core features implemented.

**Choice:** Prioritize authentication, profiles, triage, SOS, tracking, routing, and the crisis assistant. Follow with enhancements such as translation and summaries, then stretch features such as analytics and disaster mode.

**Why — documented:** Keep a 24-module academic project manageable and protect the core rescue workflow from scope overload.

**Consequences:** A screen, dependency, or roadmap checkbox alone should not be treated as proof of an integrated feature. Offline SMS, continuous background GPS, and the admin web dashboard are not established as complete in this checkout.

**Evidence:** [README](README.md), [master task list](docs/todos.md), [device readiness](docs/DEVICE_READINESS.md).

## 2. Application and service architecture

### D03 — Native Android with Kotlin and Jetpack Compose

**Status:** Implemented.

**Choice:** Use Kotlin, Compose, Material 3, Android lifecycle components, coroutines, and Navigation 3 for the mobile app.

**Why — inferred:** A native Android client provides direct access to location, notification permissions, Firebase messaging, and Google Maps. Compose lets screens render from observable state; coroutines support network and location work without blocking UI interaction.

**Consequences:** The delivered client targets Android. An iOS or web client requires additional implementation. Permission behavior and background limits still need device testing.

**Evidence:** [Android dependencies](android/gradle/libs.versions.toml), [navigation](android/app/src/main/java/com/example/nearhelp/Navigation.kt), [UI guidance](docs/UI_GUIDANCE.md).

### D04 — ViewModels and observable state, with repositories where present

**Status:** Implemented, with a mixed data-access structure.

**Choice:** Screens observe ViewModel state. Authentication, users, routing, and AI have repository layers; the current incident ViewModel calls its Retrofit service directly. Scope ViewModels to navigation entries.

**Why — inferred:** Keep long-running work and state outside composable rendering, preserve state across ordinary UI recreation, and prevent one incident from retaining another incident's state.

**Consequences:** The app is not uniformly repository-based. Future refactoring should preserve the incident-specific lifecycle, retry payload, and socket cleanup behavior.

**Evidence:** [IncidentViewModel](android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt), [repositories](android/app/src/main/java/com/example/nearhelp/data/repository/), [navigation](android/app/src/main/java/com/example/nearhelp/Navigation.kt).

### D05 — FastAPI for backend and AI APIs

**Status:** Implemented.

**Choice:** Use Python FastAPI, Pydantic request/response models, asynchronous handlers, and generated OpenAPI documentation.

**Why — documented:** Architecture ADR-1 favors async request handling, WebSockets, validation, and API documentation over a Django or Flask implementation for this workload. Using Python for both services also fits the AI code.

**Consequences:** API schemas provide a shared contract, but Kotlin DTOs must still match them. An async handler can still be delayed by synchronous work within it.

**Evidence:** [architecture ADRs](docs/architecture.md#18-architecture-decision-records), [backend entry point](backend/app/main.py), [AI entry point](ai_service/app/main.py).

### D06 — Separate coordination backend and AI service

**Status:** Implemented separation; intended latency isolation is partial.

**Choice:** The backend owns identity, incidents, responder coordination, and relational data. The AI service owns classification, retrieval, and the crisis agent. The backend reaches it over HTTP.

**Why — documented:** Isolate AI dependencies and vector-store maintenance, allow independent deployment, and keep AI failures from preventing core coordination.

**Consequences:** Service outages require timeouts and fallbacks. **The current SOS implementation awaits severity prediction before creating the incident and notifying responders.** The architecture's proposed parallel, non-blocking AI path is not implemented by that function.

**Evidence:** [architecture ADR-6](docs/architecture.md#18-architecture-decision-records), [SOS service](backend/app/services/sos_service.py), [AI client](backend/app/services/ai_client.py).

### D07 — Retrofit/OkHttp JSON APIs and one configured backend

**Status:** Implemented.

**Choice:** Use Retrofit with Gson for HTTP and OkHttp for sockets. Select the backend through build configuration; remove automatic cloud-to-private-IP failover. Disable HTTP logging in the shared Retrofit client.

**Why — documented/inferred:** Both phones must use the same incident store. Keeping requests on an explicit backend avoids replaying credentials or emergency writes against unrelated hosts; disabling HTTP logs reduces exposure of request contents.

**Consequences:** An unreachable configured backend produces a visible failure. Local trials need an explicit LAN URL; separate networks need a reachable deployment. Other application logs still need consideration: the AI repository includes query text in some logs.

**Evidence:** [RetrofitClient](android/app/src/main/java/com/example/nearhelp/data/api/RetrofitClient.kt), [Android build](android/app/build.gradle.kts), [device setup](docs/DEVICE_READINESS.md).

### D08 — Versioned routes with compatibility aliases

**Status:** Implemented.

**Choice:** Expose `/api/v1` routes and retain selected `/api/...` compatibility routes. Tracking sockets use `/ws/tracking/{incident_id}` and `/ws/sos/{incident_id}`.

**Why — inferred:** Support current clients while preserving older integrations during API evolution.

**Consequences:** Maintain equivalent behavior across aliases. New documentation uses versioned HTTP routes where available; socket paths must be checked separately.

**Evidence:** [backend route registration](backend/app/main.py), [API router](backend/app/api/__init__.py), [tracking routes](backend/app/api/tracking_ws.py).

## 3. Databases, persistence, and identity

### D09 — PostgreSQL 16 with PostGIS 3.4 as the coordination database

**Status:** Implemented and configured in Docker Compose.

**Choice:** Store users, incidents, responses, timelines, skills, and facilities in a relational database; use PostGIS for spatial queries.

**Why — documented:** Architecture ADR-2 prioritizes transactional incident creation, relationships, and mature spatial queries/indexing over a document-database design. This is a workload-specific choice, not a claim that every alternative lacks transactions or strong consistency.

**Consequences:** PostgreSQL/PostGIS is required for representative spatial verification. Latitude/longitude use WGS84; point construction uses **longitude first**, while API payloads name each coordinate explicitly. Geometry indexes exist, but geography-cast query performance still needs measurement.

**Evidence:** [Compose stack](docker-compose.yml), [models](backend/app/models/), [geo service](backend/app/services/geo_service.py), [DB initialization](backend/app/db/session.py).

### D10 — SQLAlchemy async sessions; SQLite only as a test convenience

**Status:** Implemented.

**Choice:** Use SQLAlchemy with asyncpg for PostgreSQL. Provide SQLite spatial stubs and a Haversine fallback for lightweight tests. Initialize tables and selected schema additions at startup.

**Why — inferred:** Keep database operations consistent across services and make local tests easier to run without a complete spatial database. Explicit commits and rollbacks support durable workflow changes.

**Consequences:** SQLite tests do not validate PostGIS semantics or index performance. Startup `create_all` and individual `ALTER TABLE` statements are not a complete versioned migration process. PostgreSQL currently uses `NullPool`, which also affects connection behavior under load.

**Evidence:** [session management](backend/app/db/session.py), [test setup](backend/tests/conftest.py), [network smoke test](backend/scripts/device_smoke.py).

### D11 — Redis for short-lived middleware state, with memory fallbacks

**Status:** Implemented for idempotency caching, auth rate limits, and socket publication; distributed socket delivery is partial.

**Choice:** Use Redis 7 for cached mutation responses and sliding-window authentication limits. Fall back to process memory when Redis cannot be initialized. Publish incident broadcasts to Redis as well as local sockets.

**Why — inferred:** Avoid unnecessary repeat work and reduce repeated authentication attempts without making Redis availability mandatory for every local development request.

**Consequences:** Memory fallbacks disappear on restart and are not shared across workers. Publishing to Redis alone does not synchronize rooms: there is no corresponding cross-worker subscriber in the current socket manager. Use one backend worker/replica for the documented device trial.

**Evidence:** [middleware](backend/app/core/middleware.py), [socket manager](backend/app/core/websocket_manager.py), [device readiness](docs/DEVICE_READINESS.md).

### D12 — Backend JWT sessions with multiple identity providers

**Status:** Email and guest sessions implemented; Firebase-dependent provider flows are partial.

**Choice:** Hash email passwords with bcrypt. Issue backend HS256 access and refresh JWTs; use Firebase token verification for Google/phone identity linking. Default lifetimes are 15-minute access, seven-day refresh, and 60-minute anonymous access.

**Why — inferred:** Present one backend identity model to application APIs regardless of the original sign-in method, while keeping ordinary access tokens short-lived.

**Consequences:** Firebase credentials and client setup are needed for real provider verification. The phone send-OTP endpoint currently returns an acknowledgement; the code-only verification branch accepts a code based on length rather than validating an issued OTP. It must not be described as a completed secure OTP delivery/verification flow.

**Evidence:** [auth service](backend/app/services/auth_service.py), [security](backend/app/core/security.py), [auth API](backend/app/api/auth.py), [configuration](backend/app/core/config.py).

### D13 — Guest emergency access with an explicit privacy flag

**Status:** Implemented, with ownership differences between API modes.

**Choice:** Allow anonymous-session creation and optional-auth SOS creation. The API also supports `is_anonymous` on an incident.

**Why — documented:** Reduce the registration barrier during an emergency and avoid requiring a personal profile to ask for help.

**Consequences:** A guest session still has a stored user UUID and tokens. An incident explicitly created with `is_anonymous=true` has no `broadcaster_id`; current owner checks therefore cannot identify that guest as its owner. The current Android SOS DTO does not send this flag, so guest-session use and an explicitly anonymous incident are different paths. Neither path implies automatic deletion or absence of location/text data.

**Evidence:** [auth service](backend/app/services/auth_service.py), [SOS service](backend/app/services/sos_service.py), [Android SOS DTO](android/app/src/main/java/com/example/nearhelp/data/api/SosApiService.kt).

### D14 — Encrypt selected medical fields and prefer encrypted device storage

**Status:** Implemented with compatibility/failure fallbacks.

**Choice:** Encrypt medical conditions, allergies, and medical notes with AES-256-GCM before database persistence. Use Android EncryptedSharedPreferences for tokens, selected profile values, pending SOS data, and settings.

**Why — documented/inferred:** Reduce exposure of sensitive medical and session information in storage while allowing authorized profile reads.

**Consequences:** Server encryption uses a key derived from `SECRET_KEY`; changing that secret affects both JWTs and medical-data decryption. Blood group, contacts, location, and several medical flags remain ordinary database fields. Legacy plaintext is accepted by the decrypt helper. Android falls back to ordinary app-private SharedPreferences if encrypted-store initialization fails, so encryption is not guaranteed on that path.

**Evidence:** [crypto helper](backend/app/core/crypto.py), [profile service](backend/app/services/user_service.py), [TokenStorage](android/app/src/main/java/com/example/nearhelp/data/local/TokenStorage.kt).

### D15 — Local files for avatars and skill certificates

**Status:** Implemented development storage.

**Choice:** Save uploads under `backend/uploads` and serve them through `/uploads`; persist URLs and skill-review records in the database.

**Why — inferred:** Provide a simple upload/review workflow without introducing object-storage infrastructure for the prototype.

**Consequences:** Files depend on the backend filesystem and are outside the relational transaction. The static mount does not enforce participant authorization for downloads. Durable multi-instance hosting and private certificate delivery require additional work.

**Evidence:** [static mount](backend/app/main.py), [avatar service](backend/app/services/user_service.py), [skill service](backend/app/services/skill_service.py).

## 4. SOS coordination and real-time behavior

### D16 — HTTP writes, FCM alerts, and WebSockets for live coordination

**Status:** Implemented; physical push delivery requires configuration and validation.

**Choice:** Create and update incidents through HTTP, notify devices with high-priority FCM, and exchange live GPS/chat over WebSockets.

**Why — documented:** Architecture ADR-3 separates durable requests from background notification delivery and active in-app coordination. A background app cannot rely on keeping an ordinary socket connected.

**Consequences:** FCM requires Android and backend Firebase configuration. A server `delivered` result means FCM accepted the request, not that a phone displayed it or a person read it. Push is not a guaranteed rescue-delivery mechanism.

**Evidence:** [notification service](backend/app/services/notification_service.py), [Android messaging service](android/app/src/main/java/com/example/nearhelp/notifications/NearHelpMessagingService.kt), [architecture ADR-3](docs/architecture.md#18-architecture-decision-records).

### D17 — Durable SOS retry identity, plus cached responses

**Status:** Implemented.

**Choice:** Save the pending mobile payload and request key before sending. Derive a stable incident UUID from the user and idempotency key, store a payload fingerprint, and recover an existing incident on retry. Also cache HTTP responses through middleware.

**Why — inferred:** A timeout does not tell the phone whether the server committed an incident. Retrying must recover that incident rather than dispatch a duplicate emergency.

**Consequences:** Reusing a key with changed details returns a conflict. Incident deduplication survives a backend restart; the original notification fan-out is not a durable job and is not automatically resumed by replaying an existing incident.

**Evidence:** [IncidentViewModel](android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt), [SOS creation](backend/app/services/sos_service.py), [middleware](backend/app/core/middleware.py).

### D18 — Rank available nearby responders by proximity, skill, and trust

**Status:** Implemented heuristic ranking.

**Choice:** Find nearby active users with fresh availability; score them with default weights **40% distance, 35% skills, 25% reliability**. Verified skill matches receive a bonus. Severity controls the number selected: 2, 3, 5, 8, or 15 for levels 1–5.

**Why — documented:** Prefer a useful combination of speed, competence, and prior reliability rather than selecting only the closest person or broadcasting to everyone.

**Consequences:** This is an explicit weighted formula, not a learned dispatch model. Claimed skills may contribute; verification is a bonus rather than a universal eligibility gate. Weights and trust rules require evaluation before claiming improved outcomes.

**Evidence:** [ranking service](backend/app/services/ranking_service.py), [geo service](backend/app/services/geo_service.py), [skill review](backend/app/services/skill_service.py).

### D19 — Fresh, explicit availability and foreground location sharing

**Status:** Implemented.

**Choice:** Require availability opt-in and a location heartbeat less than 15 minutes old for dispatch eligibility. Android rejects location fixes older than two minutes for SOS submission and location updates. Share responder positions while the incident screen is open.

**Why — inferred:** Avoid dispatching stale or unavailable users and avoid presenting invented positions as live GPS.

**Consequences:** A volunteer who stops refreshing availability will become ineligible. Continuous background GPS is not implemented. Latest coordinates remain in user data; expiry of availability does not delete coordinates.

**Evidence:** [geo service](backend/app/services/geo_service.py), [IncidentViewModel](android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt), [location helper](android/app/src/main/java/com/example/nearhelp/data/location/LocationHelper.kt).

### D20 — Reconnection snapshots and polling alongside socket events

**Status:** Implemented.

**Choice:** Send a tracking snapshot when a client connects, reconnect broken sockets, refresh the incident every five seconds, and refresh the discovery feed every ten seconds. Persist participant chat as timeline entries.

**Why — inferred:** Recover after network loss and use database state to reconcile live UI updates. Persisted chat remains available after reopening an incident.

**Consequences:** Polling adds requests and some changes may appear after a delay. Socket rooms and high-frequency telemetry remain process-local. Chat writes are restricted to incident participant roles, but incident reads and initial tracking access have broader visibility in the current API.

**Evidence:** [incident ViewModel](android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt), [tracking API](backend/app/api/tracking_ws.py), [tracking service](backend/app/services/tracking_service.py).

### D21 — Three escalation layers and owner-controlled incident endings

**Status:** Escalation API and ending controls implemented; automatic professional dispatch/offline execution partial or planned.

**Choice:** Expand the search radius, expose professional-service escalation, and offer guided fallback. Permit only the owner or an administrator to resolve/cancel. Lock the incident during resolution and reject repeated completion before awarding trust again.

**Why — documented/inferred:** Provide a path when community response is insufficient, protect incident closure, and prevent repeated reputation rewards.

**Consequences:** Radius changes happen when the escalation API is called; there is no proven autonomous scheduler in the core incident workflow. The service multiplies the current radius at time gates and caps it at 5 km. `auto_call_108_triggered` and `offline_fallback_ready` are state/response fields, not proof of a placed call or a complete offline system.

**Evidence:** [SOS service](backend/app/services/sos_service.py), [SOS routes](backend/app/api/sos.py).

## 5. AI, knowledge, maps, and delivery

### D22 — Gemini with configurable models and local fallback behavior

**Status:** Implemented provider integration and fallbacks; provider calls depend on credentials.

**Choice:** Use Gemini for generation and multimodal processing, with heuristic classification/severity and built-in guidance when remote services fail. Prefer remote embeddings when configured; otherwise use a deterministic 384-dimensional local vectorizer.

**Why — documented:** Architecture ADR-7 cites multilingual needs, vision, structured responses, and student-project cost constraints. These explain the original selection; current model availability and pricing are not verified by this document.

**Consequences:** Planning documents say Gemini 2.5, while current settings/Compose default to `gemini-2.0-flash` and `models/text-embedding-004`. Fallbacks are a different processing mode, not equivalent model output. Embeddings and stored vectors must use compatible dimensions and representations; switching providers can require reindexing.

**Evidence:** [AI settings](ai_service/app/core/config.py), [embedding service](ai_service/app/classifiers/embedding_service.py), [backend AI client](backend/app/services/ai_client.py), [original ADR](docs/architecture.md#18-architecture-decision-records).

### D23 — Removed RAG in favor of curated structured clinical protocols and guardrails

**Status:** Superseded / Removed.

**Choice:** Remove ChromaDB vector database, chunking, and semantic vector retrieval. Guide the emergency agent with structured curated clinical protocols and direct clinical contraindication guardrails instead of vector RAG.

**Why — documented:** Simplifies operational complexity, eliminates ChromaDB runtime failures, avoids vector indexing latency in life-critical emergencies, and relies on curated verified emergency protocols (`EMERGENCY_KNOWLEDGE_BASE`).

**Consequences:** AI guidance operates directly from verified clinical knowledge matrices and Gemini with strict safety guardrails. No ChromaDB dependencies or vector store initialization are required.

**Evidence:** [agent graph](ai_service/app/agent/gemini_agent.py), [guardrails](ai_service/app/agent/guardrails.py), [knowledge catalog](ai_service/app/agent/knowledge.py), [Compose mounts](docker-compose.yml).

### D24 — Stateful LangGraph guidance with guardrails and citations

**Status:** Implemented graph and safety checks; durable agent memory is not established.

**Choice:** Run triage → protocol lookup → safety checks → response or handover. Track session condition and completed steps; include citations, warnings, and guidance disclaimers.

**Why — documented:** Make the assistant follow an explicit emergency workflow and ground responses in protocol passages instead of relying only on open-ended generation.

**Consequences:** Sessions are process-local rather than a durable medical conversation record. Guardrails and confidence fields are application logic, not clinical validation or guarantees of correctness. Source labels refer to the repository corpus; this review does not certify the corpus or establish legal protection.

**Evidence:** [agent graph](ai_service/app/agent/gemini_agent.py), [guardrails](ai_service/app/agent/guardrails.py), [disclaimer UI](android/app/src/main/java/com/example/nearhelp/ui/components/AIDisclaimerBanner.kt).

### D25 — Support an optional direct-to-Gemini mobile path

**Status:** Implemented exception to the backend-proxy architecture.

**Choice:** If a user-configured Gemini key is stored on the device, the AI repository tries Gemini directly before the backend. Otherwise it calls the backend; failures or selected generic replies use built-in local guidance.

**Why — inferred:** Let the assistant work with a configured provider key and retain useful UI behavior when backend AI is unavailable.

**Consequences:** Assistant text can leave the phone directly for Google, bypassing backend/AI-service processing. The direct and local branches do not execute the same server LangGraph checks. A displayed response does not prove that the backend was used.

**Evidence:** [AI repository](android/app/src/main/java/com/example/nearhelp/data/repository/AiAgentRepository.kt), [device settings storage](android/app/src/main/java/com/example/nearhelp/data/local/TokenStorage.kt).

### D26 — Google Maps display, regional facility data, and route estimates

**Status:** Map/facility integration implemented; external routing configuration partial.

**Choice:** Display maps with Google Maps Compose, seed Kolkata facilities from JSON into PostgreSQL, and provide route/hazard/AED alternatives through the backend. The routing service can attempt Google Routes and otherwise builds local estimates.

**Why — documented/inferred:** Make nearby resources and responder movement visible, with a focused regional dataset for development and demonstrations.

**Consequences:** Seed data and curated hazards are not live municipal feeds. Local routes include constructed polylines, estimates, and demo-specific street instructions. The routing code looks for `GOOGLE_MAPS_API_KEY`, which the current backend settings class does not declare; a real Routes integration needs configuration work and verification.

**Evidence:** [map component](android/app/src/main/java/com/example/nearhelp/ui/map/NearHelpGoogleMapView.kt), [facility service](backend/app/services/facility_service.py), [regional dataset](data/regional/kolkata_facilities.json), [routing service](backend/app/services/routing_service.py).

### D27 — Docker Compose development and layered verification

**Status:** Local containers and tests implemented; deployment/CI goals partial.

**Choice:** Run PostGIS, Redis, backend, and AI service as separate Compose services. Use backend/AI pytest suites, Android tests/lint/build checks, and a two-client network smoke script. Keep health checks tied to real database/spatial queries on the backend.

**Why — inferred/documented:** Make local dependencies repeatable and distinguish unit behavior from actual HTTP/WebSocket/PostGIS integration and physical-device behavior.

**Consequences:** The network smoke test does not prove GPS hardware, FCM display, or AI quality. Cloud Run, Prometheus/Grafana, and an admin dashboard remain architecture goals rather than verified deployments. The checked-in GitHub workflow references a missing `admin_dashboard` directory and its Python jobs run Ruff without pytest; job names alone do not establish complete CI coverage.

**Evidence:** [Compose](docker-compose.yml), [CI workflow](.github/workflows/ci.yml), [smoke script](backend/scripts/device_smoke.py), [readiness review](docs/DEVICE_READINESS.md).

### D28 — Distinct calm and crisis UI states with deliberate SOS activation

**Status:** Implemented UI components and core SOS form; complete design specification remains partial.

**Choice:** Use a Material 3 design with calm safety/map views and visually prominent crisis actions. The design specifies soft elevated/glass surfaces, large touch targets, haptic feedback, address confirmation, and a dispatch grace period. The current core SOS workflow opens a form after a hold and requires an explicit send action with a current GPS fix.

**Why — documented:** Make emergency actions easy to identify while reducing accidental activation and helping a stressed user confirm useful location/category information.

**Consequences:** Existing design wireframes include future voice/photo and guardian features. Treat the working incident form as the current dispatch path; do not infer that every animated component or wireframe triggers a server operation. Readability and physical-device behavior still need UI verification.

**Evidence:** [design specification](docs/design.md), [UI guidance](docs/UI_GUIDANCE.md), [home screen](android/app/src/main/java/com/example/nearhelp/ui/home/HomeScreen.kt), [incident screen](android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentScreen.kt).

### D29 — English, Hindi, and Bengali as regional language priorities

**Status:** Partial language support; complete translation workflow planned.

**Choice:** Prioritize these languages in the product plan, user language preferences, and emergency-language processing. Include Bengali and Hindi/transliterated emergency terms in local classification support.

**Why — documented/inferred:** The project focuses initially on Kolkata and an Indian user base, where language accessibility matters for describing an emergency and understanding guidance.

**Consequences:** Saving a language preference or matching selected keywords does not establish full app localization, automatic participant-chat translation, or reliable speech handling. These flows need their own integration and quality verification.

**Evidence:** [proposal](docs/proposal.md), [README module plan](README.md), [embedding lexicon](ai_service/app/classifiers/embedding_service.py), [user language API](backend/app/api/users.py).

## Maintaining this document

When changing a major choice, update its status, rationale, consequences, and source links here; update [flow.md](flow.md) if data movement changes. Preserve superseded choices with a short explanation when their history matters. Add a new decision when introducing a provider, changing storage/access boundaries, altering the SOS lifecycle, or committing to a deployment model.
