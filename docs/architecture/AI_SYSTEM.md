# AI System Architecture & Implementation

NearHelp's AI capabilities span two Python modules (`ai_service/` and `backend/app/services/ai_client.py`) plus an Android-side direct-Gemini and local knowledge engine (`AiAgentRepository.kt`). See [docs/diagrams/07-ai-processing-pipeline.md](docs/diagrams/07-ai-processing-pipeline.md) for the pipeline diagram.

---

## 1. AI Microservice (`ai_service/`, Port `8001`)

### 1.1 Configuration & Models
Source: [`ai_service/app/core/config.py`](ai_service/app/core/config.py)
- `GEMINI_MODEL` defaults to `"gemini-2.0-flash"` (note: `backend/app/core/config.py` defaults `GEMINI_MODEL` to `"gemini-3.5-flash-lite"`, while docstrings/specs mention `"Gemini 2.5"`).
- `EMBEDDING_MODEL` defaults to `"models/text-embedding-004"`.
- `GEMINI_API_KEY` defaults to `""` (empty string triggers deterministic local fallbacks).

### 1.2 Emergency Taxonomy & Profiles
Source: [`ai_service/app/classifiers/crisis_types.py`](ai_service/app/classifiers/crisis_types.py)
- **8 Clinical Conditions (`CLINICAL_CONDITIONS_MATRIX`):** `cardiac_arrest`, `severe_bleeding`, `respiratory_asthma`, `unconscious_seizure`, `stroke`, `severe_burns`, `fracture_trauma`, `anaphylaxis_allergy`.
- **7 Non-Medical Profiles (`NON_MEDICAL_PROFILES`):** `structural_fire`, `electrical_fire`, `lpg_gas_leak`, `road_accident`, `drowning`, `physical_assault`, `urban_flood_collapse`.
- Reference texts include **English, Bengali, and transliterated Hindi** phrases.

### 1.3 Multimodal Classification Pipeline
Source: [`emergency_classifier.py`](ai_service/app/classifiers/emergency_classifier.py), [`embedding_service.py`](ai_service/app/classifiers/embedding_service.py), [`speech_service.py`](ai_service/app/classifiers/speech_service.py), [`vision_service.py`](ai_service/app/classifiers/vision_service.py)

1. **Text:** Collected from `request.text`.
2. **Voice Audio (`audio_base64`):** `SpeechService.transcribe_audio` calls legacy `google.generativeai` with `GEMINI_MODEL` if `GEMINI_API_KEY` is configured; otherwise checks if base64 decodes to UTF-8 test hints or returns a hardcoded mock string (`"Victim collapsed on ground..."`).
3. **Scene Image (`image_base64`):** `VisionService.analyze_scene` calls `google.genai` (or legacy `google.generativeai`) asking for strict JSON (`scene_description`, `emergency_type`, `hazards`, `injuries`, `confidence`); otherwise falls back to UTF-8 test checks or a hardcoded mock description.
4. **Embedding & Cosine Similarity (`EmbeddingService`):**
   - Precomputes phrase embeddings and centroid embeddings for all 15 profiles during FastAPI `lifespan` startup.
   - Uses `models/text-embedding-004` when `GEMINI_API_KEY` is set; otherwise uses **`ClinicalSemanticVectorizer`**, a deterministic **384-dimensional** feature-hashing vectorizer (`sha256` unigrams + `md5` bigrams) weighted by `CLINICAL_LEXICON` (English, Bengali, Hindi tokens).
   - Composite score: `0.60 * max_sim + 0.25 * top_3_avg + 0.15 * centroid_sim`, boosted by matched symptom tokens and reference word overlap.

### 1.4 Severity Predictor (`0–100` Score & Level `1–5`)
Source: [`ai_service/app/classifiers/severity_predictor.py`](ai_service/app/classifiers/severity_predictor.py)
- If `GEMINI_API_KEY` is set, calls legacy `google.generativeai.GenerativeModel` with an AHA ACLS/ATLS + Manchester Triage prompt requesting JSON.
- On missing key or error, runs `_predict_clinical_rules` across 9 priority branches (cardiac, respiratory/anaphylaxis/drowning, arterial bleeding, road accident, fire/gas leak, stroke/seizure, fractures/burns, crime, minor injuries, default):
  - Computes `life_threat`, `time_sensitivity`, `casualty_risk`, `environmental_hazard` in `[0, 100]`.
  - If `life_threat >= 85` or `time_sensitivity >= 85`: `score = 0.55*LT + 0.35*TS + 0.05*CR + 0.05*EH`.
  - Else: `score = 0.45*LT + 0.30*TS + 0.15*CR + 0.10*EH`.
  - Maps score to Level `5` (`>=80`, radius `3–5 km`, `auto_call=True`), Level `4` (`50–79`, radius `2–3 km`), Level `3` (`20–49`, radius `1–2 km`), Level `2` (`10–19`, `0.8 km`), Level `1` (`<10`, `0.5 km`).

### 1.5 LangGraph Emergency Crisis Agent & Clinical Guardrails
Source: [`gemini_agent.py`](ai_service/app/agent/gemini_agent.py), [`guardrails.py`](ai_service/app/agent/guardrails.py), [`knowledge.py`](ai_service/app/agent/knowledge.py), [`state.py`](ai_service/app/agent/state.py)

The compiled `StateGraph(EmergencyAgentState)` executes:
1. **`triage_node`**: Auto-detects `condition_id` if unset/unknown; detects `"handover"` / `"paramedic"` / `"ambulance arrived"` to switch `triage_state = "HANDOVER"`.
2. **`protocol_retriever_node`**: Loads `GROUNDED_PROTOCOLS[condition_id]` (8 conditions with AHA, IRC, WHO, GINA, ILAE, ASA, BBA, ATLS/NDMA, and Section 134A MV Act citations) and sets `cpr_bpm = 110` for `cardiac_arrest`.
3. **`safety_guardrail_node`**: Runs `evaluate_contraindications()` against 6 condition/keyword rules (`NO_ORAL_FLUIDS_UNCONSCIOUS`, `NO_MOVING_SPINAL_TRAUMA`, `NO_OBJECTS_IN_SEIZURE_MOUTH`, `NO_ICE_OR_PASTE_ON_BURNS`, `DO_NOT_STOP_CPR_FOR_RIBS`, `NO_REMOVING_IMPALED_OBJECT`).
4. **Conditional Routing (`route_after_guardrails`)**:
   - **`handover_node`**: Builds `ClinicalHandoverSummary` with a `SHA256` audit hash. **Note:** `victim_profile` (`"Rajesh Sengupta"`, age `54`, `O+`), `emergency_location`, `cpr_compressions_estimated=330`, and `destination_hospital` are currently hardcoded demo values!
   - **`reasoning_and_response_node`**: Executes in strict order:
     1. `HallucinationGuardrails.inspect_query()` (regex checks for surgical incisions, field tracheotomy, removing impaled objects, setting bones, sucking venom, sedatives, unverified antibiotics, aspirin in stroke/trauma, inducing emesis for corrosives, oral fluids to unconscious, objects in seizure mouth).
     2. Flagged `contraindications` override.
     3. Deterministic keyword rules (water, compression depth, AED, rib cracking, Section 134A legal shield, burns, bleeding, seizures, choking, fractures, stroke, snakebite, asthma, heatstroke, poison).
     4. Live Gemini call (`google.genai` `self.model_name`, fallback `"gemini-1.5-flash"`) followed by `HallucinationGuardrails.sanitize_llm_response()` (which prepends a critical safety notice if forbidden patterns appear without `"not"`/`"never"`/`"prohibit"`/`"avoid"`).
     5. Default fallback to the active protocol step text.

---

## 2. Backend `AIClient` Fallbacks (`backend/app/services/ai_client.py`)

When `ai_service` (`:8001`) is unreachable or returns non-200 (10-second timeout), `AIClient` runs local fallbacks:
- `_local_fallback_triage` & `_local_fallback_severity`: Rule-based classifiers/scorers.
- `_local_fallback_agent_chat`: Maintains process-local `_SESSION_MEMORY` per `session_id` across 27 detected conditions (including non-acute conditions such as `menstrual_health`, `gastrointestinal`, `anxiety`, `dental`, `sprain_strain`, `minor_wound`, `allergy`, `headache`, `fever`).
  - **Bug Observed During Testing:** When `settings.GEMINI_API_KEY` is configured, `_local_fallback_agent_chat` calls Gemini (`["gemini-3.5-flash-lite", settings.GEMINI_MODEL, "gemini-3.8-flash"]`) **before** running its deterministic contraindication and CPR-depth checks. This caused live Gemini responses to bypass `ContraindicationAlert` object emission and fail 3 tests in `backend/tests/test_ai_agent.py`.
- `_local_fallback_handover`: Also uses the hardcoded `"Rajesh Sengupta"` demo payload.

---

## 3. Android Direct-Gemini & Local Fallback (`AiAgentRepository.kt`)

In [`AiAgentRepository.kt`](android/app/src/main/java/com/example/nearhelp/data/repository/AiAgentRepository.kt):
1. `chatWithAgent()` checks `tokenStorage?.getGeminiApiKey()?.takeIf { it.isNotBlank() }`. When a user-supplied Gemini key is present in `TokenStorage`, it calls `generativelanguage.googleapis.com/v1beta/models/{gemini-3.5-flash-lite, gemini-3.8-flash}:generateContent` **directly from Android**, bypassing the backend and `ai_service` LangGraph guardrails.
2. Only if the direct call fails does it call `POST /api/v1/ai/agent/chat`.
3. If the server returns a generic triage phrase for a non-cardiac query, `AiAgentRepository` overrides it with its own Kotlin 27-condition clinical knowledge base.
