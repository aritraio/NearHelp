"""NearHelp AI — Live Tracking & Incident State Management Service."""

import logging
import uuid
from datetime import datetime
from typing import Any

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.response import SOSResponse
from app.models.sos_event import SOSEvent
from app.models.timeline_event import TimelineEvent
from app.models.user import User
from app.schemas.tracking import (
    LocationUpdateMessage,
    ResponderTrackingUpdate,
    TimelineTrackingEvent,
    TrackingFacilityItem,
    TrackingSnapshot,
)
from app.services.eta_service import eta_service
from app.services.facility_service import FacilityService

logger = logging.getLogger(__name__)


class TrackingService:
    """Service orchestrating real-time GPS tracking, telemetry calculation, and state snapshots."""

    # In-memory ephemeral store for high-frequency telemetry between DB sync intervals
    _live_telemetry_cache: dict[str, dict[str, Any]] = {}

    @classmethod
    def _cache_key(cls, incident_id: uuid.UUID, responder_id: uuid.UUID) -> str:
        return f"{incident_id}:{responder_id}"

    @classmethod
    def cache_responder_location(
        cls,
        incident_id: uuid.UUID,
        responder_id: uuid.UUID,
        latitude: float,
        longitude: float,
        heading: float | None = None,
        speed_mps: float | None = None,
    ) -> None:
        """Store transient live telemetry in in-memory cache for low-latency streaming."""
        key = cls._cache_key(incident_id, responder_id)
        cls._live_telemetry_cache[key] = {
            "latitude": latitude,
            "longitude": longitude,
            "heading": heading,
            "speed_mps": speed_mps,
            "updated_at": datetime.utcnow(),
        }

    @classmethod
    def get_cached_responder_location(
        cls, incident_id: uuid.UUID, responder_id: uuid.UUID
    ) -> dict[str, Any] | None:
        """Retrieve recent cached GPS telemetry for a responder."""
        key = cls._cache_key(incident_id, responder_id)
        return cls._live_telemetry_cache.get(key)

    @classmethod
    async def build_tracking_snapshot(
        cls,
        db: AsyncSession,
        incident_id: uuid.UUID,
        connected_clients_count: int = 0,
    ) -> TrackingSnapshot | None:
        """Construct full current state snapshot of an incident with all active responders and closest facilities."""
        # 1. Fetch SOS incident
        stmt = select(SOSEvent).where(SOSEvent.id == incident_id)
        event_res = await db.execute(stmt)
        event = event_res.scalars().first()
        if not event:
            return None

        # 2. Fetch all responder engagements
        resp_stmt = select(SOSResponse, User).join(User, SOSResponse.responder_id == User.id).where(
            SOSResponse.sos_event_id == incident_id,
            SOSResponse.status.in_(["ACCEPTED", "EN_ROUTE", "ARRIVED", "ON_SCENE"]),
        )
        resp_res = await db.execute(resp_stmt)
        responder_rows = resp_res.all()

        responder_updates: list[ResponderTrackingUpdate] = []
        for sos_response, user in responder_rows:
            # Check cached telemetry first, then fallback to user.device_info or deterministic position
            cached = cls.get_cached_responder_location(incident_id, user.id)
            user_lat = None
            user_lon = None
            user_heading = None
            user_speed_mps = None

            if cached:
                user_lat = cached.get("latitude")
                user_lon = cached.get("longitude")
                user_heading = cached.get("heading")
                user_speed_mps = cached.get("speed_mps")
            elif user.device_info and isinstance(user.device_info, dict):
                user_lat = user.device_info.get("last_latitude")
                user_lon = user.device_info.get("last_longitude")
                user_heading = user.device_info.get("last_heading")
                user_speed_mps = user.device_info.get("last_speed")

            if user_lat is None or user_lon is None:
                # Deterministic fallback coordinate offset for simulation
                offset_val = (user.id.int % 1000) / 10000.0
                user_lat = event.latitude + (offset_val if (user.id.int % 2 == 0) else -offset_val)
                user_lon = event.longitude + (offset_val if (user.id.int % 3 == 0) else -offset_val)

            (
                dist_m,
                eta_min,
                eta_str,
                bearing_deg,
                bearing_compass,
                dist_str,
            ) = eta_service.calculate_eta(
                responder_lat=float(user_lat),
                responder_lon=float(user_lon),
                target_lat=event.latitude,
                target_lon=event.longitude,
                speed_mps=user_speed_mps,
            )

            # Extract user skills and badges
            user_skills = user.skills or []
            if isinstance(user_skills, list):
                skill_list = [s.get("name", "") if isinstance(s, dict) else str(s) for s in user_skills]
            else:
                skill_list = []

            is_doctor = "DOCTOR" in skill_list or any("doctor" in s.lower() for s in skill_list)
            is_cpr = "CPR_CERTIFIED" in skill_list or any("cpr" in s.lower() for s in skill_list)

            speed_kmh = round(user_speed_mps * 3.6, 1) if user_speed_mps is not None else None

            # If responder is already marked ARRIVED, force status & ETA display
            current_status = sos_response.status
            if current_status in ("ARRIVED", "ON_SCENE") or dist_m <= eta_service.ARRIVAL_THRESHOLD_METERS:
                if current_status == "EN_ROUTE" and dist_m <= eta_service.ARRIVAL_THRESHOLD_METERS:
                    current_status = "ARRIVED"
                eta_min = 0.0
                eta_str = "Arrived"

            responder_updates.append(
                ResponderTrackingUpdate(
                    responder_id=user.id,
                    responder_name=user.name,
                    latitude=float(user_lat),
                    longitude=float(user_lon),
                    heading=user_heading or bearing_deg,
                    bearing_compass=bearing_compass,
                    speed_kmh=speed_kmh,
                    distance_meters=dist_m,
                    distance_formatted=dist_str,
                    eta_minutes=eta_min,
                    eta_formatted=eta_str,
                    status=current_status,
                    is_doctor=is_doctor,
                    is_cpr_certified=is_cpr,
                    verified_skills=skill_list,
                    phone=user.phone,
                    last_updated=datetime.utcnow(),
                )
            )

        # 3. Fetch closest AED and nearest Hospital
        closest_aed: TrackingFacilityItem | None = None
        closest_hospital: TrackingFacilityItem | None = None

        try:
            nearby_aeds = await FacilityService.get_nearby_facilities(
                db=db,
                latitude=event.latitude,
                longitude=event.longitude,
                radius_meters=10000.0,
                facility_type="aed",
                limit=1,
            )
            if nearby_aeds.facilities:
                aed = nearby_aeds.facilities[0]
                closest_aed = TrackingFacilityItem(
                    id=aed.id,
                    name=aed.name,
                    facility_type="aed",
                    latitude=aed.latitude,
                    longitude=aed.longitude,
                    distance_meters=aed.distance_meters or 0.0,
                    distance_formatted=f"{int(aed.distance_meters)}m" if aed.distance_meters and aed.distance_meters < 1000 else f"{aed.distance_km}km",
                    details={
                        "building": aed.aed_building_name,
                        "location": aed.aed_location_description,
                        "access_code": aed.aed_access_code,
                    },
                )
        except Exception as e:
            logger.debug(f"Facility lookup notice (AED): {e}")

        try:
            nearby_hospitals = await FacilityService.get_nearby_facilities(
                db=db,
                latitude=event.latitude,
                longitude=event.longitude,
                radius_meters=15000.0,
                facility_type="hospital",
                limit=1,
            )
            if nearby_hospitals.facilities:
                hosp = nearby_hospitals.facilities[0]
                closest_hospital = TrackingFacilityItem(
                    id=hosp.id,
                    name=hosp.name,
                    facility_type="hospital",
                    latitude=hosp.latitude,
                    longitude=hosp.longitude,
                    distance_meters=hosp.distance_meters or 0.0,
                    distance_formatted=f"{hosp.distance_km}km" if hosp.distance_km else f"{int(hosp.distance_meters or 0)}m",
                    details={
                        "bed_availability": hosp.bed_availability,
                        "icu_availability": hosp.icu_availability,
                        "emergency_helpline": hosp.emergency_helpline or hosp.phone,
                        "trauma_level": hosp.trauma_level,
                    },
                )
        except Exception as e:
            logger.debug(f"Facility lookup notice (Hospital): {e}")

        return TrackingSnapshot(
            incident_id=event.id,
            status=event.status,
            crisis_type=event.crisis_type,
            sub_type=event.sub_type,
            severity_score=event.severity_score,
            priority=event.priority,
            incident_latitude=event.latitude,
            incident_longitude=event.longitude,
            incident_address=event.address,
            incident_sub_address=event.sub_address,
            is_anonymous=event.is_anonymous,
            current_radius_meters=event.current_radius_meters,
            responders=responder_updates,
            closest_aed=closest_aed,
            closest_hospital=closest_hospital,
            connected_clients_count=connected_clients_count,
            server_timestamp=datetime.utcnow(),
        )

    @classmethod
    async def process_location_update(
        cls,
        db: AsyncSession,
        incident_id: uuid.UUID,
        responder_id: uuid.UUID,
        location_data: LocationUpdateMessage,
    ) -> tuple[ResponderTrackingUpdate | None, TimelineTrackingEvent | None]:
        """Process incoming GPS stream packet, update DB/cache, calculate dynamic ETA, and check arrival trigger."""
        # 1. Update in-memory live telemetry cache
        cls.cache_responder_location(
            incident_id=incident_id,
            responder_id=responder_id,
            latitude=location_data.latitude,
            longitude=location_data.longitude,
            heading=location_data.heading,
            speed_mps=location_data.speed_mps,
        )

        # 2. Fetch incident and responder entities
        stmt = select(SOSEvent).where(SOSEvent.id == incident_id)
        event_res = await db.execute(stmt)
        event = event_res.scalars().first()
        if not event:
            return None, None

        user_stmt = select(User).where(User.id == responder_id)
        user_res = await db.execute(user_stmt)
        user = user_res.scalars().first()
        if not user:
            return None, None

        # 3. Update user entity device telemetry
        current_info = user.device_info or {}
        if isinstance(current_info, dict):
            current_info["last_latitude"] = location_data.latitude
            current_info["last_longitude"] = location_data.longitude
            current_info["last_heading"] = location_data.heading
            current_info["last_speed"] = location_data.speed_mps
            current_info["last_updated"] = datetime.utcnow().isoformat()
            user.device_info = current_info

        # 4. Calculate real-time ETA & distance
        (
            dist_m,
            eta_min,
            eta_str,
            bearing_deg,
            bearing_compass,
            dist_str,
        ) = eta_service.calculate_eta(
            responder_lat=location_data.latitude,
            responder_lon=location_data.longitude,
            target_lat=event.latitude,
            target_lon=event.longitude,
            speed_mps=location_data.speed_mps,
        )

        # 5. Check if arrival milestone triggered
        resp_stmt = select(SOSResponse).where(
            SOSResponse.sos_event_id == incident_id,
            SOSResponse.responder_id == responder_id,
        )
        resp_res = await db.execute(resp_stmt)
        sos_response = resp_res.scalars().first()

        timeline_event_obj: TimelineTrackingEvent | None = None
        current_status = sos_response.status if sos_response else "EN_ROUTE"

        if sos_response and dist_m <= eta_service.ARRIVAL_THRESHOLD_METERS and sos_response.status in ("ACCEPTED", "EN_ROUTE"):
            sos_response.status = "ARRIVED"
            sos_response.arrived_at = datetime.utcnow()
            current_status = "ARRIVED"
            eta_min = 0.0
            eta_str = "Arrived"

            # Create arrival timeline milestone
            tl_entry = TimelineEvent(
                sos_event_id=incident_id,
                actor_id=user.id,
                event_type="RESPONDER_ARRIVED",
                details={
                    "actor_name": user.name,
                    "distance_meters": dist_m,
                    "message": f"{user.name} has arrived on scene.",
                },
            )
            db.add(tl_entry)
            await db.flush()

            timeline_event_obj = TimelineTrackingEvent(
                id=tl_entry.id,
                sos_event_id=incident_id,
                actor_id=user.id,
                actor_name=user.name,
                event_type="RESPONDER_ARRIVED",
                details=tl_entry.details,
                timestamp=tl_entry.timestamp or datetime.utcnow(),
            )

        await db.commit()

        # Build responder tracking update
        user_skills = user.skills or []
        if isinstance(user_skills, list):
            skill_list = [s.get("name", "") if isinstance(s, dict) else str(s) for s in user_skills]
        else:
            skill_list = []

        is_doctor = "DOCTOR" in skill_list or any("doctor" in s.lower() for s in skill_list)
        is_cpr = "CPR_CERTIFIED" in skill_list or any("cpr" in s.lower() for s in skill_list)
        speed_kmh = round(location_data.speed_mps * 3.6, 1) if location_data.speed_mps is not None else None

        update_payload = ResponderTrackingUpdate(
            responder_id=user.id,
            responder_name=user.name,
            latitude=location_data.latitude,
            longitude=location_data.longitude,
            heading=location_data.heading or bearing_deg,
            bearing_compass=bearing_compass,
            speed_kmh=speed_kmh,
            distance_meters=dist_m,
            distance_formatted=dist_str,
            eta_minutes=eta_min,
            eta_formatted=eta_str,
            status=current_status,
            is_doctor=is_doctor,
            is_cpr_certified=is_cpr,
            verified_skills=skill_list,
            phone=user.phone,
            last_updated=datetime.utcnow(),
        )

        return update_payload, timeline_event_obj

    @classmethod
    async def process_status_update(
        cls,
        db: AsyncSession,
        incident_id: uuid.UUID,
        responder_id: uuid.UUID,
        new_status: str,
        note: str | None = None,
    ) -> tuple[SOSResponse | None, TimelineTrackingEvent | None]:
        """Transition responder status and log timeline milestone."""
        stmt = select(SOSResponse, User).join(User, SOSResponse.responder_id == User.id).where(
            SOSResponse.sos_event_id == incident_id,
            SOSResponse.responder_id == responder_id,
        )
        res = await db.execute(stmt)
        row = res.first()
        if not row:
            return None, None

        sos_response, user = row
        sos_response.status = new_status

        if new_status in ("ARRIVED", "ON_SCENE") and not sos_response.arrived_at:
            sos_response.arrived_at = datetime.utcnow()

        event_type = f"RESPONDER_{new_status}"
        details = {
            "actor_name": user.name,
            "status": new_status,
            "note": note,
            "message": f"{user.name} changed status to {new_status}" + (f": {note}" if note else "."),
        }

        tl_entry = TimelineEvent(
            sos_event_id=incident_id,
            actor_id=user.id,
            event_type=event_type,
            details=details,
        )
        db.add(tl_entry)
        await db.commit()
        await db.refresh(sos_response)
        await db.refresh(tl_entry)

        timeline_obj = TimelineTrackingEvent(
            id=tl_entry.id,
            sos_event_id=incident_id,
            actor_id=user.id,
            actor_name=user.name,
            event_type=event_type,
            details=details,
            timestamp=tl_entry.timestamp or datetime.utcnow(),
        )

        return sos_response, timeline_obj


tracking_service = TrackingService()
