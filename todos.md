# NearHelp AI — Master TODO List

> **Last Updated**: 2026-08-12  
> **Project Start**: 2026-08-10  
> **Timeline**: 4 Months · 3 Phases · 24 Modules  
> **Status**: 🟡 Planning & Setup Phase

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

## 🏗️ Project Infrastructure & Setup

### Repository & DevOps

- [ ] 🔴 Set up monorepo directory structure (`/backend`, `/android`, `/ai-service`, `/docs`, `/data`, `/assets`)
- [ ] 🔴 Create `.gitignore` for Python, Kotlin/Android, environment files, and data directories
- [ ] 🔴 Set up `docker-compose.yml` for local development (PostgreSQL + PostGIS, Redis, FastAPI, AI service)
- [ ] 🟡 Create individual `Dockerfile` for backend service
- [ ] 🟡 Create individual `Dockerfile` for AI service
- [ ] 🟡 Set up GitHub Actions CI/CD pipeline
  - [ ] Python linting & tests (backend)
  - [ ] Python linting & tests (AI service)
  - [ ] Android build check
  - [ ] Docker image build verification
- [ ] 🟡 Configure environment variable templates (`.env.example`) for all services
- [ ] 🟢 Set up branch protection rules (`main`, `develop`)
- [ ] 🟢 Configure pre-commit hooks (black, ruff, isort for Python)

### Development Environment Documentation

- [ ] 🟡 Write `CONTRIBUTING.md` with setup instructions for each service
- [ ] 🟡 Write local development quickstart guide
- [ ] 🟢 Document API contract agreement process between backend, AI service, and Android

---

## 📐 Phase 1 — MVP Core (Months 1–2)

> **Goal**: End-to-end SOS lifecycle proven: trigger → classify → rank → alert → guide → coordinate → resolve.  
> **Modules**: 1–11

---

### Module 1 — Authentication & Identity

**Owner**: Adil (Backend) · Dishari (UI)

#### Backend (Adil)

- [ ] 🔴 Integrate Firebase Auth SDK with FastAPI
- [ ] 🔴 Implement JWT token issuance and validation middleware
  - [ ] Access token generation (15-min expiry)
  - [ ] Refresh token handling (7-day expiry)
- [ ] 🔴 Implement email/password registration and login endpoints
- [ ] 🟡 Implement Google OAuth 2.0 sign-in flow via Firebase
- [ ] 🟡 Implement phone OTP verification via Firebase Phone Auth
- [ ] 🔴 Implement Anonymous Emergency Mode (temporary disposable JWT, no PII stored)
- [ ] 🟡 Implement device registration endpoint (store FCM token per user per device)
- [ ] 🟡 Write idempotency key middleware for registration/login retries
- [ ] 🟢 Add rate limiting on auth endpoints

#### Android UI (Dishari)

- [ ] 🟡 Build Splash Screen with NearHelp branding and animation
- [ ] 🟡 Build Login Screen (email/password fields, Google sign-in button, OTP option)
- [ ] 🟡 Build Sign-Up Screen (registration form with validation)
- [ ] 🟡 Build Phone OTP Input Screen (6-digit code entry with auto-read)
- [ ] 🟡 Implement anonymous emergency mode bypass (skip login → direct SOS)
- [ ] 🟢 Implement token storage in encrypted SharedPreferences

---

### Module 2 — User Profile

**Owner**: Adil (Backend) · Dishari (UI)

#### Backend (Adil)

- [ ] 🔴 Design and apply User profile database schema (see proposal §8 Module 2)
- [ ] 🔴 Implement CRUD endpoints for user profile
  - [ ] `GET /api/users/me`
  - [ ] `PUT /api/users/me`
  - [ ] `PATCH /api/users/me/medical`
- [ ] 🟡 Implement AES-256 encryption at rest for `medical_conditions` and `known_allergies`
- [ ] 🟡 Implement emergency contacts sub-resource endpoints (max 5)
- [ ] 🟡 Implement profile photo upload (Firebase Storage or S3-compatible)
- [ ] 🟢 Implement language preferences storage (ISO 639-1 codes)

#### Android UI (Dishari)

- [ ] 🟡 Build Profile Screen layout (name, photo, blood group, medical info)
- [ ] 🟡 Build Emergency Contacts list view (add/edit/delete)
- [ ] 🟡 Build Medical ID card view (allergy list, conditions, blood group)
- [ ] 🟢 Build language selector component

---

### Module 3 — Skill Verification

**Owner**: Adil (Backend) · Dishari (UI — Phase 2)

#### Backend (Adil)

- [ ] 🟡 Implement skill claim endpoint (`POST /api/users/me/skills`)
- [ ] 🟡 Implement certificate file upload (PDF/image) to cloud storage
- [ ] 🟡 Build admin verification queue API
  - [ ] `GET /api/admin/verification-queue`
  - [ ] `POST /api/admin/verification-queue/{id}/approve`
  - [ ] `POST /api/admin/verification-queue/{id}/reject`
- [ ] 🟡 Implement trust score increment (+5 per verified skill)
- [ ] 🟢 Send notification to user on approval/rejection

---

### Module 4 — AI Emergency Detection

**Owner**: Aritra

- [ ] 🔴 Define crisis type taxonomy (medical, fire, gas_leak, accident, natural_disaster, security_threat, etc.)
- [ ] 🔴 Generate reference embeddings for each crisis type description
- [ ] 🔴 Implement text input → embedding → cosine similarity classification pipeline
- [ ] 🔴 Implement structured JSON output schema (emergency_type, sub_type, priority, confidence, etc.)
- [ ] 🟡 Integrate Google Speech-to-Text API for voice input → text pipeline
- [ ] 🟡 Integrate Gemini 2.5 Vision for photo input → scene description → text pipeline
- [ ] 🟢 Implement video frame extraction → vision → aggregated classification
- [ ] 🟡 Write unit tests for classification accuracy against test scenarios
- [ ] 🟡 Define and publish API contract for this module (`POST /api/ai/classify`)

---

### Module 5 — AI Severity Prediction

**Owner**: Aritra

- [ ] 🔴 Design structured LLM prompt for severity scoring (0–100 scale)
- [ ] 🔴 Implement severity prediction pipeline (emergency_type + description + keywords → LLM → score)
- [ ] 🔴 Implement severity-to-action mapping:
  - [ ] 80–100 → Critical (3–5 km radius, auto-call services)
  - [ ] 50–79 → High (2–3 km, push to top-ranked)
  - [ ] 20–49 → Medium (1–2 km, standard notification)
  - [ ] 0–19 → Low (0.5–1 km, low-priority)
- [ ] 🟡 Implement confidence score and reasoning output
- [ ] 🟡 Write test suite with sample emergency descriptions and expected severity ranges
- [ ] 🟡 Define and publish API contract (`POST /api/ai/severity`)

---

### Module 6 — Smart SOS Engine

**Owner**: Aritra (AI routing logic) · Adil (Backend SOS CRUD + geo queries)

#### SOS API & Geospatial (Adil)

- [ ] 🔴 Design and apply `sos_events` database schema with PostGIS POINT column
- [ ] 🔴 Implement `POST /api/sos/create` endpoint (idempotent, HTTPS)
- [ ] 🔴 Implement PostGIS `ST_DWithin` geospatial query to find nearby users
- [ ] 🔴 Create spatial index on user location column (`CREATE INDEX ... USING GIST`)
- [ ] 🟡 Implement `responses` table and `POST /api/sos/{id}/respond` endpoint (idempotent)
- [ ] 🟡 Implement SOS status lifecycle (active → responding → resolved → closed)
- [ ] 🟡 Implement privacy guard: strip location when `is_anonymous = true`

#### Responder Ranking (Aritra)

- [ ] 🔴 Implement weighted scoring function: `score = w1·(1/distance) + w2·(skill_match) + w3·(reliability_score)`
- [ ] 🔴 Define initial fixed weights (w1, w2, w3)
- [ ] 🟡 Rank candidates and return top-N based on severity
- [ ] 🟡 Tune weights against hand-crafted test scenarios
- [ ] 🟡 Write comparison benchmark: ranked dispatch vs. naive broadcast

#### 3-Layer Escalation (Aritra + Adil)

- [ ] 🟡 Layer 1: Auto-radius expansion (30s → 2×, 45s → 3×) with re-ranking
- [ ] 🟡 Layer 2: Direct 108/112 dial with AI-generated summary (60s gate)
- [ ] 🟢 Layer 3: Guided self-care AI fallback (cached RAG, offline-capable)

#### Android UI (Dishari)

- [ ] 🔴 Build Main SOS Trigger Screen (`HomeScreen.kt`)
  - [ ] Large red circular SOS button with pulse ripple animation
  - [ ] Haptic feedback on tap (double-vibration)
  - [ ] Emergency category chips (Medical, Fire, Crime, Accident)
- [ ] 🟡 Build multi-modal intake toolbar
  - [ ] Hold-to-Voice SOS button with audio waveform visualizer
  - [ ] Photo attach button for injury/scene photo
- [ ] 🟡 Build Anonymous Emergency Mode toggle
- [ ] 🟡 Build Offline Status Banner ("Cellular Network Only — SMS Backup Active")

---

### Module 7 — Live Map

**Owner**: Dishari (UI) · Adil (Backend markers data)

#### Android UI (Dishari)

- [ ] 🔴 Integrate Google Maps SDK in Jetpack Compose
- [ ] 🔴 Display victim pin (red pulsing beacon marker)
- [ ] 🟡 Display responder pins (blue markers with skill icons)
- [ ] 🟡 Display facility markers (hospitals, AEDs, blood banks, fire/police stations)
- [ ] 🟡 Implement toggle layers for different facility types
- [ ] 🟡 Build Bottom ETA Sheet (closest responder photo, badge, ETA, distance, Call/Chat buttons)
- [ ] 🟢 Implement smooth marker animation for responder movement

#### Backend (Adil)

- [ ] 🟡 Implement endpoint to return nearby facilities by category and location
- [ ] 🟢 Seed database with Kolkata facility data (from Plaban's datasets)

---

### Module 8 — Live Tracking

**Owner**: Adil (Backend WebSocket) · Dishari (UI)

#### Backend (Adil)

- [ ] 🔴 Set up WebSocket server (FastAPI WebSockets or Socket.io)
- [ ] 🔴 Implement live GPS coordinate streaming (responder → server → victim)
- [ ] 🟡 Implement ETA calculation based on real-time location updates
- [ ] 🟡 Implement connection lifecycle management (connect, reconnect, heartbeat, disconnect)
- [ ] 🟢 Rate-limit location updates (e.g., max once per second)

#### Android UI (Dishari)

- [ ] 🟡 Consume WebSocket stream and update responder marker position in real-time
- [ ] 🟡 Display ETA countdown card (distance, estimated time)
- [ ] 🟢 Implement smooth polyline path rendering

---

### Module 9 — AI Navigation

**Owner**: Aritra (route optimization logic) · Dishari (UI)

- [ ] 🟡 Integrate Google Directions API for route calculation
- [ ] 🟡 Implement route factor consideration (traffic, road closures — via Google API params)
- [ ] 🟢 Implement alternative route suggestion for emergency detours
- [ ] 🟢 Display navigation turn-by-turn on LiveMapScreen

---

### Module 10 — AI Crisis Assistant (Emergency Agent)

**Owner**: Aritra

- [ ] 🔴 Set up LangGraph agent orchestration framework
- [ ] 🔴 Design agent state machine:
  - [ ] Understand emergency → Ask follow-up → Provide first aid → Generate summary → Coordinate
- [ ] 🔴 Implement Gemini 2.5 LLM integration via LangGraph
- [ ] 🔴 Implement structured prompt with citation enforcement (model must cite retrieved procedure steps)
- [ ] 🟡 Implement first-aid guidance generation (step-by-step checklist format)
- [ ] 🟡 Implement emergency summary auto-generation (pre-filled for 108/112 call)
- [ ] 🟡 Implement follow-up question generation for ambiguous situations
- [ ] 🟡 Implement nearest hospital suggestion based on emergency type + location
- [ ] 🟡 Define and publish API contract (`POST /api/ai/assist`, WebSocket `/ws/ai/chat`)

---

### Module 11 — RAG Knowledge Base

**Owner**: Aritra (pipeline) · Plaban (data curation)

#### RAG Pipeline (Aritra)

- [ ] 🔴 Set up vector store (ChromaDB or pgvector)
- [ ] 🔴 Implement document chunking pipeline (procedure-level passages)
- [ ] 🔴 Implement embedding generation using small embedding model
- [ ] 🔴 Implement retrieval pipeline (query embedding → top-k similar chunks)
- [ ] 🔴 Integrate retrieved passages into LLM prompt for grounded generation
- [ ] 🟡 Implement retrieval quality evaluation (precision/recall against known queries)
- [ ] 🟡 Tune chunk size and overlap for optimal retrieval
- [ ] 🟡 Implement hallucination guardrails (no dosage, no diagnosis, no prescriptions)

#### Data Curation (Plaban)

- [ ] 🔴 Download and catalog WHO First Aid guidelines
- [ ] 🔴 Download and catalog Red Cross emergency protocols
- [ ] 🟡 Download and catalog NDMA Disaster Response guidelines
- [ ] 🟡 Download and catalog AHA CPR guides
- [ ] 🟡 Organize documents into category folders (`/medical`, `/disaster`, `/fire`, `/trauma`)
- [ ] 🟡 Create master catalog sheet (source name, publication year, URL, license type)
- [ ] 🟢 Download and catalog St. John Ambulance protocols
- [ ] 🟢 Download and catalog fire safety manual documents

---

### Module 1 UI — Active SOS & AI Guidance Screen (Dishari)

- [ ] 🟡 Build Active SOS Screen (`ActiveSOSScreen.kt`)
  - [ ] 3-Layer Escalation status bar (Layer 1/2/3 visual states)
  - [ ] AI Severity & Classification badge card (e.g., "Level 4 — Critical")
  - [ ] AI Protocol First-Aid Guidance card (step-by-step checklist with checkmarks)
  - [ ] Non-dismissible legal disclaimer card (Good Samaritan Law notice)

### Responder Alert Screen (Dishari)

- [ ] 🟡 Build Responder Incoming SOS Alert Modal (`ResponderAlertActivity.kt`)
  - [ ] Full-screen high-priority overlay (wakes screen, bypasses DND)
  - [ ] Emergency type, severity, distance, ETA display
  - [ ] Required skills display
  - [ ] "I'M RESPONDING" large green button
  - [ ] "Pass / Decline" button

### In-App Chat Screen (Dishari)

- [ ] 🟡 Build Chat Screen (`ChatScreen.kt`)
  - [ ] Real-time Socket.io chat feed
  - [ ] Auto-translation pill banner ("Translated from Bengali → English")
  - [ ] Automated timeline event injection into chat stream

---

### Notification Gateway (Adil)

- [ ] 🔴 Integrate Firebase Cloud Messaging (FCM) server SDK
- [ ] 🔴 Implement push notification fan-out to ranked responders
- [ ] 🟡 Use FCM High Priority for emergency alerts (wake device, bypass Doze)
- [ ] 🟡 Implement delivery receipt tracking
- [ ] 🟡 Implement retry logic on delivery failure
- [ ] 🟢 Implement secondary notification channel fallback

---

### PostgreSQL + PostGIS Database (Adil)

- [ ] 🔴 Set up PostgreSQL instance with PostGIS extension enabled
- [ ] 🔴 Design and apply initial schema migrations (users, sos_events, responses, messages, ai_summaries)
- [ ] 🔴 Create spatial indexes on location columns
- [ ] 🟡 Implement database migration tooling (Alembic)
- [ ] 🟡 Write seed data scripts for development/testing
- [ ] 🟢 Benchmark `ST_DWithin` query performance with simulated data

---

### API Contract & Integration (Aritra)

- [ ] 🔴 Define and publish OpenAPI/JSON specs for all Phase 1 endpoints
- [ ] 🟡 Provide mock JSON payloads for Dishari (Android can develop against mock data)
- [ ] 🟡 Set up Swagger UI auto-generation from FastAPI

---

## 📐 Phase 2 — Enhancement (Month 3)

> **Goal**: Enrich responder/victim experience with multilingual support, voice SOS, timeline, reputation, and community features.  
> **Modules**: 12–17  
> ⚠️ **Phase 2 begins only after all Phase 1 modules pass acceptance criteria.**

---

### Module 12 — AI Translation

**Owner**: Aritra

- [ ] 🟡 Implement multilingual message translation using Gemini API
- [ ] 🟡 Implement auto-detection of source language
- [ ] 🟡 Translate emergency summaries for cross-language responder communication
- [ ] 🟢 Implement translation caching for repeated phrases
- [ ] 🟢 Support Bengali, Hindi, English at minimum

---

### Module 13 — Voice SOS

**Owner**: Aritra (pipeline) · Dishari (UI)

#### Pipeline (Aritra)

- [ ] 🟡 Implement speech-to-text transcription pipeline
- [ ] 🟡 Implement structured JSON extraction via LLM from transcribed text
- [ ] 🟡 Auto-create SOS event from extracted emergency data (no typing required)

#### Android UI (Dishari)

- [ ] 🟡 Build hold-to-record audio interface
- [ ] 🟡 Build audio waveform visualizer (`WaveformVisualizer.kt`)
- [ ] 🟡 Build confirmation sheet showing extracted emergency details before submission

---

### Module 14 — Emergency Timeline

**Owner**: Adil (Backend) · Dishari (UI)

#### Backend (Adil)

- [ ] 🟡 Implement event tracking model (SOS Created, Accepted, En Route, Arrived, Resolved)
- [ ] 🟡 Implement real-time timeline event broadcasting via WebSocket
- [ ] 🟡 Implement status update feed API (`GET /api/sos/{id}/timeline`)

#### Android UI (Dishari)

- [ ] 🟡 Build vertical milestone tracker component
- [ ] 🟡 Display real-time dispatch updates (Dispatched → En Route → Arrived)

---

### Module 15 — AI Incident Report

**Owner**: Aritra

- [ ] 🟡 Implement auto-generated post-incident report containing:
  - [ ] Incident type and severity
  - [ ] Location and map snapshot
  - [ ] Full timeline of events
  - [ ] Participating responders and their skills
  - [ ] Average response time
  - [ ] Treatment provided
  - [ ] Outcome (resolved, escalated, false alarm)
- [ ] 🟡 Implement PDF/markdown export of incident report
- [ ] 🟢 Implement email delivery of incident report to participants

---

### Module 16 — Reputation Engine

**Owner**: Adil

- [ ] 🟡 Design trust score algorithm:
  - [ ] Positive factors: quick arrival, actual help provided, positive feedback
  - [ ] Negative factors: false responding, spam, no-show, negative feedback
- [ ] 🟡 Implement trust score update on SOS resolution
- [ ] 🟡 Implement badge assignment logic ("Verified Medic", "Top Lifesaver", "Community Responder")
- [ ] 🟢 Implement trust score decay for inactive responders
- [ ] 🟢 Implement fraud detection flags for suspicious patterns

---

### Module 17 — Community Layer

**Owner**: Dishari (UI) · Plaban (data)

#### Android UI (Dishari)

- [ ] 🟢 Build map marker toggles for AEDs, blood banks, police/fire stations, hospitals, shelters
- [ ] 🟢 Build community resource detail cards (name, address, phone, hours)

#### Data (Plaban)

- [ ] 🟡 Compile Kolkata hospital dataset (name, location, contact, specialties)
- [ ] 🟡 Compile Kolkata police station dataset
- [ ] 🟡 Compile Kolkata fire station dataset
- [ ] 🟢 Compile Kolkata blood bank dataset
- [ ] 🟢 Compile AED location dataset (if available)
- [ ] 🟢 Export all datasets as clean JSON/CSV for DB import

---

### Redis Caching Layer (Adil)

- [ ] 🟡 Set up Redis instance in Docker Compose
- [ ] 🟡 Cache active responder locations for fast geo lookups
- [ ] 🟡 Cache active SOS session states
- [ ] 🟢 Implement cache invalidation on SOS resolution

---

### Android UI Enhancements — Phase 2 (Dishari)

- [ ] 🟡 Build Skill Upload Screen (certificate upload with verification status badge)
- [ ] 🟡 Build Settings Screen (language selector, dark mode toggle, notification preferences)
- [ ] 🟢 Implement dark mode support across all screens

---

### Research & Benchmarking — Phase 2 (Plaban)

- [ ] 🟡 Research 15 academic papers on AI emergency triage, spatial dispatching, community response
- [ ] 🟡 Compile literature survey summary table (Title, Authors, Year, Key Findings, Relevance)
- [ ] 🟡 Build competitor analysis matrix:
  - [ ] 112 India
  - [ ] GoodSAM
  - [ ] PulsePoint
  - [ ] Shakti App
  - [ ] Ola Emergency
- [ ] 🟡 Document comparison: features, limitations, NearHelp innovations

---

### Design Assets — Phase 2 (Sayantan)

- [ ] 🟡 Design custom map pin markers (Victim, Responder, Hospital, Police, Fire Station)
- [ ] 🟡 Design branded presentation slide deck template (NearHelp colors and logo)
- [ ] 🟡 Convert raw architecture text diagrams into clean visual infographics

---

### Documentation — Phase 2 (Abhisikta)

- [ ] 🟡 Draft SDD (Software Design Document):
  - [ ] Component architecture description
  - [ ] Data Flow Diagrams (DFD Level 0, 1, 2)
  - [ ] Database ER Diagram
  - [ ] API Catalogue (from Swagger export)
- [ ] 🟡 Create UML Sequence Diagrams:
  - [ ] SOS Trigger → AI Triage → Responder Acceptance flow
  - [ ] RAG Knowledge Retrieval workflow (Activity Diagram)
- [ ] 🟡 Create Test Case Suite & Execution Report:
  - [ ] Unit test cases (formatted tables)
  - [ ] Integration test cases
  - [ ] UI test cases
  - [ ] Execution results against active builds

---

## 📐 Phase 3 — Final Integration & Viva Preparation (Month 4)

> **Goal**: Admin tools, analytics, load testing, polish, and viva-ready presentation.  
> **Modules**: 18–24  
> ⚠️ **Phase 3 begins only after Phase 2 is stable.**

---

### Module 18 — Admin Dashboard

**Owner**: Adil (Backend APIs) · Aritra (integration)

- [ ] ⚪ Implement admin authentication and role-based access control
- [ ] ⚪ Build admin API endpoints:
  - [ ] Live active incidents list
  - [ ] User management (search, suspend, verify)
  - [ ] Verification queue management
  - [ ] System health status
- [ ] ⚪ Build admin dashboard frontend (React/Next.js):
  - [ ] Live map with active incidents
  - [ ] Response time analytics charts
  - [ ] Most active responders leaderboard
  - [ ] Emergency heatmaps
  - [ ] Fraud detection panel
  - [ ] Suspended users management

---

### Module 19 — AI Analytics

**Owner**: Aritra

- [ ] ⚪ Implement aggregate analysis engine:
  - [ ] Average response times by area and time of day
  - [ ] Common emergency types distribution
  - [ ] Most active volunteers ranking
  - [ ] Peak emergency hours analysis
- [ ] ⚪ Implement trend detection for emergency patterns
- [ ] ⚪ Build analytics visualization API (JSON for frontend charts)

---

### Module 20 — Disaster Mode

**Owner**: Aritra + Adil

- [ ] ⚪ Implement multi-responder coordination rooms for large-scale events
- [ ] ⚪ Support disaster types: Flood, Earthquake, Cyclone, Fire, Building Collapse
- [ ] ⚪ Implement mass notification for area-wide alerts
- [ ] ⚪ Implement disaster-specific responder allocation logic

---

### Module 21 — Guardian Mode

**Owner**: Dishari (UI) · Adil (Backend)

- [ ] ⚪ Build guardian contact list management screen
- [ ] ⚪ Implement protection toggle (children, women, senior citizens, disabled users)
- [ ] ⚪ Implement instant guardian notification on SOS trigger
- [ ] ⚪ Backend: guardian notification endpoint + FCM push

---

### Module 22 — Offline Mode

**Owner**: Aritra + Adil

- [ ] ⚪ Implement SMS-to-server fallback for SOS creation when offline
- [ ] ⚪ Cache common emergency first-aid guidance locally on device
- [ ] ⚪ Implement offline detection and status banner UI
- [ ] ⚪ Implement data sync when connectivity is restored

---

### Module 23 — Digital Twin Simulator

**Owner**: Aritra

- [ ] 🟡 Build simulation dashboard generating virtual users, vehicles, emergencies
- [ ] 🟡 Implement benchmarks:
  - [ ] AI-ranked dispatch vs. broadcast dispatch (time-to-first-responder)
  - [ ] Indexed vs. unindexed geo query latency comparison
  - [ ] AI latency breakdown (classification + severity + RAG retrieval + generation)
  - [ ] Throughput curves (concurrent SOS events vs. response time)
  - [ ] Skill-aware ranking vs. distance-only ranking comparison
- [ ] 🟡 Generate publishable benchmark charts (5 comparison charts for viva demo)
- [ ] 🟡 Load test with k6 or Locust (simulate concurrent SOS triggers)

---

### Module 24 — Developer Dashboard

**Owner**: Adil

- [ ] ⚪ Set up Swagger UI for all API endpoints
- [ ] ⚪ Set up Prometheus metrics collection
- [ ] ⚪ Set up Grafana dashboards (CPU, memory, request latency, error rates)
- [ ] ⚪ Implement structured logging across all services
- [ ] ⚪ Monitor WebSocket connection pool stats
- [ ] ⚪ Monitor Redis cache hit/miss rates

---

## 🎨 Design & Theming (Dishari + Sayantan)

### Android Design System

- [ ] 🔴 Define `Color.kt` with emergency color palette (Emergency Red `#E53935`, Action Amber `#FF9800`, Safe Green `#4CAF50`, AI Info Blue `#2196F3`, Dark BG `#121212`, Card Surface `#1E1E1E`)
- [ ] 🔴 Define `Type.kt` with Inter/Roboto typography scale
- [ ] 🔴 Define `Theme.kt` with NearHelpTheme wrapper (dark theme default)
- [ ] 🟡 Build reusable composable components:
  - [ ] `SOSButton.kt` — animated pulse SOS trigger
  - [ ] `AIDisclaimerCard.kt` — mandatory Good Samaritan disclaimer
  - [ ] `SeverityBadge.kt` — Level 1-5 severity pill
  - [ ] `ResponderETACard.kt` — ETA & bottom sheet component
  - [ ] `WaveformVisualizer.kt` — Voice SOS audio wave
- [ ] 🟢 Implement micro-animations and transitions
- [ ] 🟢 Ensure minimum 48dp touch targets (72dp+ for SOS button)

### Visual Identity (Sayantan)

- [ ] 🟡 Design NearHelp AI official logo
- [ ] 🟡 Design Android adaptive launcher icon
- [ ] 🟡 Design report header banners
- [ ] 🟡 Create emergency icon pack (SVG + PNG):
  - [ ] Cardiac
  - [ ] Fire
  - [ ] Accident
  - [ ] Flood
  - [ ] Security
  - [ ] Medical (general)
- [ ] 🟡 Design responder badge graphics ("Verified Medic", "Community Responder", "Top Lifesaver")

---

## 📚 Documentation (Abhisikta)

### Phase 1 — Requirements & Architecture (Months 1–2)

- [ ] 🟡 Draft SRS Document (Software Requirements Specification):
  - [ ] Functional requirements for all 24 modules
  - [ ] Non-functional requirements (performance, security, latency, spatial precision)
  - [ ] At least 10 core emergency use cases
- [ ] 🟡 Create UML Core Diagrams:
  - [ ] High-level System Use Case Diagram
  - [ ] System Class Diagram (based on PostgreSQL schema)

### Phase 3 — Final Report & Viva Package (Month 4)

- [ ] 🟡 Compile Final Project Report (SRS + SDD + Test Results + UI Screenshots)
- [ ] 🟡 Write Executive Abstract & Synopsis (2-page summary for examiners)
- [ ] 🟡 Create Presentation Slides (PowerPoint/LaTeX for project defense)
- [ ] 🟡 Write User Manual (app installation and usage guide)

---

## 🔬 Research & Data (Plaban)

### Phase 3 — Regional Data & Viva Prep (Month 4)

- [ ] 🟡 Cross-check AI triage output against original WHO source protocols
- [ ] 🟡 Prepare 5 realistic emergency prompt test scripts for live viva demo
- [ ] 🟡 Verify emergency protocol data accuracy in RAG knowledge base

---

## 🎬 Media & Presentation (Sayantan)

### Phase 3 — Video & Exhibition (Month 4)

- [ ] 🟡 Record and edit 3-minute narrated app demo video (live app + AI response)
- [ ] 🟡 Design project exhibition poster (A1/A0 format)
- [ ] 🟡 Assist Abhisikta with formatted screenshots and figures in final report

---

## 🧪 Testing & Quality Assurance

### Unit Tests

- [ ] 🟡 Backend: Auth middleware tests
- [ ] 🟡 Backend: SOS creation and status transition tests
- [ ] 🟡 Backend: Geospatial query correctness tests (PostGIS)
- [ ] 🟡 AI Service: Emergency classification accuracy tests
- [ ] 🟡 AI Service: Severity prediction range tests
- [ ] 🟡 AI Service: RAG retrieval precision/recall tests
- [ ] 🟢 Android: ViewModel unit tests for core screens

### Integration Tests

- [ ] 🟡 End-to-end SOS lifecycle (create → classify → rank → notify → respond → resolve)
- [ ] 🟡 WebSocket connection and message delivery
- [ ] 🟡 FCM notification delivery and receipt tracking
- [ ] 🟢 AI pipeline integration (classify → severity → RAG → guidance)

### Load & Performance Tests

- [ ] 🟡 PostGIS `ST_DWithin` query latency with spatial index (benchmark)
- [ ] 🟡 PostGIS `ST_DWithin` query latency without spatial index (comparison)
- [ ] 🟡 Concurrent SOS event simulation (k6/Locust)
- [ ] 🟡 WebSocket connection scaling test
- [ ] 🟡 AI service latency profiling

### Safety & Edge Case Tests

- [ ] 🟡 Anonymous mode: verify no PII leakage to responders
- [ ] 🟡 Idempotency: duplicate SOS trigger does not create duplicate events
- [ ] 🟡 Idempotency: duplicate "I'm responding" does not create duplicate responses
- [ ] 🟡 Escalation: verify Layer 1 → 2 → 3 timeout behavior
- [ ] 🟡 RAG guardrails: verify no dosage, diagnosis, or prescription output

---

## 🚀 Deployment & Release

- [ ] 🟡 Dockerize all services with production configurations
- [ ] 🟡 Set up Google Cloud Run deployment pipeline
- [ ] 🟢 Configure production environment variables and secrets management
- [ ] 🟢 Set up production PostgreSQL + PostGIS instance
- [ ] 🟢 Set up production Redis instance
- [ ] 🟢 Configure domain and SSL/TLS
- [ ] 🟢 Set up production monitoring (Prometheus + Grafana)
- [ ] 🟢 Write deployment runbook

---

## 🎯 Viva & Defense Preparation

- [ ] 🟡 Prepare 5 live comparison demo charts for Digital Twin Simulator:
  1. AI-ranked vs. broadcast dispatch comparison
  2. Indexed vs. unindexed geo query latency
  3. AI latency breakdown chart
  4. Throughput curves under load
  5. Skill-aware vs. distance-only ranking
- [ ] 🟡 Prepare answers for anticipated viva questions:
  - [ ] "Why not just use 112?"
  - [ ] "How do you handle medical liability?"
  - [ ] "What if there are no nearby responders?"
  - [ ] "How is this different from a WhatsApp group?"
  - [ ] "What about fake/prank SOS calls?"
- [ ] 🟡 Conduct full end-to-end demo dry run
- [ ] 🟡 Prepare system architecture walkthrough presentation
- [ ] 🟡 Ensure all 5 research questions (RQ1–RQ5) have measured, presentable answers

---

## 📊 Progress Summary

| Phase | Total Tasks | Completed | Status |
| :--- | :--- | :--- | :--- |
| Infrastructure | ~15 | 0 | ⬜ Not started |
| Phase 1 (MVP) | ~95 | 0 | ⬜ Not started |
| Phase 2 (Enhancement) | ~55 | 0 | ⬜ Not started |
| Phase 3 (Stretch) | ~35 | 0 | ⬜ Not started |
| Testing & QA | ~20 | 0 | ⬜ Not started |
| Deployment | ~8 | 0 | ⬜ Not started |
| Viva Prep | ~10 | 0 | ⬜ Not started |
| **Total** | **~238** | **0** | **⬜ Not started** |
