"""Admin privilege boundaries, account lifecycle, presence, and dashboard delivery."""
import uuid
from datetime import UTC, datetime, timedelta

import pytest
from sqlalchemy import func, select

from app.core.security import create_access_token
from app.models.response import SOSResponse
from app.models.skill_verification import SkillVerification
from app.models.sos_event import SOSEvent
from app.models.timeline_event import TimelineEvent
from app.models.user import User
from app.models.user_presence import UserPresence


async def account(db, **fields):
    user = User(email=f"{uuid.uuid4().hex}@example.com", name="Admin test", **fields)
    db.add(user)
    await db.commit()
    await db.refresh(user)
    return user, {"Authorization": f"Bearer {create_access_token(user.id)}"}


@pytest.mark.asyncio
async def test_privilege_boundaries(client, db_session):
    _, headers = await account(db_session)
    for prefix in ("/api/v1/admin", "/api/admin"):
        for path in ["/stats", "/users", "/me", f"/users/{uuid.uuid4()}"]:
            assert (await client.get(prefix + path)).status_code == 401
            assert (await client.get(prefix + path, headers=headers)).status_code == 403
        for method in ("POST", "PATCH", "DELETE"):
            suffix = "/users" if method == "POST" else f"/users/{uuid.uuid4()}"
            assert (await client.request(method, prefix + suffix, headers=headers, json={})).status_code == 403


@pytest.mark.asyncio
async def test_account_lifecycle_and_self_protection(client, db_session):
    admin, headers = await account(db_session, is_superuser=True)
    payload = {"name":"New Member", "email":f"{uuid.uuid4().hex}@example.com", "password":"OriginalPassword"}
    response = await client.post('/api/v1/admin/users', headers=headers, json=payload)
    assert response.status_code == 201, response.text
    created = response.json(); user_id = created['id']
    assert not {'hashed_password', 'password', 'fcm_token', 'medical_notes'} & created.keys()
    assert (await client.post('/api/v1/admin/users', headers=headers, json=payload)).status_code == 409
    login = await client.post('/api/v1/auth/login', json={"email":payload['email'], "password":payload['password']})
    assert login.status_code == 200
    member_headers = {"Authorization":f"Bearer {login.json()['access_token']}"}
    changed = await client.patch(f'/api/v1/admin/users/{user_id}', headers=headers, json={"name":"Updated Member", "is_superuser":True, "trust_score":92, "password":"ReplacementPassword"})
    assert changed.status_code == 200, changed.text
    assert changed.json()['is_superuser'] is True
    assert (await client.get('/api/v1/admin/me', headers=member_headers)).status_code == 200
    assert (await client.post('/api/v1/auth/login', json={"email":payload['email'], "password":"ReplacementPassword"})).status_code == 200
    assert (await client.post('/api/v1/auth/login', json={"email":payload['email'], "password":payload['password']})).status_code == 401
    await client.patch(f'/api/v1/admin/users/{user_id}', headers=headers, json={"is_active":False})
    assert (await client.get('/api/v1/auth/me', headers=member_headers)).status_code == 403
    assert (await client.get('/api/v1/sos/active', headers=member_headers)).status_code == 403
    assert (await client.delete(f'/api/v1/admin/users/{user_id}', headers=headers)).status_code == 204
    assert (await client.get('/api/v1/auth/me', headers=member_headers)).status_code == 401
    assert (await client.get(f'/api/v1/admin/users/{user_id}', headers=headers)).status_code == 404
    for change in ({"is_active":False}, {"is_superuser":False}):
        assert (await client.patch(f'/api/v1/admin/users/{admin.id}', headers=headers, json=change)).status_code == 409
    assert (await client.delete(f'/api/v1/admin/users/{admin.id}', headers=headers)).status_code == 409


@pytest.mark.asyncio
async def test_admin_demotion_applies_to_existing_tokens(client, db_session):
    _, headers = await account(db_session, is_superuser=True)
    other, other_headers = await account(db_session, is_superuser=True)
    assert (await client.get('/api/v1/admin/stats', headers=other_headers)).status_code == 200
    response = await client.patch(f'/api/v1/admin/users/{other.id}', headers=headers, json={"is_superuser":False})
    assert response.status_code == 200
    assert (await client.get('/api/v1/admin/stats', headers=other_headers)).status_code == 403


@pytest.mark.asyncio
async def test_presence_filters_and_pagination(client, db_session):
    _, headers = await account(db_session, is_superuser=True)
    prefix = uuid.uuid4().hex[:10]
    member, member_headers = await account(db_session)
    member.name = prefix + ' Recent'
    stale, _ = await account(db_session); stale.name = prefix + ' Stale'
    disabled, _ = await account(db_session, is_active=False); disabled.name = prefix + ' Disabled'
    for user, seconds in [(stale, 600), (disabled, 0)]:
        db_session.add(UserPresence(user_id=user.id, last_seen_at=datetime.now(UTC)-timedelta(seconds=seconds)))
    await db_session.commit()
    assert (await client.get('/api/v1/auth/me', headers=member_headers)).status_code == 200
    data = (await client.get('/api/v1/admin/users', headers=headers, params={"q":prefix, "state":"online"})).json()
    assert data['total'] == 1 and data['users'][0]['id'] == str(member.id)
    assert data['users'][0]['is_online'] is True and data['users'][0]['last_seen_at']
    data = (await client.get('/api/v1/admin/users', headers=headers, params={"q":prefix, "limit":1, "offset":1})).json()
    assert data['total'] == 3 and len(data['users']) == 1
    data = (await client.get('/api/v1/admin/users', headers=headers, params={"q":prefix, "state":"inactive"})).json()
    assert data['total'] == 1 and data['users'][0]['id'] == str(disabled.id)
    stats = (await client.get('/api/v1/admin/stats', headers=headers)).json()
    expected = await db_session.scalar(select(func.count()).select_from(UserPresence).join(User).where(UserPresence.last_seen_at >= datetime.now(UTC)-timedelta(seconds=300), User.is_active.is_(True)))
    assert stats['online_users'] == expected and stats['online_window_seconds'] == 300
    assert (await client.get('/api/v1/admin/users?state=wrong', headers=headers)).status_code == 422


@pytest.mark.asyncio
async def test_validation_and_duplicate_updates(client, db_session):
    _, headers = await account(db_session, is_superuser=True)
    member, _ = await account(db_session); other, _ = await account(db_session)
    path = f'/api/v1/admin/users/{member.id}'
    for body in ({"is_superuser":None}, {"is_active":None}, {"trust_score":None}, {"trust_score":101}, {"name":"  "}, {"hashed_password":"x"}, {"email":None}, {"phone":"invalid"}, {"password":"é"*40}):
        assert (await client.patch(path, headers=headers, json=body)).status_code == 422, body
    assert (await client.patch(path, headers=headers, json={"email":other.email.upper()})).status_code == 409
    assert (await client.patch(path, headers=headers, json={"phone":None})).status_code == 200


@pytest.mark.asyncio
async def test_delete_reference_cleanup(client, db_session):
    admin, headers = await account(db_session, is_superuser=True)
    member, _ = await account(db_session)
    event = SOSEvent(broadcaster_id=member.id, location='SRID=4326;POINT(88.36 22.57)', latitude=22.57, longitude=88.36)
    db_session.add(event); await db_session.flush()
    timeline = TimelineEvent(sos_event_id=event.id, actor_id=member.id, event_type='sos_created')
    response = SOSResponse(sos_event_id=event.id, responder_id=member.id)
    own_claim = SkillVerification(user_id=member.id, skill_type='FIRST_AID', certificate_url='/uploads/certificates/test.pdf')
    reviewed = SkillVerification(user_id=admin.id, reviewed_by=member.id, skill_type='CPR', certificate_url='/uploads/certificates/test.pdf')
    db_session.add_all([timeline, response, own_claim, reviewed]); await db_session.commit()
    ids = [event.id, timeline.id, response.id, own_claim.id, reviewed.id]
    assert (await client.delete(f'/api/v1/admin/users/{member.id}', headers=headers)).status_code == 204
    db_session.expire_all()
    assert (await db_session.get(SOSEvent,ids[0])).broadcaster_id is None
    assert (await db_session.get(TimelineEvent,ids[1])).actor_id is None
    assert (await db_session.get(SkillVerification,ids[4])).reviewed_by is None
    assert await db_session.get(SOSResponse,ids[2]) is None
    assert await db_session.get(SkillVerification,ids[3]) is None


@pytest.mark.asyncio
async def test_dashboard_delivery(client):
    response = await client.get('/admin')
    assert response.status_code == 200 and 'Command center' in response.text
    assert "frame-ancestors 'none'" in response.headers['content-security-policy']
    for asset in ['style.css', 'app.js']:
        assert (await client.get('/admin/assets/'+asset)).status_code == 200
