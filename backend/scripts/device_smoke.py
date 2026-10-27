"""Two-user network smoke test. Creates test users and an SOS on the selected backend.

Run only against a development/test backend:
    python scripts/device_smoke.py --base-url http://127.0.0.1:8000
"""
import argparse
import asyncio
import json
import uuid

import httpx
import websockets


async def receive(socket, kind: str) -> dict:
    async with asyncio.timeout(10):
        while True:
            frame = json.loads(await socket.recv())
            if frame.get("type") == "error":
                raise RuntimeError(frame)
            if frame.get("type") == kind:
                return frame


async def run(base_url: str) -> None:
    base_url = base_url.rstrip("/")
    async with httpx.AsyncClient(base_url=base_url, timeout=60) as client:
        async def call(method, path, **kwargs):
            response = await client.request(method, path, **kwargs)
            response.raise_for_status()
            return response.json()

        assert (await call("GET", "/health"))["database"] == "connected"
        accounts = []
        for role in ("Victim", "Responder"):
            account = await call("POST", "/api/v1/auth/register", json={
                "email": f"device-check-{uuid.uuid4().hex}@example.com",
                "password": f"DeviceCheck!{uuid.uuid4().hex}", "name": f"Test {role}",
            })
            accounts.append(account)
        victim, responder = accounts
        vh = {"Authorization": f"Bearer {victim['access_token']}"}
        rh = {"Authorization": f"Bearer {responder['access_token']}"}
        await call("PUT", "/api/v1/users/me/location", headers=rh, json={
            "latitude": 22.574, "longitude": 88.365, "available": True,
        })
        payload = {"latitude": 22.5726, "longitude": 88.3639, "crisis_type": "medical", "description": "Integration test only", "address": "Test incident"}
        create_headers = {**vh, "Idempotency-Key": uuid.uuid4().hex}
        incident = await call("POST", "/api/v1/sos/create", headers=create_headers, json=payload)
        incident_id = incident["id"]
        try:
            assert responder["user"]["id"] in [r["responder_id"] for r in incident["top_ranked_responders"]]
            replay = await call("POST", "/api/v1/sos/create", headers=create_headers, json=payload)
            assert replay["id"] == incident_id
            feed = await call("GET", "/api/v1/sos/active", headers=rh)
            assert incident_id in [e["id"] for e in feed["active_events"]]
            await call("POST", f"/api/v1/sos/{incident_id}/respond", headers=rh, json={
                "status": "ACCEPTED", "current_latitude": 22.574, "current_longitude": 88.365,
            })
            ws_base = base_url.replace("https://", "wss://").replace("http://", "ws://")
            victim_url = f"{ws_base}/ws/tracking/{incident_id}?token={victim['access_token']}"
            responder_url = f"{ws_base}/ws/tracking/{incident_id}?token={responder['access_token']}"
            async with websockets.connect(victim_url) as a, websockets.connect(responder_url) as b:
                assert (await receive(a, "connection_ack"))["role"] == "victim"
                assert (await receive(b, "connection_ack"))["role"] == "responder"
                await receive(a, "tracking_snapshot")
                await receive(b, "tracking_snapshot")
                await b.send(json.dumps({"type": "location_update", "latitude": 22.5741, "longitude": 88.3651}))
                assert (await receive(a, "responder_update"))["latitude"] == 22.5741
                await receive(b, "responder_update")
                for sender, text in ((a, "At the gate"), (b, "On my way")):
                    await sender.send(json.dumps({"type": "chat_message", "text": text}))
                    assert (await receive(a, "new_message"))["text"] == text
                    assert (await receive(b, "new_message"))["text"] == text
            detail = await call("GET", f"/api/v1/sos/{incident_id}")
            assert len([e for e in detail["timeline"] if e["event_type"] == "chat_message"]) == 2
            async with websockets.connect(victim_url) as reconnected:
                await receive(reconnected, "connection_ack")
                snapshot = await receive(reconnected, "tracking_snapshot")
                assert snapshot["responders"][0]["latitude"] == 22.5741
            await call("PUT", f"/api/v1/sos/{incident_id}/resolve", headers=vh, json={"resolution_notes": "Integration test complete"})
            assert (await call("GET", f"/api/v1/sos/{incident_id}"))["status"] == "RESOLVED"
            print("PASS: real HTTP + two WebSocket clients: creation, deduplication, discovery, acceptance, GPS, bidirectional chat, reconnect history, resolution")
        finally:
            await call("PUT", f"/api/v1/sos/{incident_id}/cancel", headers=vh)
            await call("PUT", "/api/v1/users/me/location", headers=rh, json={"latitude": 22.574, "longitude": 88.365, "available": False})


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8000")
    asyncio.run(run(parser.parse_args().base_url))
