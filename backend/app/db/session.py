"""NearHelp AI — Database Engine & Async Session Management."""

import logging
from collections.abc import AsyncGenerator

from geoalchemy2 import Geometry
import geoalchemy2.admin.dialects.sqlite as sqlite_admin
from sqlalchemy import event, text
from sqlalchemy.ext.asyncio import (
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)
from sqlalchemy.ext.compiler import compiles
from sqlalchemy.pool import NullPool, StaticPool

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


# Disable spatialite administrative DDL listeners for SQLite environment
sqlite_admin.after_create = lambda *args, **kwargs: None
sqlite_admin.before_create = lambda *args, **kwargs: None
sqlite_admin.before_drop = lambda *args, **kwargs: None
sqlite_admin.after_drop = lambda *args, **kwargs: None


@compiles(Geometry, "sqlite")
def compile_geometry_sqlite(element, compiler, **kw):
    """Compile Geometry type to BLOB in SQLite dialect."""
    return "BLOB"


is_sqlite = "sqlite" in settings.DATABASE_URL

# Asynchronous SQLAlchemy Engine with NullPool for robust event loop lifecycle
async_engine = create_async_engine(
    _get_async_url(settings.DATABASE_URL),
    echo=False,
    future=True,
    poolclass=StaticPool if is_sqlite else NullPool,
    connect_args=(
        {"check_same_thread": False}
        if is_sqlite
        else {
            "statement_cache_size": 0,
            "prepared_statement_cache_size": 0,
        }
    ),
)


@event.listens_for(async_engine.sync_engine, "connect")
def set_sqlite_spatial_functions(dbapi_connection, connection_record):
    """Register spatial function stubs when using SQLite."""
    if not is_sqlite:
        return

    def mock_as_ewkb(val):
        if val is None:
            return None
        return "0101000020E610000000000000000000000000000000000000"

    def mock_as_ewkt(val):
        if val is None:
            return None
        return str(val)

    for fn_name in ["GeomFromEWKT", "ST_GeomFromEWKT", "ST_GeomFromText"]:
        dbapi_connection.create_function(fn_name, 1, lambda x: x)
    for fn_name in ["AsEWKT", "ST_AsEWKT", "ST_AsText"]:
        dbapi_connection.create_function(fn_name, 1, mock_as_ewkt)
    for fn_name in ["AsEWKB", "ST_AsEWKB", "ST_AsBinary", "AsBinary"]:
        dbapi_connection.create_function(fn_name, 1, mock_as_ewkb)

    dbapi_connection.create_function("ST_DWithin", 3, lambda a, b, dist: 1)
    dbapi_connection.create_function("ST_Distance", 2, lambda a, b: 0)
    dbapi_connection.create_function("ST_SetSRID", 2, lambda a, s: a)
    dbapi_connection.create_function("ST_MakePoint", 2, lambda x, y: f"POINT({x} {y})")


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
