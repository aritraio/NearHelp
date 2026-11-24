# ADR-005: Curated In-Memory Clinical Protocols & Guardrails

- **Status:** Implemented (Earlier Vector-DB / RAG Plans Removed)
- **Date:** 2026-10-28 (commit `9699a81`); verified 2026-11-25 against code

## Context

Earlier planning documents proposed ChromaDB / vector-store RAG over clinical PDFs. In commit `9699a81` (`refactor: remove RAG implementation and presentation build artifacts`), the team removed the RAG subsystem in favor of curated, structured in-memory clinical protocols.

## Decision

Store 8 structured resuscitation/first-aid protocols in [`ai_service/app/agent/knowledge.py`](../../ai_service/app/agent/knowledge.py) (`GROUNDED_PROTOCOLS`, `CITATIONS_CATALOG`, `CONTRAINDICATION_RULES`), orchestrate dialogue through a 5-node **LangGraph** state machine (`triage_node` -> `protocol_retriever_node` -> `safety_guardrail_node` -> `reasoning_and_response_node` / `handover_node`), and enforce regex-based pre/post-generation safety checks via `HallucinationGuardrails`. Keep the crisis assistant chat strictly text-based on Android.

## Evidence in Code

- [`ai_service/app/agent/knowledge.py`](../../ai_service/app/agent/knowledge.py)
- [`ai_service/app/agent/gemini_agent.py`](../../ai_service/app/agent/gemini_agent.py)
- [`ai_service/app/agent/guardrails.py`](../../ai_service/app/agent/guardrails.py)
- [`ai_service/app/classifiers/embedding_service.py`](../../ai_service/app/classifiers/embedding_service.py)

## Consequences & Trade-offs

- **Pros:** Zero vector-DB infrastructure overhead; deterministic protocol steps and contraindication alerts work offline without an LLM key; multilingual keyword matching for English, Bengali, and transliterated Hindi.
- **Cons / Risks:**
  - `EmergencyAgentService.sessions` and `ai_client._SESSION_MEMORY` are in-memory dictionaries that do not persist across restarts.
  - In `backend/app/services/ai_client.py` (`_local_fallback_agent_chat`), when `GEMINI_API_KEY` is configured, live Gemini is invoked *before* the deterministic contraindication checks, which caused 3 unit test failures in `test_ai_agent.py`.
  - `handover_node` and `_local_fallback_handover` currently populate hardcoded demo patient/hospital fields (`"Rajesh Sengupta"`, `"AMRI Hospital..."`).
