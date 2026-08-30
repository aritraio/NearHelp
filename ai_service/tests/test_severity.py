"""NearHelp AI — AI Severity Prediction & Clinical Triage Test Suite."""

import pytest
from httpx import AsyncClient

from app.classifiers.severity_predictor import severity_predictor
from app.schemas.severity import SeverityRequest


# ==============================================================================
# 1. LEVEL 5: CRITICAL LIFE THREAT SCENARIOS (SCORE 80–100, RADIUS 3–5 KM)
# ==============================================================================

@pytest.mark.asyncio
async def test_cardiac_arrest_severity_scoring():
    """Verify cardiac arrest is triaged as Level 5 (80-100) with 5-min hypoxia window and auto-call 108."""
    req = SeverityRequest(
        text="Victim collapsed in lobby, unresponsive, clutching chest, gasping for air, no pulse detected",
        emergency_type="medical",
        sub_type="cardiac_arrest",
        unresponsive=True,
        breathing_difficulty=True,
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 5
    assert res.severity_score >= 80
    assert res.priority == "critical"
    assert res.recommended_radius_km >= 3.0
    assert res.survival_window_minutes == 5
    assert res.auto_call_emergency_services is True
    assert res.suggested_call_action == "auto_dial"
    assert res.emergency_number == "108"
    assert "CPR_CERTIFIED" in res.required_responder_skills or "DOCTOR" in res.required_responder_skills
    assert res.factors.life_threat_score >= 90.0
    assert res.factors.time_sensitivity_score >= 90.0
    assert len(res.reasoning) >= 2
    assert any("hypoxia" in r.lower() or "cardiac" in r.lower() or "cpr" in r.lower() for r in res.reasoning)


@pytest.mark.asyncio
async def test_anaphylactic_shock_severity():
    """Verify acute anaphylaxis with airway compromise is Level 5."""
    req = SeverityRequest(
        text="Severe allergic reaction to peanut, throat swelling and closing up, severe hives, cyanotic gasping",
        emergency_type="medical",
        sub_type="anaphylaxis_allergy",
        breathing_difficulty=True,
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 5
    assert res.severity_score >= 80
    assert res.priority == "critical"
    assert res.auto_call_emergency_services is True
    assert res.survival_window_minutes is not None and res.survival_window_minutes <= 10
    assert "Epinephrine" in str(res.recommended_actions) or "EpiPen" in str(res.recommended_actions)


@pytest.mark.asyncio
async def test_drowning_hypoxia_severity():
    """Verify water submersion drowning scenario has 5-minute hypoxia window."""
    req = SeverityRequest(
        text="Person pulled from pond water, unresponsive, not breathing, cold and blue lips",
        emergency_type="accident",
        sub_type="drowning",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 5
    assert res.severity_score >= 80
    assert res.survival_window_minutes == 5
    assert res.auto_call_emergency_services is True
    assert "CPR_CERTIFIED" in res.required_responder_skills


@pytest.mark.asyncio
async def test_structural_fire_severity():
    """Verify building fire with trapped occupants triggers Level 5 and 101 Fire Brigade."""
    req = SeverityRequest(
        text="Apartment building on fire, heavy black smoke, flames spreading rapidly, people trapped inside",
        emergency_type="fire",
        sub_type="structural_fire",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 5
    assert res.severity_score >= 80
    assert res.emergency_number == "101"
    assert res.auto_call_emergency_services is True
    assert "FIRE_SAFETY" in res.required_responder_skills
    assert res.factors.environmental_hazard_score >= 85.0


@pytest.mark.asyncio
async def test_lpg_gas_leak_severity():
    """Verify LPG gas leak with explosion hazard triggers Level 5 and 101."""
    req = SeverityRequest(
        text="Strong smell of LPG cooking gas in apartment, cylinder hissing loudly, explosion risk",
        emergency_type="gas_leak",
        sub_type="lpg_gas_leak",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 5
    assert res.severity_score >= 80
    assert res.emergency_number == "101"
    assert res.auto_call_emergency_services is True


# ==============================================================================
# 2. LEVEL 4: URGENT TRAUMA SCENARIOS (SCORE 50–79, RADIUS 2–3 KM)
# ==============================================================================

@pytest.mark.asyncio
async def test_severe_bleeding_severity():
    """Verify severe arterial hemorrhage is Level 4 with tourniquet recommendation."""
    req = SeverityRequest(
        text="Deep cut on arm, bright red blood spurting pulsatile, victim pale and weak",
        emergency_type="medical",
        sub_type="severe_bleeding",
        severe_bleeding=True,
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 4
    assert 50 <= res.severity_score <= 79
    assert res.priority == "high"
    assert 2.0 <= res.recommended_radius_km <= 3.0
    assert res.suggested_call_action == "suggested"
    assert "FIRST_AID" in res.required_responder_skills
    assert any("pressure" in a.lower() or "tourniquet" in a.lower() for a in res.recommended_actions)


@pytest.mark.asyncio
async def test_stroke_fast_severity():
    """Verify acute stroke FAST is Level 4 with time-of-onset directive."""
    req = SeverityRequest(
        text="Elderly victim facial droop on right side, slurred speech, right arm weakness",
        emergency_type="medical",
        sub_type="stroke",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 4
    assert 50 <= res.severity_score <= 79
    assert res.priority == "high"
    assert any("FAST" in r or "stroke" in r.lower() or "neurological" in r.lower() for r in res.reasoning)


@pytest.mark.asyncio
async def test_compound_fracture_severity():
    """Verify open compound fracture is Level 4 with immobilization directive."""
    req = SeverityRequest(
        text="Fell from ladder, leg deformed at unnatural angle, bone protruding through skin",
        emergency_type="medical",
        sub_type="fracture_trauma",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 4
    assert 50 <= res.severity_score <= 79
    assert any("immobilize" in a.lower() for a in res.recommended_actions)


# ==============================================================================
# 3. LEVEL 3: MODERATE EMERGENCY SCENARIOS (SCORE 20–49, RADIUS 1–2 KM)
# ==============================================================================

@pytest.mark.asyncio
async def test_thermal_burns_severity():
    """Verify thermal burns is Level 3 with cool water cooling directive."""
    req = SeverityRequest(
        text="Hot cooking oil splashed on forearm, painful blisters forming, skin reddened",
        emergency_type="medical",
        sub_type="severe_burns",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 3
    assert 20 <= res.severity_score <= 49
    assert res.priority == "medium"
    assert 1.0 <= res.recommended_radius_km <= 2.0
    assert res.auto_call_emergency_services is False
    assert res.suggested_call_action == "optional"
    assert any("running tap water" in a.lower() or "cool" in a.lower() for a in res.recommended_actions)


# ==============================================================================
# 4. LEVEL 1–2: MINOR / NON-ACUTE SCENARIOS (SCORE < 20, RADIUS 0.5–1 KM)
# ==============================================================================

@pytest.mark.asyncio
async def test_minor_scratch_severity():
    """Verify minor scrape / abrasion is Level 1 or 2 with low priority."""
    req = SeverityRequest(
        text="Minor paper cut and small scrape on finger, slight bleeding stopped with tissue, mild headache",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level in (1, 2)
    assert res.severity_score < 20
    assert res.priority == "low"
    assert res.recommended_radius_km <= 1.0
    assert res.auto_call_emergency_services is False
    assert res.suggested_call_action == "none"


# ==============================================================================
# 5. MULTILINGUAL SEVERITY EVALUATION (BENGALI & HINDI)
# ==============================================================================

@pytest.mark.asyncio
async def test_bengali_cardiac_severity():
    """Verify Bengali cardiac emergency phrase is scored as Level 5 Critical."""
    req = SeverityRequest(
        text="বুকে তীব্র ব্যথা নিয়ে মাটিতে পড়ে অজ্ঞান হয়ে গেছেন, শ্বাস নিচ্ছেন না, ডাকলে সাড়া দিচ্ছেন না",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 5
    assert res.severity_score >= 80
    assert res.emergency_number == "108"
    assert res.auto_call_emergency_services is True


@pytest.mark.asyncio
async def test_bengali_gas_leak_severity():
    """Verify Bengali gas leak phrase triggers Level 5 and 101."""
    req = SeverityRequest(
        text="গ্যাস সিলিন্ডার লিক করছে তীব্র গন্ধ বের হচ্ছে বিস্ফোরণের ভয়",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 5
    assert res.emergency_number == "101"


@pytest.mark.asyncio
async def test_hindi_cardiac_severity():
    """Verify Hindi transliterated cardiac emergency is scored as Level 5 Critical."""
    req = SeverityRequest(
        text="Chhati me tez dard hai, achanak behosh ho gaya, saans nahi le raha hai, cpr chahiye",
    )
    res = await severity_predictor.predict(req)

    assert res.severity_level == 5
    assert res.severity_score >= 80
    assert res.emergency_number == "108"


# ==============================================================================
# 6. HTTP API ENDPOINT INTEGRATION TESTS
# ==============================================================================

@pytest.mark.asyncio
async def test_severity_endpoint_http(client: AsyncClient):
    """Test POST /api/v1/severity HTTP endpoint."""
    payload = {
        "text": "Victim collapsed in office lobby, unresponsive, clutching chest, gasping for air",
        "emergency_type": "medical",
        "sub_type": "cardiac_arrest",
        "unresponsive": True,
        "location": [22.5726, 88.4312],
    }
    resp = await client.post("/api/v1/severity", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["severity_score"] >= 80
    assert data["severity_level"] == 5
    assert data["priority"] == "critical"
    assert data["confidence"] > 0.85
    assert data["recommended_radius_km"] >= 3.0
    assert data["survival_window_minutes"] == 5
    assert data["auto_call_emergency_services"] is True
    assert data["emergency_number"] == "108"
    assert "factors" in data
    assert data["factors"]["life_threat_score"] >= 90
    assert len(data["reasoning"]) >= 1
    assert data["processing_time_ms"] > 0


@pytest.mark.asyncio
async def test_canonical_severity_endpoint_http(client: AsyncClient):
    """Test canonical POST /severity HTTP endpoint."""
    payload = {
        "text": "Apartment fire with heavy smoke billowing",
        "emergency_type": "fire",
    }
    resp = await client.post("/severity", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["severity_level"] == 5
    assert data["emergency_number"] == "101"
