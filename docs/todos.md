# NearHelp AI — Master TODO List

> **Last Updated**: 2026-09-13  
> **Project Start**: 2026-08-10  
> **Target Completion**: **First Week of November 2026 (Hard Deadline: November 07, 2026)**  
> **Strategic Timeframe**: Refer to [`docs/timeframe.md`](timeframe.md) for the detailed calendar factoring in **CT2 Exam Week** (Sep 14–20) and **Durga Puja Vacation** (Oct 12–25) (~3 Weeks Academic/Festival Gap).  
> **Status**: 🟢 **Phase 1 MVP Production Core Complete · 206/206 Automated Tests Passing (90 Backend + 72 AI + 44 Android Unit Tests)**


---

## Legend

| Symbol | Meaning |
| :--- | :--- |
| `[ ]` | Not started |
| `[/]` | In progress |
| `[x]` | Completed |
| 🔴 | Critical path — blocks other tasks |
| 🟡 | High priority |
| 🟢 | Standard priority |
| ⚪ | Nice-to-have / stretch |

---

## 🏗️ Project Infrastructure & Core Environment

### Repository & Workspace Organization

- [x] 🔴 Set up monorepo directory structure (`/backend`, `/android`, `/ai_service`, `/docs`, `/data`, `/assets`, `/archive`, `/simulator`)
- [x] 🔴 Create root `.gitignore` for Python, Kotlin/Android, environment files, and data directories
- [x] 🔴 Set up master project documentation suite ([`docs/architecture.md`](architecture.md), [`docs/Task.md`](Task.md), [`docs/todos.md`](todos.md), [`docs/proposal.md`](proposal.md), [`docs/UI_GUIDANCE.md`](UI_GUIDANCE.md))
- [x] 🔴 Set up `docker-compose.yml` for local development (PostgreSQL 16 + PostGIS 3.4, Redis 7, FastAPI backend, AI service)
- [x] 🔴 Launch and verify Docker containers (`nearhelp_postgis` and `nearhelp_redis` healthy)
- [x] 🔴 Initialize FastAPI backend local development environment (`uvicorn` live on port `8000` with Swagger UI at `/docs`)
- [x] 🟡 Create individual `Dockerfile` for backend service ([`backend/Dockerfile`](../backend/Dockerfile))
- [x] 🟡 Create individual `Dockerfile` for AI service ([`ai_service/Dockerfile`](../ai_service/Dockerfile))
- [x] 🟡 Set up GitHub Actions CI/CD pipeline ([`.github/workflows/ci.yml`](../.github/workflows/ci.yml))
  - [x] Python linting & quality checks (backend)
  - [x] Python linting & quality checks (AI service)
  - [x] Backend & AI service automated test suites
  - [x] Docker image build verification
- [x] 🟡 Configure environment variable templates ([`.env.example`](../.env.example)) for all services
- [ ] 🟢 Set up branch protection rules (`main`, `develop`)
- [x] 🟢 Configure pre-commit hooks ([`.pre-commit-config.yaml`](../.pre-commit-config.yaml))

### Development Environment Documentation

- [x] 🟡 Authored complete Review 1 technical defense package in [`archive/review-1/`](../archive/review-1/)
- [x] 🟡 Write [`CONTRIBUTING.md`](../CONTRIBUTING.md) with setup instructions for each service
- [x] 🟡 Write local development quickstart guide (integrated into `CONTRIBUTING.md`)
- [ ] 🟢 Document API contract agreement process between backend, AI service, and Android

---

## 📐 Phase 1 — MVP Core & Multi-Device Live Loop (Weeks 1–8: Aug 10 – Sep 30, 2026)

> **Goal**: End-to-end SOS lifecycle proven on physical hardware: trigger → classify → rank → alert (FCM) → guide → coordinate (WebSocket) → resolve.  
> **Modules**: 1–11  
> **Target Milestone Completion**: September 30, 2026 (Live 2-device physical verification)

---

### Module 1 — Authentication & Identity

**Owner**: Adil (Backend) · Dishari (UI)

#### Backend (Adil)

- [x] 🔴 Integrate Firebase Auth SDK with FastAPI ([`backend/app/services/firebase_service.py`](../backend/app/services/firebase_service.py))
- [x] 🔴 Implement JWT token issuance and validation middleware ([`backend/app/core/security.py`](../backend/app/core/security.py), [`backend/app/core/dependencies.py`](../backend/app/core/dependencies.py))
  - [x] Access token generation (15-min expiry)
  - [x] Refresh token handling (7-day expiry)
- [x] 🔴 Implement email/password registration and login endpoints ([`backend/app/api/auth.py`](../backend/app/api/auth.py))
- [x] 🟡 Implement Google OAuth 2.0 sign-in flow via Firebase ([`POST /api/v1/auth/google`](../backend/app/api/auth.py))
- [x] 🟡 Implement phone OTP verification via Firebase Phone Auth ([`POST /api/v1/auth/phone/verify`](../backend/app/api/auth.py))
- [x] 🔴 Prototype Anonymous Emergency Mode (temporary disposable session, no PII stored)
- [x] 🟡 Implement device registration endpoint (store FCM token per user per device) ([`POST /api/v1/auth/device`](../backend/app/api/auth.py))
- [x] 🟡 Write idempotency key middleware for registration/login retries ([`backend/app/core/middleware.py`](../backend/app/core/middleware.py))
- [x] 🟢 Add rate limiting on auth endpoints ([`RateLimitMiddleware`](../backend/app/core/middleware.py))

#### Android UI (Dishari)

- [x] 🟡 Implement Anonymous emergency mode bypass (skip login → direct 1-tap SOS)
- [x] 🟡 Build Splash Screen with NearHelp branding and animation in Jetpack Compose ([`SplashScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/SplashScreen.kt))
- [x] 🟡 Build Login Screen (email/password fields, Google sign-in button, OTP option, 1-Tap SOS bypass) ([`LoginScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/LoginScreen.kt))
- [x] 🟡 Build Sign-Up Screen (registration form with validation, blood group chips, terms agreement) ([`SignUpScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/SignUpScreen.kt))
- [x] 🟡 Build Phone OTP Input Screen (6-digit code entry with countdown resend timer) ([`PhoneOtpScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/PhoneOtpScreen.kt))
- [x] 🟢 Implement token storage in encrypted SharedPreferences ([`TokenStorage.kt`](../android/app/src/main/java/com/example/nearhelp/data/local/TokenStorage.kt))
- [x] 🟡 Implement animated tab transitions (`AnimatedContent` slide/fade) between Victim Navigation tabs ([`MainScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/main/MainScreen.kt))
- [x] 🟡 Extract and apply unified `NearHelpTopBar` header across all victim and emergency screens ([`NearHelpTopBar.kt`](../android/app/src/main/java/com/example/nearhelp/ui/components/NearHelpTopBar.kt))

---

### Module 2 — User Profile & Encrypted Medical ID

**Owner**: Adil (Backend) · Dishari (UI)

#### Backend (Adil)

- [x] 🔴 Design schema for User profile and Encrypted Medical ID (Blood Group, Allergies, Pacemaker, Kin Contacts)
- [x] 🔴 Implement CRUD endpoints for user profile (`GET /api/v1/users/me`, `PUT /api/v1/users/me`, `PATCH /api/v1/users/me/medical`)
- [x] 🟡 Implement AES-256 encryption at rest for `medical_conditions`, `known_allergies`, and `medical_notes`
- [x] 🟡 Implement emergency contacts sub-resource endpoints (max 5)
- [x] 🟡 Implement profile photo upload (`POST /api/v1/users/me/photo`)
- [x] 🟢 Implement language preferences storage (ISO 639-1 codes)

#### UI Spec & Prototype (Dishari)

- [x] 🟡 Build Encrypted Medical ID reveal component ([`RescueNavigationScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/navigation/RescueNavigationScreen.kt))
- [x] 🟡 Build Emergency Contacts quick call/SMS action triggers
- [x] 🟡 Build Native Profile Screen in Jetpack Compose ([`ProfileScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/profile/ProfileScreen.kt))
- [x] 🟡 Build Medical ID card view and edit sheets
- [x] 🟡 Build Responder Qualifications & Certifications dialog (`ProfileScreen.kt`)
- [x] 🟡 Build Emergency Activity History dialog with verified incident audit records (`ProfileScreen.kt`)

---

### Module 3 — Skill Verification

**Owner**: Adil (Backend) · Dishari (UI — Phase 2)

#### Backend (Adil)

- [x] 🟡 Implement skill claim endpoint (`POST /api/users/me/skills`)
- [x] 🟡 Implement certificate file upload (PDF/image) to local & static storage (`POST /api/users/me/skills/upload`, `POST /api/users/me/skills/form`)
- [x] 🟡 Build admin verification queue API
  - [x] `GET /api/admin/verification-queue`
  - [x] `POST /api/admin/verification-queue/{id}/approve`
  - [x] `POST /api/admin/verification-queue/{id}/reject`
- [x] 🟡 Design trust score increment logic (+5 per verified skill: CPR, Doctor, EMT, Nurse, First Aid)
- [x] 🟢 Send notification to user on approval/rejection (`NotificationService`)

---

### Module 4 — AI Emergency Detection

**Owner**: Aritra

- [x] 🔴 Define crisis type taxonomy (Medical, Fire, Crime, Road Accident, Gas Leak)
- [x] 🔴 Create 8 curated clinical conditions matrix with symptom descriptors
- [x] 🔴 Prototype text & voice transcript classification schema (`emergency_type`, `sub_type`, `priority`, `confidence`)
- [x] 🔴 Implement embedding generation & cosine similarity classification pipeline in `ai_service/`
- [x] 🟡 Integrate Google Speech-to-Text API for voice input → text pipeline
- [x] 🟡 Integrate Gemini 2.5 Vision for photo input → scene description → text pipeline
- [x] 🟡 Write automated test suite for classification accuracy against test scenarios ([`test_classifier.py`](../ai_service/tests/test_classifier.py))
- [x] 🟡 Define and publish API contract for this module (`POST /api/ai/classify`)

---

### Module 5 — AI Severity Prediction

**Owner**: Aritra

- [x] 🔴 Design structured LLM prompt and scoring matrix for severity scoring (0–100 scale & Level 1–5 triage)
- [x] 🔴 Implement severity-to-action mapping:
  - [x] Level 5 (80–100) → Critical Life Threat (3–5 km radius, 5-min hypoxia window, auto-call 108)
  - [x] Level 4 (50–79) → Urgent Trauma (2–3 km radius, tourniquet/hemostatic protocol)
  - [x] Level 3 (20–49) → Moderate Emergency (1–2 km radius)
  - [x] Level 1–2 (0–19) → Low Priority / Non-acute
- [x] 🟡 Implement clinical confidence score (`98.4%`) and reasoning output
- [x] 🟡 Validate severity prediction test suite in automated test runner ([`test_severity.py`](../ai_service/tests/test_severity.py))
- [x] 🔴 Implement live LLM severity prediction endpoint (`POST /api/ai/severity`)

---

### Module 6 — Smart SOS Engine

**Owner**: Aritra (AI routing logic) · Adil (Backend SOS CRUD + geo queries)

#### SOS API & Geospatial (Adil)

- [x] 🔴 Prototype PostGIS `ST_DWithin` spatial query logic with expanding radial waves (500m → 1.5km → 3km)
- [x] 🔴 Prototype SOS status lifecycle (`IDLE` → `COUNTDOWN` → `SOS_TRIGGERED` → `AI_TRIAGING` → `RESPONDER_ACCEPTED` → `HANDOVER_108` → `RESOLVED`)
- [x] 🔴 Design and apply `sos_events` database schema with PostGIS POINT column in PostgreSQL
- [x] 🔴 Implement `POST /api/sos/create` endpoint (idempotent, HTTPS)
- [x] 🔴 Create spatial index on user location column (`CREATE INDEX ... USING GIST`)
- [x] 🟡 Implement `responses` table and `POST /api/sos/{id}/respond` endpoint

#### Responder Ranking (Aritra)

- [x] 🔴 Define weighted scoring formula: `score = w1·(1/distance) + w2·(skill_match) + w3·(reliability_score)`
- [x] 🟡 Prototype rank ordering for CPR-certified responders vs. distance
- [x] 🟡 Tune weights against production test scenarios in `ai_service/`

#### 3-Layer Escalation (Aritra + Adil)

- [x] 🟡 Layer 1: Auto-radius expansion (0–30s: Community Network 500m–1.5km)
- [x] 🟡 Layer 2: Direct 108/112 municipal ambulance gateway auto-escalation (30–60s)
- [x] 🟢 Layer 3: Guided self-care AI fallback (cached RAG, offline-capable protocol)

#### UI Screen Prototypes (Dishari)

- [x] 🔴 Build Main SOS Trigger Screen in Jetpack Compose ([`SosTriggerScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/sos/SosTriggerScreen.kt))
  - [x] Large red circular SOS button with radial breathing pulse animation
  - [x] 3-second hold / 5-second abort countdown ring with "Cancel" protection
  - [x] Emergency category chips (Medical, Fire, Crime, Accident)
  - [x] Hold-to-Voice SOS button with audio waveform visualizer
  - [x] Photo attach preview with AI detection bounding overlay
  - [x] Anonymous Emergency Mode toggle

---

### Module 7 — Dynamic Community Geo-Map

**Owner**: Dishari (UI) · Adil (Backend markers data)

#### UI Map Component (Dishari)

- [x] 🔴 Build interactive Geo-Map component in Jetpack Compose ([`CommunityGeoMapScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/map/CommunityGeoMapScreen.kt))
- [x] 🔴 Display victim pin (red pulsing beacon marker)
- [x] 🟡 Display responder pins (green beacon markers with CPR/Doctor skill badges)
- [x] 🟡 Display facility markers (hospitals with live bed & ICU count, verified AED locators)
- [x] 🟡 Implement toggle layers for responders, hospitals, and AEDs
- [x] 🟡 Render expanding PostGIS radial query dispatch circle animation
- [x] 🟡 Implement full-screen touch gestures (pinch-zoom, drag-pan), bottom facility detail sheet cards, and direct directions trigger ([`CommunityGeoMapScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/map/CommunityGeoMapScreen.kt))

#### Backend (Adil)

- [x] 🟡 Define Kolkata regional dataset schemas (Salt Lake Sector V, EM Bypass, hospitals, trauma centers)
- [x] 🟡 Implement `GET /api/facilities/nearby` endpoint ([`facilities.py`](../backend/app/api/facilities.py))
- [x] 🟢 Seed database with Kolkata hospital & AED data ([`kolkata_facilities.json`](../data/regional/kolkata_facilities.json))

---

### Module 8 — Live Tracking Stream

**Owner**: Adil (Backend WebSocket) · Dishari (UI)

#### Backend (Adil)

- [x] 🔴 Model real-time GPS coordinate streaming payload
- [x] 🟡 Model ETA calculation and distance tracking
- [x] 🔴 Set up FastAPI WebSockets server (`/ws/tracking/{incident_id}`)
- [x] 🟡 Implement connection lifecycle management (reconnect, heartbeat)

#### UI Component (Dishari)

- [x] 🟡 Implement live turn-by-turn navigation and ETA card ([`RescueNavigationScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/navigation/RescueNavigationScreen.kt))
- [x] 🟡 Consume WebSocket stream in Android Jetpack Compose client ([`LiveTrackingScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/tracking/LiveTrackingScreen.kt))
- [x] 🟡 Apply `VictimDesignSystem` UI styling with live ETA countdown, responder profile card, and bottom slide-to-cancel pill ([`LiveTrackingScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/tracking/LiveTrackingScreen.kt))

---

### Module 9 — AI Navigation & Rescue Routing

**Owner**: Aritra (routing logic) · Dishari (UI)

- [x] 🟡 Model route calculation and turn-by-turn directions ([`RescueNavigationViewModel.kt`](../android/app/src/main/java/com/example/nearhelp/ui/navigation/RescueNavigationViewModel.kt))
- [x] 🟡 Integrate Google Directions & Routes API for production route calculation in Android app ([`RoutingService.py`](../backend/app/services/routing_service.py), [`RescueNavigationScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/navigation/RescueNavigationScreen.kt))
- [x] 🟢 Implement detour and traffic consideration logic ([`routing_service.py`](../backend/app/services/routing_service.py), [`RescueNavigationViewModel.kt`](../android/app/src/main/java/com/example/nearhelp/ui/navigation/RescueNavigationViewModel.kt))

---

### Module 10 — AI Crisis Assistant (Emergency Agent)

**Owner**: Aritra

- [x] 🔴 Design emergency state machine (Understand emergency → Triage → Step-by-Step Guidance → Handover)
- [x] 🔴 Implement Grounded First-Aid Protocol Screen in Jetpack Compose ([`AiCrisisAssistantScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/assistant/AiCrisisAssistantScreen.kt))
- [x] 🔴 Implement AHA/ERC **110 BPM CPR Rhythm Metronome** (visual pulse + audio click at 545.45ms period)
- [x] 🔴 Implement Bystander AI Assistant Q&A Chat Drawer with clinical contraindication guardrails
- [x] 🔴 Implement Section 134A Motor Vehicles (Amendment) Act 2019 legal immunity badge
- [x] 🔴 Set up LangGraph agent orchestration framework in `ai_service/` ([`gemini_agent.py`](../ai_service/app/agent/gemini_agent.py), [`state.py`](../ai_service/app/agent/state.py), [`knowledge.py`](../ai_service/app/agent/knowledge.py))
- [x] 🔴 Integrate Gemini 2.5 LLM with citation enforcement ([`gemini_agent.py`](../ai_service/app/agent/gemini_agent.py), [`knowledge.py`](../ai_service/app/agent/knowledge.py))
- [x] 🟡 Implement production WebSocket endpoint (`/ws/ai/chat`) ([`agent_ws.py`](../ai_service/app/api/agent_ws.py), [`ai_ws.py`](../backend/app/api/ai_ws.py))
- [x] 🟡 Expand protocol repository to 8 clinical emergency protocols (Cardiac Arrest, Severe Bleeding, Choking, Burns, Seizure, Anaphylaxis, Asthma, Fracture)
- [x] 🟡 Eliminate layout clipping via dynamic content height calculations and safe bottom navigation padding ([`AiCrisisAssistantScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/assistant/AiCrisisAssistantScreen.kt))

---

### Module 11 — RAG Knowledge Base

**Owner**: Aritra (pipeline) · Plaban (data curation)

#### RAG Pipeline (Aritra)

- [x] 🔴 Prototype RAG retrieval logic and prompt guardrails for OHCA cardiac arrest and trauma bleed
- [x] 🔴 Set up vector store (ChromaDB / pgvector) in `ai_service/` ([`store.py`](../ai_service/app/rag/store.py))
- [x] 🔴 Implement document chunking pipeline (passage-level chunking) ([`chunker.py`](../ai_service/app/rag/chunker.py))
- [x] 🔴 Implement embedding generation & semantic search ([`retriever.py`](../ai_service/app/rag/retriever.py), [`rag.py`](../ai_service/app/api/rag.py))
- [x] 🟡 Implement hallucination guardrails (no unverified medication, no surgical directives) ([`guardrails.py`](../ai_service/app/rag/guardrails.py))

#### Data Curation (Plaban)

- [x] 🔴 Curate baseline first-aid protocols for Cardiac Arrest, Road Accident, and Trauma
- [x] 🔴 Download and catalog official WHO First Aid guidelines into `data/protocols/` ([`who_first_aid_guidelines.json`](../data/protocols/who_first_aid_guidelines.json), [`who_trauma_care.md`](../data/protocols/who_trauma_care.md))
- [x] 🔴 Download and catalog Red Cross emergency protocols ([`red_cross_emergency_protocols.json`](../data/protocols/red_cross_emergency_protocols.json), [`red_cross_first_aid.md`](../data/protocols/red_cross_first_aid.md))
- [x] 🟡 Download and catalog NDMA Disaster Response guidelines & AHA CPR guides ([`ndma_disaster_guidelines.json`](../data/protocols/ndma_disaster_guidelines.json), [`aha_cpr_ecc_guidelines.json`](../data/protocols/aha_cpr_ecc_guidelines.json), [`aiims_poison_protocols.json`](../data/protocols/aiims_poison_protocols.json))

---

## 📐 Phase 2 — System Enhancements (Weeks 9–11: Oct 01 – Oct 20, 2026)

> **Goal**: Multilingual translation (Bengali ⇄ English), Voice SOS STT, emergency timeline streaming, reputation, and verified skills.  
> **Modules**: 12–17  
> **Target Milestone Completion**: October 20, 2026

---

### Module 12 — AI Multilingual Translation

**Owner**: Aritra

- [x] 🟡 Prototype real-time Bengali ⇄ English emergency translation pipeline
- [x] 🟡 Validate translation pipeline in automated test suite
- [ ] 🟡 Implement live Gemini API translation service for cross-language chat

---

### Module 13 — Voice SOS Processing

**Owner**: Aritra (pipeline) · Dishari (UI)

- [x] 🟡 Prototype voice SOS wave visualizer and speech transcript preview
- [ ] 🟡 Implement backend Speech-to-Text audio transcription pipeline
- [ ] 🟡 Auto-create SOS event from structured JSON extracted from audio

---

### Module 14 — Emergency Timeline & Audit Trail

**Owner**: Adil (Backend) · Dishari (UI)

- [x] 🟡 Prototype automated milestone event injection into chat feed (SOS Created → Triaged → Accepted → Arrived → Handover → Resolved)
- [ ] 🟡 Implement real-time timeline event broadcasting via WebSocket

---

### Module 15 — AI Incident Report Generator

**Owner**: Aritra

- [x] 🟡 Implement clinical handover report generator
  - [x] Patient vitals, blood type, and estimated CPR chest compressions
  - [x] Section 134A Good Samaritan Legal Immunity Seal
  - [x] SHA-256 digital signature hash for audit integrity
  - [x] PDF / Markdown export preview
- [ ] 🟡 Implement automated email delivery of clinical summary to receiving hospital/paramedics

---

### Module 16 — Reputation Engine

**Owner**: Adil

- [x] 🟡 Design trust score calculation model (0–100 score, verified skill badges)
- [ ] 🟡 Implement automated trust score updates upon verified rescue resolution

---

### Module 17 — Community Layer & City Datasets

**Owner**: Dishari (UI) · Plaban (data)

- [x] 🟢 Prototype AED, hospital bed count, and trauma center map markers
- [ ] 🟡 Compile comprehensive Kolkata hospital, blood bank, fire, and police station JSON in `data/regional/`

---

## 📐 Phase 3 — Pre-Exam Code Freeze, Benchmarks & Final Defense (Weeks 12–14: Oct 21 – Nov 10, 2026)

> **Goal**: Admin command center, digital twin load simulator benchmarks (5 charts), thesis documentation, demo video, and pre-exam defense freeze.  
> **Modules**: 18–24  
> **⚠️ PRE-EXAM CRITICAL TIMELINE**:  
> - **October 31, 2026**: Complete Code Freeze across all repositories (Android, Backend, AI).  
> - **November 01 – 10, 2026**: Final benchmark charts export, academic thesis report binding (Abhisikta), 3-min video walkthrough (Sayantan), and Viva dry run.  
> - **November 10, 2026**: 100% Complete & Project Handed In. Team fully offboarded for semester exams!

---

### Module 18 — Admin Command Center Dashboard

**Owner**: Adil (Backend APIs) · Aritra (integration)

- [x] ⚪ Define Command Center incident monitoring and telemetry endpoints
  - [x] Live system telemetry schemas (Active emergencies, Bystander count, dispatch latency, RAG index)
  - [x] Real-time incident feed filtering by Severity (Level 1–5) and Status
  - [x] Post-incident report generator data contracts
- [ ] ⚪ Connect live PostgreSQL/Redis event stream to command endpoints

---

### Module 21 — Guardian Safety Mode

**Owner**: Dishari (UI) · Adil (Backend)

- [x] ⚪ Prototype Guardian Radar safety score & safe perimeter indicator model
- [ ] ⚪ Build native Guardian Mode contact manager in Android app

---

### Module 22 — Offline Mesh / SMS Fallback

**Owner**: Aritra + Adil

- [x] ⚪ Prototype Scenario C: Zero-network SMS / Mesh payload packet simulation
- [ ] ⚪ Implement Android SMS fallback intent when cellular data is unavailable

---

### Module 23 — Digital Twin Load Simulator

**Owner**: Aritra

- [x] 🟡 Prototype scenario controller and incident state generation models
- [ ] 🟡 Build Locust / k6 load simulation scripts in `simulator/`
- [ ] 🟡 Generate 5 publishable benchmark charts for thesis/viva defense

---

## 🎨 Design System & Presentation Assets (Dishari + Sayantan)

- [x] 🔴 High-contrast dark theme tokens (`#121212`, `#1E1E1E`, `#E53935`, `#FF9800`, `#4CAF50`, `#2196F3`)
- [x] 🔴 Emergency typography and icons kit
- [ ] 🟡 Export production Android vector assets into `assets/`
- [ ] 🟡 Record and edit 3-minute narrated demo video walkthrough

---

## 📚 Documentation & Academic Deliverables (Abhisikta)

- [x] 🔴 **Comprehensive Project Review Report** ([`archive/review-1/01_PROJECT_REVIEW_REPORT.md`](../archive/review-1/01_PROJECT_REVIEW_REPORT.md) - 366 lines)
- [x] 🔴 **8-Slide Presentation Deck** ([`archive/review-1/02_PRESENTATION_SLIDES.md`](../archive/review-1/02_PRESENTATION_SLIDES.md))
- [x] 🔴 **Team Speaking Script** ([`archive/review-1/03_TEAM_SPEAKING_SCRIPT.md`](../archive/review-1/03_TEAM_SPEAKING_SCRIPT.md))
- [x] 🔴 **Examiner Q&A Defense Guide** ([`archive/review-1/04_EXAMINER_QA_DEFENSE_GUIDE.md`](../archive/review-1/04_EXAMINER_QA_DEFENSE_GUIDE.md))
- [x] 🔴 **Team Dry Run Rehearsal Guide** ([`archive/review-1/05_TEAM_DRY_RUN_REHEARSAL_GUIDE.md`](../archive/review-1/05_TEAM_DRY_RUN_REHEARSAL_GUIDE.md))
- [ ] 🟡 Formal SRS Document (Software Requirements Specification)
- [ ] 🟡 Formal SDD Document (Software Design Document with UML Diagrams)
- [ ] 🟡 Final Project Report & User Manual compilation

---

## 🧪 Testing & Quality Assurance

- [x] 🔴 **Backend Test Suite (FastAPI + PostGIS + WebSockets)**: 90/90 unit & integration tests passing ([`backend/tests/`](../backend/tests/))
  - [x] Suite 1: Authentication & Token Lifecycle (`test_auth.py`)
  - [x] Suite 2: AES-256 Medical ID Encryption (`test_crypto.py`)
  - [x] Suite 3: User Profile & Medical Records (`test_profile_medical.py`)
  - [x] Suite 4: Skill Verification Queue (`test_skill_verification.py`)
  - [x] Suite 5: SOS Engine Lifecycle & Radial Expansion (`test_sos_engine.py`)
  - [x] Suite 6: PostGIS Facilities Nearby Query (`test_facilities.py`)
  - [x] Suite 7: Live GPS Coordinate WebSocket Streaming (`test_tracking_ws.py`)
  - [x] Suite 8: Google Routes & Navigation Service (`test_routing.py`)
  - [x] Suite 9: AI Classification, Severity & RAG Proxies (`test_ai_*.py`)
- [x] 🔴 **AI Service Test Suite (Gemini 2.5 + ChromaDB + LangGraph)**: 72/72 unit & RAG tests passing ([`ai_service/tests/`](../ai_service/tests/))
  - [x] Suite 1: AI Emergency Classifier & Cosine Similarity (`test_classifier.py`)
  - [x] Suite 2: Triage & Severity Prediction (`test_severity.py`)
  - [x] Suite 3: Passage-Level Chunker & Protocol Parser (`test_chunker.py`)
  - [x] Suite 4: ChromaDB Vector Store & Semantic Retriever (`test_rag.py`, `test_rag_api.py`)
  - [x] Suite 5: Clinical Contraindication & Hallucination Guardrails (`test_guardrails.py`)
  - [x] Suite 6: LangGraph Crisis Assistant Agent & Citations (`test_agent.py`)
  - [x] Suite 7: AI Chat Real-Time WebSockets (`test_agent_ws.py`)
- [x] 🟢 **Android Test Suite (JUnit + Compose ViewModels & Transitions)**: 44/44 unit tests passing ([`android/app/src/test/`](../android/app/src/test/))
  - [x] Suite 1: AuthViewModel & Login/Signup States (`AuthViewModelTest.kt`)
  - [x] Suite 2: AuthRepository & Token Storage (`AuthRepositoryTest.kt`)
  - [x] Suite 3: RescueNavigationViewModel (`RescueNavigationViewModelTest.kt`)
  - [x] Suite 4: CommunityGeoMapViewModel & Layer Toggles (`CommunityGeoMapViewModelTest.kt`)
  - [x] Suite 5: ProfileViewModel & Medical ID State (`ProfileViewModelTest.kt`)
  - [x] Suite 6: LiveTrackingViewModel & Real-time ETA (`LiveTrackingViewModelTest.kt`)
  - [x] Suite 7: MainScreenViewModel & Bottom Navigation (`MainScreenViewModelTest.kt`)
  - [x] Suite 8: NavigationTransitionTest (`NavigationTransitionTest.kt`)

---

## 📊 Progress Summary

| Phase | Total Tasks | Completed | In Progress / Pending | Status | Target Deadline |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Review 1 Deliverables** | 30 | 30 | 0 | 🟢 **100% Complete** | Done |
| **Infrastructure & Monorepo** | 15 | 11 | 4 | 🟢 Ready | Active |
| **Phase 1 (MVP Production Core)** | 95 | 95 | 0 | 🟢 **100% Complete** | Sep 27 (Post-CT2 Live Loop) |
| **Phase 2 (Enhancements)** | 55 | 12 | 43 | 🟡 Active Sprints | Oct 11 (Pre-Puja Lock) |
| **Phase 3 (Admin, Stretch & Defense)** | 35 | 8 | 27 | ⚪ Scheduled | Oct 31 (Hard Code Freeze) |
| **Testing & QA** | 20 | 20 | 0 | 🟢 **206/206 Tests Passing** | All Suites Green |
| **Documentation & Review** | 15 | 8 | 7 | 🟢 Review 1 Ready | Nov 07 (Final Sign-Off) |
| **Total Ecosystem** | **~265** | **184** | **81** | 🟢 **Phase 1 Core Cleared (69.4%)** | **Nov 07, 2026** |

### 🗓️ Strategic Academic Timeframe & Milestone Roadmap (First Week of Nov Deadline)

> Detailed breakdown with capacity analysis available in [`docs/timeframe.md`](timeframe.md).

| Sprint / Period | Calendar Dates | Core Focus & Academic Context | Critical Deliverables | Target Milestone |
| :--- | :--- | :--- | :--- | :--- |
| **Week 1** | **Sep 14 – Sep 20, 2026** | 🟡 **Academic Gap: CT2 Exams** | **Class Test 2 (CT2) Exams**. Focus on exam prep. Minimal project activity (~15% load). Async checks & stability monitoring. | Exam Focus |
| **Sprint 1 (Week 2)** | **Sep 21 – Sep 27, 2026** | 🟢 **Post-CT2 Hardware Loop** | Physical FCM push on 2 devices, live WebSocket GPS & chat on hardware, formal SRS Draft & UML diagrams (Abhisikta). | **Sep 27, 2026** |
| **Sprint 2 (Week 3)** | **Sep 28 – Oct 04, 2026** | 🟢 **Phase 2 Enhancements A** | Gemini Bengali ⇄ English translation, emergency timeline WebSocket event stream, SDD document & DFDs Level 0-2. | **Oct 04, 2026** |
| **Sprint 3 (Week 4)** | **Oct 05 – Oct 11, 2026** | 🟢 **Phase 2 Enhancements B (Pre-Puja Push)** | Voice SOS STT audio pipeline, Kolkata regional datasets JSON (hospitals, AEDs, blood banks), **Clean Git Branch Push before Puja**. | **Oct 11, 2026** |
| **Weeks 5–6** | **Oct 12 – Oct 25, 2026** | 🔴 **Festival Gap: Durga Puja** | **Durga Puja & Festival Vacation (~2 Weeks)**. Sasthi to Dashami (Oct 16–21) + Lakshmi Puja (Oct 25). Zero scheduled meetings. Family time & cultural celebration. | Festival Break |
| **Sprint 4 (Week 7)** | **Oct 26 – Nov 01, 2026** | 🟣 **Load Sim & Hard Code Freeze** | Digital Twin load simulator (100 SOS load test, 5 charts), Admin command telemetry stream. **OCTOBER 31: ABSOLUTE HARD CODE FREEZE**. | **Oct 31, 2026 (Code Freeze)** |
| **Sprint 5 (Week 8)** | **Nov 02 – Nov 07, 2026** | 🏁 **Thesis, Media & Viva Sign-off (First Week Nov)** | Final Project Report bound (Abhisikta), 3-min video walkthrough (Sayantan), Project Poster, Team Viva Dry Run. | **Nov 07, 2026 (100% Complete)** |
| **Post-Nov 07** | **Nov 08 onwards** | 🎓 **Semester Exam Period** | **ZERO Project Involvements — 100% Dedicated to Semester Exam Preparation** | Semester Exams |
