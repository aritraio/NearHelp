"""NearHelp AI — Emergency Crisis Assistant Agent Test Suite."""

import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_agent_get_all_protocols(client: AsyncClient):
    """Test retrieving all curated evidence-based first-aid protocols."""
    resp = await client.get("/api/v1/agent/protocols")
    assert resp.status_code == 200
    data = resp.json()
    assert isinstance(data, list)
    assert len(data) >= 8

    condition_ids = [p["condition_id"] for p in data]
    assert "cardiac_arrest" in condition_ids
    assert "severe_bleeding" in condition_ids
    assert "respiratory_asthma" in condition_ids
    assert "unconscious_seizure" in condition_ids
    assert "stroke" in condition_ids
    assert "severe_burns" in condition_ids
    assert "fracture_trauma" in condition_ids
    assert "anaphylaxis_allergy" in condition_ids


@pytest.mark.asyncio
async def test_agent_get_cardiac_protocol(client: AsyncClient):
    """Test retrieving specific cardiac arrest protocol with AHA/IRC citations and 110 BPM CPR."""
    resp = await client.get("/api/v1/agent/protocols/cardiac_arrest")
    assert resp.status_code == 200
    proto = resp.json()
    assert proto["condition_id"] == "cardiac_arrest"
    assert proto["severity_level"] == 5
    assert proto["priority"] == "critical"
    assert proto["cpr_bpm"] == 110
    assert len(proto["steps"]) >= 4

    # Verify CPR step has metronome flag
    cpr_steps = [s for s in proto["steps"] if s["is_cpr_step"]]
    assert len(cpr_steps) >= 1
    assert cpr_steps[0]["beat_bpm"] == 110

    # Verify citations
    citation_sources = [c["source"] for c in proto["citations"]]
    assert any("AHA" in s for s in citation_sources)
    assert any("Motor Vehicles" in s or "Good Samaritan" in s for s in citation_sources)


@pytest.mark.asyncio
async def test_agent_init_session(client: AsyncClient):
    """Test initializing a new emergency agent session."""
    payload = {
        "session_id": "test-session-001",
        "condition_id": "cardiac_arrest",
        "role": "bystander",
    }
    resp = await client.post("/api/v1/agent/init", json=payload)
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "initialized"
    assert data["session_id"] == "test-session-001"
    assert data["cpr_bpm"] == 110


@pytest.mark.asyncio
async def test_agent_chat_oral_fluids_contraindication(client: AsyncClient):
    """Test bystander asking about giving water triggers clinical contraindication guardrail."""
    payload = {
        "session_id": "test-session-002",
        "text": "The patient is unconscious on the ground. Can I give him some water or tea to drink?",
        "role": "bystander",
    }
    resp = await client.post("/api/v1/agent/chat", json=payload)
    assert resp.status_code == 200
    data = resp.json()
    assert "contraindications" in data
    assert len(data["contraindications"]) >= 1

    alert = data["contraindications"][0]
    assert alert["flag"] == "NO_ORAL_FLUIDS_UNCONSCIOUS"
    assert alert["severity"] == "CRITICAL"
    assert "DO NOT give any water" in alert["action_directive"] or "NEVER" in alert["warning_title"]

    # Verify reply text contains safety directive and citation
    assert "NO" in data["reply_text"] or "NEVER" in data["reply_text"]
    assert "pulmonary aspiration" in data["reply_text"].lower() or "airway" in data["reply_text"].lower()


@pytest.mark.asyncio
async def test_agent_chat_cpr_cadence_and_depth(client: AsyncClient):
    """Test querying CPR chest compression depth and rate."""
    payload = {
        "session_id": "test-session-003",
        "text": "How deep and fast should I push on the chest during CPR?",
        "role": "bystander",
    }
    resp = await client.post("/api/v1/agent/chat", json=payload)
    assert resp.status_code == 200
    data = resp.json()
    assert "5 to 6 cm" in data["reply_text"] or "5–6 cm" in data["reply_text"]
    assert "110–120" in data["reply_text"] or "110" in data["reply_text"]
    assert data["cpr_bpm"] == 110


@pytest.mark.asyncio
async def test_agent_chat_rib_crack_reassurance(client: AsyncClient):
    """Test bystander panicking over cracking rib sound during CPR."""
    payload = {
        "session_id": "test-session-004",
        "text": "I heard a cracking sound in the ribs when pressing! Should I stop CPR?",
        "role": "bystander",
    }
    resp = await client.post("/api/v1/agent/chat", json=payload)
    assert resp.status_code == 200
    data = resp.json()
    assert "DO NOT STOP" in data["reply_text"] or "CONTINUE" in data["reply_text"]


@pytest.mark.asyncio
async def test_agent_chat_good_samaritan_legal_shield(client: AsyncClient):
    """Test inquiry regarding Good Samaritan legal liability and Section 134A protection."""
    payload = {
        "session_id": "test-session-005",
        "text": "Am I legally protected if something goes wrong? Will the police harass me?",
        "role": "bystander",
    }
    resp = await client.post("/api/v1/agent/chat", json=payload)
    assert resp.status_code == 200
    data = resp.json()
    assert "Section 134A" in data["reply_text"]
    assert "Motor Vehicles" in data["reply_text"]
    assert data["legal_shield_applied"] is True


@pytest.mark.asyncio
async def test_agent_chat_thermal_burns_no_toothpaste(client: AsyncClient):
    """Test burns query warns against applying toothpaste or ice."""
    # First init with burn condition
    await client.post("/api/v1/agent/init", json={"session_id": "test-burn", "condition_id": "severe_burns"})

    payload = {
        "session_id": "test-burn",
        "text": "Boiling oil spilled on his arm. Can I put ice, toothpaste, or turmeric on the burn?",
        "role": "bystander",
    }
    resp = await client.post("/api/v1/agent/chat", json=payload)
    assert resp.status_code == 200
    data = resp.json()
    assert "contraindications" in data
    assert len(data["contraindications"]) >= 1
    assert data["contraindications"][0]["flag"] == "NO_ICE_OR_PASTE_ON_BURNS"
    assert "20 minutes" in data["reply_text"] or "cool running" in data["reply_text"].lower()


@pytest.mark.asyncio
async def test_agent_step_progress_tracking(client: AsyncClient):
    """Test advancing protocol steps and computing progress percentage."""
    session_id = "test-step-001"
    await client.post("/api/v1/agent/init", json={"session_id": session_id, "condition_id": "cardiac_arrest"})

    # Complete step 1
    resp1 = await client.post("/api/v1/agent/step", json={"session_id": session_id, "step_number": 1, "completed": True})
    assert resp1.status_code == 200
    data1 = resp1.json()
    assert data1["completed_steps"] == [1]
    assert data1["progress_percentage"] == 25
    assert not data1["all_completed"]

    # Complete step 2
    resp2 = await client.post("/api/v1/agent/step", json={"session_id": session_id, "step_number": 2, "completed": True})
    assert resp2.status_code == 200
    data2 = resp2.json()
    assert data2["completed_steps"] == [1, 2]
    assert data2["progress_percentage"] == 50


@pytest.mark.asyncio
async def test_agent_handover_report_generation(client: AsyncClient):
    """Test generating a structured clinical handover report for arriving paramedics."""
    session_id = "test-handover-001"
    await client.post("/api/v1/agent/init", json={"session_id": session_id, "condition_id": "cardiac_arrest"})
    await client.post("/api/v1/agent/step", json={"session_id": session_id, "step_number": 1, "completed": True})
    await client.post("/api/v1/agent/step", json={"session_id": session_id, "step_number": 2, "completed": True})

    resp = await client.post("/api/v1/agent/handover", json={"session_id": session_id, "text": "Ambulance arrived on scene."})
    assert resp.status_code == 200
    report = resp.json()
    assert "report_id" in report
    assert report["severity_level"] == 5
    assert report["cpr_compressions_estimated"] > 0
    assert report["cpr_metronome_used"] is True
    assert "SHA256:" in report["digital_signature_hash"]
    assert "Section 134A" in report["legal_shield_compliance"]
    assert "AMRI Hospital" in report["destination_hospital"]
