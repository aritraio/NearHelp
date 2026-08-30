"""NearHelp AI — AI Emergency Detection & Multimodal Classification Test Suite."""

import base64
import pytest
from httpx import ASGITransport, AsyncClient

from app.classifiers.crisis_types import CLINICAL_CONDITIONS_MATRIX, CRISIS_TYPES_TAXONOMY
from app.classifiers.emergency_classifier import emergency_classifier
from app.main import app
from app.schemas.classify import ClassificationRequest

# ==============================================================================
# 1. 8 CURATED CLINICAL CONDITIONS ACCURACY TESTS
# ==============================================================================

@pytest.mark.asyncio
async def test_cardiac_arrest_classification():
    """Verify cardiac arrest scenario is triaged with Level 5 Critical priority."""
    req = ClassificationRequest(
        text="Victim collapsed in office lobby, unresponsive, clutching chest, gasping for air, no pulse"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "cardiac_arrest"
    assert res.severity_level == 5
    assert res.priority == "critical"
    assert res.confidence >= 0.80
    assert res.recommended_radius_km >= 3.0
    assert "CPR_CERTIFIED" in res.suggested_responder_skills or "DOCTOR" in res.suggested_responder_skills
    assert res.emergency_number == "108"
    assert "CPR" in res.immediate_action


@pytest.mark.asyncio
async def test_severe_bleeding_classification():
    """Verify severe arterial hemorrhage classification."""
    req = ClassificationRequest(
        text="Heavy bleeding from deep forearm cut, bright red blood spurting pulsatile, victim pale and dizzy"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "severe_bleeding"
    assert res.severity_level == 4
    assert res.priority in ("high", "critical")
    assert res.confidence >= 0.80
    assert "FIRST_AID" in res.suggested_responder_skills
    assert "pressure" in res.immediate_action.lower() or "tourniquet" in res.immediate_action.lower()


@pytest.mark.asyncio
async def test_respiratory_asthma_classification():
    """Verify respiratory distress / acute asthma bronchospasm classification."""
    req = ClassificationRequest(
        text="Severe asthma attack, struggling to breathe, blue lips, severe wheezing, oxygen saturation dropping"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "respiratory_asthma"
    assert res.severity_level == 5
    assert res.priority == "critical"
    assert res.confidence >= 0.80
    assert res.emergency_number == "108"


@pytest.mark.asyncio
async def test_unconscious_seizure_classification():
    """Verify tonic-clonic seizure / convulsion classification."""
    req = ClassificationRequest(
        text="Person having violent seizure on floor, shaking uncontrollably, foaming at mouth, unresponsive"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "unconscious_seizure"
    assert res.severity_level == 5
    assert res.priority == "critical"
    assert res.confidence >= 0.80


@pytest.mark.asyncio
async def test_stroke_fast_protocol_classification():
    """Verify acute stroke / FAST protocol triage."""
    req = ClassificationRequest(
        text="Elderly patient face is drooping on right side, right arm weak and falling, slurred speech, confused"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "stroke"
    assert res.severity_level == 4
    assert res.confidence >= 0.80


@pytest.mark.asyncio
async def test_severe_thermal_burns_classification():
    """Verify thermal scald / flame burn classification."""
    req = ClassificationRequest(
        text="Boiling oil spilled over hands and torso, extensive blistered charred skin, excruciating agony"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "severe_burns"
    assert res.severity_level == 3
    assert "water" in res.immediate_action.lower() or "cool" in res.immediate_action.lower()


@pytest.mark.asyncio
async def test_compound_fracture_trauma_classification():
    """Verify open compound fracture and orthopedic trauma classification."""
    req = ClassificationRequest(
        text="Fell from ladder, leg broken at unnatural angle, bone protruding through skin, severe bleeding"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "fracture_trauma"
    assert res.severity_level == 4
    assert res.confidence >= 0.80


@pytest.mark.asyncio
async def test_anaphylaxis_shock_classification():
    """Verify systemic anaphylactic shock classification."""
    req = ClassificationRequest(
        text="Ate peanut, throat swelling and closing up, severe hives all over body, struggling to breathe, fainting"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "anaphylaxis_allergy"
    assert res.severity_level == 5
    assert res.priority == "critical"
    assert res.confidence >= 0.80
    assert "epinephrine" in res.immediate_action.lower() or "epipen" in res.immediate_action.lower()


# ==============================================================================
# 2. NON-MEDICAL CRISIS CATEGORIES TESTS
# ==============================================================================

@pytest.mark.asyncio
async def test_structural_fire_classification():
    """Verify building fire outbreak classification."""
    req = ClassificationRequest(
        text="Apartment building on fire, heavy black smoke billowing from windows, flames spreading fast"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "fire"
    assert res.sub_type == "structural_fire"
    assert res.severity_level == 5
    assert res.emergency_number == "101"


@pytest.mark.asyncio
async def test_lpg_gas_leak_classification():
    """Verify LPG cylinder gas leak classification."""
    req = ClassificationRequest(
        text="Strong pungent smell of cooking gas in kitchen, LPG cylinder hissing loudly, risk of explosion"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "gas_leak"
    assert res.sub_type == "lpg_gas_leak"
    assert res.severity_level == 5
    assert res.emergency_number == "101"


@pytest.mark.asyncio
async def test_road_accident_collision_classification():
    """Verify vehicular collision road accident classification."""
    req = ClassificationRequest(
        text="Severe car crash on highway, two cars collided head-on, multiple passengers injured and bleeding"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "accident"
    assert res.sub_type == "road_accident"
    assert res.severity_level == 5
    assert res.emergency_number == "108"


@pytest.mark.asyncio
async def test_violent_assault_classification():
    """Verify violent crime / weapon attack classification."""
    req = ClassificationRequest(
        text="Violent physical assault in alley, knife attack, person stabbed and bleeding profusely, police needed"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "crime"
    assert res.sub_type == "physical_assault"
    assert res.emergency_number == "100"


@pytest.mark.asyncio
async def test_urban_flood_collapse_classification():
    """Verify structural collapse disaster classification."""
    req = ClassificationRequest(
        text="Old building balcony collapsed on street, people trapped under concrete rubble and bricks"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "natural_disaster"
    assert res.sub_type == "urban_flood_collapse"
    assert res.emergency_number == "112"


# ==============================================================================
# 3. MULTI-LINGUAL EMERGENCY TRIAGE TESTS (BENGALI & HINDI)
# ==============================================================================

@pytest.mark.asyncio
async def test_bengali_cardiac_emergency():
    """Verify Bengali emergency phrase detection."""
    req = ClassificationRequest(
        text="মাটিতে পড়ে গেছেন, ডাকলে সাড়া দিচ্ছেন না, শ্বাস নিচ্ছেন না, বুকে প্রচণ্ড ব্যথা"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "cardiac_arrest"
    assert res.severity_level == 5
    assert res.confidence >= 0.80


@pytest.mark.asyncio
async def test_bengali_fire_emergency():
    """Verify Bengali fire emergency phrase detection."""
    req = ClassificationRequest(
        text="বাড়িতে ভয়াবহ আগুন লেগেছে, চারদিক কালো ধোঁয়ায় ঢেকে গেছে"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "fire"
    assert res.sub_type == "structural_fire"
    assert res.emergency_number == "101"


@pytest.mark.asyncio
async def test_hindi_bleeding_emergency():
    """Verify Hindi transliterated bleeding emergency phrase detection."""
    req = ClassificationRequest(
        text="Chot lagne se bohot tez khoon beh raha hai, artery cut ho gayi, patti se khoon nahi ruk raha"
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "severe_bleeding"
    assert res.severity_level == 4


# ==============================================================================
# 4. MULTIMODAL VOICE & PHOTO CLASSIFICATION TESTS
# ==============================================================================

@pytest.mark.asyncio
async def test_voice_audio_multimodal_classification():
    """Verify audio payload is transcribed and classified."""
    audio_sample = base64.b64encode(b"Person collapsed on ground, not responding to voice, gasping for air").decode("utf-8")

    req = ClassificationRequest(
        audio_base64=audio_sample,
        audio_format="wav",
        language_code="en-IN",
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "medical"
    assert res.sub_type == "cardiac_arrest"
    assert res.transcription is not None
    assert "collapsed" in res.transcription.lower() or "not responding" in res.transcription.lower()


@pytest.mark.asyncio
async def test_photo_multimodal_classification():
    """Verify photo payload is analyzed for scene description and classified."""
    image_sample = base64.b64encode(b"Active fire with heavy smoke").decode("utf-8")

    req = ClassificationRequest(
        image_base64=image_sample,
        image_mime_type="image/jpeg",
    )
    res = await emergency_classifier.classify(req)

    assert res.emergency_type == "fire"
    assert res.image_description is not None
    assert "fire" in res.image_description.lower() or "smoke" in res.image_description.lower()


# ==============================================================================
# 5. FASTAPI REST ENDPOINTS INTEGRATION TESTS
# ==============================================================================

@pytest.mark.asyncio
async def test_classify_endpoint_http(client: AsyncClient):
    """Test POST /api/v1/classify HTTP endpoint."""
    payload = {
        "text": "Victim collapsed in office lobby, unresponsive, clutching chest, gasping for air",
        "location": [22.5726, 88.4312]
    }
    resp = await client.post("/api/v1/classify", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["emergency_type"] == "medical"
    assert data["sub_type"] == "cardiac_arrest"
    assert data["priority"] == "critical"
    assert data["severity_level"] == 5
    assert data["confidence"] > 0.80
    assert data["recommended_radius_km"] >= 3.0
    assert data["emergency_number"] == "108"
    assert data["processing_time_ms"] > 0


@pytest.mark.asyncio
async def test_taxonomy_endpoint_http(client: AsyncClient):
    """Test GET /api/v1/taxonomy HTTP endpoint."""
    resp = await client.get("/api/v1/taxonomy")
    assert resp.status_code == 200
    data = resp.json()

    assert len(data["crisis_types"]) >= 6
    assert len(data["clinical_conditions"]) == 8

    # Verify 8 clinical conditions are present
    condition_ids = [c["id"] for c in data["clinical_conditions"]]
    expected_ids = [
        "cardiac_arrest", "severe_bleeding", "respiratory_asthma",
        "unconscious_seizure", "stroke", "severe_burns",
        "fracture_trauma", "anaphylaxis_allergy"
    ]
    for expected_id in expected_ids:
        assert expected_id in condition_ids
