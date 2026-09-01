"""NearHelp AI — AI Navigation & Rescue Routing Test Suite (Module 9)."""

import pytest
from httpx import AsyncClient

from app.services.routing_service import routing_service


@pytest.mark.asyncio
async def test_calculate_primary_and_detour_routes():
    """Test core routing engine computing primary and detour routes."""
    origin_lat, origin_lon = 22.5835, 88.4410
    dest_lat, dest_lon = 22.5804, 88.4378

    result = await routing_service.calculate_rescue_routes(
        origin_lat=origin_lat,
        origin_lon=origin_lon,
        dest_lat=dest_lat,
        dest_lon=dest_lon,
        travel_mode="walking",
        avoid_hazards=True,
        include_aed_detour=False,
    )

    assert "primary_route" in result
    assert "detour_route" in result
    assert "recommendation" in result

    primary = result["primary_route"]
    assert primary["distance_meters"] > 0
    assert len(primary["steps"]) >= 3
    assert len(primary["polyline_points"]) >= 3
    assert "duration_formatted" in primary
    assert primary["route_type"] == "PRIMARY"

    detour = result["detour_route"]
    assert detour["route_type"] == "DETOUR"
    assert len(detour["steps"]) >= 3
    assert len(detour["polyline_points"]) >= 3


@pytest.mark.asyncio
async def test_aed_pickup_detour_route():
    """Test multi-leg AED pickup detour route calculation."""
    origin_lat, origin_lon = 22.5835, 88.4410
    aed_lat, aed_lon = 22.5806, 88.4385
    dest_lat, dest_lon = 22.5804, 88.4378

    result = await routing_service.calculate_rescue_routes(
        origin_lat=origin_lat,
        origin_lon=origin_lon,
        dest_lat=dest_lat,
        dest_lon=dest_lon,
        travel_mode="walking",
        avoid_hazards=True,
        include_aed_detour=True,
        aed_lat=aed_lat,
        aed_lon=aed_lon,
        aed_name="Webel Security Gate AED",
    )

    assert result["aed_pickup_route"] is not None
    aed_route = result["aed_pickup_route"]
    assert aed_route["route_type"] == "AED_PICKUP"
    assert aed_route["aed_waypoint"]["name"] == "Webel Security Gate AED"
    assert len(aed_route["steps"]) >= 2


@pytest.mark.asyncio
async def test_hazard_registry_and_avoidance():
    """Test dynamic road hazard registration, proximity lookup, and clearance."""
    # 1. Add test hazard
    hazard = routing_service.add_hazard(
        title="Test Fallen Tree on Salt Lake Bypass",
        hazard_type="ROAD_CLOSURE",
        severity="CRITICAL",
        latitude=22.5820,
        longitude=88.4400,
        radius_meters=150.0,
        description="Fallen banyan tree blocking all vehicle access.",
        delay_seconds=400,
    )
    hazard_id = hazard["hazard_id"]
    assert hazard_id.startswith("haz_")

    # 2. Check hazard is listed
    hazards = routing_service.get_all_hazards()
    assert any(h["hazard_id"] == hazard_id for h in hazards)

    # 3. Detect hazard along waypoints
    detected = routing_service.find_hazards_near_route([(22.5820, 88.4400)])
    assert any(h.hazard_id == hazard_id for h in detected)

    # 4. Remove hazard
    cleared = routing_service.remove_hazard(hazard_id)
    assert cleared is True
    assert not any(h["hazard_id"] == hazard_id for h in routing_service.get_all_hazards())


@pytest.mark.asyncio
async def test_directions_api_endpoints(client: AsyncClient):
    """Test POST /api/v1/routing/directions and compatibility /api/routing/directions."""
    payload = {
        "origin_latitude": 22.5835,
        "origin_longitude": 88.4410,
        "destination_latitude": 22.5804,
        "destination_longitude": 88.4378,
        "travel_mode": "walking",
        "avoid_hazards": True,
        "include_aed_detour": True,
        "aed_latitude": 22.5806,
        "aed_longitude": 88.4385,
        "aed_name": "Godrej Waterside Tower 1 Lobby AED",
    }

    # Versioned route
    res_v1 = await client.post("/api/v1/routing/directions", json=payload)
    assert res_v1.status_code == 200
    data_v1 = res_v1.json()
    assert data_v1["status"] == "success"
    assert "primary_route" in data_v1["data"]
    assert "detour_route" in data_v1["data"]
    assert "aed_pickup_route" in data_v1["data"]

    # Direct compatibility route
    res_compat = await client.post("/api/routing/directions", json=payload)
    assert res_compat.status_code == 200
    data_compat = res_compat.json()
    assert data_compat["status"] == "success"


@pytest.mark.asyncio
async def test_detour_analysis_endpoint(client: AsyncClient):
    """Test POST /api/v1/routing/detour endpoint."""
    payload = {
        "origin_latitude": 22.5835,
        "origin_longitude": 88.4410,
        "destination_latitude": 22.5804,
        "destination_longitude": 88.4378,
        "travel_mode": "bike",
        "avoid_hazards": True,
    }

    res = await client.post("/api/v1/routing/detour", json=payload)
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "success"
    assert "detour_route" in data
    assert "recommendation" in data


@pytest.mark.asyncio
async def test_hazards_api_crud(client: AsyncClient):
    """Test GET /api/v1/routing/hazards, POST, and DELETE."""
    # 1. List hazards
    res_list = await client.get("/api/v1/routing/hazards")
    assert res_list.status_code == 200
    assert res_list.json()["status"] == "success"

    # 2. Report new hazard
    report_payload = {
        "title": "Sector V Waterlogging at Webel Gate",
        "hazard_type": "FLOODING",
        "severity": "CRITICAL",
        "latitude": 22.5812,
        "longitude": 88.4388,
        "radius_meters": 100.0,
        "description": "Severe puddle blocking two-wheeler lane",
        "delay_seconds": 240,
        "is_passable_for_emergency": False,
    }
    res_create = await client.post("/api/v1/routing/hazards", json=report_payload)
    assert res_create.status_code == 201
    created_data = res_create.json()
    assert created_data["status"] == "success"
    hazard_id = created_data["hazard"]["hazard_id"]

    # 3. Delete / Resolve hazard
    res_del = await client.delete(f"/api/v1/routing/hazards/{hazard_id}")
    assert res_del.status_code == 200
    assert res_del.json()["status"] == "success"
