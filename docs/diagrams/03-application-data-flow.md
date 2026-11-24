# 3. Application Data Flow

This diagram traces user data across Android local storage, backend PostgreSQL/PostGIS, Redis, local uploads, AI services, and external APIs.

```mermaid
flowchart LR
    subgraph Device["Android Device"]
        Input["User Input (Auth, Profile, SOS, Chat)"]
        Prefs["EncryptedSharedPreferences (Tokens, Pending SOS, Active ID, History)"]
        UIState["ViewModel StateFlows"]
    end

    subgraph Backend["FastAPI Backend (:8000)"]
        Idem["Idempotency & RateLimit Middleware"]
        Crypto["AES-256-GCM Encryption (crypto.py)"]
        SOS["SOSService & RankingService"]
        Track["TrackingService (In-Memory Telemetry Cache)"]
        Files["Upload Writers (Avatars & Certificates)"]
    end

    subgraph Stores["Data Stores"]
        Redis[("Redis 7 (or Process Memory)")]
        DB[("PostgreSQL 16 + PostGIS 3.4")]
        Disk[("backend/uploads/")]
    end

    subgraph AI["AI Service (:8001)"]
        Severity["SeverityPredictor"]
        Agent["LangGraph EmergencyAgent"]
    end

    subgraph Push["Delivery & External"]
        FCM["Firebase Cloud Messaging"]
        Gemini["Google Gemini"]
    end

    Input --> Prefs
    Input --> Idem
    Idem <--> Redis
    Idem --> Crypto
    Crypto -->|"medical_conditions, known_allergies, medical_notes"| DB
    Idem --> SOS
    SOS -->|"Awaits severity_score"| Severity
    Severity -.-> Gemini
    SOS -->|"PostGIS ST_DWithin + ST_Distance"| DB
    SOS -->|"High-Priority Push"| FCM
    FCM -.->|"Wakes Responder"| Device
    Track <-->|"Ephemeral GPS cache"| Track
    Track -->|"Updates users.device_info & sos_responses"| DB
    Files --> Disk
    Input -->|"AI Crisis Chat"| Agent
    Agent -.-> Gemini
    DB --> UIState
    Track --> UIState
```

## Key Source References

- Encryption helper: [`backend/app/core/crypto.py`](../../backend/app/core/crypto.py)
- Middleware cache: [`backend/app/core/middleware.py`](../../backend/app/core/middleware.py)
- SOS lifecycle: [`backend/app/services/sos_service.py`](../../backend/app/services/sos_service.py)
- Live tracking state: [`backend/app/services/tracking_service.py`](../../backend/app/services/tracking_service.py)
