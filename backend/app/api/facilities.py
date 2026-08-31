"""NearHelp AI — Facilities & AED Geolocation API Router."""

import logging
import uuid
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.db.session import get_db
from app.models.facility import Facility
from app.schemas.facility import (
    FacilityResponse,
    FacilitySeedResponse,
    FacilityType,
    NearbyFacilitiesResponse,
)
from app.services.facility_service import FacilityService

logger = logging.getLogger(__name__)

router = APIRouter(tags=["Facilities & AED Mesh"])


@router.get(
    "/nearby",
    response_model=NearbyFacilitiesResponse,
    summary="Query nearby emergency hospitals, trauma centers, and AED locator stations",
    description=(
        "Performs spatial proximity search around the given GPS coordinate, "
        "filtering by radius, facility category, available hospital bed capacity, "
        "and critical ICU beds. Computes transit ETA and returns results sorted by distance."
    ),
)
async def get_nearby_facilities(
    latitude: float = Query(..., ge=-90.0, le=90.0, description="Epicenter latitude in WGS 84 (e.g. 22.5804 for Salt Lake)"),
    longitude: float = Query(..., ge=-180.0, le=180.0, description="Epicenter longitude in WGS 84 (e.g. 88.4378 for Sector V)"),
    radius_km: float = Query(5.0, gt=0.0, le=100.0, description="Radial search perimeter in kilometers"),
    type: Optional[str] = Query(None, description="Filter category: 'hospital', 'aed', 'trauma_center', 'blood_bank'"),
    min_beds: Optional[int] = Query(None, ge=0, description="Minimum available acute/inpatient beds"),
    min_icu: Optional[int] = Query(None, ge=0, description="Minimum available vacant ICU beds"),
    verified_only: bool = Query(True, description="Filter only verified facilities"),
    limit: int = Query(50, ge=1, le=200, description="Maximum number of records to return"),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve emergency facilities near a geographic coordinate point."""
    radius_meters = radius_km * 1000.0
    return await FacilityService.get_nearby_facilities(
        db=db,
        latitude=latitude,
        longitude=longitude,
        radius_meters=radius_meters,
        facility_type=type,
        min_beds=min_beds,
        min_icu=min_icu,
        verified_only=verified_only,
        limit=limit,
    )


@router.post(
    "/seed",
    response_model=FacilitySeedResponse,
    summary="Seed or synchronize regional Kolkata hospitals and AED mesh stations",
    description="Loads the verified Kolkata regional emergency infrastructure dataset into the database.",
)
async def seed_facilities(
    db: AsyncSession = Depends(get_db),
):
    """Trigger seeding of verified regional facilities."""
    result = await FacilityService.seed_kolkata_facilities(db)
    if result.get("status") == "error":
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=result.get("message", "Failed to seed facility dataset"),
        )
    return result


@router.get(
    "/{facility_id}",
    response_model=FacilityResponse,
    summary="Get emergency facility details by ID",
)
async def get_facility(
    facility_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
):
    """Retrieve detailed clinical and spatial metadata for a single facility."""
    facility = await FacilityService.get_facility_by_id(db, facility_id)
    if not facility:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Facility with ID '{facility_id}' not found",
        )
    return facility


@router.get(
    "/",
    response_model=list[FacilityResponse],
    summary="List all registered facilities",
)
async def list_facilities(
    type: Optional[str] = Query(None, description="Optional facility type filter"),
    skip: int = Query(0, ge=0),
    limit: int = Query(50, ge=1, le=200),
    db: AsyncSession = Depends(get_db),
):
    """List registered facilities with pagination."""
    stmt = select(Facility)
    if type:
        stmt = stmt.where(Facility.facility_type == type)
    stmt = stmt.offset(skip).limit(limit)

    res = await db.execute(stmt)
    return res.scalars().all()
