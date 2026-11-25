# NearHelp AI Documentation Hub

Documentation is organized into five clear sections:

| Directory | Contents |
| :--- | :--- |
| [`architecture/`](architecture/) | Current runtime topology, system design, codebase map, database schema, AI system, data flows, decision register, and Mermaid diagrams |
| [`reference/`](reference/) | REST/WebSocket API reference, user journeys, security audit, testing guide, deployment guide, and known issues |
| [`guides/`](guides/) | Operational & UI guides ([DEVICE_READINESS.md](guides/DEVICE_READINESS.md), [UI_GUIDANCE.md](guides/UI_GUIDANCE.md), [ANDROID_REDESIGN.md](guides/ANDROID_REDESIGN.md)) |
| [`decisions/`](decisions/README.md) | 10 numbered Architecture Decision Records (`ADR-001` through `ADR-010`) |
| [`planning/`](planning/) | Historical pre-implementation blueprint (`architecture.md`), design system (`design.md`), proposal (`proposal.md`), and task list (`todos.md`) |

## Recommended Reading Order

1. [Project Overview](architecture/PROJECT_OVERVIEW.md) — product purpose and implemented-vs-planned matrix
2. [Architecture](architecture/ARCHITECTURE.md) — three-tier runtime topology and service boundaries
3. [Codebase Map](architecture/CODEBASE_MAP.md) — directory-by-directory guide to `backend/`, `ai_service/`, `android/`, and `data/`
4. [System Design](architecture/SYSTEM_DESIGN.md) & [Data Flow](architecture/DATA_FLOW.md) — SOS dispatch, ranking, WebSocket state, and routing
5. [API Reference](reference/API_REFERENCE.md) & [Testing](reference/TESTING.md) — endpoints and verification commands
