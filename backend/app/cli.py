"""Run `python -m app.cli create-admin --email you@example.com` from backend/."""

import argparse
import asyncio
import getpass

from sqlalchemy import delete, select, update

from app.core.security import get_password_hash
from app.db.session import AsyncSessionLocal, async_engine
from app.models.response import SOSResponse
from app.models.skill_verification import SkillVerification
from app.models.sos_event import SOSEvent
from app.models.timeline_event import TimelineEvent
from app.models.user import User
from app.models.user_presence import UserPresence
from app.schemas.admin import AdminUserCreate


async def create_admin(email, name, password):
    req = AdminUserCreate(email=email, name=name, password=password, is_superuser=True)
    async with AsyncSessionLocal() as db:
        user = await db.scalar(select(User).where(User.email == str(req.email).lower()))
        if user and user.auth_provider != "email":
            raise ValueError("This email belongs to an external identity. Use an email account for bootstrap.")
        if not user:
            user = User(email=str(req.email).lower(), name=req.name, auth_provider="email")
            db.add(user)
        user.hashed_password = get_password_hash(req.password)
        user.is_superuser = True
        user.is_active = True
        await db.commit()
    await async_engine.dispose()
    print("Administrator ready. Sign in at /admin with the email and password you supplied.")


async def purge_demo_users():
    async with AsyncSessionLocal() as db:
        anon_user_ids = (await db.scalars(select(User.id).where(User.is_anonymous.is_(True)))).all()
        if not anon_user_ids:
            print("No demo/anonymous users found in database.")
            await async_engine.dispose()
            return
        await db.execute(delete(UserPresence).where(UserPresence.user_id.in_(anon_user_ids)))
        await db.execute(delete(SkillVerification).where(SkillVerification.user_id.in_(anon_user_ids)))
        await db.execute(update(SkillVerification).where(SkillVerification.reviewed_by.in_(anon_user_ids)).values(reviewed_by=None))
        await db.execute(delete(SOSResponse).where(SOSResponse.responder_id.in_(anon_user_ids)))
        await db.execute(update(SOSEvent).where(SOSEvent.broadcaster_id.in_(anon_user_ids)).values(broadcaster_id=None))
        await db.execute(update(TimelineEvent).where(TimelineEvent.actor_id.in_(anon_user_ids)).values(actor_id=None))
        await db.execute(delete(User).where(User.id.in_(anon_user_ids)))
        await db.commit()
        print(f"Successfully purged {len(anon_user_ids)} demo/anonymous accounts.")
    await async_engine.dispose()


def main():
    parser = argparse.ArgumentParser(description="NearHelp account administration")
    parser.add_argument("command", choices=["create-admin", "purge-demo-users"])
    parser.add_argument("--email", required=False)
    parser.add_argument("--name", default="NearHelp Administrator")
    args = parser.parse_args()

    if args.command == "create-admin":
        if not args.email:
            parser.error("--email is required for create-admin.")
        password = getpass.getpass("Administrator password (8–72 bytes): ")
        if password != getpass.getpass("Confirm password: "):
            parser.error("Passwords do not match.")
        try:
            asyncio.run(create_admin(args.email, args.name, password))
        except ValueError as exc:
            parser.error(str(exc))
    elif args.command == "purge-demo-users":
        asyncio.run(purge_demo_users())


if __name__ == "__main__":
    main()
