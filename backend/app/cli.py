"""Run `python -m app.cli create-admin --email you@example.com` from backend/."""

import argparse
import asyncio
import getpass

from sqlalchemy import select

from app.core.security import get_password_hash
from app.db.session import AsyncSessionLocal, async_engine
from app.models.user import User
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


def main():
    parser = argparse.ArgumentParser(description="NearHelp account administration")
    parser.add_argument("command", choices=["create-admin"])
    parser.add_argument("--email", required=True)
    parser.add_argument("--name", default="NearHelp Administrator")
    args = parser.parse_args()
    password = getpass.getpass("Administrator password (8–72 bytes): ")
    if password != getpass.getpass("Confirm password: "):
        parser.error("Passwords do not match.")
    try:
        asyncio.run(create_admin(args.email, args.name, password))
    except ValueError as exc:
        parser.error(str(exc))


if __name__ == "__main__":
    main()
