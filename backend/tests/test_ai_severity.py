"""NearHelp AI — Backend AI Severity Prediction Proxy & Fallback Tests."""

import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_backend_severity_endpoint(client: AsyncClient):
    """Test POST /api/ai/severity via backend proxy."""
    payload = {
        "text": "Elderly person having crushing chest pain, collapsed, unresponsive, gasping for air",
        "emergency_type": "medical",
        "sub_type": "cardiac_arrest",
        "unresponsive": True,
        "location": [22.5726, 88.4312],
    }
    resp = await client.post("/api/ai/severity", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["severity_score"] >= 80
    assert data["severity_level"] == 5
    assert data["priority"] == "critical"
    assert data["confidence"] >= 0.85
    assert data["recommended_radius_km"] >= 3.0
    assert data["survival_window_minutes"] == 5
    assert data["auto_call_emergency_services"] is True
    assert data["suggested_call_action"] == "auto_dial"
    assert data["emergency_number"] == "108"
    assert "factors" in data
    assert data["factors"]["life_threat_score"] >= 90.0
    assert len(data["reasoning"]) >= 1
    assert "CPR_CERTIFIED" in data["required_responder_skills"] or "DOCTOR" in data["required_responder_skills"]


@pytest.mark.asyncio
async def test_backend_v1_severity_endpoint(client: AsyncClient):
    """Test POST /api/v1/ai/severity versioned endpoint."""
    payload = {
        "text": "Arterial bleed from deep cut on arm, blood spurting pulsatile",
        "emergency_type": "medical",
        "sub_type": "severe_bleeding",
        "severe_bleeding": True,
    }
    resp = await client.post("/api/v1/ai/severity", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["severity_level"] == 4
    assert 50 <= data["severity_score"] <= 79
    assert data["priority"] == "high"
    assert data["suggested_call_action"] == "suggested"


@pytest.mark.asyncio
async def test_backend_offline_fallback_severity(client: AsyncClient):
    """Test that backend falls back gracefully without 500 error when AI service is offline."""
    payload = {
        "text": "Apartment on fire with heavy smoke billowing from windows",
    }
    resp = await client.post("/api/ai/severity", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["severity_level"] == 5
    assert data["severity_score"] >= 80
    assert data["emergency_number"] == "101"
    assert data["auto_call_emergency_services"] is True
