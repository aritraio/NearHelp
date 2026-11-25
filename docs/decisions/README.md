# NearHelp AI — Architecture Decision Records (ADRs)

This directory contains detailed Architecture Decision Records reverse-engineered from the repository source code, commit history, [`decisions.md`](../architecture/DECISIONS.md), and [`docs/architecture.md`](../planning/architecture.md).

Each ADR documents the **Context**, **Decision**, **Implementation Status** (`Implemented`, `Partial`, `Diverged`, or `Planned`), **Evidence in Code**, and **Consequences / Trade-offs**.

| ID | Title | Status | File |
| :--- | :--- | :--- | :--- |
| ADR-001 | FastAPI Async Microservice Framework | Implemented | [ADR-001-fastapi-framework.md](ADR-001-fastapi-framework.md) |
| ADR-002 | PostgreSQL 16 + PostGIS 3.4 with SQLite Test Stubs | Implemented (Migrations Partial) | [ADR-002-postgresql-postgis.md](ADR-002-postgresql-postgis.md) |
| ADR-003 | Separate Backend and AI Microservice | Implemented (Sync Blocking Diverged) | [ADR-003-separate-ai-microservice.md](ADR-003-separate-ai-microservice.md) |
| ADR-004 | FCM Push for Alerts + WebSockets & Polling for Live Sync | Implemented (Single-Worker Scope) | [ADR-004-fcm-plus-websockets.md](ADR-004-fcm-plus-websockets.md) |
| ADR-005 | Curated In-Memory Clinical Protocols & Guardrails | Implemented (RAG Removed) | [ADR-005-curated-protocols-over-rag.md](ADR-005-curated-protocols-over-rag.md) |
| ADR-006 | Native Android (Kotlin + Jetpack Compose + Navigation 3) | Implemented | [ADR-006-native-android-compose.md](ADR-006-native-android-compose.md) |
| ADR-007 | Backend-Issued HS256 JWTs + Multi-Provider Identity | Partial (Dev Fallbacks Active) | [ADR-007-jwt-and-identity-providers.md](ADR-007-jwt-and-identity-providers.md) |
| ADR-008 | AES-256-GCM Medical Encryption & Device Storage | Implemented (With Fallbacks) | [ADR-008-medical-encryption-and-storage.md](ADR-008-medical-encryption-and-storage.md) |
| ADR-009 | Same-Origin Static Admin Panel Served by FastAPI | Implemented (Diverged from Next.js Plan) | [ADR-009-in-process-admin-web.md](ADR-009-in-process-admin-web.md) |
| ADR-010 | Weighted Responder Ranking & 3-Layer Escalation | Implemented (Manual Escalation Trigger) | [ADR-010-responder-ranking-and-escalation.md](ADR-010-responder-ranking-and-escalation.md) |
