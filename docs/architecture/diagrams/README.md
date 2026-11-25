# NearHelp AI — Diagrams Index

All architecture, data-flow, schema, security, and deployment diagrams for the implemented NearHelp AI codebase live in this directory. Diagrams distinguish between **implemented runtime paths** (solid lines) and **optional or unverified paths** (dashed lines).

| # | Diagram | Description | File |
| :--- | :--- | :--- | :--- |
| 1 | Overall System Architecture | Services, clients, databases, external providers, and ports | [01-system-architecture.md](01-system-architecture.md) |
| 2 | Frontend-Backend Communication | Android Compose, Admin Web UI, REST, and WebSocket transport | [02-frontend-backend-communication.md](02-frontend-backend-communication.md) |
| 3 | Application Data Flow | End-to-end SOS creation, dispatch, live tracking, and resolution data paths | [03-application-data-flow.md](03-application-data-flow.md) |
| 4 | Database Entity Relationship Diagram | Actual SQLAlchemy tables, columns, keys, and PostGIS spatial columns | [04-database-erd.md](04-database-erd.md) |
| 5 | Authentication Workflow | Email/password, Google OAuth/Firebase, Phone OTP, Anonymous guest, and JWT refresh | [05-authentication-workflow.md](05-authentication-workflow.md) |
| 6 | Important User Workflows | Victim SOS creation, Responder dispatch/arrival, and Admin verification/moderation | [06-user-workflows.md](06-user-workflows.md) |
| 7 | AI Processing Pipeline | Multimodal classification, severity scoring, LangGraph agent, guardrails, and fallbacks | [07-ai-processing-pipeline.md](07-ai-processing-pipeline.md) |
| 8 | Deployment Architecture | Docker Compose stack, local LAN APK testing, and unverified cloud targets | [08-deployment-architecture.md](08-deployment-architecture.md) |

## Data Flow Diagrams (DFD Series)

| Level | Diagram | Scope & Description | File |
| :--- | :--- | :--- | :--- |
| Level 0 | Context Diagram | Whole-system black box, external users & third-party services (`E1`–`E8`) | [dfd-level-0-context.md](dfd-level-0-context.md) |
| Level 1 | System Decomposition | Major subsystems (`1.0`–`7.0`), data stores (`D1`–`D6`), and cross-service flows | [dfd-level-1-system.md](dfd-level-1-system.md) |
| Level 2 | Authentication & Identity | Email, Google/Firebase OAuth, Phone OTP stub, Anonymous 1-tap, JWT & presence (`1.0`) | [dfd-level-2-authentication.md](dfd-level-2-authentication.md) |
| Level 2 | Core SOS & Live Tracking | Idempotency, severity scoring, PostGIS ranking, FCM, escalation, WS tracking & resolve (`3.0`, `4.0`) | [dfd-level-2-core-workflow.md](dfd-level-2-core-workflow.md) |
| Level 2 | AI Triage & Crisis Agent | Android direct bypass, backend fallback, multimodal classifier, severity & LangGraph (`5.0`) | [dfd-level-2-ai-processing.md](dfd-level-2-ai-processing.md) |
