"""NearHelp AI — Pytest Fixtures & Configuration."""

from collections.abc import AsyncGenerator

import pytest_asyncio
from httpx import ASGITransport, AsyncClient

from app.db.session import AsyncSessionLocal, init_db
from app.main import app


@pytest_asyncio.fixture(autouse=True)
async def setup_test_db():
    """Ensure database tables are initialized before test execution."""
    await init_db()
    yield


@pytest_asyncio.fixture
async def db_session() -> AsyncGenerator:
    """Provide a database session for test setup and verification."""
    async with AsyncSessionLocal() as session:
        yield session


@pytest_asyncio.fixture
async def client() -> AsyncGenerator[AsyncClient, None]:
    """Provide an asynchronous HTTP test client bound to FastAPI application."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        yield ac
