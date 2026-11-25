# 7. AI Processing Pipeline

NearHelp AI implements a multi-tier AI architecture across the **Android client**, **Backend proxy (`AIClient`)**, and **AI microservice (`ai_service`)**.

## 7.1 LangGraph Emergency Agent Pipeline (`ai_service`)

```mermaid
flowchart TD
    Req["POST /api/v1/agent/chat or WS /ws/ai/chat"] --> Triage["Node 1: triage_node (Auto-detect condition, severity, handover intent)"]
    Triage --> Retriever["Node 2: protocol_retriever_node (Lookup GROUNDED_PROTOCOLS, CPR BPM, citations)"]
    Retriever --> Guard["Node 3: safety_guardrail_node (evaluate_contraindications keyword/condition rules)"]
    Guard --> Branch{"triage_state == HANDOVER?"}
    Branch -->|"Yes"| Handover["Node 5: handover_node (Generate summary + SHA256 signature)"]
    Branch -->|"No"| Reason["Node 4: reasoning_and_response_node"]

    subgraph ReasonDetail["reasoning_and_response_node Execution Order"]
        direction TB
        PreCheck["1. HallucinationGuardrails.inspect_query (regex surgical/med/fluid checks)"]
        ContraCheck["2. Check flagged contraindications"]
        RuleMatch["3. Deterministic keyword rules (CPR depth, AED, burns, stroke, etc.)"]
        GeminiCall["4. Gemini API call (google.genai) if client initialized"]
        PostSanitize["5. HallucinationGuardrails.sanitize_llm_response"]
        StepDefault["6. Fallback to current protocol step text"]

        PreCheck -->|"Blocked"| ReturnOverride["Return Contraindication Override"]
        PreCheck -->|"Passed"| ContraCheck
        ContraCheck -->|"Flagged"| ReturnContra["Return Contraindication Directive"]
        ContraCheck -->|"None"| RuleMatch
        RuleMatch -->|"Matched"| ReturnRule["Return Grounded Rule Reply"]
        RuleMatch -->|"Unmatched"| GeminiCall
        GeminiCall -->|"Success"| PostSanitize --> ReturnLLM["Return Sanitized Reply"]
        GeminiCall -->|"Unavailable/Error"| StepDefault --> ReturnStep["Return Protocol Step"]
    end

    Reason --> ReasonDetail
    Handover --> EndNode(["END -> AgentChatResponse"])
    ReasonDetail --> EndNode
```

## 7.2 Three-Tier Request Routing & Fallback Chain (Android Chat)

```mermaid
flowchart LR
    Mobile["Android AiCrisisAssistantViewModel"] --> Repo["AiAgentRepository.chatWithAgent"]
    Repo -->|"1. Optional user key in TokenStorage"| DirectGemini["Direct OkHttp to Gemini REST API (Bypasses LangGraph!)"]
    DirectGemini -->|"Success"| Reply["Render AgentChatResponseDto"]
    DirectGemini -->|"Failure"| BackendProxy["2. POST /api/v1/ai/agent/chat"]
    BackendProxy --> AISvc["3. HTTP to ai_service:8001/api/v1/agent/chat"]
    AISvc -->|"Success"| BackendCheck{"Generic triage reply?"}
    AISvc -->|"Timeout / Offline"| BackendFallback["Backend AIClient._local_fallback_agent_chat (Direct Gemini or 27-condition rules)"]
    BackendFallback --> BackendCheck
    BackendCheck -->|"No"| Reply
    BackendCheck -->|"Yes (non-cardiac)"| LocalFallback["4. Android Local Knowledge Base Fallback"]
    LocalFallback --> Reply
```

## 7.3 Multimodal Classification & Severity Pipeline (`ai_service`)

```mermaid
flowchart LR
    Input["ClassificationRequest (text, audio_base64, image_base64)"] --> Text["Text input"]
    Input -->|"audio_base64"| STT["SpeechService (Gemini audio or mock fallback)"]
    Input -->|"image_base64"| Vision["VisionService (Gemini vision or mock fallback)"]
    Text --> Merge["Combined text"]
    STT --> Merge
    Vision --> Merge
    Merge --> Embed["EmbeddingService (Gemini text-embedding-004 OR 384-dim ClinicalSemanticVectorizer)"]
    Embed --> Sim["Cosine Similarity (0.60*max + 0.25*top3 + 0.15*centroid) + Symptom/Lexicon Boost"]
    Sim --> Out["ClassificationResponse (emergency_type, sub_type, severity_level, skills, radius)"]
```
