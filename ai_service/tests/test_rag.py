"""NearHelp AI — Vector Store & RAG Retriever Tests."""

import pytest
from app.rag.retriever import rag_retriever
from app.rag.store import vector_store


@pytest.mark.asyncio
async def test_vector_store_initialization():
    """Verify vector store initializes and indexes protocols."""
    await vector_store.initialize()
    stats = vector_store.get_stats()

    assert stats["is_initialized"] is True
    assert stats["total_chunks"] > 0
    assert stats["vector_store"] == "ChromaDB"


@pytest.mark.asyncio
async def test_semantic_search_cardiac_cpr():
    """Verify vector store retrieves AHA/IRC CPR guidance for cardiac queries."""
    await vector_store.initialize()
    passages = await rag_retriever.retrieve(
        query="How many chest compressions per minute and how deep for adult CPR?",
        condition_id="cardiac_arrest",
        top_k=3,
    )
    assert len(passages) > 0
    top = passages[0]

    assert "cardiac" in top.condition_id or "cpr" in top.content.lower() or "compression" in top.content.lower()
    assert top.citation.source is not None
    assert top.citation.authority is not None
    assert top.confidence_score > 0.30


@pytest.mark.asyncio
async def test_semantic_search_severe_bleeding():
    """Verify vector store retrieves WHO trauma bleeding control."""
    await vector_store.initialize()
    passages = await rag_retriever.retrieve(
        query="Blood spurting from severed leg artery tourniquet",
        condition_id="severe_bleeding",
        top_k=3,
    )
    assert len(passages) > 0
    top = passages[0]

    assert "bleed" in top.content.lower() or "pressure" in top.content.lower() or "tourniquet" in top.content.lower()
    assert top.citation.source != ""


@pytest.mark.asyncio
async def test_semantic_search_snakebite():
    """Verify vector store retrieves Indian Red Cross / MoHFW snakebite protocol."""
    await vector_store.initialize()
    passages = await rag_retriever.retrieve(
        query="Russell viper snake bite on foot swelling fast",
        condition_id="snakebite",
        top_k=3,
    )
    assert len(passages) > 0
    top = passages[0]

    assert "snake" in top.condition_id or "bite" in top.content.lower() or "venom" in top.content.lower()
    assert "Section 134A" in top.legal_shield


@pytest.mark.asyncio
async def test_rag_context_formatting():
    """Verify retriever formats structured context for Gemini 2.5 prompt injection."""
    await vector_store.initialize()
    passages = await rag_retriever.retrieve(
        query="Thermal burn on arm from boiling oil",
        condition_id="burns_scalds",
        top_k=2,
    )
    assert len(passages) > 0

    context_str = rag_retriever.format_context_for_prompt(passages)
    assert "VERIFIED PASSAGE" in context_str
    assert "Source Authority:" in context_str
    assert "Guideline:" in context_str
