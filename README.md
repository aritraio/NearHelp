# NearHelp AI

> **AI-Powered Community Emergency Response Network**  
> *Connecting People. Coordinating Rescue. Powered by AI.*

---

## 📌 Documentation Index

All primary project design, governance, and planning specifications have been organized into the [`docs/`](docs/) directory:

| Document | Description | Link |
| :--- | :--- | :--- |
| **System Architecture** | Technical blueprint: microservices, data flow, algorithms, DB schemas, security | [docs/architecture.md](docs/architecture.md) |
| **Master TODO List** | Comprehensive task tracking (~238 tasks) across 3 MoSCoW phases | [docs/todos.md](docs/todos.md) |
| **Project Proposal** | Complete academic proposal, problem statement, research questions, deliverables | [docs/proposal.md](docs/proposal.md) |
| **Team Task Allocation** | Governance, 6-member team roles, zero-conflict rules, contingency matrix | [docs/Task.md](docs/Task.md) |
| **Mobile UI/UX Guidance** | Jetpack Compose guidelines, color tokens, wireframes, disclaimer rules | [docs/UI_GUIDANCE.md](docs/UI_GUIDANCE.md) |

---

## 🚀 Problem Statement & Vision

During the first few minutes of an emergency (cardiac arrest, fire, accident), professional responders (108/112) are often several minutes away. The nearest person capable of helping is frequently a neighbor, passerby, or off-duty medical volunteer within walking distance. 

**NearHelp AI** creates an AI-assisted, real-time emergency response network that:
1. **Detects & Classifies** emergencies instantly from text, voice, or photo.
2. **Ranks & Mobilizes** nearby verified community responders based on proximity, skills, and reliability.
3. **Guides** bystanders with step-by-step, WHO/Red Cross-grounded first-aid protocols via RAG (Retrieval-Augmented Generation).
4. **Coordinates** rescue via live GPS tracking, real-time WebSockets chat, and automated emergency summaries for professional services.

---

## 🏗️ Repository Structure

```
NearHelp/
├── docs/                           # Architecture, Specs, Guidelines, TODOs
│   ├── architecture.md             # System Architecture & Technical Specifications
│   ├── todos.md                    # Master TODO List (~238 tasks)
│   ├── proposal.md                 # Master Project Proposal
│   ├── Task.md                     # Team Task Allocations & Governance
│   └── UI_GUIDANCE.md              # Android UI/UX & Jetpack Compose Specs
│
├── android/                        # Android Client (Kotlin + Jetpack Compose)
├── backend/                        # Backend API Service (FastAPI + PostgreSQL/PostGIS)
├── ai_service/                     # AI Microservice (Gemini 2.5 + RAG + LangGraph)
├── admin_dashboard/                # Admin Web Panel (Next.js / React)
├── simulator/                      # Digital Twin Simulator (Locust + Load Testing)
├── data/                           # Emergency Protocols & Kolkata Regional JSON Data
│   ├── protocols/                  # WHO, Red Cross, NDMA PDFs
│   └── regional/                   # Hospitals, Police, Fire Station JSONs
│
└── assets/                         # Branding, Icons, Map Pins, Media
```

---

## 🧩 24 Core System Modules (3-Phase MoSCoW Model)

### Phase 1 — MVP Core (Must Have)
- **Module 1**: Authentication & Identity (Firebase Auth, JWT, Anonymous SOS)
- **Module 2**: User Profile & Encrypted Medical ID
- **Module 3**: Skill Verification Workflow (Doctor, Nurse, CPR, NGO)
- **Module 4**: AI Emergency Detection (Text, Voice, Image)
- **Module 5**: AI Severity Prediction (0–100 Triage Score)
- **Module 6**: Smart SOS Engine (Geospatial PostGIS Query + 3-Layer Escalation)
- **Module 7**: Live Rescue Map (Google Maps SDK)
- **Module 8**: Live Tracking Stream (WebSocket Location Updates)
- **Module 9**: AI Navigation & Rescue Routing
- **Module 10**: AI Crisis Assistant (LangGraph Emergency Agent)
- **Module 11**: RAG Knowledge Base (ChromaDB + WHO/Red Cross Corpus)

### Phase 2 — System Enhancements (Should Have)
- **Module 12**: AI Multilingual Translation (Bengali, Hindi, English)
- **Module 13**: Voice SOS Triage (Speech-to-Text → Structured JSON)
- **Module 14**: Emergency Timeline Auto-Generation
- **Module 15**: AI Post-Incident Summary Report
- **Module 16**: Reputation Engine (Trust Score & Badges)
- **Module 17**: Community Resource Layer (AEDs, Blood Banks, Shelters)

### Phase 3 — Stretch & Analytics (Could Have)
- **Module 18**: Admin Web Dashboard
- **Module 19**: AI Analytics & Emergency Heatmaps
- **Module 20**: Mass Disaster Mode (Multi-Responder Coordination)
- **Module 21**: Guardian Safety Mode
- **Module 22**: Offline SMS Emergency Fallback
- **Module 23**: Digital Twin Load Simulator (Viva Benchmark Generator)
- **Module 24**: Developer Dashboard & System Monitoring (Prometheus/Grafana)

---

## 🛡️ Risk Mitigation & Fail-Safe Architecture

| Risk | Strategic Solution | Technical Implementation |
| :--- | :--- | :--- |
| **Scope Overload** | **3-Phase MoSCoW Model** | MVP (Modules 1–11) completed first. Phase 2/3 advance only after prior phase stability. |
| **No Volunteers Nearby** | **3-Layer Escalation** | **Layer 1**: Auto-radius expansion (30s/45s/60s). **Layer 2**: Direct 108/112 auto-dial with AI summary. **Layer 3**: Guided offline self-care AI fallback. |
| **Medical Liability** | **Good Samaritan Law + Strict Guardrails** | Non-dismissible legal disclaimer on guidance UI. Strict RAG prompt constraints (no dosage, no diagnosis, no prescriptions). Protected under India's Good Samaritan Law (2016). |
| **Viva/Defense Weakness** | **Digital Twin Simulation** | 5 auto-generated comparative evaluation charts: AI-ranked vs broadcast, indexed vs unindexed geo queries, AI latency breakdown, throughput curves, and skill-aware vs distance-only ranking. |

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Mobile Client** | Kotlin + Jetpack Compose + Google Maps SDK |
| **Backend API** | FastAPI (Python) + SQLAlchemy 2.0 |
| **Database & Cache** | PostgreSQL 16 + PostGIS 3.4 / Redis 7 |
| **AI & LLM** | Gemini 2.5 + LangGraph + RAG (ChromaDB) |
| **Real-Time** | WebSockets + Firebase Cloud Messaging (FCM) |
| **DevOps & Infra** | Docker + Docker Compose + Google Cloud Run + GitHub Actions |
| **Monitoring** | Prometheus + Grafana + Swagger / OpenAPI 3.1 |

---

## 👥 Team Roster & Ownership

- **Aritra** — Project Lead & AI Architect (*Core AI, LangGraph, RAG, Integration*)
- **Adil** — Backend & Real-Time Lead (*FastAPI, PostGIS DB, WebSockets, FCM*)
- **Dishari** — Android App UI/UX Lead (*Jetpack Compose Screens, Navigation, Theme*)
- **Abhisikta** — Documentation & QA Specialist (*SRS, SDD, UML Diagrams, Test Cases*)
- **Plaban** — Data & Knowledge Analyst (*RAG PDFs, Competitor Benchmarks, City Data*)
- **Sayantan** — Design, Assets & Media Specialist (*Branding, Icons, Slide Deck, Demo Video*)

---

*For detailed technical specifications, check out [docs/architecture.md](docs/architecture.md) and [docs/todos.md](docs/todos.md).*