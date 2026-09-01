"""NearHelp AI — AI Navigation & Rescue Routing API Endpoints (Module 9)."""

from typing import Any, Dict, List, Optional
from fastapi import APIRouter, HTTPException, Query, status
from pydantic import BaseModel, Field

from app.services.routing_service import routing_service

router = APIRouter(prefix="", tags=["AI Navigation & Rescue Routing"])


# -----------------------------------------------------------------------------
# Request & Response Schemas
# -----------------------------------------------------------------------------

class RouteLocationDto(BaseModel):
    latitude: float = Field(..., description="Latitude in decimal degrees")
    longitude: float = Field(..., description="Longitude in decimal degrees")


class DirectionsRequest(BaseModel):
    origin_latitude: float = Field(default=22.5835, description="Responder origin latitude")
    origin_longitude: float = Field(default=88.4410, description="Responder origin longitude")
    destination_latitude: float = Field(default=22.5804, description="Victim/Incident destination latitude")
    destination_longitude: float = Field(default=88.4378, description="Victim/Incident destination longitude")
    travel_mode: str = Field(default="walking", description="Travel mode: walking, bike, driving, ambulance")
    avoid_hazards: bool = Field(default=True, description="Whether to avoid reported traffic & flood hazards")
    include_aed_detour: bool = Field(default=False, description="Whether to include optional AED pickup route")
    aed_latitude: Optional[float] = Field(default=22.5806, description="Closest AED latitude")
    aed_longitude: Optional[float] = Field(default=88.4385, description="Closest AED longitude")
    aed_name: Optional[str] = Field(default="Godrej Waterside Tower 1 Lobby AED", description="AED unit name")


class RoadHazardReportRequest(BaseModel):
    title: str = Field(..., description="Hazard title, e.g. 'EM Bypass Flooded'")
    hazard_type: str = Field(default="FLOODING", description="FLOODING, CONSTRUCTION, TRAFFIC_JAM, ROAD_CLOSURE")
    severity: str = Field(default="MODERATE", description="LOW, MODERATE, CRITICAL, BLOCKED")
    latitude: float = Field(..., description="Hazard central latitude")
    longitude: float = Field(..., description="Hazard central longitude")
    radius_meters: float = Field(default=100.0, description="Hazard radius in meters")
    description: str = Field(default="", description="Detailed description")
    delay_seconds: int = Field(default=180, description="Estimated delay in seconds")
    is_passable_for_emergency: bool = Field(default=False, description="Whether emergency vehicles can pass")


# -----------------------------------------------------------------------------
# Endpoints
# -----------------------------------------------------------------------------

@router.post(
    "/directions",
    summary="Compute AI Rescue Directions & Detour Routes",
    response_model=Dict[str, Any],
)
async def compute_directions(request: DirectionsRequest) -> Dict[str, Any]:
    """Compute primary emergency route, traffic bypass detour, and optional AED pickup route."""
    routes = await routing_service.calculate_rescue_routes(
        origin_lat=request.origin_latitude,
        origin_lon=request.origin_longitude,
        dest_lat=request.destination_latitude,
        dest_lon=request.destination_longitude,
        travel_mode=request.travel_mode,
        avoid_hazards=request.avoid_hazards,
        include_aed_detour=request.include_aed_detour,
        aed_lat=request.aed_latitude,
        aed_lon=request.aed_longitude,
        aed_name=request.aed_name,
    )
    return {
        "status": "success",
        "data": routes,
    }


@router.post(
    "/detour",
    summary="Compute Detour Analysis",
    response_model=Dict[str, Any],
)
async def compute_detour_analysis(request: DirectionsRequest) -> Dict[str, Any]:
    """Analyze primary route versus detour alternatives with traffic and hazard deltas."""
    routes = await routing_service.calculate_rescue_routes(
        origin_lat=request.origin_latitude,
        origin_lon=request.origin_longitude,
        dest_lat=request.destination_latitude,
        dest_lon=request.destination_longitude,
        travel_mode=request.travel_mode,
        avoid_hazards=True,
        include_aed_detour=True,
        aed_lat=request.aed_latitude,
        aed_lon=request.aed_longitude,
        aed_name=request.aed_name,
    )
    return {
        "status": "success",
        "detour_route": routes.get("detour_route"),
        "aed_pickup_route": routes.get("aed_pickup_route"),
        "recommendation": routes.get("recommendation"),
    }


@router.get(
    "/hazards",
    summary="List Active Road Hazards & Flood Zones",
    response_model=Dict[str, Any],
)
def list_road_hazards() -> Dict[str, Any]:
    """Retrieve all active road blockages, construction zones, and waterlogging hazards."""
    hazards = routing_service.get_all_hazards()
    return {
        "status": "success",
        "count": len(hazards),
        "hazards": hazards,
    }


@router.post(
    "/hazards",
    summary="Report New Road Hazard or Blockage",
    status_code=status.HTTP_201_CREATED,
    response_model=Dict[str, Any],
)
def report_road_hazard(request: RoadHazardReportRequest) -> Dict[str, Any]:
    """Report a new road closure, flood zone, or severe traffic jam for AI routing consideration."""
    created = routing_service.add_hazard(
        title=request.title,
        hazard_type=request.hazard_type,
        severity=request.severity,
        latitude=request.latitude,
        longitude=request.longitude,
        radius_meters=request.radius_meters,
        description=request.description,
        delay_seconds=request.delay_seconds,
        is_passable_for_emergency=request.is_passable_for_emergency,
    )
    return {
        "status": "success",
        "message": "Hazard successfully logged in emergency dispatch registry.",
        "hazard": created,
    }


@router.delete(
    "/hazards/{hazard_id}",
    summary="Resolve and Clear Road Hazard",
    response_model=Dict[str, Any],
)
def resolve_road_hazard(hazard_id: str) -> Dict[str, Any]:
    """Mark a road hazard or blockage as resolved and clear it from routing avoidance."""
    success = routing_service.remove_hazard(hazard_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Hazard with ID '{hazard_id}' not found.",
        )
    return {
        "status": "success",
        "message": f"Hazard '{hazard_id}' resolved and removed from avoidance map.",
    }
