"""NearHelp AI — Emergency Facility & Geospatial Spatial Service."""

import json
import logging
import math
import os
import uuid
from typing import Any

from sqlalchemy import select, text
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.models.facility import Facility
from app.schemas.facility import (
    FacilityCreate,
    FacilityResponse,
    FacilityType,
    NearbyFacilitiesResponse,
)
from app.services.geo_service import haversine_distance

logger = logging.getLogger(__name__)


class FacilityService:
    """Service providing spatial lookups, live capacity filtering, and seeding for emergency facilities."""

    @classmethod
    async def get_nearby_facilities(
        cls,
        db: AsyncSession,
        latitude: float,
        longitude: float,
        radius_meters: float = 5000.0,
        facility_type: str | None = None,
        min_beds: int | None = None,
        min_icu: int | None = None,
        verified_only: bool = True,
        limit: int = 50,
    ) -> NearbyFacilitiesResponse:
        """Query physical emergency facilities within geodesic radius sorted by proximity.
        
        Supports filtering by facility type (hospital, aed, trauma_center), vacant inpatient beds,
        critical care ICU beds, and verification status. Computes transit ETA based on urban Kolkata flow.
        """
        # Ensure database is seeded if empty
        count_stmt = select(Facility)
        res_count = await db.execute(count_stmt)
        if not res_count.scalars().first():
            logger.info("Facilities table empty during query; trigger auto-seed...")
            await cls.seed_kolkata_facilities(db)

        # Build base query
        stmt = select(Facility)

        if facility_type:
            # Handle trauma_center matching hospital with trauma level or exact type
            if facility_type == "trauma_center":
                stmt = stmt.where(
                    (Facility.facility_type == "trauma_center") |
                    (Facility.facility_type == "hospital") & (Facility.trauma_level.isnot(None))
                )
            else:
                stmt = stmt.where(Facility.facility_type == facility_type)

        if min_beds is not None and min_beds > 0:
            stmt = stmt.where(Facility.bed_availability >= min_beds)

        if min_icu is not None and min_icu > 0:
            stmt = stmt.where(Facility.icu_availability >= min_icu)

        if verified_only:
            stmt = stmt.where(Facility.is_verified.is_(True))

        exec_res = await db.execute(stmt)
        candidate_facilities = exec_res.scalars().all()

        # Calculate haversine distances and filter by radius
        facility_results: list[FacilityResponse] = []
        for facility in candidate_facilities:
            dist_m = haversine_distance(latitude, longitude, facility.latitude, facility.longitude)
            if dist_m <= radius_meters:
                dist_km = round(dist_m / 1000.0, 2)
                # Urban emergency response transit model: ~25 km/h average emergency vehicle speed
                eta_min = max(1, int(round((dist_km / 25.0) * 60.0))) if dist_m > 30 else 0

                resp_obj = FacilityResponse(
                    id=facility.id,
                    name=facility.name,
                    facility_type=FacilityType(facility.facility_type) if facility.facility_type in FacilityType._value2member_map_ else FacilityType.HOSPITAL,
                    address=facility.address,
                    phone=facility.phone,
                    emergency_helpline=facility.emergency_helpline,
                    latitude=facility.latitude,
                    longitude=facility.longitude,
                    zone=facility.zone,
                    bed_availability=facility.bed_availability,
                    total_beds=facility.total_beds,
                    icu_availability=facility.icu_availability,
                    total_icu=facility.total_icu,
                    trauma_level=facility.trauma_level,
                    has_cardiac_unit=facility.has_cardiac_unit,
                    has_burn_unit=facility.has_burn_unit,
                    is_24_hours=facility.is_24_hours,
                    is_verified=facility.is_verified,
                    aed_building_name=facility.aed_building_name,
                    aed_location_description=facility.aed_location_description,
                    aed_access_code=facility.aed_access_code,
                    extra_metadata=facility.extra_metadata or {},
                    created_at=facility.created_at,
                    updated_at=facility.updated_at,
                    distance_meters=round(dist_m, 1),
                    distance_km=dist_km,
                    eta_minutes=eta_min,
                )
                facility_results.append(resp_obj)

        # Sort strictly by proximity ascending
        facility_results.sort(key=lambda f: f.distance_meters if f.distance_meters is not None else float("inf"))
        sliced_results = facility_results[:limit]

        return NearbyFacilitiesResponse(
            count=len(sliced_results),
            center_latitude=latitude,
            center_longitude=longitude,
            radius_km=round(radius_meters / 1000.0, 2),
            facilities=sliced_results,
        )

    @classmethod
    async def get_facility_by_id(cls, db: AsyncSession, facility_id: uuid.UUID) -> Facility | None:
        """Fetch facility record by primary key."""
        stmt = select(Facility).where(Facility.id == facility_id)
        res = await db.execute(stmt)
        return res.scalars().first()

    @classmethod
    async def seed_kolkata_facilities(cls, db: AsyncSession) -> dict[str, Any]:
        """Seed or synchronize the database with verified Kolkata hospitals and AED mesh stations."""
        # Locate regional dataset JSON
        possible_paths = [
            os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../data/regional/kolkata_facilities.json")),
            os.path.abspath(os.path.join(os.getcwd(), "data/regional/kolkata_facilities.json")),
            os.path.abspath(os.path.join(os.getcwd(), "../data/regional/kolkata_facilities.json")),
        ]

        json_path = None
        for p in possible_paths:
            if os.path.exists(p):
                json_path = p
                break

        if not json_path:
            logger.warning(f"Could not locate kolkata_facilities.json in paths: {possible_paths}")
            return {
                "status": "error",
                "message": "kolkata_facilities.json dataset file not found on disk",
                "inserted_count": 0,
                "updated_count": 0,
                "total_count": 0,
            }

        with open(json_path, encoding="utf-8") as f:
            facilities_data: list[dict[str, Any]] = json.load(f)

        inserted = 0
        updated = 0

        for item in facilities_data:
            name = item["name"]
            stmt = select(Facility).where(Facility.name == name)
            res = await db.execute(stmt)
            existing = res.scalars().first()

            if existing:
                # Update dynamic metrics
                existing.bed_availability = item.get("bed_availability", existing.bed_availability)
                existing.total_beds = item.get("total_beds", existing.total_beds)
                existing.icu_availability = item.get("icu_availability", existing.icu_availability)
                existing.total_icu = item.get("total_icu", existing.total_icu)
                existing.emergency_helpline = item.get("emergency_helpline", existing.emergency_helpline)
                existing.phone = item.get("phone", existing.phone)
                existing.extra_metadata = item.get("metadata", existing.extra_metadata or {})
                updated += 1
            else:
                new_facility = Facility(
                    name=item["name"],
                    facility_type=item.get("facility_type", "hospital"),
                    address=item["address"],
                    phone=item.get("phone"),
                    emergency_helpline=item.get("emergency_helpline"),
                    latitude=float(item["latitude"]),
                    longitude=float(item["longitude"]),
                    zone=item.get("zone"),
                    bed_availability=item.get("bed_availability", 0),
                    total_beds=item.get("total_beds", 0),
                    icu_availability=item.get("icu_availability", 0),
                    total_icu=item.get("total_icu", 0),
                    trauma_level=item.get("trauma_level"),
                    has_cardiac_unit=item.get("has_cardiac_unit", False),
                    has_burn_unit=item.get("has_burn_unit", False),
                    is_24_hours=item.get("is_24_hours", True),
                    is_verified=item.get("is_verified", True),
                    aed_building_name=item.get("aed_building_name"),
                    aed_location_description=item.get("aed_location_description"),
                    aed_access_code=item.get("aed_access_code"),
                    extra_metadata=item.get("metadata", {}),
                )
                # PostGIS Geometry population for PostgreSQL
                if "postgresql" in settings.DATABASE_URL:
                    try:
                        from geoalchemy2.elements import WKTElement
                        new_facility.location = WKTElement(
                            f"POINT({new_facility.longitude} {new_facility.latitude})", srid=4326
                        )
                    except Exception as ex:
                        logger.debug(f"WKTElement generation: {ex}")

                db.add(new_facility)
                inserted += 1

        await db.commit()

        total_stmt = select(Facility)
        total_res = await db.execute(total_stmt)
        total_count = len(total_res.scalars().all())

        logger.info(
            f"Kolkata facilities seeded successfully: {inserted} inserted, {updated} updated, {total_count} total."
        )

        return {
            "status": "success",
            "message": f"Successfully seeded {inserted} new facilities and updated {updated} existing records",
            "inserted_count": inserted,
            "updated_count": updated,
            "total_count": total_count,
        }
