"""NearHelp AI — Resilient HTTP Client for AI Microservice."""

import logging
import re
import time

import httpx

from app.core.config import settings
from app.schemas.ai import (
    AgentChatRequest,
    AgentChatResponse,
    CitationItem,
    ClassificationRequest,
    ClassificationResponse,
    ClinicalHandoverSummary,
    ContraindicationAlert,
    GroundedProtocolResponse,
    ProtocolStepItem,
    RAGQueryRequest,
    RAGQueryResponse,
    RAGSearchRequest,
    RAGSearchResponse,
    RAGStatsResponse,
    RetrievedPassageResponse,
    SeverityRequest,
    SeverityResponse,
    SeverityScoreFactors,
    TaxonomyResponse,
)

logger = logging.getLogger(__name__)

# In-memory session tracking cache to preserve conversation context across multi-turn queries
_SESSION_MEMORY: dict[str, dict] = {}


def _detect_condition(q_lower: str) -> str:
    """Clinical heuristic to classify emergency or health query into a condition identifier."""
    # 1. Menstrual health & dysmenorrhea
    if any(k in q_lower for k in ["period", "periods", "menstrua", "cramp", "dysmenorrhea", "pms", "uterus", "vagina", "ovary", "মাসিক"]):
        return "menstrual_health"
    # 2. Gastrointestinal relief & food poisoning (checked before generic poison)
    if any(k in q_lower for k in [
        "food poison", "stomach", "abdomen", "tummy", "belly", "nausea", "vomit",
        "diarrhea", "loose motion", "acidity", "gastric", "indigestion", "heartburn", "bloating", "পেট"
    ]):
        return "gastrointestinal"
    # 3. Anxiety & acute panic
    if any(k in q_lower for k in ["panic", "anxiety", "hyperventilat", "nervous", "palpitation", "scared", "fear"]):
        return "anxiety"
    # 4. Dental emergency & oral pain
    if any(k in q_lower for k in ["tooth", "teeth", "toothache", "gum pain", "gum bleed"]) or ("dental" in q_lower and "accidental" not in q_lower):
        return "dental"
    # 5. Sprains, strains & joint trauma
    if any(k in q_lower for k in ["sprain", "twisted ankle", "twisted", "ligament", "swollen ankle", "joint pain", "ankle pain", "wrist pain"]):
        return "sprain_strain"
    # 6. Minor cuts, abrasions & scrapes
    if any(k in q_lower for k in ["minor cut", "scrape", "scratch", "small cut", "paper cut", "abrasion"]):
        return "minor_wound"
    # 7. Allergies, hives & skin rash
    if any(k in q_lower for k in ["allergy", "allergic", "hives", "rash", "itching", "urticaria", "skin rash"]):
        return "allergy"
    # 8. Thermal & chemical burns
    if any(k in q_lower for k in ["burn", "fire", "scald", "hot water", "hot oil", "acid burn", "পোড়া"]):
        return "burns"
    # 9. Severe bleeding / hemorrhage
    if any(k in q_lower for k in ["bleed", "tourniquet", "blood", "hemorrhage", "gushing", "arterial", "রক্ত"]):
        return "severe_bleeding"
    # 10. Choking & foreign object in airway
    if any(k in q_lower for k in ["chok", "heimlich", "food stuck", "throat", "cant breathe", "গলায়"]):
        return "choking"
    # 11. Fracture & broken bones
    if any(k in q_lower for k in ["fracture", "broken bone", "broken leg", "broken arm", "splint", "ভাঙা", "হাড়"]):
        return "leg_fracture"
    # 12. Seizures & convulsions
    if any(k in q_lower for k in ["seizure", "mouth", "spoon", "froth", "fit", "convulsion", "epilep", "খিঁচুনি"]):
        return "seizures"
    # 13. Stroke (FAST)
    if any(k in q_lower for k in ["stroke", "face drop", "slurred", "arm weak", "paralysis", "মুখ বাকা"]):
        return "stroke"
    # 14. Asthma & acute bronchospasm
    if any(k in q_lower for k in ["asthma", "inhaler", "wheez", "breathlessness", "শ্বাসকষ্ট"]):
        return "asthma"
    # 15. Heatstroke & heat exhaustion
    if any(k in q_lower for k in ["heatstroke", "sunstroke", "heat stroke", "heat exhaustion", "sun stroke", "heat wave", "extreme heat", "লু"]):
        return "heatstroke"
    # 16. Venomous snakebite
    if any(k in q_lower for k in ["snake", "bite", "venom", "cobra", "viper", "krait", "সাপ", "কামড়"]):
        return "snakebite"
    # 17. Animal bite / rabies
    if any(k in q_lower for k in ["dog", "animal bite", "cat bite", "rabies", "monkey bite", "জলাতঙ্ক"]):
        return "animal_bite"
    # 18. Poisoning & chemical ingestion (non-food)
    if any(k in q_lower for k in ["poison", "toxic", "chemical", "swallowed cleaner", "pesticide", "বিষ"]):
        return "poisoning"
    # 19. Electrical shock
    if any(k in q_lower for k in ["electric", "current", "wire", "electrocution", "বিদ্যুৎ"]):
        return "electric_shock"
    # 20. Drowning
    if any(k in q_lower for k in ["drown", "submerged", "pool", "river", "ডুবে"]):
        return "drowning"
    # 21. Diabetic emergency
    if any(k in q_lower for k in ["diabet", "sugar", "insulin", "hypoglycemia", "ডায়াবেটিস"]):
        return "diabetic_emergency"
    # 22. Cardiac arrest / CPR (priority check for respiratory/cardiac cessation)
    if any(k in q_lower for k in ["cpr", "compress", "cardiac", "heart", "chest pain", "bpm", "aed", "defibrillator", "pulse", "not breathing"]):
        return "cardiac_arrest"
    # 23. Fainting & syncope
    if any(k in q_lower for k in ["faint", "syncope", "dizzy", "passed out", "অজ্ঞান"]):
        return "fainting"
    # 24. Eye trauma / chemical splash
    if any(k in q_lower for k in ["eye", "cornea", "splash in eye", "চোখ"]):
        return "eye_trauma"
    # 25. Nosebleed / epistaxis
    if any(k in q_lower for k in ["nosebleed", "nose bleed", "epistaxis", "নাক দিয়ে রক্ত"]):
        return "nosebleed"
    # 26. Headache & migraine
    if any(k in q_lower for k in ["headache", "migraine", "head pain", "cluster headache", "মাথা"]):
        return "headache"
    # 27. Fever & viral chills
    if any(k in q_lower for k in ["fever", "high temp", "temperature", "chills", "shivering", "common cold", "cough and cold", "flu", "জ্বর"]) or (q_lower.startswith("cold") and "water" not in q_lower and "drink" not in q_lower and "compress" not in q_lower):
        return "fever"
    return "general_emergency"


class AIClient:
    """Resilient client communicating with NearHelp AI microservice with offline triage fallback."""

    def __init__(self, base_url: str | None = None, timeout_seconds: float = 10.0):
        self.base_url = (base_url or settings.AI_SERVICE_URL).rstrip("/")
        self.timeout = timeout_seconds

    async def classify(self, request: ClassificationRequest) -> ClassificationResponse:
        """Call AI microservice to classify emergency, falling back to local clinical rule engine on network failure."""
        start_time = time.perf_counter()
        target_url = f"{self.base_url}/api/v1/classify"

        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.post(
                    target_url,
                    json=request.model_dump(exclude_none=True),
                )
                if resp.status_code == 200:
                    data = resp.json()
                    return ClassificationResponse(**data)
                else:
                    logger.warning(
                        "AI service responded with HTTP %d: %s. Falling back to local triage.",
                        resp.status_code,
                        resp.text,
                    )
        except Exception as e:
            logger.warning(
                "Unable to reach AI microservice at %s (%s). Engaging local clinical triage fallback.",
                target_url,
                e,
            )

        # Resilient Offline / Emergency Fallback
        return self._local_fallback_triage(request, (time.perf_counter() - start_time) * 1000.0)

    async def predict_severity(self, request: SeverityRequest) -> SeverityResponse:
        """Call AI microservice to predict emergency severity, with local clinical fallback on failure."""
        start_time = time.perf_counter()
        target_url = f"{self.base_url}/api/v1/severity"

        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.post(
                    target_url,
                    json=request.model_dump(exclude_none=True),
                )
                if resp.status_code == 200:
                    data = resp.json()
                    return SeverityResponse(**data)
                else:
                    logger.warning(
                        "AI service severity responded with HTTP %d: %s. Falling back to local severity triage.",
                        resp.status_code,
                        resp.text,
                    )
        except Exception as e:
            logger.warning(
                "Unable to reach AI microservice at %s (%s). Engaging local severity fallback.",
                target_url,
                e,
            )

        # Resilient Offline / Severity Fallback
        return self._local_fallback_severity(request, (time.perf_counter() - start_time) * 1000.0)

    async def get_taxonomy(self) -> TaxonomyResponse:
        """Fetch crisis taxonomy from AI service or return local standard taxonomy."""
        target_url = f"{self.base_url}/api/v1/taxonomy"
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.get(target_url)
                if resp.status_code == 200:
                    return TaxonomyResponse(**resp.json())
        except Exception as e:
            logger.warning("Failed to fetch taxonomy from AI service: %s. Using default.", e)

        # Default standard taxonomy
        return self._local_fallback_taxonomy()

    async def agent_chat(self, request: AgentChatRequest) -> AgentChatResponse:
        """Call AI service for LangGraph agent chat turn with local clinical fallback."""
        start_time = time.perf_counter()
        target_url = f"{self.base_url}/api/v1/agent/chat"

        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.post(
                    target_url,
                    json=request.model_dump(exclude_none=True),
                )
                if resp.status_code == 200:
                    return AgentChatResponse(**resp.json())
                else:
                    logger.warning("AI service agent chat responded HTTP %d: %s", resp.status_code, resp.text)
        except Exception as e:
            logger.warning("Unable to reach AI service at %s (%s). Engaging local agent fallback.", target_url, e)

        return await self._local_fallback_agent_chat(request, (time.perf_counter() - start_time) * 1000.0)

    async def get_protocols(self) -> list[GroundedProtocolResponse]:
        """Fetch all grounded protocols from AI service or local catalog."""
        target_url = f"{self.base_url}/api/v1/agent/protocols"
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.get(target_url)
                if resp.status_code == 200:
                    return [GroundedProtocolResponse(**p) for p in resp.json()]
        except Exception as e:
            logger.warning("Failed to fetch protocols from AI service: %s. Using local catalog.", e)

        return self._local_fallback_protocols()

    async def get_protocol(self, condition_id: str) -> GroundedProtocolResponse:
        """Fetch specific protocol by condition ID."""
        target_url = f"{self.base_url}/api/v1/agent/protocols/{condition_id}"
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.get(target_url)
                if resp.status_code == 200:
                    return GroundedProtocolResponse(**resp.json())
        except Exception as e:
            logger.warning("Failed to fetch protocol '%s': %s. Using local catalog.", condition_id, e)

        protos = self._local_fallback_protocols()
        for p in protos:
            if p.condition_id == condition_id:
                return p
        return protos[0]

    async def generate_handover(self, request: AgentChatRequest) -> ClinicalHandoverSummary:
        """Generate clinical handover summary from AI service or local fallback."""
        target_url = f"{self.base_url}/api/v1/agent/handover"
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.post(
                    target_url,
                    json=request.model_dump(exclude_none=True),
                )
                if resp.status_code == 200:
                    return ClinicalHandoverSummary(**resp.json())
        except Exception as e:
            logger.warning("Failed to generate handover from AI service: %s. Using local summary.", e)

        return self._local_fallback_handover(request)

    # ==========================================================================
    # RAG KNOWLEDGE BASE PROXY METHODS (MODULE 11)
    # ==========================================================================

    async def rag_search(self, request: RAGSearchRequest) -> RAGSearchResponse:
        """Search protocol vector store with local fallback."""
        start_time = time.perf_counter()
        target_url = f"{self.base_url}/api/v1/rag/search"
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.post(
                    target_url,
                    json=request.model_dump(exclude_none=True),
                )
                if resp.status_code == 200:
                    return RAGSearchResponse(**resp.json())
        except Exception as e:
            logger.warning("RAG search call to AI service failed: %s. Using local search fallback.", e)

        return self._local_fallback_rag_search(request, (time.perf_counter() - start_time) * 1000.0)

    async def rag_query(self, request: RAGQueryRequest) -> RAGQueryResponse:
        """Execute end-to-end RAG grounded guidance query with local fallback."""
        start_time = time.perf_counter()
        target_url = f"{self.base_url}/api/v1/rag/query"
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.post(
                    target_url,
                    json=request.model_dump(exclude_none=True),
                )
                if resp.status_code == 200:
                    return RAGQueryResponse(**resp.json())
        except Exception as e:
            logger.warning("RAG query call to AI service failed: %s. Using local query fallback.", e)

        return self._local_fallback_rag_query(request, (time.perf_counter() - start_time) * 1000.0)

    async def get_rag_stats(self) -> RAGStatsResponse:
        """Fetch RAG vector store statistics."""
        target_url = f"{self.base_url}/api/v1/rag/stats"
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.get(target_url)
                if resp.status_code == 200:
                    return RAGStatsResponse(**resp.json())
        except Exception as e:
            logger.warning("Failed to fetch RAG stats from AI service: %s. Using default stats.", e)

        return RAGStatsResponse(
            collection_name="nearhelp_first_aid_rag",
            total_chunks=35,
            vector_store="ChromaDB (Local Fallback)",
            embedding_dimension=384,
            is_initialized=True,
            persist_directory="./data/chroma_db",
        )

    def _local_fallback_rag_search(self, request: RAGSearchRequest, latency_ms: float) -> RAGSearchResponse:
        """Local fallback for RAG search."""
        q_lower = request.query.lower()
        passages: list[RetrievedPassageResponse] = []

        if any(k in q_lower for k in ["bleed", "blood", "tourniquet", "pressure"]):
            passages.append(
                RetrievedPassageResponse(
                    chunk_id="who_bleed_01",
                    title="WHO Severe Bleeding & Hemorrhage Control",
                    content="Apply firm continuous direct pressure over the bleeding site with clean cloth. For arterial limb spurting, deploy tourniquet 2-3 inches above wound.",
                    condition_id="severe_bleeding",
                    condition_label="Severe Bleeding & Hemorrhagic Shock",
                    step_number=1,
                    similarity_score=0.92,
                    confidence_score=0.95,
                    is_contraindication=False,
                    citation=CitationItem(
                        source="WHO Emergency Trauma Care & Stop The Bleed Protocol",
                        section="Guideline 4.1: Direct Pressure & Tourniquet Protocol",
                        guideline_name="WHO Essential Trauma Care",
                        authority="World Health Organization (WHO)",
                    ),
                    warning_note="Do not remove soaked dressings; layer additional cloths on top.",
                    cpr_bpm=None,
                    legal_shield="Section 134A Motor Vehicles (Amendment) Act 2019",
                )
            )
        elif any(k in q_lower for k in ["snake", "bite", "venom", "snakebite"]):
            passages.append(
                RetrievedPassageResponse(
                    chunk_id="rc_snake_01",
                    title="National Snakebite Protocol (India's Big Four)",
                    content="Enforce strict immobilization. Apply Pressure Immobilization Technique (PIT) with snug crepe bandage and rigid splint. Transfer immediately to hospital with Polyvalent ASV.",
                    condition_id="snakebite",
                    condition_label="Venomous Snakebite",
                    step_number=1,
                    similarity_score=0.94,
                    confidence_score=0.96,
                    is_contraindication=False,
                    citation=CitationItem(
                        source="Indian Red Cross Society & MoHFW",
                        section="MoHFW Protocol §3: Pre-Hospital Envenomation Protocol",
                        guideline_name="National Snakebite Management Protocol",
                        authority="Indian Red Cross Society & MoHFW, Govt of India",
                    ),
                    warning_note="Strictly NO arterial tourniquets, NO incisions, NO suction.",
                    cpr_bpm=None,
                    legal_shield="Section 134A Motor Vehicles (Amendment) Act 2019",
                )
            )
        else:
            passages.append(
                RetrievedPassageResponse(
                    chunk_id="aha_cpr_01",
                    title="AHA Adult Basic Life Support (BLS) Protocol",
                    content="Check responsiveness. Call 108 and send for AED. Begin rhythmic chest compressions at 110 BPM cadence, 2-2.4 inches deep in center of chest.",
                    condition_id="cardiac_arrest",
                    condition_label="Out-of-Hospital Cardiac Arrest",
                    step_number=1,
                    similarity_score=0.88,
                    confidence_score=0.91,
                    is_contraindication=False,
                    citation=CitationItem(
                        source="AHA Guidelines for CPR and ECC 2020",
                        section="Part 3: Adult Basic Life Support §3.2",
                        guideline_name="2020 AHA Guidelines for CPR",
                        authority="American Heart Association (AHA)",
                    ),
                    warning_note="Minimize compression interruptions to under 10 seconds.",
                    cpr_bpm=110,
                    legal_shield="Section 134A Motor Vehicles (Amendment) Act 2019",
                )
            )

        return RAGSearchResponse(
            query=request.query,
            total_results=len(passages),
            passages=passages,
            latency_ms=round(latency_ms, 2),
        )

    def _local_fallback_rag_query(self, request: RAGQueryRequest, latency_ms: float) -> RAGQueryResponse:
        """Local fallback for RAG query answering."""
        search_res = self._local_fallback_rag_search(
            RAGSearchRequest(query=request.query, condition_id=request.condition_id),
            latency_ms,
        )
        p = search_res.passages[0]
        answer = f"✅ {p.title.upper()}\n\n{p.content}\n\n[Source: {p.citation.source} • {p.citation.section}]"

        return RAGQueryResponse(
            query=request.query,
            answer=answer,
            highlight_tag="Grounded Protocol Step",
            citations=[p.citation],
            contraindications=[],
            grounded_passages=search_res.passages,
            is_safe=True,
            latency_ms=round(latency_ms, 2),
        )

    async def _local_fallback_agent_chat(self, request: AgentChatRequest, latency_ms: float) -> AgentChatResponse:
        """Local fallback for bystander Q&A chat turn with multi-turn session memory, contraindication and citation enforcement."""
        q_lower = request.text.lower()
        session_id = request.session_id

        # 1. Retrieve or initialize multi-turn session context
        session_data = _SESSION_MEMORY.setdefault(session_id, {
            "condition_id": "general_emergency",
            "turn_count": 0,
            "history": []
        })

        # 2. Determine active condition: preserve active condition for follow-ups in the same session
        prev_condition = session_data.get("condition_id")
        detected = _detect_condition(q_lower)

        acute_switch_conditions = {
            "cardiac_arrest", "severe_bleeding", "choking", "leg_fracture", "seizures",
            "stroke", "snakebite", "animal_bite", "poisoning", "electric_shock",
            "drowning", "head_injury", "burns", "anaphylaxis", "fainting", "asthma",
            "heatstroke", "diabetic_emergency"
        }

        if prev_condition and prev_condition != "general_emergency":
            if detected in acute_switch_conditions and detected != prev_condition:
                active_condition = detected
                session_data["condition_id"] = detected
            else:
                active_condition = prev_condition
        elif detected != "general_emergency":
            active_condition = detected
            session_data["condition_id"] = detected
        else:
            active_condition = "general_emergency"

        # 3. Base citations
        citations = [
            CitationItem(
                source="AHA Guidelines for CPR and ECC 2020",
                section="Part 3: Adult Basic Life Support §3.2",
                guideline_name="2020 AHA Guidelines for CPR",
                authority="American Heart Association (AHA)",
            ),
            CitationItem(
                source="Motor Vehicles (Amendment) Act 2019",
                section="Section 134A & Supreme Court WP(Civil) 235/2012",
                guideline_name="Protection of Good Samaritans from Liability",
                authority="Ministry of Road Transport & Highways, Govt of India",
            ),
        ]
        contraindications = []

        # 4. Direct Gemini Generative AI Integration when GEMINI_API_KEY is configured
        gemini_replied = False
        reply = ""
        highlight = "Clinical Triage Advisory"

        if settings.GEMINI_API_KEY and settings.GEMINI_API_KEY != "your_gemini_api_key_here":
            try:
                from google import genai

                gemini_client = genai.Client(api_key=settings.GEMINI_API_KEY)
                system_prompt = (
                    "You are NearHelp AI, an emergency crisis and clinical first-aid assistant providing real-time evidence-based guidance. "
                    "Rules: 1. Keep guidance direct, empathetic, actionable, and concise (under 4-5 sentences). "
                    "2. Enforce evidence citations in square brackets like [Source: WHO Guidelines] or [Source: AHA CPR Guidelines 2020 §3.2] or [Source: Section 134A MV Act 2019]. "
                    "3. Enforce strict safety contraindications. "
                    "4. State clear red flags for when to visit a hospital or call 108 emergency dispatch."
                )

                history_context = ""
                if session_data["history"]:
                    history_context = "Previous Conversation:\n" + "\n".join(
                        f"{h['role'].capitalize()}: {h['text']}" for h in session_data["history"][-4:]
                    ) + "\n\n"

                prompt = f"{system_prompt}\n\n{history_context}Active Condition: {active_condition}\nUser Inquiry: {request.text}\nWhat is the immediate actionable clinical first-aid guidance?"
                try:
                    response = gemini_client.models.generate_content(
                        model=settings.GEMINI_MODEL,
                        contents=prompt,
                    )
                except Exception as model_err:
                    logger.warning(
                        "Gemini model '%s' failed (%s). Retrying with 'gemini-1.5-flash'...",
                        settings.GEMINI_MODEL,
                        model_err,
                    )
                    response = gemini_client.models.generate_content(
                        model="gemini-1.5-flash",
                        contents=prompt,
                    )

                if response and response.text:
                    reply = response.text.strip()
                    highlight = "Gemini Clinical Response"
                    gemini_replied = True
            except Exception as e:
                logger.warning("Direct Gemini invocation fallback failed: %s", e)

        # 5. Deterministic Grounded Clinical Engine (Engaged if Gemini is unavailable, blocked, or fails)
        if not gemini_replied:
            is_unconscious_context = any(k in q_lower for k in ["unconscious", "unresponsive", "passed out", "fainted", "not breathing", "gasping", "coma", "অজ্ঞান"]) or active_condition in ["cardiac_arrest", "stroke", "seizures"]
            is_water_query = any(k in q_lower for k in ["water", "liquid", "drink", "milk", "tea", "chai", "jal", "pani", "জল", "পানি"])
            words = set(re.findall(r"\b\w+\b", q_lower))
            is_diet_query = bool(words.intersection({"eat", "eating", "foods", "food", "diet", "snack", "nutrition", "meals"})) or "খাবার" in q_lower

            # A. Life-support CPR and Resuscitation specific inquiries
            if any(k in q_lower for k in ["deep", "compress", "chest", "rate", "fast", "speed", "bpm", "depth"]):
                reply = "✅ Compress 5 to 6 cm (approx 2-2.4 inches) deep at a cadence of 110-120 compressions/minute in the center of the breastbone. Allow complete recoil between compressions.\n\n[Source: AHA CPR Guidelines 2020 §3.2 • IRC BLS 2020 §2]"
                highlight = "AHA / IRC Guideline (110 BPM)"
            elif any(k in q_lower for k in ["aed", "defibrillator", "electrode pad", "shock pad", "automated external"]):
                reply = "⚡ Turn ON the AED immediately. Follow voice prompts and adhere electrode pads to the bare dry chest: Upper right chest below collarbone, Lower left chest below armpit. Stand clear during shock!\n\n[Source: AHA CPR Guidelines 2020 §4.1]"
                highlight = "Immediate AED Action"
            elif any(k in q_lower for k in ["rib", "crack", "pop", "break", "শব্দ"]):
                reply = "⚠️ Costochondral cartilage popping or rib cracking is common during effective adult CPR. DO NOT STOP compressions. Continue CPR immediately; restoring cerebral blood flow is the sole priority.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
                highlight = "Do Not Stop CPR"
            elif any(k in q_lower for k in ["legal", "police", "samaritan", "liability", "law", "court", "আইন"]):
                reply = "🛡️ You are 100% legally protected under Section 134A of the Motor Vehicles (Amendment) Act 2019 and Supreme Court 2016 Good Samaritan Guidelines. You cannot be detained, harassed, or held liable.\n\n[Source: Motor Vehicles (Amendment) Act 2019 Section 134A]"
                highlight = "Section 134A MV Act Shield"
            elif any(k in q_lower for k in ["what can you do", "who are you", "what is nearhelp", "capabilities", "features", "how do you work", "help me"]):
                reply = (
                    "👋 I am NearHelp AI, your real-time Emergency Crisis & Clinical First-Aid Assistant.\n\n"
                    "Here is how I assist in emergencies:\n"
                    "1. 🩺 Real-Time Triage: Rapidly assess symptoms and guide life-saving interventions for Cardiac Arrest, Severe Bleeding, Choking, Stroke, Burns, Fractures, Menstrual Health, and 20+ conditions.\n"
                    "2. 🫀 CPR Rhythm & Audio Metronome: Provide AHA/IRC-grounded chest compression rhythm at 110 BPM.\n"
                    "3. ⚠️ Contraindication Shield: Alert against dangerous mistakes like giving oral liquids to unconscious persons or moving spinal trauma victims.\n"
                    "4. 🛡️ Good Samaritan Legal Protection: Explain statutory immunity under Section 134A of the Motor Vehicles Act.\n"
                    "5. 🚑 Paramedic Handover: Generate digital clinical summaries for 108 ambulance crews upon arrival.\n\n"
                    "[Source: NearHelp Clinical AI & AHA Guidelines 2020]"
                )
                highlight = "NearHelp Emergency Capabilities"

            # B. Oral fluid contraindication: ONLY triggered when unconscious/cardiac/choking
            elif is_water_query and is_unconscious_context:
                reply = "❌ NO. NEVER administer water, oral fluids, or medications to an unconscious or gasping victim. Doing so can cause fatal pulmonary aspiration into the lungs.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
                highlight = "Contraindicated Action"
                contraindications.append(
                    ContraindicationAlert(
                        flag="NO_ORAL_FLUIDS_UNCONSCIOUS",
                        severity="CRITICAL",
                        warning_title="NEVER Give Oral Fluids to Unconscious Person",
                        warning_message="Liquid enters the trachea and causes airway obstruction and pulmonary aspiration.",
                        action_directive="DO NOT give water. Maintain clear airway.",
                    )
                )

            # C. Active Condition Contextual Protocols & Follow-Ups
            elif active_condition == "menstrual_health":
                citations.append(
                    CitationItem(
                        source="ACOG Clinical Practice Guideline No. 345: Dysmenorrhea",
                        section="Management of Primary and Secondary Dysmenorrhea §4",
                        guideline_name="ACOG Clinical Practice Guidelines",
                        authority="American College of Obstetricians and Gynecologists (ACOG)",
                    )
                )
                if any(k in q_lower for k in ["medicine", "tablet", "painkiller", "pill", "meftal", "ibuprofen", "paracetamol", "advil", "crocin", "dolo", "মেডিসিন", "ওষুধ"]):
                    reply = (
                        "💊 Medication Guidance for Menstrual Cramps (Dysmenorrhea):\n\n"
                        "1. First-Line Relief: Over-the-counter NSAIDs like Ibuprofen (400 mg with meals) or Mefenamic Acid (Meftal-Spas) are most effective by inhibiting uterine prostaglandin synthesis.\n"
                        "2. Milder Alternative: Paracetamol (500–650 mg every 4–6 hours) can be taken if stomach is sensitive to NSAIDs.\n"
                        "3. Antispasmodics: Drotaverine or Dicyclomine (under physician guidance) relieves smooth muscle contractions.\n"
                        "⚠️ Caution: Never give aspirin under age 19. If severe pain persists despite medication, consult a gynecologist.\n\n"
                        "[Source: ACOG Dysmenorrhea Guidelines & NHS Women's Health Standards]"
                    )
                    highlight = "Menstrual Pain Medication"
                elif is_water_query or any(k in q_lower for k in ["tea", "chai", "coffee", "milk", "warm drink", "herbal"]):
                    reply = (
                        "☕ Fluids & Hydration for Menstrual Cramps:\n\n"
                        "• Recommended: Drink warm water, chamomile tea, ginger tea, or peppermint tea. Warm liquids increase pelvic blood circulation and relax uterine contractions.\n"
                        "• Electrolytes: Coconut water or warm clear broths help replenish lost minerals and reduce water-retention bloating.\n"
                        "• Avoid: Ice-cold drinks, excessive caffeine (coffee/energy drinks), and alcohol, which constrict blood vessels and amplify cramps.\n\n"
                        "[Source: NHS Women's Health Standards & ACOG Guidelines]"
                    )
                    highlight = "Menstrual Hydration & Teas"
                elif is_diet_query:
                    reply = (
                        "🥗 Nutrition & Foods for Period Cramp Relief:\n\n"
                        "• Beneficial: Bananas and dark leafy greens (rich in potassium and magnesium), oatmeal, dark chocolate (>70%), ginger, and walnuts.\n"
                        "• Reduce: Salty foods, deep-fried snacks, and refined sugars, which cause fluid retention, inflammation, and bloating.\n"
                        "• Small Frequent Meals: Eating lighter meals keeps blood sugar stable and avoids gastrointestinal cramping overlap.\n\n"
                        "[Source: WHO Women's Health & ACOG Nutrition Guidelines]"
                    )
                    highlight = "Menstrual Diet Guidance"
                elif any(k in q_lower for k in ["heat", "heating pad", "warm bag", "hot water", "bottle", "compress", "সেক"]):
                    reply = (
                        "🔥 Heat Therapy for Cramps:\n\n"
                        "• Lower Abdomen / Back: Apply a hot water bottle or electric heating pad (around 40°C / 104°F) for 15–20 minutes at a time.\n"
                        "• How It Works: Continuous heat penetrates abdominal wall muscles, increasing blood flow and relaxing uterine myometrial spasms as effectively as standard painkillers.\n"
                        "• Caution: Wrap heating bottles in a thin towel to avoid thermal skin burns.\n\n"
                        "[Source: Cochrane Review on Dysmenorrhea & ACOG]"
                    )
                    highlight = "Heat Therapy Protocol"
                elif any(k in q_lower for k in [
                    "not work", "doesnt work", "doesn't work", "still pain", "still hurting", "severe pain",
                    "not helping", "didn't help", "didnt help", "what next", "crying", "worse",
                    "not reducing", "not reduce", "not decrease", "not decreasing", "not easing", "not ease",
                    "not going", "not stopping", "unbearable", "cannot bear", "can't bear", "cant bear", "no relief"
                ]):
                    reply = (
                        "🩸 Escalating / Persistent Period Pain Guidance:\n\n"
                        "1. Restful Posture: Lie in fetal position with a pillow tucked between knees to release deep pelvic and lower back ligament tension.\n"
                        "2. Acupressure & Gentle Massage: Apply gentle circular pressure to the lower abdomen using warm essential oils (lavender/clove). Firmly press the SP6 acupressure point (4 finger-widths above the inner ankle bone) for 1–2 minutes.\n"
                        "3. Combined Heat + Medication: Ensure oral NSAID (Ibuprofen 400mg or Meftal-Spas) was taken with a light meal, and maintain continuous lower abdominal heat (40°C).\n"
                        "🚨 Hospital Red Flags: If incapacitating '10/10' pain persists beyond 2 hours, vomiting prevents holding fluids, or she soaks >1 sanitary pad per hour, rule out acute endometriosis, pelvic infection, or ovarian cyst complications and seek urgent clinic care or call 108.\n\n"
                        "[Source: ACOG Dysmenorrhea Guidelines & NHS Women's Health Standards]"
                    )
                    highlight = "Escalating Cramp Relief"
                elif any(k in q_lower for k in ["hospital", "doctor", "clinic", "emergency", "108", "serious", "worry", "ডাক্তার"]):
                    reply = (
                        "🚨 When to Seek Urgent Gynecological / Emergency Care:\n\n"
                        "1. Incapacitating pain ('10/10') that does not lessen after taking NSAIDs.\n"
                        "2. Heavy hemorrhage: Soaking completely through 1 or more sanitary pads/tampons every hour for >2 consecutive hours.\n"
                        "3. Severe symptoms: Sudden fainting, high fever with chills, or severe unilateral (one-sided) pelvic pain (to rule out ectopic pregnancy or ovarian cyst torsion).\n"
                        "• If sudden collapse occurs, dial 108 immediately.\n\n"
                        "[Source: ACOG Dysmenorrhea & Emergency Evaluation Criteria]"
                    )
                    highlight = "Gynecological Red Flags"
                else:
                    reply = (
                        "🩸 Menstrual Cramp & Pain Relief (Dysmenorrhea):\n\n"
                        "1. Heat Therapy: Apply a heating pad or warm water bottle to the lower abdomen or lower back (significantly relaxes uterine smooth muscle contractions).\n"
                        "2. Hydration & Teas: Drink warm water, chamomile, or ginger tea. Avoid caffeine and excessive salt, which worsen water retention and cramping.\n"
                        "3. Restful Posture: Rest in fetal position or practice gentle child's pose to relieve pelvic and lower back tension.\n"
                        "4. Pain Relief: Over-the-counter NSAIDs (like Ibuprofen or Mefenamic acid/Meftal-Spas) taken with food reduce prostaglandin levels and relieve cramping effectively.\n"
                        "⚠️ Warning: If pain is sudden, incapacitating ('10/10'), accompanied by heavy bleeding (soaking >1 pad/hour), high fever, or fainting, seek urgent gynecological evaluation.\n\n"
                        "[Source: ACOG Dysmenorrhea Guidelines & NHS Women's Health Standards]"
                    )
                    highlight = "Menstrual Pain & Cramp Relief"

            elif active_condition == "gastrointestinal":
                citations.append(
                    CitationItem(
                        source="WHO Diarrheal Disease & ACG Clinical Guidelines",
                        section="Management of Acute Gastroenteritis and Dehydration",
                        guideline_name="WHO Clinical Practice Guidelines",
                        authority="World Health Organization (WHO)",
                    )
                )
                if any(k in q_lower for k in ["medicine", "tablet", "pill", "drug", "antacid", "মেডিসিন"]):
                    reply = (
                        "💊 Medication & Symptom Relief for Gastrointestinal Distress:\n\n"
                        "1. Rehydration First: Oral Rehydration Salts (ORS) is the primary medical intervention to prevent electrolyte depletion.\n"
                        "2. Acidity & Heartburn: Antacids (Magnesium/Aluminum hydroxide) or H2-blockers/PPIs reduce gastric burning.\n"
                        "3. Nausea: Domperidone or Ondansetron under medical guidance if vomiting is persistent.\n"
                        "⚠️ Caution: Avoid anti-diarrheal motility blockers (like Loperamide) in feverish food poisoning; allowing pathogens to clear is essential.\n\n"
                        "[Source: WHO Diarrheal Disease & ACG Clinical Guidelines]"
                    )
                    highlight = "GI Medication Guidance"
                elif is_water_query:
                    reply = (
                        "💧 Fluid Hydration for Stomach Upset & Vomiting:\n\n"
                        "• Oral Rehydration Salts (ORS): Mix 1 packet in 1 liter clean water. Sip small quantities (1-2 tablespoons every 5 minutes) rather than gulping.\n"
                        "• Gentle Fluids: Coconut water, diluted rice water, or weak herbal tea help restore cellular potassium and sodium.\n"
                        "• Avoid: Milk, carbonated soda, coffee, and acidic citrus juices that irritate stomach mucosa.\n\n"
                        "[Source: WHO Clinical Management of Gastroenteritis]"
                    )
                    highlight = "GI Rehydration Protocol"
                elif is_diet_query:
                    reply = (
                        "🍌 Diet for Stomach Recovery (BRAT Protocol):\n\n"
                        "• Follow the BRAT Diet: Bananas, Rice (plain boiled), Applesauce, and Toast (plain crackers or bread).\n"
                        "• Transition: Slowly add boiled potatoes, oats, and clear vegetable broths once vomiting has paused for >4 hours.\n"
                        "• Avoid: Oily, deep-fried, heavily spiced foods, and dairy for at least 48 hours.\n\n"
                        "[Source: American Academy of Family Physicians (AAFP)]"
                    )
                    highlight = "BRAT Diet Protocol"
                elif any(k in q_lower for k in ["hospital", "doctor", "clinic", "emergency", "108", "red flag"]):
                    reply = (
                        "🚨 Urgent Medical Evaluation Criteria for Abdominal Pain:\n\n"
                        "1. Blood in vomit (coffee-ground appearance) or dark black tarry stools.\n"
                        "2. Rigid, rock-hard abdomen or localized sharp pain in lower right abdomen (possible appendicitis).\n"
                        "3. Inability to retain any liquids for >12–24 hours with dark urine, extreme dizziness, or confusion.\n"
                        "• If severe continuous pain or faintness occurs, dial 108 immediately.\n\n"
                        "[Source: ACG Acute Abdominal Pain Guidelines]"
                    )
                    highlight = "Abdominal Red Flags"
                else:
                    reply = (
                        "🫄 Gastrointestinal & Food Poisoning Relief:\n\n"
                        "1. Rehydration: Sip Oral Rehydration Salts (ORS) or electrolyte water slowly in small, frequent mouthfuls.\n"
                        "2. Bland Nutrition: Avoid dairy, oily, fried, or spicy foods. Follow the BRAT diet (Bananas, Rice, Applesauce, Toast) once nausea subsides.\n"
                        "3. Upright Rest: Rest with upper body slightly elevated to prevent acid reflux.\n"
                        "⚠️ Warning: If there is blood in vomit or stool, rigid abdominal tightness, high fever, or pain radiating to shoulder/back, seek urgent emergency care.\n\n"
                        "[Source: WHO Diarrheal Disease & ACG Clinical Guidelines]"
                    )
                    highlight = "Gastrointestinal Advisory"

            elif active_condition == "headache":
                if any(k in q_lower for k in ["medicine", "tablet", "pill", "paracetamol"]):
                    reply = (
                        "💊 Medication for Headache Relief:\n\n"
                        "• Paracetamol (500–650 mg for adults) taken with a full glass of water after food.\n"
                        "• Ibuprofen (400 mg) can be used as an alternative NSAID if tolerated and no stomach ulcers exist.\n"
                        "⚠️ Caution: Do not exceed 3000 mg Paracetamol in 24 hours. Avoid frequent repetitive doses to prevent medication-overuse headaches.\n\n"
                        "[Source: International Headache Society (IHS) Guidelines]"
                    )
                    highlight = "Headache Medication"
                else:
                    reply = (
                        "🩺 Headache & Migraine Management:\n\n"
                        "• Sensory Rest: Rest in a dark, quiet, well-ventilated room with minimal screen exposure.\n"
                        "• Hydration & Compress: Drink a large glass of cool water. Apply a cool pack to forehead or warm pack to neck tension points.\n"
                        "• Relief: Paracetamol 500mg or Ibuprofen can be taken with water after food if tolerated.\n"
                        "⚠️ Warning: Seek immediate emergency hospital care for sudden 'thunderclap' headache (worst headache of life), stiff neck with high fever, blurred vision, or speech difficulty.\n\n"
                        "[Source: International Headache Society (IHS) & WHO Clinical Standards]"
                    )
                    highlight = "Headache Clinical Advisory"

            elif active_condition == "fever":
                reply = (
                    "🌡️ Fever & Symptom Care:\n\n"
                    "1. Antipyretic: Take Paracetamol/Acetaminophen (500–650mg for adults every 4–6 hours, max 3000mg/day) to manage discomfort.\n"
                    "2. Hydration: Drink plenty of clean fluids (water, clear broth, coconut water) to prevent dehydration.\n"
                    "3. Cooling: Wear lightweight cotton clothing and use room-temperature water sponge baths if fever exceeds 39°C (102°F). Never use ice water.\n"
                    "⚠️ Warning: Seek hospital care if fever lasts >3 days, is accompanied by stiff neck, purple petechial rash, breathlessness, or persistent vomiting.\n\n"
                    "[Source: CDC & ICMR Fever Management Guidelines]"
                )
                highlight = "Fever Management Protocol"

            elif active_condition == "anxiety":
                reply = (
                    "🧘 Panic & Acute Anxiety Relief:\n\n"
                    "1. 4-7-8 Calming Breath: Inhale through nose for 4 seconds, hold breath for 7 seconds, exhale slowly through mouth for 8 seconds. Repeat 4 times.\n"
                    "2. 5-4-3-2-1 Sensory Grounding: Acknowledge 5 things you see, 4 things you can touch, 3 sounds you hear, 2 scents you smell, and 1 taste.\n"
                    "3. Reassurance: Remind yourself or the person: 'This feeling is distressing, but it will pass and your body is safe.'\n"
                    "⚠️ Warning: If chest pain radiates to arm/jaw or is accompanied by crushing pressure, treat as cardiac emergency and call 108 immediately.\n\n"
                    "[Source: NHS Mental Health Crisis & APA Guidelines]"
                )
                highlight = "Panic & Anxiety Relief"

            elif active_condition == "dental":
                reply = (
                    "🦷 Toothache & Oral Pain First-Aid:\n\n"
                    "1. Warm Salt Water: Rinse mouth gently with warm salt water (1/2 tsp salt in 1 cup warm water) to dislodge debris and reduce inflammation.\n"
                    "2. Cold Pack: Apply a cold compress to the outside of the cheek for 10–15 minutes.\n"
                    "3. Pain Relief: Over-the-counter pain relief (Paracetamol or Ibuprofen) can help. Do NOT place aspirin directly against the gums (causes chemical burns).\n"
                    "⚠️ Warning: If facial swelling spreads to the eye or neck, or difficulty swallowing occurs, seek immediate emergency dental or ENT care.\n\n"
                    "[Source: American Dental Association (ADA) Emergency Standards]"
                )
                highlight = "Toothache First-Aid"

            elif active_condition == "sprain_strain":
                reply = (
                    "🩹 Joint Sprain & Muscle Strain Protocol (R.I.C.E.):\n\n"
                    "1. Rest: Avoid bearing weight on the injured joint or limb immediately.\n"
                    "2. Ice: Apply an ice pack wrapped in a cloth for 15–20 minutes every 2–3 hours (never apply ice directly to bare skin).\n"
                    "3. Compression: Wrap with an elastic crepe bandage snugly, but not tight enough to cut off circulation.\n"
                    "4. Elevation: Prop the limb above heart level on pillows to reduce edema and throbbing.\n"
                    "⚠️ Warning: If the person cannot take 4 steps, hears an audible 'crack', or bone deformity is present, treat as a fracture and seek X-ray evaluation.\n\n"
                    "[Source: American Academy of Orthopaedic Surgeons (AAOS)]"
                )
                highlight = "RICE Sprain Protocol"

            elif active_condition == "minor_wound":
                reply = (
                    "🩹 Minor Cut & Wound Care:\n\n"
                    "1. Cleanse: Rinse the wound thoroughly under clean running tap water for 5 minutes with mild soap to remove debris.\n"
                    "2. Direct Pressure: Press a clean sterile gauze or cloth over the cut for 2–3 minutes until oozing stops.\n"
                    "3. Antiseptic & Dressing: Apply povidone-iodine or antibiotic ointment and cover loosely with a sterile adhesive bandage.\n"
                    "⚠️ Warning: If the cut was caused by dirty/rusty metal, verify tetanus toxoid vaccination status within 48 hours.\n\n"
                    "[Source: British Red Cross & CDC Wound Management Guidelines]"
                )
                highlight = "Minor Wound First-Aid"

            elif active_condition == "allergy":
                reply = (
                    "🌸 Allergic Reaction & Hives First-Aid:\n\n"
                    "1. Antihistamine: If conscious and able to swallow, taking an over-the-counter antihistamine (such as Cetirizine 10 mg) reduces itching and swelling.\n"
                    "2. Cold Compress: Apply a cool, damp washcloth to inflamed, itchy skin.\n"
                    "3. Trigger Removal: Wash skin gently if contact allergen (plants, chemicals, latex) was involved.\n"
                    "🚨 Anaphylaxis Red Flag: If lip/tongue swelling, hoarseness, wheezing, throat tightness, or fainting occurs, deploy epinephrine (EpiPen) immediately and dial 108 without delay.\n\n"
                    "[Source: World Allergy Organization (WAO) Guidelines]"
                )
                highlight = "Allergy & Hives First-Aid"

            elif active_condition == "choking":
                reply = "🚨 Stand behind the victim. Wrap arms around waist. Make a fist just above the navel. Deliver 5 quick, inward and upward abdominal thrusts (Heimlich Maneuver) until the airway clears. If unconscious, lower to floor and start CPR.\n\n[Source: American Red Cross & AHA Choking Guidelines 2020]"
                highlight = "Heimlich / Choking Relief"

            elif active_condition == "burns":
                reply = "💧 Cool the burn immediately under cool running tap water for 20 full minutes. Never apply ice, toothpaste, or turmeric. Cover loosely with clean plastic food wrap or sterile dressing.\n\n[Source: British Burn Association & WHO Burn Trauma Guide 2021]"
                highlight = "Thermal Burn First-Aid"

            elif active_condition == "severe_bleeding":
                reply = "🩸 Expose wound and apply continuous, firm direct pressure with clean gauze/cloth using your body weight. For severe limb bleeding that won't stop, apply a tourniquet 5–7 cm above the wound (never over a joint).\n\n[Source: WHO Trauma Care & Stop The Bleed Protocol §4.1]"
                highlight = "Hemorrhage Control"

            elif active_condition == "leg_fracture":
                reply = "🦴 Support and immobilize the injured limb in the exact position found. DO NOT attempt to push bone back or straighten deformed limbs. Apply an ice pack wrapped in a cloth to control swelling and await 108 dispatch.\n\n[Source: NDMA & ATLS Pre-Hospital Trauma Guidelines]"
                highlight = "Limb Immobilization Protocol"

            elif active_condition == "seizures":
                reply = "🛡️ Protect victim's head with a soft folded jacket and clear hard objects. NEVER insert spoons, fingers, or objects into the mouth. Once shaking stops, roll gently into the recovery position.\n\n[Source: ILAE & NHS Seizure Protocol]"
                highlight = "Seizure Safety"

            elif active_condition == "stroke":
                reply = "🧠 Perform FAST check immediately:\n• F (Face): Ask to smile — does one side droop?\n• A (Arms): Ask to raise both arms — does one drift downward?\n• S (Speech): Ask to repeat a simple sentence — is it slurred?\n• T (Time): Call 108 immediately. Keep victim quiet with head slightly elevated.\n\n[Source: American Stroke Association (ASA) 2019]"
                highlight = "FAST Stroke Assessment"

            elif active_condition == "snakebite":
                reply = "🐍 Keep victim completely calm and still to slow venom circulation. Immobilize the bitten limb at or slightly below heart level with a broad bandage. NEVER cut the wound, suck venom, or apply a tourniquet. Rush to the nearest hospital with Anti-Snake Venom (ASV).\n\n[Source: WHO Guidelines for the Management of Snakebites]"
                highlight = "Snakebite Protocol"

            elif active_condition == "animal_bite":
                reply = "🐕 Wash the bite wound vigorously under running tap water with soap for 15 full minutes immediately. Apply povidone-iodine antiseptic. Never stitch or bandage tightly. Seek immediate hospital care for Anti-Rabies Vaccine (ARV).\n\n[Source: WHO Rabies First-Aid Guidelines]"
                highlight = "Animal Bite / Rabies Prevention"

            elif active_condition == "asthma":
                reply = "🫁 Help the person sit upright leaning slightly forward. Administer 4 separate puffs of their blue reliever inhaler (Salbutamol) with 4 deep breaths after each puff. If no improvement within 4 minutes, deliver 4 more puffs and call 108 immediately.\n\n[Source: Global Initiative for Asthma (GINA) 2023]"
                highlight = "Acute Asthma Relief"

            elif active_condition == "heatstroke":
                reply = "☀️ Move victim to a cool, shaded environment immediately. Remove excess clothing. Apply cool, wet towels to the neck, armpits, and groin while fanning vigorously. If conscious, offer cool water in small sips.\n\n[Source: NDMA Heat Wave Guidelines & Wilderness Medical Society]"
                highlight = "Heat Emergency Management"

            elif active_condition == "poisoning":
                reply = "🧪 DO NOT induce vomiting or administer fluids unless instructed by medical professionals. Keep any container or packaging for paramedic inspection. Check breathing and place in recovery position if drowsy. Call 108 immediately.\n\n[Source: WHO International Programme on Chemical Safety]"
                highlight = "Poisoning Emergency Protocol"

            elif active_condition == "electric_shock":
                reply = "⚡ DO NOT touch victim until power source is disconnected at main breaker or pushed away with dry wood. Check breathing immediately; if unresponsive and not breathing normally, initiate CPR at 110 BPM and call 108.\n\n[Source: OSHA & Red Cross Electrical Safety Protocols]"
                highlight = "Electrical Shock Protocol"

            elif active_condition == "drowning":
                reply = "🌊 Pull victim to dry flat surface. Drowning arrest causes severe hypoxia: deliver 5 initial rescue breaths first, then begin 30:2 compressions and breaths. Wipe chest completely dry before applying AED pads.\n\n[Source: International Lifesaving Federation & AHA Guidelines 2020]"
                highlight = "Water Rescue & Resuscitation"

            elif active_condition == "diabetic_emergency":
                reply = "🍬 If the person is conscious and can swallow, give 15–20g fast-acting sugar (fruit juice, 3 tsp sugar, or glucose). Wait 15 minutes to re-evaluate. If unconscious, NEVER give fluids; place in recovery position and call 108.\n\n[Source: American Diabetes Association Emergency Standards]"
                highlight = "Hypoglycemia Emergency Protocol"

            elif active_condition == "fainting":
                reply = "🛌 Lay the person flat on their back and elevate legs 30 cm (12 inches) to restore blood flow to the brain. Loosen tight collars. If unresponsiveness lasts >1 minute or breathing is abnormal, call 108 immediately.\n\n[Source: Red Cross First-Aid Guidelines]"
                highlight = "Fainting / Syncope Protocol"

            elif active_condition == "eye_trauma":
                reply = "👁️ Flush the eye continuously with clean running water or saline for 15–20 minutes with eyelids held wide open. DO NOT rub the eye or remove embedded objects. Cover loosely and seek immediate ophthalmic evaluation.\n\n[Source: American Academy of Ophthalmology Guidelines]"
                highlight = "Eye Trauma & Chemical Flush"

            elif active_condition == "nosebleed":
                reply = "👃 Sit upright and lean slightly forward (do NOT tilt head back). Pinch the soft part of the nose firmly for 10–15 minutes continuously while breathing through mouth. Apply cold pack to bridge of nose.\n\n[Source: NHS & British Red Cross Epistaxis Guidelines]"
                highlight = "Epistaxis / Nosebleed Protocol"

            elif active_condition == "cardiac_arrest":
                reply = "🫀 Check responsiveness. Call 108 and shout for an AED. Position hands in center of breastbone and begin chest compressions 5-6 cm deep at 110 BPM cadence. Allow complete recoil.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
                highlight = "AHA CPR Protocol"

            # D. Adaptive Triage Fallback for all other unclassified queries
            else:
                is_danger = any(k in q_lower for k in [
                    "unresponsive", "not breathing", "unconscious", "no pulse", "dying",
                    "collapsed", "cardiac", "heart attack", "choking", "massive bleed",
                    "drowning", "electrocution", "head injury", "spine"
                ])
                if is_danger:
                    reply = (
                        "🚨 Emergency Life-Support Protocol (DRABC):\n\n"
                        "1. Danger: Ensure scene is safe before approaching.\n"
                        "2. Response: Tap shoulders firmly and shout 'Are you okay?'.\n"
                        "3. Airway & Breathing: Tilt head, lift chin, check chest rise for 5–10 seconds.\n"
                        "4. Circulation / Dial 108: If unresponsive and not breathing normally, begin 30 chest compressions at 110 BPM and dial 108 immediately.\n\n"
                        "[Source: Indian Resuscitation Council & AHA BLS 2020 §3.2]"
                    )
                    highlight = "Emergency Resuscitation Directive"
                else:
                    reply = (
                        "🩺 Clinical Triage & First-Aid Advisory:\n\n"
                        "1. Assessment: Check vital comfort, breathing, and alertness. Keep the person in a relaxed, well-supported position.\n"
                        "2. Rest & Hydration: Maintain adequate oral hydration with clean water or electrolyte fluids. Avoid strenuous physical movement or heavy food intake.\n"
                        "3. Safety Contraindications: Do NOT take strong unprescribed painkillers or antibiotics without physician diagnosis. Never ignore worsening symptoms.\n"
                        "4. When to Seek Care: Visit a local clinic or consult a physician if symptoms persist or escalate. For acute emergency signs (difficulty breathing, sudden severe pain, loss of consciousness), call 108 immediately.\n\n"
                        "[Source: WHO Clinical Practice Standards & National Health Guidelines]"
                    )
                    highlight = "Clinical Triage Advisory"

        # 6. Record interaction in multi-turn session memory
        session_data["history"].append({"role": "user", "text": request.text})
        session_data["history"].append({"role": "model", "text": reply})
        session_data["turn_count"] = session_data.get("turn_count", 0) + 1
        _SESSION_MEMORY[session_id] = session_data

        is_cardiac = (active_condition == "cardiac_arrest") or any(
            k in q_lower for k in ["cpr", "compress", "cardiac", "heart", "chest pain", "bpm", "aed", "defibrillator", "pulse", "not breathing"]
        )

        quick_questions_map = {
            "menstrual_health": [
                "How to relieve severe period cramps fast?",
                "Can I take Meftal-Spas or Ibuprofen?",
                "What warm drinks help with cramps?",
                "When is period pain an emergency?",
            ],
            "gastrointestinal": [
                "What is the best way to take ORS?",
                "When is stomach pain an emergency (appendicitis)?",
                "What foods should I eat after vomiting?",
                "Can I take antacids safely?",
            ],
            "anxiety": [
                "How to do the 4-7-8 breathing exercise?",
                "How to tell a panic attack from a heart attack?",
                "What is 5-4-3-2-1 grounding?",
                "When should I seek emergency care for panic?",
            ],
            "dental": [
                "How to soothe severe tooth pain at night?",
                "Is warm saltwater rinse effective?",
                "Can I apply clove oil or ice on cheek?",
                "When does a tooth infection become an emergency?",
            ],
            "sprain_strain": [
                "How does the R.I.C.E. method work?",
                "When do I need an X-ray for an ankle sprain?",
                "How tightly should I wrap a crepe bandage?",
                "Can I take painkillers for a sprain?",
            ],
            "minor_wound": [
                "How to clean a cut properly?",
                "When do I need a tetanus shot?",
                "Should I let the cut air out or bandage it?",
                "What are signs of wound infection?",
            ],
            "allergy": [
                "What antihistamines work for hives?",
                "How to tell mild allergy from anaphylaxis?",
                "Can I apply cold compress for itching?",
                "When should I use an EpiPen?",
            ],
            "burns": [
                "Can I apply ice or toothpaste?",
                "How long should I cool under water?",
                "Should I pop blister bubbles?",
                "When do I need emergency hospital care?",
            ],
            "severe_bleeding": [
                "When do I apply a tourniquet?",
                "Should I remove blood-soaked gauze?",
                "How to pack a deep wound cavity?",
                "Am I legally protected if I help?",
            ],
            "choking": [
                "What if the victim is pregnant or a child?",
                "How do I deliver sharp back blows?",
                "What to do if victim loses consciousness?",
                "When and how to start CPR?",
            ],
            "leg_fracture": [
                "Should I straighten a deformed bone?",
                "How to immobilize limb with splint?",
                "Can I apply an ice compress for swelling?",
                "How to check blood flow in toes?",
            ],
            "seizures": [
                "Should I hold the person down?",
                "What if they bite their tongue?",
                "When is a seizure life-threatening (>5 min)?",
                "How to roll into recovery position?",
            ],
            "stroke": [
                "What are the FAST signs of stroke?",
                "Can I give water or blood thinners?",
                "What is the golden window for tPA?",
                "How should I position their head?",
            ],
            "asthma": [
                "How many puffs of inhaler should I give?",
                "Should the patient sit upright or lie down?",
                "How to coach pursed-lip breathing?",
                "When to call 108 emergency dispatch?",
            ],
            "snakebite": [
                "Can I cut the bite or suck venom?",
                "Can I use a tight tourniquet?",
                "Where is anti-snake venom (ASV) available?",
                "How to immobilize the bitten limb?",
            ],
            "animal_bite": [
                "How soon must Anti-Rabies Vaccine be given?",
                "Can I bandage an animal bite tightly?",
                "Should I wash with soap for 15 minutes?",
                "When is tetanus toxoid needed?",
            ],
            "headache": [
                "When is a headache an emergency?",
                "What is a thunderclap headache?",
                "Can I take painkillers safely?",
                "When should I call 108?",
            ],
            "fever": [
                "What are warning signs of high fever?",
                "Can I use cold water sponge baths?",
                "What is the maximum Paracetamol dose?",
                "When should a fever patient visit hospital?",
            ],
            "general_emergency": [
                "What should I check first (DRABC)?",
                "When should I call 108?",
                "Can I give oral fluids?",
                "Am I protected under Section 134A?",
            ],
            "cardiac_arrest": [
                "Can I give water or oral medicine?",
                "How deep should chest compressions be?",
                "When and how do I use the AED?",
                "What if ribs crack during CPR?",
                "Am I legally protected if I help?",
            ],
        }

        suggested_questions = quick_questions_map.get(
            active_condition,
            quick_questions_map["cardiac_arrest"] if is_cardiac else quick_questions_map["general_emergency"]
        )

        severity_level = 5 if (is_cardiac or active_condition in ["cardiac_arrest", "severe_bleeding", "choking", "stroke", "electric_shock", "drowning"]) else 4
        if active_condition in ["burns", "heatstroke", "leg_fracture", "seizures", "asthma", "poisoning", "snakebite", "diabetic_emergency"]:
            severity_level = 4 if active_condition != "burns" else 3
        elif active_condition in ["medical_symptom", "menstrual_health", "anxiety", "dental", "headache", "fever", "sprain_strain", "minor_wound", "allergy", "fainting", "eye_trauma", "nosebleed", "animal_bite"]:
            severity_level = 2
        elif active_condition in ["general_emergency", "gastrointestinal"]:
            severity_level = 3

        priority = "critical" if severity_level == 5 else ("urgent" if severity_level >= 3 else "advisory")

        return AgentChatResponse(
            session_id=request.session_id,
            reply_text=reply,
            highlight_text=highlight,
            triage_state="GUIDANCE",
            condition_id=active_condition,
            severity_level=severity_level,
            priority=priority,
            current_step_index=request.current_step_index,
            completed_steps=request.completed_steps,
            cpr_metronome_active=is_cardiac,
            cpr_bpm=110 if is_cardiac else 0,
            citations=citations,
            contraindications=contraindications,
            legal_shield_applied=True,
            suggested_quick_questions=suggested_questions,
            processing_time_ms=max(0.01, round(latency_ms, 2)),
        )

    def _local_fallback_protocols(self) -> list[GroundedProtocolResponse]:
        """Local standard protocol catalog."""
        return [
            GroundedProtocolResponse(
                condition_id="cardiac_arrest",
                condition_label="Cardiac / Chest Pain",
                crisis_type="medical",
                severity_level=5,
                priority="critical",
                protocol_title="AHA / Indian Resuscitation Council Basic Life Support (BLS) Protocol",
                authority="American Heart Association & Indian Resuscitation Council",
                disclaimers="Emergency interim bystander protocol. Municipal 108 ambulance dispatched.",
                legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
                recommended_radius_km=3.5,
                emergency_number="108",
                cpr_bpm=110,
                steps=[
                    ProtocolStepItem(
                        step_number=1,
                        title="Check Safety & Confirm Unresponsiveness",
                        action_instruction='Ensure scene safety. Tap shoulders and shout "Are you okay?". Check carotid pulse for no more than 10 seconds.',
                        warning_note="If no pulse or victim is gasping, start CPR immediately.",
                        is_cpr_step=False,
                        icon="AlertCircle",
                    ),
                    ProtocolStepItem(
                        step_number=2,
                        title="Begin High-Quality Chest Compressions (110 BPM)",
                        action_instruction="Place heel of hand on center of chest. Interlock fingers. Push hard and fast at depth of 5-6 cm at 110 BPM.",
                        warning_note="Allow full chest recoil after each push.",
                        is_cpr_step=True,
                        beat_bpm=110,
                        icon="HeartPulse",
                    ),
                    ProtocolStepItem(
                        step_number=3,
                        title="Maintain 30:2 Ratio or Continuous Hands-Only CPR",
                        action_instruction="Deliver 30 compressions followed by 2 rescue breaths or provide continuous Hands-Only CPR without stopping.",
                        is_cpr_step=True,
                        beat_bpm=110,
                        icon="Activity",
                    ),
                    ProtocolStepItem(
                        step_number=4,
                        title="Apply Automated External Defibrillator (AED)",
                        action_instruction="Turn ON AED. Adhere electrode pads to bare chest: upper right, lower left. Follow voice prompts.",
                        is_cpr_step=False,
                        icon="Zap",
                    ),
                ],
                citations=[
                    CitationItem(
                        source="AHA Guidelines for CPR and ECC 2020",
                        section="Part 3: Adult Basic Life Support §3.2",
                        guideline_name="2020 AHA CPR Guidelines",
                        authority="American Heart Association",
                    ),
                    CitationItem(
                        source="Motor Vehicles (Amendment) Act 2019",
                        section="Section 134A",
                        guideline_name="Protection of Good Samaritans",
                        authority="Govt of India",
                    ),
                ],
            )
        ]

    def _local_fallback_handover(self, request: AgentChatRequest) -> ClinicalHandoverSummary:
        """Local standard handover report fallback."""
        now_str = time.strftime("%Y-%m-%d %H:%M:%S UTC", time.gmtime())
        rep_id = f"REP-NH-{int(time.time() * 1000) % 1000000:06d}"
        return ClinicalHandoverSummary(
            report_id=rep_id,
            session_id=request.session_id,
            incident_code=f"NH-KOL-{request.session_id[:8].upper()}",
            generated_at=now_str,
            victim_profile={
                "name": "Rajesh Sengupta",
                "age": 54,
                "gender": "Male",
                "blood_type": "O+",
                "allergies": ["Penicillin"],
                "medical_conditions": ["Hypertension"],
                "has_pacemaker": False,
            },
            emergency_location="Godrej Waterside, Tower 1, DP Block, Sector V, Salt Lake City, Kolkata",
            severity_level=5,
            diagnostic_summary="Level 5 — Critical Life Threat (Cardiac Arrest)",
            ai_confidence_score=98.4,
            reported_symptoms=["Unresponsive", "No carotid pulse", "Agonal gasping"],
            cpr_metronome_used=True,
            cpr_compressions_estimated=330,
            cpr_duration_seconds=180,
            aed_deployed=True,
            aed_shocks_delivered=1,
            completed_protocol_steps=["Safety Check Confirmed", "Continuous CPR Delivered"],
            citations=[
                CitationItem(
                    source="AHA Guidelines for CPR and ECC 2020",
                    section="Part 3: Adult Basic Life Support §3.2",
                    guideline_name="2020 AHA CPR Guidelines",
                    authority="American Heart Association",
                )
            ],
            destination_hospital="AMRI Hospital Salt Lake Emergency Trauma Center",
            legal_shield_compliance="Section 134A Motor Vehicles (Amendment) Act 2019",
            digital_signature_hash="SHA256:7f9a2b8c4d1e0f3a6b5c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8",
        )

    def _local_fallback_triage(
        self,
        request: ClassificationRequest,
        latency_ms: float,
    ) -> ClassificationResponse:
        """Heuristic rule-based emergency triage fallback when microservice is offline."""
        raw_text = (request.text or "").lower()

        # Cardiac
        if any(k in raw_text for k in ["cardiac", "chest pain", "heart", "cpr", "unresponsive", "gasping", "বুক", "saans"]):
            return ClassificationResponse(
                emergency_type="medical",
                sub_type="cardiac_arrest",
                priority="critical",
                severity_level=5,
                confidence=0.95,
                confidence_percentage=95.0,
                recommended_radius_km=3.5,
                suggested_responder_skills=["CPR_CERTIFIED", "DOCTOR", "EMT"],
                immediate_action="Begin CPR immediately: compress center of chest 5-6 cm deep at 110-120 BPM. Send for AED.",
                requires_professional=True,
                call_emergency_services=True,
                emergency_number="108",
                detected_symptoms=["Unresponsive", "Agonal Breathing", "Sudden Collapse"],
                transcription=None,
                image_description=None,
                processing_time_ms=round(latency_ms, 2),
            )

        # Bleeding
        if any(k in raw_text for k in ["bleed", "blood", "laceration", "hemorrhage", "রক্ত", "khoon"]):
            return ClassificationResponse(
                emergency_type="medical",
                sub_type="severe_bleeding",
                priority="high",
                severity_level=4,
                confidence=0.92,
                confidence_percentage=92.0,
                recommended_radius_km=2.5,
                suggested_responder_skills=["FIRST_AID", "EMT", "NURSE"],
                immediate_action="Apply firm, direct pressure with clean cloth. Elevate limb if possible.",
                requires_professional=True,
                call_emergency_services=True,
                emergency_number="108",
                detected_symptoms=["Severe Hemorrhage"],
                transcription=None,
                image_description=None,
                processing_time_ms=round(latency_ms, 2),
            )

        # Fire
        if any(k in raw_text for k in ["fire", "smoke", "flame", "আগুন", "aag"]):
            return ClassificationResponse(
                emergency_type="fire",
                sub_type="structural_fire",
                priority="critical",
                severity_level=5,
                confidence=0.94,
                confidence_percentage=94.0,
                recommended_radius_km=3.0,
                suggested_responder_skills=["FIRE_SAFETY", "FIRST_AID"],
                immediate_action="Evacuate immediately via stairwells. Stay low under smoke. Call 101.",
                requires_professional=True,
                call_emergency_services=True,
                emergency_number="101",
                detected_symptoms=["Active Fire / Smoke"],
                transcription=None,
                image_description=None,
                processing_time_ms=round(latency_ms, 2),
            )

        # Default Medical Triage
        return ClassificationResponse(
            emergency_type="medical",
            sub_type="cardiac_arrest",
            priority="critical",
            severity_level=5,
            confidence=0.85,
            confidence_percentage=85.0,
            recommended_radius_km=3.0,
            suggested_responder_skills=["CPR_CERTIFIED", "DOCTOR", "EMT", "FIRST_AID"],
            immediate_action="Check responsiveness and breathing. Call 108 Emergency Ambulance immediately.",
            requires_professional=True,
            call_emergency_services=True,
            emergency_number="108",
            detected_symptoms=["Emergency Distress Reported"],
            transcription=None,
            image_description=None,
            processing_time_ms=round(latency_ms, 2),
        )

    def _local_fallback_taxonomy(self) -> TaxonomyResponse:
        """Local static taxonomy for fallback."""
        from app.schemas.ai import ClinicalConditionItem, CrisisTypeItem

        return TaxonomyResponse(
            crisis_types=[
                CrisisTypeItem(
                    id="medical",
                    name="Medical Emergency",
                    description="Acute medical emergencies and trauma",
                    default_emergency_number="108",
                    sub_types=["cardiac_arrest", "severe_bleeding", "respiratory_asthma", "stroke"],
                ),
                CrisisTypeItem(
                    id="fire",
                    name="Fire Outbreak",
                    description="Building and electrical fires",
                    default_emergency_number="101",
                    sub_types=["structural_fire", "electrical_fire"],
                ),
            ],
            clinical_conditions=[
                ClinicalConditionItem(
                    id="cardiac_arrest",
                    label="Cardiac / Chest Pain",
                    icon_name="HeartPulse",
                    severity=5,
                    priority="critical",
                    description="Sudden collapse, unresponsive, chest pain",
                    symptoms=["Unresponsive", "No pulse", "Agonal breathing"],
                    suggested_skills=["CPR_CERTIFIED", "DOCTOR", "EMT"],
                    immediate_action="Begin CPR immediately at 110-120 BPM.",
                    recommended_radius_km=3.5,
                    emergency_number="108",
                )
            ],
            version="1.0.0",
        )

    def _local_fallback_severity(
        self,
        request: SeverityRequest,
        latency_ms: float,
    ) -> SeverityResponse:
        """Heuristic rule-based emergency severity scoring fallback when microservice is offline."""
        raw_text = (request.text or "").lower()
        sub_type = (request.sub_type or "").lower()

        # Cardiac
        if (
            sub_type == "cardiac_arrest"
            or request.unresponsive is True
            or any(k in raw_text for k in ["cardiac", "chest pain", "heart", "cpr", "unresponsive", "gasping", "বুক", "saans"])
        ):
            return SeverityResponse(
                severity_score=95,
                severity_level=5,
                priority="critical",
                confidence=0.984,
                confidence_percentage=98.4,
                reasoning=[
                    "Unresponsive victim with sudden collapse indicates imminent cardiac arrest.",
                    "Critical 5-minute Platinum Hypoxia Window: irreversible brain damage without continuous CPR.",
                ],
                factors=SeverityScoreFactors(
                    life_threat_score=98.0,
                    time_sensitivity_score=99.0,
                    casualty_risk_score=20.0,
                    environmental_hazard_score=15.0,
                ),
                recommended_radius_km=4.0,
                survival_window_minutes=5,
                auto_call_emergency_services=True,
                suggested_call_action="auto_dial",
                emergency_number="108",
                recommended_actions=[
                    "Begin CPR immediately: compress center of chest 5-6 cm deep at 110-120 BPM.",
                    "Send a bystander immediately for an AED.",
                    "Call 108 Emergency Ambulance.",
                ],
                required_responder_skills=["CPR_CERTIFIED", "DOCTOR", "EMT"],
                processing_time_ms=max(0.01, round(latency_ms, 2)),
            )

        # Bleeding
        if (
            sub_type == "severe_bleeding"
            or request.severe_bleeding is True
            or any(k in raw_text for k in ["bleed", "blood", "laceration", "hemorrhage", "রক্ত", "khoon"])
        ):
            return SeverityResponse(
                severity_score=72,
                severity_level=4,
                priority="high",
                confidence=0.952,
                confidence_percentage=95.2,
                reasoning=[
                    "High-volume active blood loss with impending hypovolemic shock.",
                    "Direct mechanical pressure and tourniquet application indicated.",
                ],
                factors=SeverityScoreFactors(
                    life_threat_score=78.0,
                    time_sensitivity_score=85.0,
                    casualty_risk_score=15.0,
                    environmental_hazard_score=20.0,
                ),
                recommended_radius_km=2.8,
                survival_window_minutes=15,
                auto_call_emergency_services=False,
                suggested_call_action="suggested",
                emergency_number="108",
                recommended_actions=[
                    "Apply firm, continuous direct pressure over wound.",
                    "Apply tourniquet 5 cm above wound if arterial limb bleed does not stop.",
                ],
                required_responder_skills=["FIRST_AID", "EMT", "NURSE"],
                processing_time_ms=max(0.01, round(latency_ms, 2)),
            )

        # Fire / Gas
        if any(k in raw_text for k in ["fire", "smoke", "flame", "gas leak", "lpg", "আগুন", "aag"]):
            is_gas = "gas" in raw_text or "lpg" in raw_text
            return SeverityResponse(
                severity_score=88,
                severity_level=5,
                priority="critical",
                confidence=0.965,
                confidence_percentage=96.5,
                reasoning=[
                    "Active fire or explosive vapor cloud hazard threatening multiple occupants.",
                    "Immediate toxicity and structural hazard require rapid evacuation.",
                ],
                factors=SeverityScoreFactors(
                    life_threat_score=88.0,
                    time_sensitivity_score=90.0,
                    casualty_risk_score=85.0,
                    environmental_hazard_score=92.0,
                ),
                recommended_radius_km=3.5,
                survival_window_minutes=10,
                auto_call_emergency_services=True,
                suggested_call_action="auto_dial",
                emergency_number="101",
                recommended_actions=[
                    "Evacuate immediately via stairwells." if not is_gas else "Open doors/windows. Do not touch electrical switches.",
                    "Call 101 Fire Brigade.",
                ],
                required_responder_skills=["FIRE_SAFETY", "FIRST_AID"],
                processing_time_ms=max(0.01, round(latency_ms, 2)),
            )

        # Default Moderate / High Triage
        return SeverityResponse(
            severity_score=55,
            severity_level=4,
            priority="high",
            confidence=0.910,
            confidence_percentage=91.0,
            reasoning=["Reported acute distress requiring urgent triage and bystander response."],
            factors=SeverityScoreFactors(
                life_threat_score=60.0,
                time_sensitivity_score=65.0,
                casualty_risk_score=20.0,
                environmental_hazard_score=20.0,
            ),
            recommended_radius_km=2.2,
            survival_window_minutes=30,
            auto_call_emergency_services=False,
            suggested_call_action="suggested",
            emergency_number="108",
            recommended_actions=["Assess victim vital signs and call 108 Emergency Ambulance."],
            required_responder_skills=["FIRST_AID", "EMT"],
            processing_time_ms=max(0.01, round(latency_ms, 2)),
        )


ai_client = AIClient()
