"""NearHelp AI — AI Severity Prediction API Endpoints."""

import logging

from app.classifiers.severity_predictor import severity_predictor
from app.schemas.severity import SeverityRequest, SeverityResponse
from fastapi import APIRouter, HTTPException, status

logger = logging.getLogger(__name__)

router = APIRouter(tags=["AI Severity Prediction"])


@router.post(
    "/severity",
    response_model=SeverityResponse,
    status_code=status.HTTP_200_OK,
    summary="Predict Emergency Severity & Clinical Triage Score",
    description="Evaluates emergency incident narrative, vital signs distress, and clinical factors to compute a 0-100 severity score, Level 1-5 triage, radius scaling, and 108 auto-call directives.",
)
async def predict_severity(request: SeverityRequest) -> SeverityResponse:
    """Predict emergency severity, triage level, dynamic dispatch radius, and clinical reasoning."""
    try:
        response = await severity_predictor.predict(request)
        return response
    except Exception as e:
        logger.error("Severity prediction error: %s", e, exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Severity prediction processing failed: {e!s}",
        ) from e
