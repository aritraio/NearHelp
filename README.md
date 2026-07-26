# NearHelp AI

**AI-Powered Community Emergency Response Network**

> **Connecting People. Coordinating Rescue. Powered by AI.**

## Problem Statement

During the first few minutes of an emergency, professional responders are often too far away. The nearest person capable of helping is frequently a neighbor, passerby, or trained volunteer. Existing emergency systems rarely leverage nearby community members in a structured, intelligent, and trustworthy way.

NearHelp AI aims to bridge this gap by creating an AI-assisted, real-time emergency response network that intelligently connects victims with nearby verified responders while providing AI-guided emergency assistance, multilingual communication, and real-time coordination.

## Project Objectives

The project should achieve the following goals:
* Reduce emergency response time.
* Build a trusted community responder network.
* Assist victims before ambulances arrive.
* Help emergency responders collect accurate information.
* Use AI to improve decision making.
* Provide authorities with emergency analytics.

## Architecture and Data Flow

1. **SOS Trigger**: Someone taps SOS on the Android client → hits the backend over a normal HTTPS request (the trigger itself should be a reliable request/response).
2. **Geospatial Query**: Backend runs a query against the database's geospatial index (e.g., MongoDB `$nearSphere` or PostgreSQL `PostGIS`) to find users in the configured radius.
3. **Fan-out**: Backend fans the alert out two ways: 
   * **FCM push** to every matched device (this is what actually wakes a backgrounded phone).
   * Opens a **WebSocket/Socket.io** channel for anyone who taps "I'm responding," allowing live location updates and chat.
4. **AI Processing**: In parallel, the backend calls the AI service, which retrieves the relevant first-response procedure from the vector store and returns grounded guidance + an auto-filled summary.

*Note: The database choices are owned by the subsystem that uses them, keeping real-time coordination decoupled from AI retrieval.*

## AI Service

- **Retrieval corpus**: A curated set of first-aid/crisis protocols (Red Cross, WHO, St. John Ambulance) chunked into short procedure-level passages.
- **Embedding + store**: A small embedding model plus a simple vector store (e.g., Chroma or `pgvector`). Focus is on explaining chunking and retrieval quality decisions rather than enterprise-scale vector infra.
- **Generation**: Retrieved passages + crisis type + free-text description go into the LLM prompt, generating:
  - First-response guidance for the responder.
  - A pre-filled emergency summary for the broadcaster to read to services.
  - *The prompt is structured so the model cites which retrieved step it's using to prevent hallucinations.*
- **Classification**: Free-text crisis intake uses embedding-similarity match against defined crisis types.

## Data Model (Sketch)

- `users`: id, location (geo point, only updated on active SOS), skills[], verified flag, response history summary
- `sos_events`: id, broadcaster_id, crisis_type, location, status (active/resolved), created_at, is_anonymous
- `responses`: sos_event_id, responder_id, status, joined_at
- `messages`: sos_event_id, responder_id, text, timestamp
- `ai_summaries`: sos_event_id, generated_guidance, generated_summary, retrieved_procedure_refs

*Note: `sos_events.location` is the only place PII-adjacent location data lives, and it is stripped entirely from anything responders see when `is_anonymous` is true.*

## Responder Ranking

Score = `w1·(1/distance) + w2·(skill_match) + w3·(reliability_score)`. 
Start with fixed weights, tune them by hand against a few test scenarios. This provides a clearly-justified weighted scoring function without needing a complex ML model.

## Safety-Critical Engineering

- **Idempotency**: SOS trigger and "I'm responding" taps need idempotency keys so a flaky connection retry doesn't create duplicate events.
- **Delivery confirmation**: Track FCM delivery receipts; fall back to a retry or secondary channel on failure.
- **Load test the geo query**: Simulate concurrent SOS triggers (k6/Locust) and report actual query latency with vs. without the spatial index.

## Timeline

- **Month 1**: Android + FCM + basic SOS trigger proven end-to-end. Stand up vector store and get one crisis type retrieving real guidance.
- **Month 2**: Responder flow, live map, chat, resolve.
- **Month 3**: Full RAG pipeline (multi crisis-type, free-text intake, ranking algorithm), skill registry.
- **Month 4**: Trust layer, admin dashboard, load test + benchmark numbers, polish, defense prep, report writing.

---

## System Modules

### Module 1 – Authentication
* Email Login, Google Login, Phone OTP
* JWT Authentication
* Anonymous Emergency Mode
* Device Registration

### Module 2 – User Profile
* Name, Photo, Blood Group
* Medical Conditions, Known Allergies
* Emergency Contacts, Languages
* Skills, Trust Score, Badges

### Module 3 – Skill Verification
Users upload certificates (Medical License, NGO, CPR) for Admin verification. Verified badge appears for skills like Doctor, Nurse, Police, Firefighter, CPR Certified, etc.

### Module 4 – AI Emergency Detection
AI detects emergency type from Text, Voice, Photo, or Video.
*Example: User speaks "My father is unconscious and not breathing." AI returns: Medical, Critical Priority, Immediate CPR Required, 3km Radius, Suggested Responders.*

### Module 5 – AI Severity Prediction
Calculates Severity Score, Confidence, and Reason (e.g., Possible cardiac arrest, No breathing).

### Module 6 – Smart SOS Engine
AI decides Whom to notify, When, How far, and Priority (e.g., Gas leak → Fire Dept, nearby Electricians).

### Module 7 – Live Map
Features: Victim, Responders, Hospital, Police, Fire Station, AED, Traffic, Blocked Roads. Live updates.

### Module 8 – Live Tracking
Victim watches responders approach with ETA, like ride-sharing apps.

### Module 9 – AI Navigation
Considers Traffic, Flood, Road Closure, Accident, Construction to return the fastest rescue route.

### Module 10 – AI Crisis Assistant (Emergency Agent)
Responsibilities: Understand emergency → Ask follow-up questions → Provide first aid → Generate emergency summary → Coordinate responders → Translate messages → Suggest nearest hospital.

### Module 11 – RAG Knowledge Base
Knowledge Sources: WHO First Aid, Red Cross, CPR Guidelines, Fire Safety Manual.
Pipeline: Question → Retriever → Relevant Documents → LLM → Verified Answer.

### Module 12 – AI Translation
Seamless translation of Voice, Text, and Emergency Summaries across languages.

### Module 13 – Voice SOS
One button. Transcribes speech, extracts structured JSON via LLM, and creates an emergency without typing.

### Module 14 – Emergency Timeline
Automatically generated event timeline (e.g., SOS Created, Doctor Accepted, CPR Started, Ambulance Arrived).

### Module 15 – AI Incident Report
Automatically creates a report containing Incident Type, Location, Timeline, Responders, Average Response Time, Treatment, and Outcome.

### Module 16 – Reputation Engine
Every response affects trust score (Positive for quick reach and actual help; Negative for false responding, spam).

### Module 17 – Community Layer
Map includes AEDs, Blood Banks, Police/Fire Stations, Hospitals, Shelters, Public Toilets, Wheelchair Access.

### Module 18 – Admin Dashboard
Live Map, Response Time, Most Active Responders, Heatmaps, Fraud Detection, Suspended Users, Emergency Trends.

### Module 19 – AI Analytics
Analyzes data for average response times, common emergencies, most active volunteers, and peak hours.

### Module 20 – Disaster Mode
Supports multi-responder coordination rooms for Flood, Earthquake, Cyclone, Fire, Building Collapse.

### Module 21 – Guardian Mode
Immediately notifies guardians for Children, Women, Senior Citizens, and Disabled Users.

### Module 22 – Offline Mode
Falls back to SMS to Server to create emergencies when there is no internet connection.

### Module 23 – Digital Twin Simulator (Unique Feature)
Simulation dashboard generating virtual users, vehicles, and emergencies to scientifically measure Average Response Time, WebSocket Load, Database Query Time, Notification Delivery, and AI Latency.

### Module 24 – Developer Dashboard
Swagger API, Logs, Monitoring, Database, Redis, WebSocket Connections, CPU/Memory Usage, Errors.

---

## Technology Stack

| Layer          | Technology                                         |
| -------------- | -------------------------------------------------- |
| Android        | Kotlin + Jetpack Compose                           |
| Backend        | FastAPI                                            |
| Database       | PostgreSQL + PostGIS / MongoDB + 2dsphere          |
| Cache          | Redis                                              |
| Authentication | JWT + Firebase Auth                                |
| Maps           | Google Maps SDK                                    |
| AI             | Gemini 2.5 + LangGraph (agent orchestration) + RAG |
| Real-time      | WebSockets / Socket.io                             |
| Notifications  | Firebase Cloud Messaging                           |
| Deployment     | Docker + Google Cloud Run                          |
| Monitoring     | Prometheus + Grafana                               |
| CI/CD          | GitHub Actions                                     |
| Documentation  | Swagger/OpenAPI                                    |

---

## Research Contributions

This project can answer several measurable questions through experiments:
* How much faster is AI-based responder selection compared to fixed-radius broadcasting?
* Does skill-aware matching improve response quality?
* What is the latency impact of AI processing?
* How many concurrent SOS events can the platform handle?
* Which geospatial indexing strategy performs best for nearby-user queries?

## Deliverables

By the end of four months, the project will yield:
* Native Android application
* Production-style backend API
* AI-powered emergency coordination agent
* RAG knowledge base
* Admin dashboard
* Real-time WebSocket system
* Geospatial database with PostGIS
* Digital Twin simulation environment
* Dockerized deployment
* Automated testing and CI/CD
* Complete SRS, SDD, UML diagrams, API documentation, and performance evaluation