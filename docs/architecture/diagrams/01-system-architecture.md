# 1. Overall System Architecture

This diagram depicts the actual services, clients, data stores, and external integrations present in the repository.

```mermaid
flowchart TB
    subgraph Clients["Client Layer"]
        Android["Android App (Kotlin + Jetpack Compose)"]
        AdminWeb["Admin Web Panel (/admin, Vanilla JS/HTML/CSS)"]
    end

    subgraph BackendTier["Core Backend Service (:8000, FastAPI)"]
        Middleware["Middleware (CORS, Auth RateLimit, Idempotency)"]
        RestAPI["REST Routers (/api/v1/* & /api/*)"]
        WsTracking["Tracking WebSocket (/ws/tracking/{id}, /ws/sos/{id})"]
        WsAI["AI Proxy WebSocket (/ws/ai/chat)"]
        BackendServices["Domain Services (Auth, SOS, Geo, Ranking, Tracking, Routing, Skills)"]
        StaticUploads["Static Mount (/uploads/avatars, /uploads/certificates)"]
    end

    subgraph DataTier["Persistence & Cache"]
        Postgres[("PostgreSQL 16 + PostGIS 3.4 (or SQLite in unit tests)")]
        Redis[("Redis 7 (Idempotency, Rate Limit, Pub/Sub publish)")]
        LocalDisk[("Local Filesystem (backend/uploads/)")]
    end

    subgraph AITier["AI Microservice (:8001, FastAPI)"]
        AIRoutes["AI REST & WS Routers (/api/v1/classify, /severity, /agent/*, /ws/ai/chat)"]
        Classifiers["Emergency Classifier, Severity Predictor, Speech & Vision"]
        LangGraph["LangGraph Emergency Agent + Guardrails + Curated Protocols"]
    end

    subgraph External["External Services"]
        Gemini["Google Gemini API"]
        Firebase["Firebase Auth & FCM Push"]
        GMaps["Google Maps SDK & Places API (New)"]
        GRoutes["Google Routes API v2 (Unconfigured key in Settings)"]
    end

    Android -->|"HTTP JSON & WS"| Middleware
    AdminWeb -->|"Same-Origin HTTP JSON"| Middleware
    Middleware --> RestAPI
    Middleware --> WsTracking
    Middleware --> WsAI
    RestAPI --> BackendServices
    WsTracking --> BackendServices
    WsAI --> BackendServices
    RestAPI --> StaticUploads
    StaticUploads --> LocalDisk

    BackendServices --> Postgres
    BackendServices --> Redis
    BackendServices -->|"HTTP (:8001, 10s timeout)"| AIRoutes
    BackendServices -->|"Firebase Admin SDK"| Firebase
    BackendServices -.->|"Direct Gemini Fallback (if GEMINI_API_KEY set)"| Gemini
    BackendServices -.->|"Optional if GOOGLE_MAPS_API_KEY added"| GRoutes

    AIRoutes --> Classifiers
    AIRoutes --> LangGraph
    Classifiers -.->|"If GEMINI_API_KEY set"| Gemini
    LangGraph -.->|"If GEMINI_API_KEY set"| Gemini

    Android <-->|"Maps & Nearby Places"| GMaps
    Android -.->|"Direct Gemini Call (AiAgentRepository)"| Gemini
    Firebase -.->|"High-Priority Push"| Android
```

## Key Source References

- Backend entry point: [`backend/app/main.py`](../../../backend/app/main.py)
- AI Service entry point: [`ai_service/app/main.py`](../../../ai_service/app/main.py)
- Compose stack: [`docker-compose.yml`](../../../docker-compose.yml)
- Android networking: [`RetrofitClient.kt`](../../../android/app/src/main/java/com/example/nearhelp/data/api/RetrofitClient.kt)
