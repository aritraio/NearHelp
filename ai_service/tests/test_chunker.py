"""NearHelp AI — Document Chunker & Protocol Parser Unit Tests."""

from pathlib import Path

import pytest
from app.rag.chunker import DocumentChunker, ProtocolChunk, estimate_tokens


def test_estimate_tokens():
    """Verify token count estimation on short and long strings."""
    assert estimate_tokens("") == 0
    short_text = "Apply firm direct pressure to the wound."
    assert 5 <= estimate_tokens(short_text) <= 15

    long_text = " ".join(["emergency"] * 100)
    assert 90 <= estimate_tokens(long_text) <= 150


def test_chunk_json_protocol_who():
    """Verify procedure-level chunking of WHO trauma guidelines."""
    chunker = DocumentChunker()
    who_file = Path("data/protocols/who_first_aid_guidelines.json")
    if not who_file.exists():
        who_file = Path("../data/protocols/who_first_aid_guidelines.json")

    chunks = chunker.chunk_json_file(who_file)
    assert len(chunks) > 0

    # Verify at least one severe bleeding step chunk
    bleeding_chunks = [c for c in chunks if c.condition_id == "severe_bleeding"]
    assert len(bleeding_chunks) >= 4

    step_1 = next(c for c in bleeding_chunks if c.step_number == 1)
    assert "Direct Pressure" in step_1.title
    assert "WHO" in step_1.source or "World Health Organization" in step_1.authority
    assert "Section 134A" in step_1.legal_shield
    assert step_1.token_count > 10

    # Verify ChromaDB metadata compatibility (all primitives)
    meta = step_1.to_metadata()
    assert isinstance(meta["step_number"], int)
    assert isinstance(meta["is_contraindication"], bool)
    assert isinstance(meta["chunk_id"], str)
    assert isinstance(meta["authority"], str)


def test_chunk_json_protocol_red_cross_and_aha():
    """Verify chunking of Red Cross and AHA protocol files."""
    chunker = DocumentChunker()
    rc_file = Path("data/protocols/red_cross_emergency_protocols.json")
    if not rc_file.exists():
        rc_file = Path("../data/protocols/red_cross_emergency_protocols.json")

    rc_chunks = chunker.chunk_json_file(rc_file)
    assert len(rc_chunks) > 0

    # Check snakebite protocol
    snake_chunks = [c for c in rc_chunks if c.condition_id == "snakebite"]
    assert len(snake_chunks) >= 4
    assert any(c.is_contraindication for c in snake_chunks)

    aha_file = Path("data/protocols/aha_cpr_ecc_guidelines.json")
    if not aha_file.exists():
        aha_file = Path("../data/protocols/aha_cpr_ecc_guidelines.json")

    aha_chunks = chunker.chunk_json_file(aha_file)
    assert len(aha_chunks) > 0
    cpr_chunks = [c for c in aha_chunks if c.condition_id == "cardiac_arrest"]
    assert any(c.cpr_bpm == 110 for c in cpr_chunks)


def test_chunk_markdown_file():
    """Verify procedure-level chunking of companion markdown documents."""
    chunker = DocumentChunker()
    md_file = Path("data/protocols/who_trauma_care.md")
    if not md_file.exists():
        md_file = Path("../data/protocols/who_trauma_care.md")

    chunks = chunker.chunk_markdown_file(md_file)
    assert len(chunks) > 0
    assert any("Bleeding" in c.title or "Trauma" in c.title for c in chunks)


def test_chunk_directory():
    """Verify batch ingestion of the entire protocols directory."""
    chunker = DocumentChunker()
    proto_dir = Path("data/protocols")
    if not proto_dir.exists():
        proto_dir = Path("../data/protocols")

    all_chunks = chunker.chunk_directory(proto_dir)
    assert len(all_chunks) >= 20

    # Verify diversity of conditions
    conditions = {c.condition_id for c in all_chunks}
    assert "severe_bleeding" in conditions or "cardiac_arrest" in conditions
    assert "snakebite" in conditions or "burns_scalds" in conditions
