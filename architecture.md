# NearHelp AI — System Architecture

> **Version**: 1.0  
> **Last Updated**: 2026-08-12  
> **Author**: Aritra (Project Lead & AI Architect)  
> **Status**: Reference Architecture — Pre-Implementation

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [System Tiers & Deployment Topology](#2-system-tiers--deployment-topology)
3. [Client Layer — Android Application](#3-client-layer--android-application)
4. [Backend Layer — FastAPI Service](#4-backend-layer--fastapi-service)
5. [AI Service Layer](#5-ai-service-layer)
6. [Data Layer](#6-data-layer)
7. [Real-Time Communication Layer](#7-real-time-communication-layer)
8. [Notification Delivery System](#8-notification-delivery-system)
9. [SOS Lifecycle — Complete Data Flow](#9-sos-lifecycle--complete-data-flow)
10. [Responder Ranking Algorithm](#10-responder-ranking-algorithm)
11. [3-Layer Escalation Protocol](#11-3-layer-escalation-protocol)
12. [RAG Pipeline Architecture](#12-rag-pipeline-architecture)
13. [LangGraph Agent Architecture](#13-langgraph-agent-architecture)
14. [Security & Privacy Architecture](#14-security--privacy-architecture)
15. [Infrastructure & DevOps](#15-infrastructure--devops)
16. [Repository Structure](#16-repository-structure)
17. [Technology Stack Reference](#17-technology-stack-reference)
18. [Architecture Decision Records](#18-architecture-decision-records)
19. [Performance Targets & Constraints](#19-performance-targets--constraints)

---

## 1. Architecture Overview

NearHelp AI follows a **three-tier, microservice-oriented architecture** with strict subsystem ownership. The system is designed around one core principle: **the critical alert path must never be blocked by AI processing**.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│                          NEARHELP AI ARCHITECTURE                           │
│                                                                             │
│  ┌─────────────┐     ┌──────────────────┐     ┌────────────────────────┐   │
│  │             │     │                  │     │                        │   │
│  │   CLIENT    │────▶│    BACKEND       │────▶│     AI SERVICE         │   │
│  │   LAYER     │◀────│    LAYER         │◀────│     LAYER              │   │
│  │             │     │                  │     │                        │   │
│  │  Android    │     │  FastAPI         │     │  RAG + LangGraph       │   │
│  │  Kotlin     │     │  PostgreSQL      │     │  ChromaDB              │   │
│  │  Compose    │     │  Redis           │     │  Gemini 2.5            │   │
│  │             │     │  WebSocket       │     │                        │   │
│  └─────────────┘     └──────────────────┘     └────────────────────────┘   │
│                                                                             │
│  ───────────────────── SUPPORTING INFRASTRUCTURE ─────────────────────────  │
│                                                                             │
│  ┌───────────┐  ┌──────────────┐  ┌──────────┐  ┌──────────────────────┐  │
│  │  Firebase  │  │ Google Maps  │  │  Docker  │  │  GitHub Actions      │  │
│  │  Auth+FCM  │  │ SDK + APIs   │  │  + GCR   │  │  CI/CD               │  │
│  └───────────┘  └──────────────┘  └──────────┘  └──────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Design Principles

| Principle | Implementation |
| :--- | :--- |
| **Subsystem Ownership** | Backend owns PostgreSQL/PostGIS (coordination data). AI Service owns ChromaDB (retrieval data). Independently deployable. |
| **Reliability-First Critical Path** | SOS trigger uses HTTPS (not WebSocket). Alerts use FCM push (not WebSocket). Real-time features use WebSocket only *after* reliable delivery succeeds. |
| **AI as Parallel Enhancement** | AI processing runs in parallel with alert fan-out. Alerts are delivered immediately; AI guidance follows. The critical path never blocks on AI. |
| **Decoupled Data Stores** | Each subsystem owns its own database/store, preventing cross-service coupling and enabling independent scaling. |
| **Privacy by Default** | Location stored only during active events. Anonymous mode strips all PII. Medical data encrypted at rest. |

---

## 2. System Tiers & Deployment Topology

```
                    ┌─────────────────────────────────────────────┐
                    │              INTERNET / CDN                 │
                    └────────────────────┬────────────────────────┘
                                         │
                           ┌─────────────┼─────────────┐
                           │             │             │
                    ┌──────▼──────┐      │      ┌──────▼──────┐
                    │   Android   │      │      │    Admin     │
                    │   Client    │      │      │  Dashboard   │
                    │ (Kotlin +   │      │      │ (Next.js)    │
                    │  Compose)   │      │      │              │
                    └──────┬──────┘      │      └──────┬───────┘
                           │             │             │
                    HTTPS  │    FCM Push │      HTTPS  │
                    + WS   │             │             │
                           │             │             │
          ┌────────────────▼─────────────▼─────────────▼───────────────┐
          │                    GOOGLE CLOUD RUN                         │
          │                                                            │
          │  ┌────────────────────────────────────────────────────┐    │
          │  │              BACKEND SERVICE (FastAPI)              │    │
          │  │                                                    │    │
          │  │  ┌──────────┐ ┌──────────┐ ┌─────────┐ ┌───────┐ │    │
          │  │  │ Auth API │ │ SOS API  │ │ User API│ │ Admin │ │    │
          │  │  └──────────┘ └──────────┘ └─────────┘ │  API  │ │    │
          │  │  ┌──────────┐ ┌──────────┐ ┌─────────┐ └───────┘ │    │
          │  │  │WebSocket │ │Geo Query │ │Responder│            │    │
          │  │  │ Server   │ │ Engine   │ │ Ranker  │            │    │
          │  │  └──────────┘ └──────────┘ └─────────┘            │    │
          │  └────────────────────┬───────────────────────────────┘    │
          │                      │ HTTP (internal)                     │
          │  ┌───────────────────▼────────────────────────────────┐    │
          │  │              AI SERVICE (FastAPI)                   │    │
          │  │                                                    │    │
          │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐          │    │
          │  │  │Emergency │ │ Severity │ │  Crisis  │          │    │
          │  │  │Classifier│ │Predictor │ │  Agent   │          │    │
          │  │  └──────────┘ └──────────┘ │(LangGraph│          │    │
          │  │  ┌──────────┐ ┌──────────┐ └──────────┘          │    │
          │  │  │   RAG    │ │Translator│                       │    │
          │  │  │ Pipeline │ │          │                       │    │
          │  │  └──────────┘ └──────────┘                       │    │
          │  └────────────────────────────────────────────────────┘    │
          └────────────────────────────────────────────────────────────┘
                      │              │              │
            ┌─────────▼──────┐ ┌─────▼──────┐ ┌────▼──────┐
            │  PostgreSQL    │ │   Redis     │ │ ChromaDB  │
            │  + PostGIS     │ │   Cache     │ │ (Vector)  │
            │                │ │             │ │           │
            │ users          │ │ sessions    │ │ embeddings│
            │ sos_events     │ │ idempotency │ │ protocol  │
            │ responses      │ │ rate_limits │ │ chunks    │
            │ messages       │ │ geo_cache   │ │           │
            │ timeline_events│ │ pub/sub     │ │           │
            │ ai_summaries   │ │             │ │           │
            │ skill_verify   │ │             │ │           │
            └────────────────┘ └─────────────┘ └───────────┘
```

### Service Boundaries

| Service | Responsibility | Port | Protocol |
| :--- | :--- | :--- | :--- |
| Backend Service | Auth, SOS CRUD, geo queries, WebSocket, FCM, admin | 8000 | HTTPS + WSS |
| AI Service | Classification, severity, RAG, translation, agent | 8001 | HTTP (internal only) |
| PostgreSQL + PostGIS | Relational data + geospatial indexing | 5432 | TCP |
| Redis | Cache, sessions, idempotency, pub/sub | 6379 | TCP |
| ChromaDB | Vector embeddings for RAG retrieval | 8002 (or in-process) | HTTP / in-process |

---

## 3. Client Layer — Android Application

### Technology

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (declarative)
- **Maps**: Google Maps SDK for Android
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **Architecture Pattern**: MVVM + Clean Architecture

### Module Structure

```
android/app/src/main/java/com/nearhelp/
│
├── data/                          # Data Layer
│   ├── remote/                    # Retrofit API clients
│   │   ├── AuthApiService.kt
│   │   ├── SOSApiService.kt
│   │   ├── UserApiService.kt
│   │   └── AIApiService.kt
│   ├── websocket/                 # WebSocket client
│   │   └── SOSWebSocketClient.kt
│   ├── local/                     # Local storage
│   │   ├── EncryptedPrefs.kt      # Token + medical data
│   │   └── OfflineGuidanceCache.kt
│   └── repository/                # Repository implementations
│       ├── AuthRepository.kt
│       ├── SOSRepository.kt
│       └── UserRepository.kt
│
├── domain/                        # Domain Layer
│   ├── model/                     # Domain models
│   │   ├── User.kt
│   │   ├── SOSEvent.kt
│   │   ├── Responder.kt
│   │   └── AIGuidance.kt
│   └── usecase/                   # Business logic
│       ├── TriggerSOSUseCase.kt
│       ├── AcceptSOSUseCase.kt
│       └── GetGuidanceUseCase.kt
│
├── ui/                            # Presentation Layer
│   ├── theme/
│   │   ├── Color.kt               # Emergency color palette
│   │   ├── Type.kt                # Inter/Roboto typography
│   │   └── Theme.kt               # NearHelpTheme wrapper
│   ├── components/                # Reusable composables
│   │   ├── SOSButton.kt           # Animated pulse SOS trigger
│   │   ├── AIDisclaimerCard.kt    # Good Samaritan disclaimer
│   │   ├── SeverityBadge.kt       # Level 1–5 severity pill
│   │   ├── ResponderETACard.kt    # ETA bottom sheet
│   │   └── WaveformVisualizer.kt  # Voice SOS waveform
│   ├── screens/
│   │   ├── HomeScreen.kt          # SOS trigger screen
│   │   ├── ActiveSOSScreen.kt     # AI triage + guidance
│   │   ├── LiveMapScreen.kt       # Real-time map
│   │   ├── ChatScreen.kt          # In-app emergency chat
│   │   ├── ProfileScreen.kt       # Medical ID + skills
│   │   └── SettingsScreen.kt      # Preferences
│   └── navigation/
│       └── NavGraph.kt
│
├── di/                            # Dependency Injection (Hilt)
│   └── AppModule.kt
│
└── service/
    ├── FCMService.kt              # Push notification handler
    └── LocationService.kt         # Foreground location streaming
```

### Navigation Architecture

```
                        ┌──────────────────────────────┐
                        │       App Entry Point        │
                        └──────────────┬───────────────┘
                                       │
                          ┌────────────┴────────────┐
                          │   Auth Check             │
                          │   (Token in Prefs?)      │
                          └─────┬──────────────┬─────┘
                                │ No           │ Yes
                        ┌───────▼───────┐  ┌───▼──────────────────┐
                        │  Auth Flow    │  │  Main App Shell      │
                        │  ┌─────────┐  │  │  (Bottom Nav Bar)    │
                        │  │ Login   │  │  │                      │
                        │  │ Sign Up │  │  │  ┌──────────────┐    │
                        │  │ OTP     │  │  │  │ 🚨 SOS/Home │    │
                        │  └─────────┘  │  │  │ 🗺️ Live Map  │    │
                        └───────────────┘  │  │ 🤖 AI Assist │    │
                                           │  │ 💬 Chat      │    │
                               ┌───────────│  │ 👤 Profile   │    │
                               │           │  └──────────────┘    │
                               │           └──────────────────────┘
                               │
                     ┌─────────▼──────────┐
                     │ Anonymous SOS Mode │
                     │ (No auth required) │
                     └────────────────────┘
```

### App Modes

The app operates in two distinct modes, determined by the user's role in an active SOS event:

| Mode | Entry Condition | Key Screens |
| :--- | :--- | :--- |
| **Victim Mode** | User triggers an SOS | SOS Trigger → Active SOS (AI guidance) → Live Map → Chat |
| **Responder Mode** | User receives and accepts an SOS alert | Alert Modal → Live Map (navigation) → Chat → Timeline |

---

## 4. Backend Layer — FastAPI Service

### Architecture

```
backend/app/
│
├── main.py                        # FastAPI app initialization
│
├── api/                           # Route Handlers (Controllers)
│   ├── auth.py                    # POST /api/auth/*
│   ├── users.py                   # GET/PUT /api/users/*
│   ├── sos.py                     # POST/GET /api/sos/*
│   ├── admin.py                   # GET/PUT /api/admin/*
│   └── websocket.py               # WS /ws/sos/{event_id}
│
├── core/                          # Cross-Cutting Concerns
│   ├── config.py                  # Environment configuration
│   ├── security.py                # JWT validation, password hashing
│   ├── dependencies.py            # FastAPI dependency injection
│   └── middleware.py              # Idempotency, rate limiting, CORS
│
├── models/                        # SQLAlchemy + GeoAlchemy2 Models
│   ├── user.py                    # users table (with PostGIS POINT)
│   ├── sos_event.py               # sos_events table
│   ├── response.py                # responses table
│   ├── message.py                 # messages table
│   ├── timeline_event.py          # timeline_events table
│   ├── ai_summary.py              # ai_summaries table
│   └── skill_verification.py      # skill_verifications table
│
├── schemas/                       # Pydantic Request/Response Schemas
│   ├── auth.py
│   ├── user.py
│   ├── sos.py
│   └── ai.py
│
├── services/                      # Business Logic Layer
│   ├── auth_service.py            # Firebase Auth + JWT management
│   ├── user_service.py            # Profile CRUD, skill management
│   ├── sos_service.py             # SOS lifecycle management
│   ├── geo_service.py             # PostGIS queries (ST_DWithin)
│   ├── ranking_service.py         # Responder ranking algorithm
│   ├── notification_service.py    # FCM push + delivery tracking
│   ├── event_service.py           # Timeline event management
│   ├── reputation_service.py      # Trust score calculation
│   └── websocket_manager.py       # WebSocket connection management
│
├── db/                            # Database Layer
│   ├── session.py                 # Async SQLAlchemy session factory
│   └── migrations/                # Alembic migration scripts
│       └── versions/
│
└── tests/
    ├── test_auth.py
    ├── test_sos.py
    ├── test_geo.py
    └── conftest.py
```

### Request Flow

```
Client Request
     │
     ▼
┌────────────────┐
│   MIDDLEWARE    │
│                │
│ ┌────────────┐ │
│ │   CORS     │ │
│ ├────────────┤ │
│ │ Rate Limit │ │──── Redis (counter per user, 100 req/min)
│ ├────────────┤ │
│ │Idempotency │ │──── Redis (key lookup, 24h TTL)
│ ├────────────┤ │
│ │ JWT Auth   │ │──── Validate token, extract user_id
│ └────────────┘ │
└───────┬────────┘
        │
        ▼
┌────────────────┐
│  ROUTE HANDLER │──── Pydantic schema validation (auto)
│   (api/*.py)   │
└───────┬────────┘
        │
        ▼
┌────────────────┐
│   SERVICE      │──── Business logic, orchestration
│(services/*.py) │
└───────┬────────┘
        │
   ┌────┴────┐
   ▼         ▼
┌──────┐  ┌──────┐
│ DB   │  │Redis │
│      │  │Cache │
└──────┘  └──────┘
```

### API Surface

#### REST Endpoints

```
Authentication
──────────────
POST   /api/auth/register           Create account (email/password)
POST   /api/auth/login              Login → JWT token pair
POST   /api/auth/google             Google OAuth → JWT token pair
POST   /api/auth/phone/send-otp     Send phone verification OTP
POST   /api/auth/phone/verify       Verify OTP → JWT token pair
POST   /api/auth/refresh            Refresh expired access token
POST   /api/auth/anonymous          Create anonymous session → temp JWT

User Profile
────────────
GET    /api/users/me                Get authenticated user's profile
PUT    /api/users/me                Update profile fields
PATCH  /api/users/me/medical        Update encrypted medical data
POST   /api/users/me/skills         Claim a new skill (+ upload cert)
POST   /api/users/me/fcm-token      Register/update FCM device token
PUT    /api/users/me/location        Update location (active SOS only)

SOS Events
──────────
POST   /api/sos/create              Create SOS event (idempotency key required)
GET    /api/sos/{id}                Get SOS event details
PUT    /api/sos/{id}/resolve        Resolve SOS event
GET    /api/sos/active              Get user's active SOS events
POST   /api/sos/{id}/respond        Accept SOS (idempotency key required)
GET    /api/sos/{id}/timeline       Get event timeline
GET    /api/sos/{id}/report         Get AI-generated incident report

AI Service (proxied to AI microservice)
───────────
POST   /api/ai/classify             Classify emergency from text/voice/image
POST   /api/ai/severity             Predict severity score (0–100)
POST   /api/ai/guidance             Get RAG-grounded first-aid guidance
POST   /api/ai/translate            Translate text between languages
POST   /api/ai/summary              Generate emergency summary for dispatch

Admin
─────
GET    /api/admin/dashboard          Aggregate dashboard statistics
GET    /api/admin/verifications      Pending skill verification queue
PUT    /api/admin/verifications/{id} Approve/reject skill verification
GET    /api/admin/analytics          Analytics data (heatmaps, trends)
GET    /api/admin/users              User search and management
PUT    /api/admin/users/{id}/suspend Suspend a user account
```

#### WebSocket Events

```
Connection: WSS /ws/sos/{event_id}?token={jwt}

Client → Server
────────────────
location_update    { lat, lon, timestamp, accuracy }
send_message       { text, language }
action_log         { action_type, details }

Server → Client
────────────────
responder_update   { responder_id, lat, lon, eta, name, skill_badge }
new_message        { sender_id, sender_name, text, translated_text, timestamp }
timeline_event     { event_type, actor, details, timestamp }
ai_guidance        { guidance_text, source_refs[], severity_badge }
sos_resolved       { resolved_by, timestamp, feedback_prompt }
```

---

## 5. AI Service Layer

The AI service is a **separate, independently deployable FastAPI application** that the backend calls via internal HTTP. This isolation ensures AI latency does not block the critical alert path, and the AI service can be scaled and updated independently.

### Architecture

```
ai_service/app/
│
├── main.py                        # FastAPI app (port 8001)
│
├── api/
│   ├── classify.py                # POST /classify
│   ├── severity.py                # POST /severity
│   ├── guidance.py                # POST /guidance
│   ├── translate.py               # POST /translate
│   └── chat.py                    # POST /chat (agent interaction)
│
├── classifiers/
│   ├── emergency_classifier.py    # Embedding-similarity classification
│   ├── crisis_types.py            # Crisis type taxonomy + reference embeddings
│   └── severity_predictor.py      # LLM-based severity scoring
│
├── rag/
│   ├── chunker.py                 # Document → procedure-level chunks
│   ├── embedder.py                # Chunk → vector embedding
│   ├── retriever.py               # Query → top-k similar chunks
│   ├── generator.py               # Retrieved chunks + prompt → guidance
│   └── guardrails.py              # Post-generation safety filters
│
├── agents/
│   ├── crisis_agent.py            # LangGraph agent definition
│   ├── nodes/
│   │   ├── intent_router.py       # Route to appropriate sub-agent
│   │   ├── first_aid_node.py      # RAG-powered guidance node
│   │   ├── followup_node.py       # Clarifying question generator
│   │   ├── coordinator_node.py    # Multi-responder coordination
│   │   └── response_builder.py    # Final response assembly + translation
│   └── state.py                   # Agent state schema
│
├── knowledge_base/                # Raw protocol documents
│   ├── medical/
│   ├── disaster/
│   ├── fire/
│   └── trauma/
│
└── tests/
    ├── test_classifier.py
    ├── test_rag.py
    └── test_agent.py
```

### Emergency Classification Pipeline

```
Input (text / voice transcript / image description)
     │
     ▼
┌────────────────────────────────┐
│      EMBEDDING GENERATION      │
│                                │
│  Input text → embedding model  │
│  → 384-dim vector              │
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│   COSINE SIMILARITY MATCH     │
│                                │
│  Compare input embedding vs.  │
│  reference embeddings for:    │
│                                │
│  • medical (cardiac, choking, │
│    trauma, poisoning, burn,   │
│    seizure, allergic_reaction)│
│  • fire                       │
│  • gas_leak                   │
│  • accident (road, fall,      │
│    construction, drowning)    │
│  • natural_disaster (flood,   │
│    earthquake, cyclone)       │
│  • security_threat            │
│                                │
│  → Best match + confidence    │
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│     STRUCTURED OUTPUT          │
│                                │
│  {                             │
│    "emergency_type": "medical",│
│    "sub_type": "cardiac_arrest"│
│    "priority": "critical",    │
│    "confidence": 0.94,        │
│    "radius_km": 3,            │
│    "required_skills": [       │
│      "doctor", "cpr_certified"│
│    ],                         │
│    "immediate_action": "...", │
│    "call_services": true      │
│  }                            │
└────────────────────────────────┘
```

### Model Selection

| Component | Model | Justification |
| :--- | :--- | :--- |
| **LLM** | Gemini 2.5 | Multilingual (critical for India), vision capabilities, structured output, generous free tier |
| **Embeddings** | `all-MiniLM-L6-v2` or Gemini embeddings | Small (384-dim), fast, sufficient for ~500–2000 chunk corpus |
| **Speech-to-Text** | Google Speech API | Best-in-class for Indian languages and accents |
| **Agent Orchestration** | LangGraph | Graph-based state machine; multi-step reasoning with tool use |
| **Vector Store** | ChromaDB | In-process, zero infra overhead, appropriate for corpus size |

---

## 6. Data Layer

### Entity-Relationship Diagram

```
┌──────────────────┐          ┌───────────────────────┐        ┌──────────────────┐
│      users       │          │      sos_events       │        │    responses     │
├──────────────────┤          ├───────────────────────┤        ├──────────────────┤
│ id (PK, UUID)    │──────┐   │ id (PK, UUID)         │───┐    │ id (PK, UUID)    │
│ email            │      │   │ broadcaster_id (FK)───│───┘    │ sos_event_id(FK) │
│ name             │      │   │ crisis_type           │   │    │ responder_id(FK) │
│ phone            │      └──▶│ sub_type              │   │    │ status           │
│ blood_group      │          │ severity_score        │   └───▶│ joined_at        │
│ medical_cond 🔒  │          │ description           │        │ arrived_at       │
│ allergies 🔒     │          │ location (POINT) 📍   │        │ feedback_score   │
│ emergency_contact│          │ status (enum)         │        └──────────────────┘
│ languages []     │          │ is_anonymous          │
│ skills (JSONB)   │          │ created_at            │        ┌──────────────────┐
│ trust_score      │          │ resolved_at           │        │    messages      │
│ badges []        │          └───────────────────────┘        ├──────────────────┤
│ location (POINT) │                     │                     │ id (PK, UUID)    │
│ fcm_token 🔒     │                     │                     │ sos_event_id(FK) │
│ is_active        │                     │                     │ sender_id (FK)   │
│ created_at       │                     │                     │ text             │
│ updated_at       │                     │                     │ language         │
└──────────────────┘                     │                     │ timestamp        │
                                         │                     └──────────────────┘
┌──────────────────┐                     │
│   ai_summaries   │                     │                     ┌──────────────────┐
├──────────────────┤                     │                     │ timeline_events  │
│ id (PK, UUID)    │                     │                     ├──────────────────┤
│ sos_event_id(FK)─│─────────────────────┤                     │ id (PK, UUID)    │
│ guidance_text    │                     └────────────────────▶│ sos_event_id(FK) │
│ summary_text     │                                           │ event_type       │
│ retrieved_refs   │          ┌───────────────────────┐        │ actor_id (FK)    │
│ severity_score   │          │ skill_verifications   │        │ details (JSONB)  │
│ crisis_type      │          ├───────────────────────┤        │ timestamp        │
│ confidence       │          │ id (PK, UUID)         │        └──────────────────┘
│ created_at       │          │ user_id (FK → users)  │
└──────────────────┘          │ skill_type            │
                              │ certificate_url       │
  🔒 = Encrypted at rest     │ status (enum)         │
  📍 = PostGIS POINT         │ reviewed_by (FK)      │
                              │ submitted_at          │
                              │ reviewed_at           │
                              └───────────────────────┘
```

### Geospatial Indexing (PostGIS)

```sql
-- Spatial index on user locations (GiST R-tree)
CREATE INDEX idx_users_location ON users USING GIST (location);

-- Spatial index on SOS event locations
CREATE INDEX idx_sos_events_location ON sos_events USING GIST (location);

-- Core nearby-user query (used by Smart SOS Engine)
SELECT
    id, name, skills, trust_score,
    ST_Distance(
        location::geography,
        ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
    ) AS distance_meters
FROM users
WHERE ST_DWithin(
    location::geography,
    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
    :radius_meters
)
AND is_active = true
ORDER BY distance_meters;
```

### Data Store Ownership

| Store | Owner | Contents | Purpose |
| :--- | :--- | :--- | :--- |
| **PostgreSQL + PostGIS** | Backend Service | All relational data + geospatial | Source of truth for users, events, coordination |
| **Redis** | Backend Service | Sessions, idempotency keys, rate limits, geo cache, pub/sub | Ephemeral data, performance optimization |
| **ChromaDB** | AI Service | Embedded protocol chunks + metadata | RAG retrieval for emergency guidance |

### Privacy Constraints

| Data | Storage Rule | Access Rule |
| :--- | :--- | :--- |
| `users.location` | Updated only during active SOS; set to `NULL` otherwise | Never exposed to other users directly |
| `sos_events.location` | Stored during event; anonymized post-resolution for analytics | Visible to responders only if `is_anonymous = false` |
| `users.medical_conditions` | AES-256 encrypted at rest | Decrypted only for user or consented emergency sharing |
| `messages.text` | Stored for event duration + 30 days | Accessible only to event participants |
| `users.fcm_token` | Encrypted; rotated on each app launch | Internal use only (notification delivery) |

---

## 7. Real-Time Communication Layer

### WebSocket Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Victim     │     │  Responder 1 │     │  Responder 2 │
│   Client     │     │   Client     │     │   Client     │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │ WSS                │ WSS                │ WSS
       │                    │                    │
┌──────▼────────────────────▼────────────────────▼──────────┐
│                   FASTAPI WEBSOCKET SERVER                 │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐  │
│  │              WebSocket Connection Manager            │  │
│  │                                                     │  │
│  │  Connections indexed by: sos_event_id               │  │
│  │                                                     │  │
│  │  sos_event_123 → [victim_ws, resp1_ws, resp2_ws]    │  │
│  │  sos_event_456 → [victim_ws, resp3_ws]              │  │
│  └──────────────────────┬──────────────────────────────┘  │
│                         │                                  │
│                    ┌────▼────┐                              │
│                    │  Redis  │                              │
│                    │ Pub/Sub │ (for multi-instance fanout)  │
│                    └─────────┘                              │
└───────────────────────────────────────────────────────────┘
```

### Message Flow Example (Location Update)

```
Responder Android App
     │
     │  FusedLocationProvider (every 3 seconds)
     │
     ▼
ws:location_update { lat: 22.58, lon: 88.42, ts: 1722072015 }
     │
     ▼
WebSocket Server receives
     │
     ├──▶ Update responder location in Redis (geo cache)
     ├──▶ Calculate new ETA (Google Directions API, every 15s)
     │
     ▼
Broadcast to all connections in sos_event_id:
ws:responder_update { responder_id, lat, lon, eta: "2 min", distance: "400m" }
     │
     ▼
Victim's map marker updates in real-time
```

---

## 8. Notification Delivery System

### FCM Push Architecture

```
SOS Event Created
     │
     ▼
Backend: Geo query → ranked responder list
     │
     ▼
┌────────────────────────────────────────────────────┐
│              FCM NOTIFICATION PIPELINE             │
│                                                    │
│  For each ranked responder:                        │
│    │                                               │
│    ▼                                               │
│  Build FCM payload:                                │
│    {                                               │
│      "priority": "HIGH",                           │
│      "data": {                                     │
│        "sos_event_id": "...",                      │
│        "crisis_type": "medical",                   │
│        "severity": "critical",                     │
│        "distance": "600m",                         │
│        "required_skills": ["cpr_certified"],       │
│        "eta_estimate": "3 min"                     │
│      },                                            │
│      "android": {                                  │
│        "priority": "HIGH"   ← Wakes device,       │
│      }                        bypasses Doze        │
│    }                                               │
│    │                                               │
│    ▼                                               │
│  Send via Firebase Admin SDK                       │
│    │                                               │
│    ├── Success → Log delivery receipt              │
│    │                                               │
│    └── Failure →                                   │
│         ├── Retry (3×, exponential backoff)         │
│         └── If still failed → Flag for SMS fallback│
│                                                    │
└────────────────────────────────────────────────────┘
```

### Why FCM, Not WebSocket, for Alerts

| Factor | FCM Push | WebSocket |
| :--- | :--- | :--- |
| **Background delivery** | ✅ Wakes killed/backgrounded apps via Google Play Services | ❌ Android kills background WS connections aggressively |
| **Battery** | ✅ Batched by OS, minimal battery impact | ❌ Persistent connection drains battery |
| **Reliability** | ✅ Google infrastructure, delivery receipts | ❌ Connection drops on network switch |
| **Use case** | Alert delivery (one-shot, critical) | Live coordination (ongoing, interactive) |

**Rule**: FCM for the *alert*. WebSocket for *coordination after the user opens the app*.

---

## 9. SOS Lifecycle — Complete Data Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       COMPLETE SOS LIFECYCLE                            │
└─────────────────────────────────────────────────────────────────────────┘

[1] USER TRIGGERS SOS
    │
    │  HTTPS POST /api/sos/create
    │  Header: Idempotency-Key: <UUID>
    │  Body: { crisis_type, description, location, is_anonymous }
    │
    ▼
[2] BACKEND VALIDATES & CREATES EVENT
    │
    ├── Check idempotency key in Redis → prevent duplicates
    ├── Validate JWT (or create anonymous session)
    ├── Insert into sos_events table
    ├── Log timeline_event: "sos_created"
    │
    │  Two paths execute IN PARALLEL:
    │
    ├───────────────────────────────────────────────────────┐
    │                                                       │
    ▼                                                       ▼
[3] GEOSPATIAL QUERY + RANKING                     [4] AI SERVICE CALL
    │                                                       │
    │  PostGIS ST_DWithin(location, radius)                 │  POST /classify
    │  → Candidate responders                               │  POST /severity
    │                                                       │  POST /guidance
    ▼                                                       │
[3a] RESPONDER RANKING                                      ▼
    │                                              [5] AI PROCESSES
    │  score = w1·D + w2·S + w3·R                          │
    │  Sort by score, take top-N                            │  Classification
    │                                                       │  → Severity scoring
    ▼                                                       │  → RAG retrieval
[3b] FCM FAN-OUT                                            │  → Guidance generation
    │                                                       │  → Summary generation
    │  Push to each ranked responder                        │
    │  (HIGH priority, wakes device)                        ▼
    │                                              [6] AI RESULTS STORED
    │                                                       │
    │                                                       │  Insert ai_summaries
    │                                                       │  Log timeline_event:
    │                                                       │    "ai_classified"
    │                                                       │
    └───────────────────────────┬───────────────────────────┘
                                │
                                ▼
[7] RESPONDER RECEIVES ALERT (FCM push wakes phone)
    │
    │  Full-screen high-priority overlay
    │  Shows: crisis type, severity, distance, required skills
    │
    ▼
[8] RESPONDER TAPS "I'M RESPONDING"
    │
    │  POST /api/sos/{id}/respond (idempotent)
    │  Log timeline_event: "response_accepted"
    │
    ▼
[9] WEBSOCKET CHANNEL OPENED
    │
    ├── Responder location streaming (every 3s)
    ├── ETA calculation (every 15s)
    ├── In-app chat (real-time messages)
    ├── AI guidance delivered via WS
    ├── Timeline events broadcast to all participants
    │
    ▼
[10] RESPONDER ARRIVES
    │
    │  GPS proximity detection (< 50m from victim)
    │  Log timeline_event: "responder_arrived"
    │  AI provides on-site guidance via chat
    │
    ▼
[11] EMERGENCY RESOLVED
    │
    │  PUT /api/sos/{id}/resolve
    │  Log timeline_event: "sos_resolved"
    │
    ├── Prompt feedback from both victim and responder
    ├── Update trust scores (reputation engine)
    ├── AI generates incident report (Module 15)
    ├── Close WebSocket connections
    │
    │  If is_anonymous:
    ├── Destroy temp session
    ├── Remove location from event record
    └── Auto-delete chat messages after 24h
```

---

## 10. Responder Ranking Algorithm

### Scoring Function

```
Score(responder, emergency) = w1 · D(responder) + w2 · S(responder) + w3 · R(responder)
```

### Components

```
┌─────────────────────────────────────────────────────────────────────┐
│                     SCORING COMPONENTS                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  D — Distance Score (weight: 0.40)                                  │
│  ─────────────────────────────────                                  │
│  D = 1 - (distance / max_radius)                                    │
│  Range: [0, 1]  ·  1 = at victim location  ·  0 = at radius edge   │
│                                                                     │
│  S — Skill Match Score (weight: 0.35)                               │
│  ────────────────────────────────────                                │
│  S = |responder_skills ∩ required_skills| / |required_skills|       │
│  Bonus: +0.2 if any matching skill is verified                      │
│  Range: [0, 1.2]                                                    │
│                                                                     │
│  R — Reliability Score (weight: 0.25)                               │
│  ────────────────────────────────────                                │
│  R = trust_score / 100                                              │
│  Range: [0, 1]                                                      │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  DEFAULT WEIGHTS                                                    │
│  ───────────────                                                    │
│  w1 (Distance)    = 0.40  ← Proximity is king: minutes matter       │
│  w2 (Skill Match) = 0.35  ← A skilled responder further away can    │
│                              be more valuable than an unskilled one  │
│  w3 (Reliability) = 0.25  ← Consistent responders are preferred     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Validation Example

```
Scenario: Cardiac arrest at Location A. max_radius = 3000m.

Responder 1: 200m away, no medical skills, trust = 60
Responder 2: 800m away, verified nurse + CPR, trust = 85

──────────────────────────────────────────────────────────

Responder 1:
  D = 0.40 × (1 - 200/3000)  = 0.40 × 0.933 = 0.373
  S = 0.35 × 0               = 0.000
  R = 0.25 × 0.60            = 0.150
  TOTAL                      = 0.523

Responder 2:
  D = 0.40 × (1 - 800/3000)  = 0.40 × 0.733 = 0.293
  S = 0.35 × 1.2             = 0.420  (full match + verified bonus)
  R = 0.25 × 0.85            = 0.213
  TOTAL                      = 0.926

Result: Verified nurse at 800m (0.926) ranks above unskilled
        person at 200m (0.523). ✅ Correct behavior.
```

---

## 11. 3-Layer Escalation Protocol

The Smart SOS Engine guarantees that **a victim is never left without assistance**, even if no volunteers respond and there is no network connectivity.

```
Time ──────────────────────────────────────────────────────────────────►

0s            30s             45s             60s            ongoing
│             │               │               │               │
▼             ▼               ▼               ▼               ▼
┌───────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ LAYER 1a  │ │  LAYER 1b   │ │  LAYER 1c   │ │  LAYER 2    │ │  LAYER 3    │
│           │ │             │ │             │ │             │ │             │
│ Initial   │ │ 2× radius   │ │ 3× radius   │ │ Auto-dial   │ │ AI Self-    │
│ radius    │ │ expansion   │ │ expansion   │ │ 108 / 112   │ │ Care Guide  │
│ push to   │ │ re-rank,    │ │ all nearby  │ │ with AI-    │ │ (cached     │
│ top-N     │ │ notify new  │ │ notified    │ │ generated   │ │  offline    │
│           │ │             │ │             │ │ summary     │ │  RAG)       │
└───────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
```

### Severity-Based Radius Configuration

| Severity Score | Initial Radius | Notification Strategy | Auto-Call 108/112 |
| :--- | :--- | :--- | :--- |
| 80–100 (Critical) | 3–5 km | Immediate push to all skill-matched | Yes |
| 50–79 (High) | 2–3 km | Push to top-ranked responders | Suggested |
| 20–49 (Medium) | 1–2 km | Standard notification | No |
| 0–19 (Low) | 0.5–1 km | Low-priority notification | No |

---

## 12. RAG Pipeline Architecture

### End-to-End Pipeline

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          RAG PIPELINE                                    │
└─────────────────────────────────────────────────────────────────────────┘

  OFFLINE (Ingestion)                    ONLINE (Query-Time)
  ──────────────────                     ───────────────────

  ┌────────────────┐                     ┌────────────────┐
  │  Raw Documents │                     │  User Query    │
  │  (PDF, HTML)   │                     │  + crisis_type │
  │                │                     │  + severity    │
  │  WHO, Red Cross│                     └───────┬────────┘
  │  NDMA, AHA     │                             │
  └───────┬────────┘                             ▼
          │                              ┌────────────────┐
          ▼                              │    EMBEDDING   │
  ┌────────────────┐                     │    GENERATION  │
  │    PARSING     │                     │                │
  │                │                     │  Query → 384d  │
  │  PDF → text    │                     │  vector        │
  │  HTML → text   │                     └───────┬────────┘
  └───────┬────────┘                             │
          │                                      ▼
          ▼                              ┌────────────────┐
  ┌────────────────┐                     │   RETRIEVAL    │
  │   CHUNKING     │                     │                │
  │                │                     │  Cosine sim    │
  │  Strategy:     │                     │  Top-K = 5     │
  │  procedure-    │                     │  + crisis_type │
  │  level splits  │                     │  metadata      │
  │                │                     │  filter        │
  │  Target:       │                     └───────┬────────┘
  │  200-400 tok   │                             │
  │  Overlap: 50   │                             ▼
  └───────┬────────┘                     ┌────────────────┐
          │                              │  CONFIDENCE    │
          ▼                              │  CHECK         │
  ┌────────────────┐                     │                │
  │   EMBEDDING    │                     │  Score < 0.6?  │
  │   GENERATION   │                     │  → Fallback:   │
  │                │                     │  "Wait for     │
  │  all-MiniLM-   │                     │   professional │
  │  L6-v2         │                     │   help"        │
  │  → 384-dim     │                     └───────┬────────┘
  └───────┬────────┘                             │ Score ≥ 0.6
          │                                      ▼
          ▼                              ┌────────────────┐
  ┌────────────────┐                     │   LLM          │
  │  VECTOR STORE  │                     │   GENERATION   │
  │  (ChromaDB)    │                     │                │
  │                │                     │  Gemini 2.5    │
  │  Index chunks  │                     │  + retrieved   │
  │  with metadata:│                     │    passages    │
  │  - source      │                     │  + structured  │
  │  - procedure   │                     │    prompt      │
  │  - crisis_type │                     └───────┬────────┘
  │  - step_number │                             │
  └────────────────┘                             ▼
                                         ┌────────────────┐
                                         │  GUARDRAILS    │
                                         │                │
                                         │  ✓ Citations   │
                                         │  ✗ No dosage   │
                                         │  ✗ No diagnosis│
                                         │  ✗ No Rx       │
                                         │  ✓ Scope check │
                                         └───────┬────────┘
                                                 │
                                                 ▼
                                         ┌────────────────┐
                                         │  RESPONSE      │
                                         │                │
                                         │  Step-by-step  │
                                         │  guidance with │
                                         │  source refs   │
                                         └────────────────┘
```

### Chunking Strategy

Emergency protocols are inherently step-based ("Step 1: Check responsiveness", "Step 2: Begin CPR"). Chunking at the **procedure-step level** preserves natural granularity and ensures retrievals return specific, actionable instructions.

| Parameter | Value | Rationale |
| :--- | :--- | :--- |
| Chunk size | 200–400 tokens | One complete procedure step per chunk |
| Overlap | 50 tokens | Context continuity between adjacent steps |
| Split strategy | By procedure step, not word count | Preserves instruction integrity |
| Metadata per chunk | source, procedure_name, crisis_type, step_number | Enables filtered retrieval |

### Hallucination Prevention (5 Layers)

| Layer | Mechanism |
| :--- | :--- |
| **Source Grounding** | Prompt explicitly instructs: cite retrieved passages, refuse unsupported advice |
| **Confidence Threshold** | Top retrieved passage similarity < 0.6 → fallback to "wait for professional help" |
| **Post-Generation Check** | Response checked for passage ID references; uncited instructions stripped |
| **Structured Output** | Gemini JSON mode enforces response format, preventing free-form generation |
| **Scope Guardrail** | System prompt prohibits dosage, diagnosis, surgical advice, prescriptions |

---

## 13. LangGraph Agent Architecture

### Agent State Machine

```
                        ┌─────────────────────┐
                        │     User Input      │
                        │   (text / voice)    │
                        └──────────┬──────────┘
                                   │
                          ┌────────▼────────┐
                          │  INTENT ROUTER  │
                          │  (entry node)   │
                          │                 │
                          │  Classifies:    │
                          │  • first_aid    │
                          │  • follow_up    │
                          │  • coordinate   │
                          │  • translate    │
                          │  • summarize    │
                          └───┬────┬────┬───┘
                              │    │    │
               ┌──────────────┘    │    └──────────────┐
               │                   │                   │
      ┌────────▼────────┐ ┌───────▼───────┐ ┌─────────▼───────┐
      │  FIRST AID      │ │  FOLLOW-UP    │ │   COORDINATOR   │
      │  GUIDANCE NODE  │ │  QUESTION     │ │   NODE          │
      │                 │ │  NODE         │ │                 │
      │  RAG retrieval  │ │  Generate     │ │  Suggest task   │
      │  → Gemini gen   │ │  clarifying   │ │  distribution   │
      │  → Cited steps  │ │  questions    │ │  among multiple │
      │                 │ │  ("Is the     │ │  responders     │
      │                 │ │   person      │ │                 │
      │                 │ │   breathing?")│ │                 │
      └────────┬────────┘ └───────┬───────┘ └─────────┬───────┘
               │                  │                    │
               └──────────────────┼────────────────────┘
                                  │
                         ┌────────▼────────┐
                         │ RESPONSE        │
                         │ BUILDER         │
                         │                 │
                         │ + Translation   │
                         │   (if needed)   │
                         │ + Disclaimer    │
                         │   injection     │
                         └────────┬────────┘
                                  │
                         ┌────────▼────────┐
                         │  DELIVER TO     │
                         │  USER /         │
                         │  RESPONDER      │
                         └─────────────────┘
```

### Agent Capabilities

| Capability | Description | Implementation |
| :--- | :--- | :--- |
| **Understand** | Parse emergency, classify type/severity | Classifier + severity predictor |
| **Ask** | Generate clarifying follow-ups | LLM node ("Is the person breathing?") |
| **Guide** | Step-by-step first aid from verified sources | RAG pipeline node |
| **Summarize** | Structured emergency summary for 108/112 | LLM with structured output |
| **Translate** | Real-time language translation | Gemini multilingual |
| **Coordinate** | Distribute tasks among responders | Coordinator node |
| **Locate** | Recommend nearest appropriate hospital | Google Places + emergency type mapping |

---

## 14. Security & Privacy Architecture

### Authentication Flow

```
┌───────────────┐     ┌─────────────────┐     ┌──────────────┐
│   Android     │     │   Backend       │     │   Firebase   │
│   Client      │     │   (FastAPI)     │     │   Auth       │
└───────┬───────┘     └────────┬────────┘     └──────┬───────┘
        │                      │                      │
        │  1. Login request    │                      │
        │─────────────────────▶│                      │
        │                      │  2. Verify with      │
        │                      │     Firebase         │
        │                      │─────────────────────▶│
        │                      │                      │
        │                      │  3. Firebase token   │
        │                      │◀─────────────────────│
        │                      │                      │
        │                      │  4. Generate JWT     │
        │                      │     (RS256 signed)   │
        │                      │     access: 15 min   │
        │                      │     refresh: 7 days  │
        │                      │                      │
        │  5. JWT token pair   │                      │
        │◀─────────────────────│                      │
        │                      │                      │
        │  6. API calls with   │                      │
        │     Bearer token     │                      │
        │─────────────────────▶│                      │
        │                      │  7. Validate JWT     │
        │                      │     (middleware)      │
```

### Security Measures

| Layer | Mechanism | Implementation |
| :--- | :--- | :--- |
| **Transport** | TLS 1.3 | All API + WebSocket connections encrypted |
| **Authentication** | JWT (RS256) | 15-min access + 7-day refresh tokens |
| **Password** | bcrypt (cost 12) | Never stored in plaintext |
| **Medical Data** | AES-256 at rest | Decrypted only for owner or consented emergency |
| **Rate Limiting** | Redis counters | 100 req/min per user, 10 SOS/day per user |
| **Input Validation** | Pydantic strict mode | All inputs validated before processing |
| **RBAC** | Role-based access | User, Verified Responder, Admin |
| **Idempotency** | Redis-cached keys | 24h TTL, prevents duplicate SOS/responses |

### Anonymous Mode Privacy Guarantees

When `is_anonymous = true`:

```
1. No user ID associated with SOS event (temporary session ID only)
2. Location used for geo query but NEVER sent to responders
   → Responders see only: crisis type, severity, AI guidance
3. After resolution: temporary session destroyed, location removed
4. Chat messages stored with temp session ID → auto-deleted after 24h
```

---

## 15. Infrastructure & DevOps

### Docker Compose (Local Development)

```yaml
# docker-compose.yml (conceptual)
services:
  backend:
    build: ./backend
    ports: ["8000:8000"]
    depends_on: [postgres, redis]
    environment:
      - DATABASE_URL=postgresql+asyncpg://...
      - REDIS_URL=redis://redis:6379
      - AI_SERVICE_URL=http://ai-service:8001

  ai-service:
    build: ./ai_service
    ports: ["8001:8001"]
    environment:
      - GEMINI_API_KEY=${GEMINI_API_KEY}
      - CHROMA_PERSIST_DIR=/data/chroma

  postgres:
    image: postgis/postgis:16-3.4
    ports: ["5432:5432"]
    volumes: ["pgdata:/var/lib/postgresql/data"]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  prometheus:
    image: prom/prometheus
    ports: ["9090:9090"]

  grafana:
    image: grafana/grafana
    ports: ["3000:3000"]
```

### CI/CD Pipeline (GitHub Actions)

```
Push to feature branch
        │
        ▼
┌────────────────────────────────────────────┐
│              GITHUB ACTIONS CI              │
│                                            │
│  ┌──────────────────────────────────────┐  │
│  │  Stage 1: Lint & Type Check          │  │
│  │  ├── ruff (Python linting)           │  │
│  │  ├── mypy (type checking)            │  │
│  │  └── ktlint (Kotlin linting)         │  │
│  └──────────────────────────────────────┘  │
│                    │                        │
│                    ▼                        │
│  ┌──────────────────────────────────────┐  │
│  │  Stage 2: Unit Tests                 │  │
│  │  ├── pytest (backend)                │  │
│  │  ├── pytest (AI service)             │  │
│  │  └── JUnit (Android — future)        │  │
│  └──────────────────────────────────────┘  │
│                    │                        │
│                    ▼                        │
│  ┌──────────────────────────────────────┐  │
│  │  Stage 3: Integration Tests          │  │
│  │  ├── Docker Compose test env         │  │
│  │  ├── API endpoint tests (httpx)      │  │
│  │  └── PostGIS query tests             │  │
│  └──────────────────────────────────────┘  │
│                    │                        │
│                    ▼                        │
│  ┌──────────────────────────────────────┐  │
│  │  Stage 4: Docker Build               │  │
│  │  ├── Build backend image             │  │
│  │  └── Build AI service image          │  │
│  └──────────────────────────────────────┘  │
│                                            │
└─────────────────┬──────────────────────────┘
                  │ Merge to main
                  ▼
┌────────────────────────────────────────────┐
│              DEPLOY TO CLOUD RUN            │
│                                            │
│  Push images → Google Container Registry   │
│  Deploy → Cloud Run (auto-scaling)         │
└────────────────────────────────────────────┘
```

### Monitoring Stack

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Backend    │     │  AI Service  │     │   Redis      │
│   /metrics   │     │   /metrics   │     │   (stats)    │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       └────────────────────┼────────────────────┘
                            │
                    ┌───────▼───────┐
                    │  Prometheus   │
                    │  (scrapes     │
                    │   /metrics)   │
                    └───────┬───────┘
                            │
                    ┌───────▼───────┐
                    │   Grafana     │
                    │               │
                    │  Dashboards:  │
                    │  • Request    │
                    │    latency    │
                    │  • Error rate │
                    │  • WS conns   │
                    │  • CPU/Memory │
                    │  • DB queries │
                    │  • AI latency │
                    └───────────────┘

                    ┌───────────────┐
                    │    Sentry     │
                    │               │
                    │  Real-time    │
                    │  error        │
                    │  tracking     │
                    └───────────────┘
```

---

## 16. Repository Structure

```
NearHelp/
├── android/                        # Android Application
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/nearhelp/
│   │   │   │   ├── data/           # Repositories, API clients, local storage
│   │   │   │   ├── domain/         # Use cases, domain models
│   │   │   │   ├── ui/             # Composable screens, components, theme
│   │   │   │   ├── di/             # Hilt dependency injection
│   │   │   │   └── service/        # FCM handler, location service
│   │   │   └── res/                # Resources (strings, drawables)
│   │   └── build.gradle.kts
│   └── build.gradle.kts
│
├── backend/                        # FastAPI Backend Service
│   ├── app/
│   │   ├── api/                    # Route handlers (auth, sos, users, admin, ws)
│   │   ├── core/                   # Config, security, middleware
│   │   ├── models/                 # SQLAlchemy + GeoAlchemy2 models
│   │   ├── schemas/                # Pydantic request/response schemas
│   │   ├── services/               # Business logic layer
│   │   ├── db/                     # Session factory, Alembic migrations
│   │   └── main.py
│   ├── tests/
│   ├── requirements.txt
│   ├── Dockerfile
│   └── alembic.ini
│
├── ai_service/                     # AI Microservice
│   ├── app/
│   │   ├── api/                    # AI endpoint handlers
│   │   ├── agents/                 # LangGraph agent + node definitions
│   │   ├── rag/                    # Chunker, embedder, retriever, generator
│   │   ├── classifiers/            # Emergency classification + severity
│   │   ├── knowledge_base/         # Raw protocol documents (organized by type)
│   │   └── main.py
│   ├── tests/
│   ├── requirements.txt
│   └── Dockerfile
│
├── admin_dashboard/                # Admin Web Panel (Phase 3)
│   ├── src/
│   └── package.json
│
├── simulator/                      # Digital Twin Simulator (Phase 3)
│   ├── scenarios/                  # Configurable simulation parameters
│   ├── locustfile.py               # Load test definitions
│   └── analysis.py                 # Benchmark chart generation
│
├── docs/                           # Documentation
│   ├── SRS.md
│   ├── SDD.md
│   ├── API.md
│   └── diagrams/
│
├── data/                           # Datasets (Plaban)
│   ├── protocols/                  # Emergency protocol PDFs
│   │   ├── medical/
│   │   ├── disaster/
│   │   ├── fire/
│   │   └── trauma/
│   └── regional/                   # Kolkata facility datasets
│
├── assets/                         # Design Assets (Sayantan)
│   ├── icons/
│   ├── map_pins/
│   └── branding/
│
├── docker-compose.yml              # Local development environment
├── .github/workflows/              # CI/CD pipeline definitions
├── .env.example                    # Environment variable template
├── .gitignore
├── README.md
├── architecture.md                 # This document
├── todos.md                        # Master task list
└── proposal.md                     # Full project proposal
```

---

## 17. Technology Stack Reference

| Layer | Technology | Version | Role |
| :--- | :--- | :--- | :--- |
| **Mobile** | Kotlin + Jetpack Compose | Latest stable | Android UI (declarative) |
| **Mobile Maps** | Google Maps SDK | Latest | Maps, markers, directions, traffic |
| **Mobile Push** | Firebase Cloud Messaging | Latest | Reliable push to backgrounded apps |
| **Backend** | FastAPI (Python) | 0.100+ | Async REST API + WebSocket server |
| **ORM** | SQLAlchemy 2.0 + GeoAlchemy2 | Latest | Async DB access + PostGIS integration |
| **Database** | PostgreSQL 16 + PostGIS 3.4 | Latest | Relational data + geospatial queries |
| **Cache** | Redis 7 | Latest | Sessions, idempotency, rate limits, pub/sub |
| **LLM** | Google Gemini 2.5 | Latest | Generation, classification, translation, vision |
| **Agent** | LangGraph | Latest | Graph-based agent state machine |
| **Embeddings** | all-MiniLM-L6-v2 / Gemini | Latest | 384-dim vectors for RAG retrieval |
| **Vector Store** | ChromaDB | Latest | In-process vector storage & retrieval |
| **Auth** | Firebase Auth + python-jose | Latest | OAuth, OTP, JWT management |
| **Containers** | Docker + Docker Compose | Latest | Reproducible dev & deployment |
| **Cloud** | Google Cloud Run | N/A | Serverless container hosting |
| **CI/CD** | GitHub Actions | N/A | Automated lint, test, build, deploy |
| **Monitoring** | Prometheus + Grafana | Latest | Metrics collection + dashboards |
| **Errors** | Sentry | Latest | Real-time error tracking |
| **API Docs** | Swagger/OpenAPI 3.1 | Auto-gen | FastAPI auto-generates docs |
| **Admin** | Next.js / React | Latest | Admin dashboard web app (Phase 3) |
| **Load Test** | Locust | Latest | Python-based load testing framework |

---

## 18. Architecture Decision Records

### ADR-1: FastAPI over Django / Flask

**Decision**: FastAPI  
**Rationale**: Async-first (critical for WebSocket + concurrent geo queries), native Pydantic validation, auto-generated OpenAPI docs. Django ORM doesn't natively support async geo queries. Flask lacks built-in WebSocket support.

### ADR-2: PostgreSQL + PostGIS over MongoDB

**Decision**: PostgreSQL + PostGIS  
**Rationale**: ACID transactions (critical for SOS event creation), mature GiST/SP-GiST geospatial indexing, ability to add pgvector if needed. MongoDB's eventual consistency is a risk in the safety-critical alert path.

### ADR-3: FCM Push over WebSocket for Alert Delivery

**Decision**: FCM for alerts, WebSocket for live coordination  
**Rationale**: Android aggressively kills background WebSocket connections. FCM uses Google Play Services to deliver to killed/backgrounded apps reliably. WebSocket is used only after the user foregrounds the app.

### ADR-4: ChromaDB over Pinecone / Weaviate / pgvector

**Decision**: ChromaDB  
**Rationale**: Corpus is small (~500–2000 chunks). ChromaDB runs in-process with zero infra overhead. Enterprise vector DBs add operational complexity without proportional benefit at this scale.

### ADR-5: RAG over Fine-Tuning for Medical Guidance

**Decision**: RAG  
**Rationale**: Easy corpus updates (new WHO guidelines = re-embed only), citation support, no training data required, grounded in published sources. Fine-tuning risks catastrophic forgetting and requires expensive retraining for corpus updates.

### ADR-6: Separate AI Service over Monolith

**Decision**: AI service as independent microservice  
**Rationale**: AI latency must not block the critical alert path. Independent deployment allows AI updates without backend redeployment. Vector store maintenance is isolated.

### ADR-7: Gemini 2.5 over OpenAI / Anthropic

**Decision**: Gemini 2.5  
**Rationale**: Strong multilingual support (critical for India — Hindi, Bengali, English), vision capabilities for photo/video intake, structured JSON output, generous free tier for a student project.

---

## 19. Performance Targets & Constraints

### Latency Targets

| Operation | Target | Measurement |
| :--- | :--- | :--- |
| SOS creation (end-to-end) | < 2 seconds | Request → event stored → geo query complete |
| FCM notification delivery | < 5 seconds | Event creation → push received on device |
| Geospatial query (with index) | < 50 ms | PostGIS `ST_DWithin` at 10K users |
| AI classification | < 1 second | Text input → emergency type output |
| AI severity prediction | < 1.5 seconds | Description → severity score |
| RAG retrieval + generation | < 3 seconds | Query → cited guidance response |
| WebSocket message delivery | < 200 ms | Sender → recipient (same event) |
| ETA calculation | < 1 second | Google Directions API call |

### Throughput Targets

| Scenario | Target |
| :--- | :--- |
| Concurrent SOS events | 100 simultaneous without degradation |
| Concurrent WebSocket connections | 500 per instance |
| Concurrent RAG queries | 50 per second |
| Database connections (pool) | 20 per service instance |

### Scalability Approach

```
Low Load                              High Load
(1 instance each)                     (auto-scaled)

┌──────────┐                          ┌──────────┐ ┌──────────┐
│ Backend  │            ──────▶       │ Backend  │ │ Backend  │
│ (1)      │                          │ (1)      │ │ (2)      │
└──────────┘                          └──────────┘ └──────────┘
┌──────────┐                          ┌──────────┐ ┌──────────┐
│ AI Svc   │            ──────▶       │ AI Svc   │ │ AI Svc   │
│ (1)      │                          │ (1)      │ │ (2)      │
└──────────┘                          └──────────┘ └──────────┘

                                      Redis Pub/Sub ensures WS
                                      fan-out works across instances
```

Google Cloud Run provides auto-scaling based on request volume. Redis Pub/Sub ensures WebSocket messages fan out correctly across multiple backend instances.

---

*This document is a living specification and will be updated as implementation progresses.*
