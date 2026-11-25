"""NearHelp AI — Emergency SOS Website & Zero-Login Guest Flow Tests."""

import uuid

import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_sos_web_pages_and_assets_served(client: AsyncClient):
    """Verify the SOS-only web app and static assets are served with strict CSP headers."""
    res_sos = await client.get("/sos")
    assert res_sos.status_code == 200
    assert "text/html" in res_sos.headers["content-type"]
    assert "default-src 'self'" in res_sos.headers["content-security-policy"]
    assert "NearHelp" in res_sos.text
    assert "Hold for 2 seconds to trigger it" in res_sos.text
    assert "login-form" not in res_sos.text.lower()

    res_css = await client.get("/sos/assets/style.css")
    assert res_css.status_code == 200
    assert "--primary: #17635d" in res_css.text

    res_js = await client.get("/sos/assets/app.js")
    assert res_js.status_code == 200
    assert "/api/v1/sos/create" in res_js.text

    res_browser_root = await client.get("/", headers={"Accept": "text/html,application/xhtml+xml"})
    assert res_browser_root.status_code == 200
    assert "text/html" in res_browser_root.headers["content-type"]
    assert "Hold for 2 seconds to trigger it" in res_browser_root.text

    res_api_root = await client.get("/", headers={"Accept": "application/json"})
    assert res_api_root.status_code == 200
    assert res_api_root.json()["status"] == "online"


@pytest.mark.asyncio
async def test_guest_sos_create_idempotency_and_cancel_token(client: AsyncClient):
    """Verify zero-login SOS creation, idempotent replay, and capability-token cancellation."""
    idem_key = str(uuid.uuid4())
    payload = {
        "latitude": 22.5726,
        "longitude": 88.3639,
        "crisis_type": "medical",
        "description": "Medical emergency: Chest pain",
        "address": "Salt Lake Sector V",
        "is_anonymous": True,
    }

    create_res = await client.post(
        "/api/v1/sos/create",
        headers={"Idempotency-Key": idem_key},
        json=payload,
    )
    assert create_res.status_code == 201
    created = create_res.json()
    sos_id = created["id"]
    cancel_token = created["guest_cancel_token"]
    assert cancel_token and len(cancel_token) >= 20
    assert created["broadcaster_id"] is None

    # Idempotent retry replays the same ID and guest cancel token
    replay_res = await client.post(
        "/api/v1/sos/create",
        headers={"Idempotency-Key": idem_key},
        json=payload,
    )
    assert replay_res.status_code == 201
    assert replay_res.json()["id"] == sos_id
    assert replay_res.json()["guest_cancel_token"] == cancel_token

    # Public detail endpoint must not expose the guest cancel token
    detail_res = await client.get(f"/api/v1/sos/{sos_id}")
    assert detail_res.status_code == 200
    assert "guest_cancel_token" not in detail_res.json()

    # Cancel without token -> 401; wrong token -> 403; valid token -> 200
    assert (await client.put(f"/api/v1/sos/{sos_id}/cancel")).status_code == 401
    assert (
        await client.put(
            f"/api/v1/sos/{sos_id}/cancel",
            headers={"X-SOS-Cancel-Token": "wrong-token"},
        )
    ).status_code == 403

    cancel_res = await client.put(
        f"/api/v1/sos/{sos_id}/cancel",
        headers={"X-SOS-Cancel-Token": cancel_token},
    )
    assert cancel_res.status_code == 200
    assert cancel_res.json()["status"] == "CANCELLED"

    # Timeline reflects guest cancellation
    timeline_res = await client.get(f"/api/v1/sos/{sos_id}/timeline")
    assert timeline_res.status_code == 200
    events = timeline_res.json()
    assert any(ev["event_type"] == "sos_cancelled" for ev in events)
