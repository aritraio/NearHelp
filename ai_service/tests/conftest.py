"""NearHelp AI — AI Service Pytest Fixtures."""

import pytest_asyncio
from httpx import ASGITransport, AsyncClient

from app.classifiers.embedding_service import embedding_service
from app.main import app


@pytest_asyncio.fixture(autouse=True)
async def setup_embedding_service():
    """Ensure embedding reference matrix is initialized before test execution."""
    await embedding_service.initialize()
    yield


@pytest_asyncio.fixture
async def client():
    """Provide async HTTP client for FastAPI app."""
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        yield ac
