# ADR-004: FCM Push for Alerts + WebSockets & Polling for Live Sync

- **Status:** Implemented (Single-Worker WebSocket Scope)
- **Date:** 2026-08-12 (original); verified 2026-11-25 against code

## Context

Backgrounded Android devices kill persistent WebSocket connections, whereas active rescue coordination requires sub-second bidirectional location and chat updates.

## Decision

Use **HTTP POST** for durable SOS creation/acceptance, **high-priority Firebase Cloud Messaging (FCM)** (`AndroidConfig(priority="high", ttl=300)`) for background responder alerting, and **WebSockets** (`/ws/tracking/{incident_id}`) combined with **REST polling** (every 5s on active incident screen, every 10s on active feed, every 4s on Home alert overlay) for live state synchronization.

## Evidence in Code

- [`backend/app/services/notification_service.py`](../../backend/app/services/notification_service.py)
- [`backend/app/api/tracking_ws.py`](../../backend/app/api/tracking_ws.py)
- [`backend/app/core/websocket_manager.py`](../../backend/app/core/websocket_manager.py)
- [`android/app/src/main/java/com/example/nearhelp/notifications/NearHelpMessagingService.kt`](../../android/app/src/main/java/com/example/nearhelp/notifications/NearHelpMessagingService.kt)
- [`android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt)

## Consequences & Trade-offs

- **Pros:** Reliable reconnection recovery via server `tracking_snapshot` frames and 5s REST fallback; chat persistence in `timeline_events`.
- **Cons / Risks:**
  - `ConnectionManager._rooms` and `TrackingService._live_telemetry_cache` are **process-local**. Although `ConnectionManager.broadcast_to_incident` publishes to Redis (`channel:tracking:{incident_id}`), **no Redis Pub/Sub subscriber exists**. Running >1 Uvicorn worker breaks real-time fan-out.
  - Physical FCM delivery requires `google-services.json` (excluded from Git) and `FIREBASE_CREDENTIALS_JSON` / service account JSON, which are unverified in this workspace.
