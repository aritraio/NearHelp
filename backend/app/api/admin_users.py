"""Superuser-only account operations and live dashboard statistics."""

import uuid
from datetime import UTC, datetime, timedelta
from typing import Literal

from fastapi import APIRouter, Depends, HTTPException, Query, Response
from sqlalchemy import delete, func, or_, select, update
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_current_admin_user
from app.core.security import get_password_hash
from app.db.session import get_db
from app.models.response import SOSResponse
from app.models.skill_verification import SkillVerification
from app.models.sos_event import SOSEvent
from app.models.timeline_event import TimelineEvent
from app.models.user import User
from app.models.user_presence import UserPresence
from app.schemas.admin import AdminUserCreate, AdminUserList, AdminUserResponse, AdminUserUpdate
from app.services.presence_service import ONLINE_WINDOW_SECONDS

router = APIRouter(tags=["Admin Users"], dependencies=[Depends(get_current_admin_user)])


def online_cutoff():
    return datetime.now(UTC) - timedelta(seconds=ONLINE_WINDOW_SECONDS)


def user_response(user, last_seen=None):
    data = AdminUserResponse.model_validate(user)
    data.last_seen_at = last_seen
    if last_seen:
        seen_utc = last_seen.astimezone(UTC) if last_seen.tzinfo else last_seen.replace(tzinfo=UTC)
        data.is_online = user.is_active and seen_utc >= online_cutoff()
    return data


async def find_user(db, user_id):
    user = await db.get(User, user_id)
    if not user:
        raise HTTPException(404, "User not found.")
    return user


async def save_user(db, user):
    try:
        await db.commit()
    except IntegrityError:
        await db.rollback()
        raise HTTPException(409, "This email or phone number is already in use.") from None
    await db.refresh(user)
    return user_response(user)


@router.get("/me", response_model=AdminUserResponse)
async def admin_me(admin: User = Depends(get_current_admin_user)):
    return user_response(admin)


@router.get("/stats")
async def dashboard_stats(db: AsyncSession = Depends(get_db)):
    async def count(model, *conditions):
        return await db.scalar(select(func.count()).select_from(model).where(*conditions))

    return {
        "total_users": await count(User, User.is_anonymous.is_(False)),
        "active_users": await count(User, User.is_active.is_(True), User.is_anonymous.is_(False)),
        "online_users": await db.scalar(
            select(func.count()).select_from(UserPresence).join(User)
            .where(UserPresence.last_seen_at >= online_cutoff(), User.is_active.is_(True), User.is_anonymous.is_(False))
        ),
        "admin_users": await count(User, User.is_superuser.is_(True)),
        "new_users_today": await count(
            User,
            User.created_at >= datetime.now(UTC).replace(hour=0, minute=0, second=0, microsecond=0),
            User.is_anonymous.is_(False),
        ),
        "pending_verifications": await count(SkillVerification, SkillVerification.status == "PENDING"),
        "active_emergencies": await count(SOSEvent, SOSEvent.status.notin_(["RESOLVED", "CANCELLED", "IDLE"])),
        "online_window_seconds": ONLINE_WINDOW_SECONDS,
        "updated_at": datetime.now(UTC),
    }


@router.get("/users", response_model=AdminUserList)
async def list_users(
    q: str = Query("", max_length=100),
    state: Literal["all", "active", "inactive", "online"] = "all",
    role: Literal["all", "admin", "user"] = "all",
    user_type: Literal["real", "all", "anonymous"] = "real",
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    db: AsyncSession = Depends(get_db),
):
    filters = []
    if user_type == "real":
        filters.append(User.is_anonymous.is_(False))
    elif user_type == "anonymous":
        filters.append(User.is_anonymous.is_(True))

    if q.strip():
        filters.append(or_(*(column.icontains(q.strip(), autoescape=True) for column in (User.name, User.email, User.phone))))
    if state in {"active", "inactive"}:
        filters.append(User.is_active.is_(state == "active"))
    if state == "online":
        filters.extend([User.is_active.is_(True), UserPresence.last_seen_at >= online_cutoff()])
    if role != "all":
        filters.append(User.is_superuser.is_(role == "admin"))
    query = select(User, UserPresence.last_seen_at).outerjoin(UserPresence).where(*filters)
    total = await db.scalar(select(func.count()).select_from(query.subquery()))
    rows = (await db.execute(query.order_by(User.created_at.desc(), User.id).offset(offset).limit(limit))).all()
    return AdminUserList(users=[user_response(user, seen) for user, seen in rows], total=total, limit=limit, offset=offset)


@router.post("/maintenance/purge-demo-users")
async def purge_demo_users(
    admin: User = Depends(get_current_admin_user),
    db: AsyncSession = Depends(get_db),
):
    """Purge all temporary anonymous/demo logins and their associations."""
    anon_user_ids = (await db.scalars(select(User.id).where(User.is_anonymous.is_(True)))).all()
    if not anon_user_ids:
        return {"purged": 0, "message": "No demo/anonymous users found."}

    await db.execute(delete(UserPresence).where(UserPresence.user_id.in_(anon_user_ids)))
    await db.execute(delete(SkillVerification).where(SkillVerification.user_id.in_(anon_user_ids)))
    await db.execute(update(SkillVerification).where(SkillVerification.reviewed_by.in_(anon_user_ids)).values(reviewed_by=None))
    await db.execute(delete(SOSResponse).where(SOSResponse.responder_id.in_(anon_user_ids)))
    await db.execute(update(SOSEvent).where(SOSEvent.broadcaster_id.in_(anon_user_ids)).values(broadcaster_id=None))
    await db.execute(update(TimelineEvent).where(TimelineEvent.actor_id.in_(anon_user_ids)).values(actor_id=None))
    await db.execute(delete(User).where(User.id.in_(anon_user_ids)))
    await db.commit()

    return {"purged": len(anon_user_ids), "message": f"Successfully purged {len(anon_user_ids)} demo/anonymous accounts."}


@router.post("/users", response_model=AdminUserResponse, status_code=201)
async def create_user(req: AdminUserCreate, db: AsyncSession = Depends(get_db)):
    data = req.model_dump(exclude={"password"})
    data["email"] = str(req.email).lower()
    user = User(**data, hashed_password=get_password_hash(req.password), auth_provider="email")
    db.add(user)
    return await save_user(db, user)


@router.get("/users/{user_id}", response_model=AdminUserResponse)
async def get_user(user_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    user = await find_user(db, user_id)
    seen = await db.scalar(select(UserPresence.last_seen_at).where(UserPresence.user_id == user_id))
    return user_response(user, seen)


@router.patch("/users/{user_id}", response_model=AdminUserResponse)
async def update_user(
    user_id: uuid.UUID, req: AdminUserUpdate,
    admin: User = Depends(get_current_admin_user), db: AsyncSession = Depends(get_db),
):
    user = await find_user(db, user_id)
    changes = req.model_dump(exclude_unset=True)
    if user_id == admin.id and (changes.get("is_active") is False or changes.get("is_superuser") is False):
        raise HTTPException(409, "You cannot deactivate or demote your own administrator account.")
    if "email" in changes:
        if changes["email"] is None and user.auth_provider == "email":
            raise HTTPException(422, "Email accounts must retain an email address.")
        changes["email"] = str(changes["email"]).lower() if changes["email"] else None
    password = changes.pop("password", None)
    if password:
        if user.auth_provider != "email":
            raise HTTPException(422, "Password resets are available for email accounts only.")
        user.hashed_password = get_password_hash(password)
    for key, value in changes.items():
        setattr(user, key, value)
    return await save_user(db, user)


@router.delete("/users/{user_id}", status_code=204)
async def delete_user(
    user_id: uuid.UUID, admin: User = Depends(get_current_admin_user), db: AsyncSession = Depends(get_db),
):
    await find_user(db, user_id)
    if user_id == admin.id:
        raise HTTPException(409, "You cannot delete your own administrator account.")
    # Explicit cleanup also makes behavior identical on SQLite without FK enforcement.
    await db.execute(delete(UserPresence).where(UserPresence.user_id == user_id))
    await db.execute(delete(SkillVerification).where(SkillVerification.user_id == user_id))
    await db.execute(update(SkillVerification).where(SkillVerification.reviewed_by == user_id).values(reviewed_by=None))
    await db.execute(delete(SOSResponse).where(SOSResponse.responder_id == user_id))
    await db.execute(update(SOSEvent).where(SOSEvent.broadcaster_id == user_id).values(broadcaster_id=None))
    await db.execute(update(TimelineEvent).where(TimelineEvent.actor_id == user_id).values(actor_id=None))
    await db.execute(delete(User).where(User.id == user_id))
    await db.commit()
    return Response(status_code=204)
