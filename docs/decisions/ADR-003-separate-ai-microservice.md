# ADR-003: Separate Backend and AI Microservice

- **Status:** Implemented Separation; Non-Blocking Critical Path **Diverged**
- **Date:** 2026-08-12 (original); verified 2026-11-25 against code

## Context

AI dependencies (LangGraph, Google GenAI SDKs, embedding initialization) should be isolated from core incident coordination so AI failures or latency do not break emergency dispatch.

## Decision

Split `backend/` (`:8000`) and `ai_service/` (`:8001`) into two FastAPI services communicating over internal HTTP via `AIClient` (with a 10-second HTTPX timeout and local deterministic/Gemini fallbacks in `backend/app/services/ai_client.py`).

## Evidence in Code

- [`backend/app/services/ai_client.py`](../../backend/app/services/ai_client.py)
- [`backend/app/services/sos_service.py`](../../backend/app/services/sos_service.py)
- [`ai_service/app/main.py`](../../ai_service/app/main.py)

## Consequences & Trade-offs

- **Divergence from Planned Architecture:** [`docs/architecture.md`](../architecture.md) states *"the critical alert path must never be blocked by AI processing"* and diagrams SOS creation + AI triage running in parallel. In reality, [`SOSService.create_sos_event`](../../backend/app/services/sos_service.py) **awaits** `ai_client.predict_severity()` *before* persisting `SOSEvent` or notifying responders (up to a 10s timeout if `ai_service` hangs).
- **Direct Client Bypass:** [`AiAgentRepository.kt`](../../android/app/src/main/java/com/example/nearhelp/data/repository/AiAgentRepository.kt) also calls the Gemini REST API directly from Android before calling the backend proxy, bypassing `ai_service` LangGraph guardrails.
- **Pros:** When `ai_service` is down (connection refused), `AIClient` immediately falls back to deterministic local rules so SOS creation still succeeds.
