"""NearHelp AI — Emergency Classification & Multimodal Triage API Endpoints."""

import logging
from fastapi import APIRouter, HTTPException, status

from app.classifiers.crisis_types import (
    CLINICAL_CONDITIONS_MATRIX,
    CRISIS_TYPES_TAXONOMY,
)
from app.classifiers.emergency_classifier import emergency_classifier
from app.classifiers.speech_service import speech_service
from app.classifiers.vision_service import vision_service
from app.schemas.classify import (
    ClassificationRequest,
    ClassificationResponse,
    ClinicalConditionItem,
    CrisisTypeItem,
    TaxonomyResponse,
    TranscribeRequest,
    TranscribeResponse,
    VisionRequest,
    VisionResponse,
)

logger = logging.getLogger(__name__)

router = APIRouter(tags=["AI Emergency Detection"])


@router.post(
    "/classify",
    response_model=ClassificationResponse,
    status_code=status.HTTP_200_OK,
    summary="Classify Emergency from Multimodal Input",
    description="Analyzes free-text description, voice audio, or photo to detect emergency category, clinical sub-type, priority, radius, and recommended skills.",
)
async def classify_emergency(request: ClassificationRequest) -> ClassificationResponse:
    """Classify emergency intent and clinical symptoms from multimodal inputs."""
    try:
        response = await emergency_classifier.classify(request)
        return response
    except Exception as e:
        logger.error("Emergency classification error: %s", e, exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Classification processing failed: {str(e)}",
        ) from e


@router.get(
    "/taxonomy",
    response_model=TaxonomyResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Emergency Types Taxonomy & 8 Clinical Conditions Matrix",
    description="Retrieves reference crisis taxonomy, 8 curated clinical conditions, and required skills.",
)
async def get_taxonomy() -> TaxonomyResponse:
    """Return the full taxonomy of crisis types and 8 curated clinical conditions."""
    crisis_types = [
        CrisisTypeItem(
            id=c["id"],
            name=c["name"],
            description=c["description"],
            default_emergency_number=c["default_emergency_number"],
            sub_types=c["sub_types"],
        )
        for c in CRISIS_TYPES_TAXONOMY
    ]

    clinical_conditions = [
        ClinicalConditionItem(
            id=cond.id,
            label=cond.label,
            icon_name=cond.icon_name,
            severity=cond.severity,
            priority=cond.priority,
            description=cond.description,
            symptoms=cond.symptoms,
            suggested_skills=cond.suggested_skills,
            immediate_action=cond.immediate_action,
            recommended_radius_km=cond.recommended_radius_km,
            emergency_number=cond.emergency_number,
        )
        for cond in CLINICAL_CONDITIONS_MATRIX.values()
    ]

    return TaxonomyResponse(
        crisis_types=crisis_types,
        clinical_conditions=clinical_conditions,
        version="1.0.0",
    )


@router.post(
    "/transcribe",
    response_model=TranscribeResponse,
    status_code=status.HTTP_200_OK,
    summary="Transcribe Emergency Voice Audio",
    description="Converts voice recording into text transcript using Google Speech-to-Text.",
)
async def transcribe_audio(request: TranscribeRequest) -> TranscribeResponse:
    """Transcribe emergency voice audio."""
    try:
        transcript, lang, conf, latency = await speech_service.transcribe_audio(
            audio_base64=request.audio_base64,
            audio_format=request.audio_format,
            language_code=request.language_code,
        )
        return TranscribeResponse(
            transcription=transcript,
            language_detected=lang,
            confidence=conf,
            processing_time_ms=round(latency, 2),
        )
    except Exception as e:
        logger.error("Audio transcription error: %s", e, exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Speech transcription failed: {str(e)}",
        ) from e


@router.post(
    "/vision",
    response_model=VisionResponse,
    status_code=status.HTTP_200_OK,
    summary="Analyze Emergency Scene Photo",
    description="Extracts visual scene description, hazards, and injuries using Gemini 2.5 Vision.",
)
async def analyze_scene(request: VisionRequest) -> VisionResponse:
    """Analyze emergency scene photograph."""
    try:
        desc, em_type, hazards, injuries, conf, latency = await vision_service.analyze_scene(
            image_base64=request.image_base64,
            image_mime_type=request.image_mime_type,
            prompt=request.prompt,
        )
        return VisionResponse(
            scene_description=desc,
            inferred_emergency_type=em_type,
            detected_hazards=hazards,
            detected_injuries=injuries,
            confidence=conf,
            processing_time_ms=round(latency, 2),
        )
    except Exception as e:
        logger.error("Vision scene analysis error: %s", e, exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Vision analysis failed: {str(e)}",
        ) from e
