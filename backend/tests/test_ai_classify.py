"""NearHelp AI — Backend AI Classification Proxy & Fallback Tests."""

import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_backend_classify_endpoint(client: AsyncClient):
    """Test POST /api/ai/classify via backend proxy."""
    payload = {
        "text": "Elderly person having crushing chest pain, collapsed, unresponsive, gasping for air",
        "location": [22.5726, 88.4312],
    }
    resp = await client.post("/api/ai/classify", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["emergency_type"] == "medical"
    assert data["sub_type"] == "cardiac_arrest"
    assert data["priority"] == "critical"
    assert data["severity_level"] == 5
    assert data["confidence"] >= 0.80
    assert data["emergency_number"] == "108"
    assert data["recommended_radius_km"] >= 3.0
    assert len(data["suggested_responder_skills"]) > 0


@pytest.mark.asyncio
async def test_backend_v1_classify_endpoint(client: AsyncClient):
    """Test POST /api/v1/ai/classify versioned route."""
    payload = {
        "text": "Severe road accident on highway, two cars collided head on, trapped bleeding victims",
        "location": [22.5726, 88.4312],
    }
    resp = await client.post("/api/v1/ai/classify", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["emergency_type"] in ("accident", "medical")
    assert data["severity_level"] in (4, 5)
    assert data["priority"] in ("high", "critical")


@pytest.mark.asyncio
async def test_backend_taxonomy_endpoint(client: AsyncClient):
    """Test GET /api/ai/taxonomy via backend proxy."""
    resp = await client.get("/api/ai/taxonomy")
    assert resp.status_code == 200
    data = resp.json()

    assert len(data["crisis_types"]) >= 2
    assert len(data["clinical_conditions"]) >= 1


@pytest.mark.asyncio
async def test_backend_offline_fallback_triage(client: AsyncClient):
    """Test that backend falls back gracefully without 500 error when AI service is offline."""
    payload = {
        "text": "Fire breaking out in building, heavy smoke and flames",
    }
    resp = await client.post("/api/ai/classify", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["emergency_type"] == "fire"
    assert data["sub_type"] == "structural_fire"
    assert data["emergency_number"] == "101"
