# 🤝 NearHelp AI — Contributing & Developer Onboarding Guide

> **Welcome to the NearHelp AI Engineering Workspace!**  
> This guide details how to set up your local development environment, run microservices, and adhere to our team governance standards.

---

## 📑 Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Quickstart (3 Minutes)](#2-quickstart-3-minutes)
3. [Service-by-Service Setup](#3-service-by-service-setup)
   - [Backend Service (Adil)](#-backend-service-fastapi--postgis)
   - [AI Microservice (Aritra)](#-ai-microservice-gemini-25--langgraph)
   - [Android Client (Dishari)](#-android-client-kotlin--jetpack-compose)
   - [Documentation & QA (Abhisikta)](#-documentation--qa-abhisikta)
   - [Data & Research (Plaban)](#-data--research-plaban)
   - [Design & Assets (Sayantan)](#-design--assets-sayantan)
4. [Git Workflow & Commit Guidelines](#4-git-workflow--commit-guidelines)
5. [Code Quality & Pre-Commit](#5-code-quality--pre-commit)

---

## 1. Prerequisites

Ensure you have the following installed on your machine:
* **Docker & Docker Compose** (v24.0+)
* **Python** (v3.11 or v3.12)
* **Node.js** (v20+ LTS) & **npm**
* **Android Studio** (Ladybug / Koala or newer with Android SDK 34)
* **Git**

---

## 2. Quickstart (3 Minutes)

```bash
# 1. Clone the repository and navigate into it
cd /path/to/NearHelp

# 2. Copy the environment configuration template
cp .env.example .env

# 3. Start the core database & cache containers
docker compose up -d db redis

# 4. (Optional) Start all backend and AI services in Docker
docker compose up -d

# 5. Launch the showcase admin dashboard
cd admin_dashboard
npm install
npm test          # Runs 192 automated unit/integration tests
npm run dev       # Starts Vite dev server at http://localhost:5173
```

---

## 3. Service-by-Service Setup

### 🟢 Backend Service (FastAPI + PostGIS)
**Owner**: Adil  
**Path**: `backend/`

* **Local Python Virtual Environment**:
  ```bash
  cd backend
  python3.11 -m venv venv
  source venv/bin/activate    # On Windows: venv\Scripts\activate
  pip install -r requirements.txt
  ```
* **Run Server with Hot-Reload**:
  ```bash
  uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
  ```
* **Interactive API Documentation**:
  * Swagger UI: [http://localhost:8000/docs](http://localhost:8000/docs)
  * ReDoc: [http://localhost:8000/redoc](http://localhost:8000/redoc)
* **Run Unit Tests**:
  ```bash
  pytest
  ```

---

### 🔴 AI Microservice (Gemini 2.5 + LangGraph + ChromaDB)
**Owner**: Aritra  
**Path**: `ai_service/`

* **Setup & Dependencies**:
  ```bash
  cd ai_service
  python3.11 -m venv venv
  source venv/bin/activate
  pip install -r requirements.txt
  ```
* **Configuration**: Set your `GEMINI_API_KEY` in `.env`.
* **Run Server**:
  ```bash
  uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload
  ```
* **API Documentation**: [http://localhost:8001/docs](http://localhost:8001/docs)

---

### 🟡 Android Client (Kotlin + Jetpack Compose)
**Owner**: Dishari  
**Path**: `android/`

1. Open Android Studio and select **Open Project** -> `android/`.
2. Ensure **JDK 17** is selected in *Gradle Settings*.
3. Build the project: `./gradlew build`.
4. Run on a virtual device (Pixel 8 API 34) or physical Android device.
5. Base URL for local development:
   * Android Emulator: `http://10.0.2.2:8000`
   * Physical Device: `http://<your-local-ip>:8000`

---

### 🟣 Documentation & QA (Abhisikta)
**Owner**: Abhisikta  
**Path**: `docs/` & `archive/review-1/`

* Review reports and academic deliverables live in `archive/review-1/`.
* Follow Markdown standards for SRS, SDD, and UML diagrams.

---

### 🟠 Data & Research (Plaban)
**Owner**: Plaban  
**Path**: `data/`

* Place medical protocols in `data/protocols/<category>/` (PDF/Markdown).
* Place regional geospatial data in `data/regional/<category>.json`.

---

### 🔵 Design & Assets (Sayantan)
**Owner**: Sayantan  
**Path**: `assets/`

* Export SVG vector icons into `assets/icons/`.
* Save branding assets and presentation decks into `assets/branding/`.

---

## 4. Git Workflow & Commit Guidelines

We use **Conventional Commits** for clean and readable change histories:

| Prefix | Usage | Example |
| :--- | :--- | :--- |
| `feat:` | New feature | `feat: implement PostGIS radial spatial query` |
| `fix:` | Bug fix | `fix: resolve 110 BPM metronome audio latency` |
| `docs:` | Documentation changes | `docs: add SRS emergency use cases` |
| `chore:` | Dependency or config updates | `chore: add docker-compose redis service` |
| `refactor:` | Code reorganization | `refactor: extract triage card component` |

---

## 5. Code Quality & Pre-Commit

Before submitting a Pull Request, ensure pre-commit hooks pass:

```bash
pip install pre-commit
pre-commit install
pre-commit run --all-files
```

Linting Python code with **Ruff**:
```bash
ruff check . --fix
ruff format .
```
