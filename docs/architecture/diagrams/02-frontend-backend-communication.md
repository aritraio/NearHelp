# 2. Frontend-Backend Communication

This diagram shows how the Android app and Admin Web UI communicate with the FastAPI backend over HTTP/REST and WebSockets, including token refresh and polling fallbacks.

```mermaid
sequenceDiagram
    autonumber
    participant UI as "Compose Screen / Admin Web"
    participant VM as "ViewModel / Admin JS"
    participant Repo as "Repository / SosApiService"
    participant Tokens as "SessionTokens / TokenStorage"
    participant API as "FastAPI Backend (:8000)"
    participant WS as "WebSocket Manager"

    UI->>VM: User triggers action
    VM->>Repo: Call API operation
    Repo->>Tokens: Request Bearer token
    alt Access token expires within 60s
        Tokens->>API: POST /api/v1/auth/refresh
        API-->>Tokens: New access_token + refresh_token
        Tokens->>Tokens: Persist in EncryptedSharedPreferences
    end
    Tokens-->>Repo: Bearer access_token
    Repo->>API: HTTP JSON Request (+ Idempotency-Key on SOS create)
    API-->>Repo: JSON Response (or 4xx/5xx)
    Repo-->>VM: Result / Exception
    VM-->>UI: Update StateFlow UI state

    Note over VM,WS: Live Incident Coordination Loop
    VM->>WS: Connect /ws/tracking/{incident_id}?token=JWT
    WS-->>VM: connection_ack (role) + tracking_snapshot
    loop Every 5s while incident open
        VM->>API: GET /api/v1/sos/{id} & /tracking (REST fallback/sync)
        API-->>VM: Incident + TrackingSnapshot DTOs
    end
    loop When responder GPS changes
        VM->>WS: location_update {latitude, longitude, accuracy}
        WS-->>VM: responder_update (broadcast to room)
    end
```

## Key Observations from Code

- **No global OkHttp auth interceptor**: Token refresh is handled explicitly by [`SessionTokens.kt`](../../../android/app/src/main/java/com/example/nearhelp/data/api/SessionTokens.kt) on callers that invoke `SessionTokens.bearer()`. Note that `UserRepository` reads `tokenStorage.getAccessToken()` directly without calling `SessionTokens.bearer()`.
- **Admin Web session storage**: [`backend/app/admin_web/app.js`](../../../backend/app/admin_web/app.js) stores `accessToken` and `refreshToken` strictly in page memory (`let accessToken = ''`) and automatically retries once on HTTP 401 via `/api/v1/auth/refresh`.
- **Direct incident service call**: [`IncidentViewModel.kt`](../../../android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt) delegates to [`AndroidIncidentRepository`](../../../android/app/src/main/java/com/example/nearhelp/data/repository/IncidentRepository.kt) (which wraps `SosApiService`) and [`LiveTrackingWebSocketClient.kt`](../../../android/app/src/main/java/com/example/nearhelp/data/api/ws/LiveTrackingWebSocketClient.kt).
