"""NearHelp AI — RAG REST Endpoints Integration Tests."""

import pytest
from app.main import app
from fastapi.testclient import TestClient


@pytest.fixture(scope="module")
def client():
    """Create test client with lifecycle initialization."""
    with TestClient(app) as c:
        yield c


def test_rag_stats_endpoint(client):
    """Verify GET /api/v1/rag/stats returns corpus metrics."""
    response = client.get("/api/v1/rag/stats")
    assert response.status_code == 200
    data = response.json()

    assert data["collection_name"] == "nearhelp_first_aid_rag"
    assert data["total_chunks"] > 0
    assert data["is_initialized"] is True
    assert data["embedding_dimension"] == 384


def test_rag_search_endpoint(client):
    """Verify POST /api/v1/rag/search returns relevant protocol passages."""
    payload = {
        "query": "How to control arterial bleeding with tourniquet",
        "condition_id": "severe_bleeding",
        "top_k": 3,
    }
    response = client.post("/api/v1/rag/search", json=payload)
    assert response.status_code == 200
    data = response.json()

    assert data["total_results"] > 0
    assert len(data["passages"]) <= 3
    top = data["passages"][0]
    assert "bleed" in top["content"].lower() or "pressure" in top["content"].lower() or "tourniquet" in top["content"].lower()
    assert top["citation"]["authority"] != ""


def test_rag_query_endpoint_standard(client):
    """Verify POST /api/v1/rag/query generates grounded clinical answer with citations."""
    payload = {
        "query": "What is the proper compression rate and depth for CPR?",
        "condition_id": "cardiac_arrest",
        "top_k": 3,
    }
    response = client.post("/api/v1/rag/query", json=payload)
    assert response.status_code == 200
    data = response.json()

    assert data["is_safe"] is True
    assert len(data["citations"]) > 0
    assert "110" in data["answer"] or "compression" in data["answer"].lower() or "cpr" in data["answer"].lower()


def test_rag_query_endpoint_contraindication_intercept(client):
    """Verify POST /api/v1/rag/query immediately blocks contraindicated inquiries."""
    payload = {
        "query": "Can I give water to the unconscious person who collapsed?",
        "condition_id": "cardiac_arrest",
    }
    response = client.post("/api/v1/rag/query", json=payload)
    assert response.status_code == 200
    data = response.json()

    assert data["is_safe"] is False
    assert len(data["contraindications"]) > 0
    assert "NO_ORAL_FLUIDS" in data["contraindications"][0]["flag"] or "UNCONSCIOUS" in data["contraindications"][0]["flag"]
    assert "Section 134A" in data["answer"] or "Motor Vehicles" in data["answer"]


def test_rag_ingest_endpoint(client):
    """Verify POST /api/v1/rag/ingest triggers re-ingestion."""
    response = client.post("/api/v1/rag/ingest")
    assert response.status_code == 200
    data = response.json()

    assert data["status"] == "reingested"
    assert data["total_chunks"] > 0
