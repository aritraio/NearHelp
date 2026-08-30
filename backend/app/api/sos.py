"""NearHelp AI — Smart SOS Engine API Endpoints."""

import uuid

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import (
    get_current_active_user,
    get_optional_current_user,
)
from app.db.session import get_db
from app.models.user import User
from app.schemas.sos import (
    SOSActiveListResponse,
    SOSCreateRequest,
    SOSCreateResponse,
    SOSDetailResponse,
    SOSEscalateRequest,
    SOSEscalationStatus,
    SOSResolveRequest,
    SOSResolveResponse,
    SOSResponseItem,
    SOSResponseRequest,
    TimelineEventItem,
)
from app.services.sos_service import sos_service

router = APIRouter(prefix="", tags=["Smart SOS Engine"])


@router.post(
    "/create",
    response_model=SOSCreateResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Trigger Smart SOS Emergency",
    description="Creates an emergency broadcast, executes AI triage, ranks candidate responders, and dispatches push alerts.",
)
@router.post(
    "/",
    response_model=SOSCreateResponse,
    status_code=status.HTTP_201_CREATED,
    include_in_schema=False,
)
async def create_sos_emergency(
    req: SOSCreateRequest,
    current_user: User | None = Depends(get_optional_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Trigger an emergency alert broadcast with AI triage and PostGIS geospatial dispatch."""
    return await sos_service.create_sos_event(db=db, user=current_user, req=req)


@router.get(
    "/active",
    response_model=SOSActiveListResponse,
    status_code=status.HTTP_200_OK,
    summary="List Active Emergency Broadcasts",
    description="Retrieve all currently active, unresolved emergencies, optionally calculating distance from current coordinates.",
)
async def get_active_emergencies(
    lat: float | None = Query(None, ge=-90.0, le=90.0, description="Observer GPS Latitude"),
    lon: float | None = Query(None, ge=-180.0, le=180.0, description="Observer GPS Longitude"),
    limit: int = Query(50, ge=1, le=100),
    current_user: User | None = Depends(get_optional_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve active emergency incidents within the community."""
    return await sos_service.get_active_events(
        db=db, user=current_user, latitude=lat, longitude=lon, limit=limit
    )


@router.get(
    "/{id}",
    response_model=SOSDetailResponse,
    status_code=status.HTTP_200_OK,
    summary="Get SOS Emergency Details",
    description="Retrieve complete status, responder engagements, AI triage analysis, and audit timeline for an emergency incident.",
)
async def get_sos_detail(
    id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
):
    """Retrieve detailed state of an emergency incident."""
    return await sos_service.get_sos_details(db=db, sos_id=id)


@router.post(
    "/{id}/respond",
    response_model=SOSResponseItem,
    status_code=status.HTTP_200_OK,
    summary="Accept or Update SOS Emergency Response",
    description="Register responder acceptance, calculate live ETA, update incident state, and notify broadcaster.",
)
async def respond_to_sos_emergency(
    id: uuid.UUID,
    req: SOSResponseRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Accept an emergency dispatch assignment."""
    return await sos_service.respond_to_sos(
        db=db, responder=current_user, sos_id=id, req=req
    )


@router.post(
    "/{id}/escalate",
    response_model=SOSEscalationStatus,
    status_code=status.HTTP_200_OK,
    summary="Evaluate or Trigger 3-Layer Escalation",
    description="Evaluates elapsed time, expands radial search waves (500m -> 1.5km -> 3km/5km), or triggers 108/112 auto-dial recommendation.",
)
async def escalate_sos_emergency(
    id: uuid.UUID,
    req: SOSEscalateRequest,
    db: AsyncSession = Depends(get_db),
):
    """Evaluate and trigger time-based or manual emergency escalation."""
    return await sos_service.escalate_sos(db=db, sos_id=id, req=req)


@router.put(
    "/{id}/resolve",
    response_model=SOSResolveResponse,
    status_code=status.HTTP_200_OK,
    summary="Resolve SOS Emergency",
    description="Mark emergency resolved, record clinical resolution notes, and award reputation trust points to responding heroes.",
)
async def resolve_sos_emergency(
    id: uuid.UUID,
    req: SOSResolveRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Resolve an emergency incident."""
    return await sos_service.resolve_sos(
        db=db, actor=current_user, sos_id=id, req=req
    )


@router.get(
    "/{id}/timeline",
    response_model=list[TimelineEventItem],
    status_code=status.HTTP_200_OK,
    summary="Get Emergency Audit Timeline",
    description="Retrieve chronological milestones from trigger to resolution.",
)
async def get_emergency_timeline(
    id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
):
    """Retrieve chronological audit timeline events for an emergency."""
    detail = await sos_service.get_sos_details(db=db, sos_id=id)
    return detail.timeline
