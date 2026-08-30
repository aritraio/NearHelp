"""NearHelp AI — Multimodal Emergency Classifier Orchestrator."""

import logging
import time

from app.classifiers.embedding_service import embedding_service
from app.classifiers.speech_service import speech_service
from app.classifiers.vision_service import vision_service
from app.schemas.classify import ClassificationRequest, ClassificationResponse

logger = logging.getLogger(__name__)


class EmergencyClassifier:
    """Multimodal Emergency Classifier combining Text, Voice STT, and Vision."""

    async def initialize(self):
        """Initialize embedding matrix."""
        await embedding_service.initialize()

    async def classify(self, request: ClassificationRequest) -> ClassificationResponse:
        """Classify emergency from multimodal input (Text, Voice audio, Photo)."""
        start_time = time.perf_counter()

        text_parts: list[str] = []
        transcription: str | None = None
        image_description: str | None = None
        detected_symptoms: list[str] = []

        # 1. Direct text input
        if request.text and request.text.strip():
            text_parts.append(request.text.strip())

        # 2. Voice Audio input (Google Speech-to-Text)
        if request.audio_base64 and request.audio_base64.strip():
            transcript, _, _, _ = await speech_service.transcribe_audio(
                audio_base64=request.audio_base64,
                audio_format=request.audio_format or "wav",
                language_code=request.language_code,
            )
            if transcript:
                transcription = transcript
                text_parts.append(transcript)

        # 3. Photo input (Gemini 2.5 Vision)
        if request.image_base64 and request.image_base64.strip():
            scene_desc, inferred_type, hazards, injuries, _, _ = await vision_service.analyze_scene(
                image_base64=request.image_base64,
                image_mime_type=request.image_mime_type or "image/jpeg",
            )
            if scene_desc:
                image_description = scene_desc
                text_parts.append(scene_desc)
            if injuries:
                detected_symptoms.extend(injuries)

        # Combine all input modalities into unified emergency context
        combined_text = " ".join(text_parts).strip()

        # 4. Embedding & Cosine Similarity Match
        matches = await embedding_service.match_emergency(combined_text, top_k=1)
        top_profile, confidence_score, symptoms = matches[0]

        # Merge extracted symptoms
        for s in symptoms:
            if s not in detected_symptoms:
                detected_symptoms.append(s)

        # Confidence percentage (formatted e.g. 98.4%)
        confidence_pct = round(confidence_score * 100.0, 1)

        total_latency_ms = (time.perf_counter() - start_time) * 1000.0

        return ClassificationResponse(
            emergency_type=top_profile.crisis_type,
            sub_type=top_profile.id,
            priority=top_profile.priority,
            severity_level=top_profile.severity,
            confidence=round(confidence_score, 4),
            confidence_percentage=confidence_pct,
            recommended_radius_km=top_profile.recommended_radius_km,
            suggested_responder_skills=top_profile.suggested_skills,
            immediate_action=top_profile.immediate_action,
            requires_professional=top_profile.requires_professional,
            call_emergency_services=top_profile.call_emergency_services,
            emergency_number=top_profile.emergency_number,
            detected_symptoms=detected_symptoms,
            transcription=transcription,
            image_description=image_description,
            processing_time_ms=round(total_latency_ms, 2),
        )


emergency_classifier = EmergencyClassifier()
