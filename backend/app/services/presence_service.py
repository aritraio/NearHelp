"""Presence measures recent authenticated HTTP activity, not an open socket."""

from datetime import UTC, datetime, timedelta

from sqlalchemy.dialects.postgresql import insert as pg_insert
from sqlalchemy.dialects.sqlite import insert as sqlite_insert

from app.models.user_presence import UserPresence

ONLINE_WINDOW_SECONDS = 300


async def record_activity(db, user_id):
    now = datetime.now(UTC)
    insert = sqlite_insert if db.bind.dialect.name == "sqlite" else pg_insert
    statement = insert(UserPresence).values(user_id=user_id, last_seen_at=now)
    statement = statement.on_conflict_do_update(
        index_elements=[UserPresence.user_id],
        set_={"last_seen_at": now},
        where=UserPresence.last_seen_at < now - timedelta(seconds=30),
    )
    await db.execute(statement)
    await db.commit()
