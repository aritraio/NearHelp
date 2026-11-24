# ADR-001: FastAPI Async Microservice Framework

- **Status:** Implemented
- **Date:** 2026-08-12 (original); verified 2026-11-25 against code

## Context

NearHelp AI requires concurrent REST API handling, WebSocket connections for real-time GPS/chat streaming, automatic request/response validation, and interactive OpenAPI documentation for both the core coordination backend and the AI inference service.

## Decision

Use Python **FastAPI** with Pydantic v2 schemas and Uvicorn ASGI servers for both `backend` (port `8000`) and `ai_service` (port `8001`). Expose canonical `/api/v1/*` routes alongside legacy `/api/*` compatibility aliases.

## Evidence in Code

- [`backend/app/main.py`](../../backend/app/main.py)
- [`backend/app/api/__init__.py`](../../backend/app/api/__init__.py)
- [`ai_service/app/main.py`](../../ai_service/app/main.py)

## Consequences & Trade-offs

- **Pros:** Native async/await support for HTTPX calls, `asyncpg` DB queries, and FastAPI WebSockets; unified Python stack across backend and AI; automatic Swagger/OpenAPI generation at `/docs`.
- **Cons / Risks:** Synchronous SDK calls inside async handlers (e.g., `firebase_admin.messaging.send` without `to_thread` in older code—now wrapped in `asyncio.to_thread`, or legacy synchronous `google.generativeai` in `severity_predictor.py`) can block the event loop. Dual route prefixes (`/api/v1/*` and `/api/*`) increase maintenance surface.
