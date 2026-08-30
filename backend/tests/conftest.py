"""NearHelp AI — Pytest Fixtures & Comprehensive Test Configuration."""

from collections.abc import AsyncGenerator

import pytest_asyncio
from geoalchemy2 import Geometry
import geoalchemy2.admin.dialects.sqlite as sqlite_admin
from httpx import ASGITransport, AsyncClient
from sqlalchemy import event
from sqlalchemy.ext.asyncio import (
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)
from sqlalchemy.ext.compiler import compiles
from sqlalchemy.pool import StaticPool

from app.db.base import Base
from app.db.session import get_db
from app.main import app as fastapi_app
import app.models  # noqa: F401

# Disable spatialite administrative DDL listeners for SQLite test environment
sqlite_admin.after_create = lambda *args, **kwargs: None
sqlite_admin.before_create = lambda *args, **kwargs: None
sqlite_admin.before_drop = lambda *args, **kwargs: None
sqlite_admin.after_drop = lambda *args, **kwargs: None


@compiles(Geometry, "sqlite")
def compile_geometry_sqlite(element, compiler, **kw):
    """Compile Geometry type to BLOB in SQLite dialect."""
    return "BLOB"


# StaticPool in-memory SQLite engine shared across all test coroutines in a test run
test_engine = create_async_engine(
    "sqlite+aiosqlite:///:memory:",
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
    echo=False,
)


@event.listens_for(test_engine.sync_engine, "connect")
def set_sqlite_spatial_functions(dbapi_connection, connection_record):
    """Mock spatial function stubs for SQLite tests."""
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


TestingSessionLocal = async_sessionmaker(
    bind=test_engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False,
)


async def override_get_db() -> AsyncGenerator[AsyncSession, None]:
    """Dependency override providing test session."""
    async with TestingSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()


fastapi_app.dependency_overrides[get_db] = override_get_db


@pytest_asyncio.fixture(autouse=True)
async def setup_test_db():
    """Ensure database schema is created fresh for tests."""
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield


@pytest_asyncio.fixture
async def db_session() -> AsyncGenerator[AsyncSession, None]:
    """Provide a database session for test assertions and fixture setups."""
    async with TestingSessionLocal() as session:
        yield session


@pytest_asyncio.fixture
async def client() -> AsyncGenerator[AsyncClient, None]:
    """Provide an asynchronous HTTP test client bound to FastAPI application."""
    from app.core.middleware import _in_memory_rate_limit
    _in_memory_rate_limit.clear()
    async with AsyncClient(transport=ASGITransport(app=fastapi_app), base_url="http://test") as ac:
        yield ac
