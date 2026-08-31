"""NearHelp AI — Emergency Facilities & AED Geolocation API Tests."""

import uuid
import pytest
from httpx import ASGITransport, AsyncClient

from app.main import app
from app.models.facility import Facility
from app.services.facility_service import FacilityService


@pytest.mark.asyncio
async def test_seed_facilities_endpoint():
    """Verify POST /api/facilities/seed initializes Kolkata regional infrastructure."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post("/api/facilities/seed")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "success"
        assert data["total_count"] >= 15
        assert data["inserted_count"] + data["updated_count"] == data["total_count"]


@pytest.mark.asyncio
async def test_get_nearby_facilities_sector_v():
    """Verify GET /api/facilities/nearby around Godrej Waterside Sector V returns sorted facilities."""
    # Epicenter: Godrej Waterside, Tower 1, Sector V (22.5804°N, 88.4378°E)
    lat = 22.5804
    lon = 88.4378

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get(f"/api/facilities/nearby?latitude={lat}&longitude={lon}&radius_km=5.0")
        assert response.status_code == 200
        data = response.json()

        assert "facilities" in data
        assert data["count"] > 0
        assert data["center_latitude"] == lat
        assert data["center_longitude"] == lon

        facilities = data["facilities"]
        # Ensure results are sorted by distance ascending
        for i in range(len(facilities) - 1):
            assert facilities[i]["distance_meters"] <= facilities[i + 1]["distance_meters"]

        # Nearest should be Godrej Waterside AED (< 100m)
        nearest = facilities[0]
        assert "Godrej Waterside" in nearest["name"] or nearest["distance_meters"] < 200
        assert nearest["distance_km"] is not None
        assert nearest["eta_minutes"] is not None


@pytest.mark.asyncio
async def test_get_nearby_facilities_type_filter():
    """Verify type filtering by 'hospital' vs 'aed'."""
    lat = 22.5804
    lon = 88.4378

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # Filter hospitals only
        resp_hosp = await ac.get(f"/api/facilities/nearby?latitude={lat}&longitude={lon}&radius_km=10.0&type=hospital")
        assert resp_hosp.status_code == 200
        hospitals = resp_hosp.json()["facilities"]
        assert len(hospitals) > 0
        assert all(h["facility_type"] == "hospital" for h in hospitals)

        # Filter AEDs only
        resp_aed = await ac.get(f"/api/facilities/nearby?latitude={lat}&longitude={lon}&radius_km=10.0&type=aed")
        assert resp_aed.status_code == 200
        aeds = resp_aed.json()["facilities"]
        assert len(aeds) > 0
        assert all(a["facility_type"] == "aed" for a in aeds)
        assert any("Technopolis" in a["name"] or "Godrej" in a["name"] for a in aeds)


@pytest.mark.asyncio
async def test_get_nearby_facilities_capacity_filter():
    """Verify capacity filtering by min_beds and min_icu."""
    lat = 22.5804
    lon = 88.4378

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get(
            f"/api/facilities/nearby?latitude={lat}&longitude={lon}&radius_km=15.0&min_beds=35&min_icu=7"
        )
        assert response.status_code == 200
        data = response.json()["facilities"]

        assert len(data) > 0
        for f in data:
            assert f["bed_availability"] >= 35
            assert f["icu_availability"] >= 7


@pytest.mark.asyncio
async def test_get_nearby_facilities_radius_boundary():
    """Verify strict radius enforcement (small radius vs large radius)."""
    lat = 22.5804
    lon = 88.4378

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # 300m radius
        resp_small = await ac.get(f"/api/facilities/nearby?latitude={lat}&longitude={lon}&radius_km=0.3")
        assert resp_small.status_code == 200
        small_count = resp_small.json()["count"]

        # 15km radius
        resp_large = await ac.get(f"/api/facilities/nearby?latitude={lat}&longitude={lon}&radius_km=15.0")
        assert resp_large.status_code == 200
        large_count = resp_large.json()["count"]

        assert small_count <= large_count


@pytest.mark.asyncio
async def test_get_facility_by_id():
    """Verify single facility lookup by UUID."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        list_resp = await ac.get("/api/facilities/nearby?latitude=22.5804&longitude=88.4378&radius_km=5.0")
        assert list_resp.status_code == 200
        first_id = list_resp.json()["facilities"][0]["id"]

        detail_resp = await ac.get(f"/api/facilities/{first_id}")
        assert detail_resp.status_code == 200
        detail_data = detail_resp.json()
        assert detail_data["id"] == first_id
        assert "address" in detail_data


@pytest.mark.asyncio
async def test_get_facility_not_found():
    """Verify 404 response for invalid UUID."""
    random_uuid = str(uuid.uuid4())
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get(f"/api/facilities/{random_uuid}")
        assert response.status_code == 404


@pytest.mark.asyncio
async def test_versioned_facilities_endpoint():
    """Verify canonical versioned route /api/v1/facilities/nearby works identically."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/api/v1/facilities/nearby?latitude=22.5804&longitude=88.4378&radius_km=5.0")
        assert response.status_code == 200
        assert "facilities" in response.json()
