# ADR-010: Weighted Responder Ranking & 3-Layer Escalation

- **Status:** Implemented (Automatic Background Escalation Scheduler Not Implemented)
- **Date:** 2026-08-12 (original); verified 2026-11-25 against code

## Context

Broadcasting an SOS to every user creates alert fatigue, while picking only the single nearest bystander ignores medical qualifications and reliability. Additionally, if no local volunteer responds, the system must escalate.

## Decision

1. **Weighted Scoring:** Rank available nearby responders (location heartbeat < 900s) using:
   `Score = 0.40 * Distance + 0.35 * SkillMatch + 0.25 * Reliability`,
   where `SkillMatch` grants a `+0.20` bonus (capped at `1.20`) for verified skills/badges, and severity levels 1–5 select top `2, 3, 5, 8, 15` responders respectively.
2. **3-Layer Escalation:** Expose `POST /api/v1/sos/{id}/escalate` supporting 30s (`2x` radius), 45s (`3x` radius, capped at `5000m`), and 60s (Layer 2 `auto_call_108_triggered = true`), plus Layer 3 (`offline_fallback_ready = true`).

## Evidence in Code

- [`backend/app/services/ranking_service.py`](../../backend/app/services/ranking_service.py)
- [`backend/app/services/geo_service.py`](../../backend/app/services/geo_service.py)
- [`backend/app/services/sos_service.py`](../../backend/app/services/sos_service.py)

## Consequences & Trade-offs

- **Pros:** Explainable, deterministic heuristic; prevents unverified users from outranking verified medical responders at slightly greater distances.
- **Cons / Risks:**
  - **No background timer/worker calls `escalate_sos` automatically** on the server, and Android's `IncidentViewModel` does not call `/api/v1/sos/{id}/escalate` either—escalation only runs if an external client explicitly invokes the endpoint.
  - `auto_call_108_triggered` is a boolean state flag; the backend does not place an actual telephone/SIP call to 108/112 (Android provides a manual `Call 112` button via `Intent.ACTION_DIAL`).
