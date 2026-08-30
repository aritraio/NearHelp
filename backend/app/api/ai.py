"""NearHelp AI — AI Emergency Classification & Triage Proxy Endpoints."""

import logging

from fastapi import APIRouter, HTTPException, status

from app.schemas.ai import (
    ClassificationRequest,
    ClassificationResponse,
    SeverityRequest,
    SeverityResponse,
    TaxonomyResponse,
)
from app.services.ai_client import ai_client

logger = logging.getLogger(__name__)

router = APIRouter(tags=["AI Triage & Classification"])


@router.post(
    "/classify",
    response_model=ClassificationResponse,
    status_code=status.HTTP_200_OK,
    summary="Classify Emergency from Multimodal Input",
    description="Proxies multimodal emergency classification (Text, Voice audio, Photo) to the AI microservice with instant local fallback.",
)
async def classify_emergency(request: ClassificationRequest) -> ClassificationResponse:
    """Classify emergency intent, crisis category, priority, and clinical dispatch parameters."""
    try:
        response = await ai_client.classify(request)
        return response
    except Exception as e:
        logger.error("AI classification endpoint error: %s", e, exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Emergency classification failed: {e!s}",
        ) from e


@router.post(
    "/severity",
    response_model=SeverityResponse,
    status_code=status.HTTP_200_OK,
    summary="Predict Emergency Severity & Clinical Triage Score",
    description="Proxies emergency severity scoring and Level 1-5 clinical triage evaluation to AI microservice with instant local fallback.",
)
async def predict_severity(request: SeverityRequest) -> SeverityResponse:
    """Predict emergency severity score (0-100), Level 1-5 triage, radius scaling, and 108 auto-dial flags."""
    try:
        response = await ai_client.predict_severity(request)
        return response
    except Exception as e:
        logger.error("AI severity prediction endpoint error: %s", e, exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Emergency severity prediction failed: {e!s}",
        ) from e


@router.get(
    "/taxonomy",
    response_model=TaxonomyResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Emergency Types Taxonomy & Clinical Matrix",
    description="Retrieves crisis categories and 8 curated clinical conditions matrix.",
)
async def get_taxonomy() -> TaxonomyResponse:
    """Get emergency taxonomy and clinical conditions."""
    try:
        return await ai_client.get_taxonomy()
    except Exception as e:
        logger.error("Failed to retrieve emergency taxonomy: %s", e, exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Taxonomy retrieval failed: {e!s}",
        ) from e
