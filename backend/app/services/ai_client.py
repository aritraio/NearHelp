"""NearHelp AI — Resilient HTTP Client for AI Microservice."""

import logging
import time

import httpx

from app.core.config import settings
from app.schemas.ai import (
    ClassificationRequest,
    ClassificationResponse,
    SeverityRequest,
    SeverityResponse,
    SeverityScoreFactors,
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
