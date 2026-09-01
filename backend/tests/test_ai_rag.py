"""NearHelp AI — Backend Module 11 RAG Knowledge Base Test Suite."""

import pytest
from httpx import AsyncClient

from app.schemas.ai import RAGQueryRequest, RAGSearchRequest
from app.services.ai_client import ai_client


@pytest.mark.asyncio
async def test_backend_rag_search_proxy(client: AsyncClient):
    """Verify backend proxy executes semantic search across verified protocols."""
    payload = {
        "query": "How to stop spurting arterial bleeding with a tourniquet",
        "condition_id": "severe_bleeding",
        "top_k": 3,
    }
    resp = await client.post("/api/v1/ai/rag/search", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["total_results"] > 0
    assert len(data["passages"]) >= 1
    top = data["passages"][0]
    assert "bleed" in top["content"].lower() or "tourniquet" in top["content"].lower() or "pressure" in top["content"].lower()
    assert top["citation"]["authority"] != ""


@pytest.mark.asyncio
async def test_backend_rag_query_guidance(client: AsyncClient):
    """Verify backend proxy executes end-to-end RAG grounded guidance query."""
    payload = {
        "query": "What are the first steps for snakebite in India?",
        "condition_id": "snakebite",
        "top_k": 3,
    }
    resp = await client.post("/api/v1/ai/rag/query", json=payload)
    assert resp.status_code == 200
    data = resp.json()

    assert data["is_safe"] is True
    assert len(data["citations"]) > 0
    assert len(data["grounded_passages"]) > 0
    assert "snake" in data["answer"].lower() or "immobiliz" in data["answer"].lower() or "bite" in data["answer"].lower()


@pytest.mark.asyncio
async def test_backend_rag_stats_proxy(client: AsyncClient):
    """Verify backend proxy returns RAG corpus statistics."""
    resp = await client.get("/api/v1/ai/rag/stats")
    assert resp.status_code == 200
    data = resp.json()

    assert data["collection_name"] == "nearhelp_first_aid_rag"
    assert data["total_chunks"] > 0
    assert data["embedding_dimension"] == 384


@pytest.mark.asyncio
async def test_backend_ai_client_direct_rag_fallback():
    """Verify direct AIClient offline fallback generates valid RAG search and query objects."""
    search_req = RAGSearchRequest(
        query="Severe arterial bleed",
        condition_id="severe_bleeding",
    )
    fallback_search = ai_client._local_fallback_rag_search(search_req, latency_ms=1.5)
    assert fallback_search.total_results == 1
    assert fallback_search.passages[0].condition_id == "severe_bleeding"

    query_req = RAGQueryRequest(
        query="Severe arterial bleed",
        condition_id="severe_bleeding",
    )
    fallback_query = ai_client._local_fallback_rag_query(query_req, latency_ms=1.5)
    assert fallback_query.is_safe is True
    assert len(fallback_query.citations) >= 1
    assert "WHO" in fallback_query.citations[0].authority or "WHO" in fallback_query.citations[0].source
