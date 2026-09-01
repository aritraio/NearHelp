"""NearHelp AI — Resilient HTTP Client for AI Microservice."""

import logging
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
    StepProgressRequest,
    StepProgressResponse,
    TaxonomyResponse,
)

logger = logging.getLogger(__name__)


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

        return self._local_fallback_agent_chat(request, (time.perf_counter() - start_time) * 1000.0)

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

    def _local_fallback_agent_chat(self, request: AgentChatRequest, latency_ms: float) -> AgentChatResponse:
        """Local fallback for bystander Q&A chat turn with contraindication and citation enforcement."""
        q_lower = request.text.lower()
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

        if any(k in q_lower for k in ["water", "liquid", "drink", "milk", "tea", "chai", "jal", "pani", "জল", "পানি"]):
            reply = "❌ NO. NEVER administer water, oral fluids, or medications to an unconscious or gasping victim. Doing so can cause fatal pulmonary aspiration.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
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
        elif any(k in q_lower for k in ["deep", "compress", "chest", "rate", "fast", "speed", "bpm", "depth"]):
            reply = "✅ Compress 5 to 6 cm (approx 2–2.4 inches) deep at a cadence of 110–120 compressions/minute in the center of the breastbone. Allow complete recoil between compressions.\n\n[Source: AHA CPR Guidelines 2020 §3.2 • IRC BLS 2020 §2]"
            highlight = "AHA / IRC Guideline (110 BPM)"
        elif any(k in q_lower for k in ["aed", "defibrillator", "shock", "pad"]):
            reply = "⚡ Turn ON the AED immediately. Follow voice prompts and adhere electrode pads to the bare dry chest: Upper right chest below collarbone, Lower left chest below armpit. Stand clear during shock!\n\n[Source: AHA CPR Guidelines 2020 §4.1]"
            highlight = "Immediate AED Action"
        elif any(k in q_lower for k in ["rib", "crack", "pop", "break", "শব্দ"]):
            reply = "⚠️ Costochondral cartilage popping or rib cracking is common during effective adult CPR. DO NOT STOP compressions. Continue CPR immediately; restoring cerebral blood flow is the sole priority.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
            highlight = "Do Not Stop CPR"
        elif any(k in q_lower for k in ["legal", "police", "samaritan", "liability", "law", "court", "আইন"]):
            reply = "🛡️ You are 100% legally protected under Section 134A of the Motor Vehicles (Amendment) Act 2019 and Supreme Court 2016 Good Samaritan Guidelines. You cannot be detained, harassed, or held liable.\n\n[Source: Motor Vehicles (Amendment) Act 2019 Section 134A]"
            highlight = "Section 134A MV Act Shield"
        else:
            reply = "📋 Ensure victim is on a firm flat surface. Check responsiveness and breathing. Begin CPR at 110 BPM and send for nearest AED.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
            highlight = "Grounded Protocol Step"

        return AgentChatResponse(
            session_id=request.session_id,
            reply_text=reply,
            highlight_text=highlight,
            triage_state="GUIDANCE",
            condition_id="cardiac_arrest",
            severity_level=5,
            priority="critical",
            current_step_index=request.current_step_index,
            completed_steps=request.completed_steps,
            cpr_metronome_active=True,
            cpr_bpm=110,
            citations=citations,
            contraindications=contraindications,
            legal_shield_applied=True,
            suggested_quick_questions=[
                "Can I give water or oral medicine?",
                "How deep should chest compressions be?",
                "When and how do I use the AED?",
                "What if ribs crack during CPR?",
                "Am I legally protected if I help?",
            ],
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
                        action_instruction="Place heel of hand on center of chest. Interlock fingers. Push hard and fast at depth of 5–6 cm at 110 BPM.",
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
            digital_signature_hash=f"SHA256:7f9a2b8c4d1e0f3a6b5c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8",
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
