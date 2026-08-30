"""NearHelp AI — Resilient HTTP Client for AI Microservice."""

import logging
import time
from typing import Any

import httpx

from app.core.config import settings
from app.schemas.ai import ClassificationRequest, ClassificationResponse, TaxonomyResponse

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


ai_client = AIClient()
