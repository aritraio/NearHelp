"""NearHelp AI — Database Engine & Async Session Management."""

import logging
from collections.abc import AsyncGenerator

from sqlalchemy import text
from sqlalchemy.ext.asyncio import (
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)
from sqlalchemy.pool import NullPool

import app.models  # noqa: F401 (register models with Base.metadata)
from app.core.config import settings
from app.db.base import Base

logger = logging.getLogger(__name__)

def _get_async_url(raw_url: str) -> str:
    url = raw_url.strip()
    if url.startswith("postgres://"):
        return url.replace("postgres://", "postgresql+asyncpg://", 1)
    elif url.startswith("postgresql://") and not url.startswith("postgresql+"):
        return url.replace("postgresql://", "postgresql+asyncpg://", 1)
    return url

# Asynchronous SQLAlchemy Engine with NullPool for robust event loop lifecycle
async_engine = create_async_engine(
    _get_async_url(settings.DATABASE_URL),
    echo=False,
    future=True,
    poolclass=NullPool,
)

# Async Session Factory
AsyncSessionLocal = async_sessionmaker(
    bind=async_engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False,
)


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """Dependency that yields an asynchronous database session per request."""
    async with AsyncSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()


async def init_db() -> None:
    """Initialize database tables and verify PostGIS extension."""
    try:
        async with async_engine.begin() as conn:
            # Enable PostGIS extension if available on PostgreSQL
            if "postgresql" in settings.DATABASE_URL:
                try:
                    await conn.execute(text("CREATE EXTENSION IF NOT EXISTS postgis;"))
                    logger.info("PostGIS extension checked/enabled.")
                except Exception as e:
                    logger.warning(f"Could not enable PostGIS extension (may already exist or insufficient permissions): {e}")

            # Create all registered tables
            await conn.run_sync(Base.metadata.create_all)
            
            # Ensure newly added columns and spatial indexes exist in PostgreSQL
            if "postgresql" in settings.DATABASE_URL:
                try:
                    await conn.execute(text("ALTER TABLE users ADD COLUMN IF NOT EXISTS has_pacemaker BOOLEAN DEFAULT FALSE;"))
                    await conn.execute(text("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_organ_donor BOOLEAN DEFAULT FALSE;"))
                    await conn.execute(text("ALTER TABLE users ADD COLUMN IF NOT EXISTS medical_notes VARCHAR(2048);"))
                    await conn.execute(text("CREATE INDEX IF NOT EXISTS idx_users_location ON users USING GIST (location);"))
                    await conn.execute(text("CREATE INDEX IF NOT EXISTS idx_sos_events_location ON sos_events USING GIST (location);"))
                    await conn.execute(text("CREATE INDEX IF NOT EXISTS idx_facilities_location ON facilities USING GIST (location);"))
                except Exception as ex:
                    logger.debug(f"Column/Index migration check: {ex}")

            logger.info("Database tables and spatial indexes initialized successfully.")

        # Auto-seed regional facilities if table is unpopulated
        async with AsyncSessionLocal() as session:
            try:
                from app.services.facility_service import FacilityService
                await FacilityService.seed_kolkata_facilities(session)
            except Exception as e:
                logger.debug(f"Facility auto-seed check: {e}")

    except Exception as e:
        logger.error(f"Error during database initialization: {e}")
