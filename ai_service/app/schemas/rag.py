"""NearHelp AI — RAG Knowledge Base Schemas & Contracts."""

from typing import Any

from pydantic import BaseModel, Field

from app.schemas.agent import CitationItem, ContraindicationAlert


class RAGSearchRequest(BaseModel):
    """Payload to search protocol vector store using semantic similarity."""

    query: str = Field(..., description="Bystander question or emergency scenario description", examples=["How to stop severe bleeding from arm?"])
    condition_id: str | None = Field(default=None, description="Optional condition filter (e.g. 'severe_bleeding', 'snakebite')")
    crisis_type: str | None = Field(default=None, description="Optional crisis category ('medical', 'accident', 'fire', 'gas_leak', 'natural_disaster')")
    top_k: int = Field(default=5, ge=1, le=20, description="Maximum number of retrieved passages")


class RetrievedPassageResponse(BaseModel):
    """Individual evidence-based protocol passage with citation metadata."""

    chunk_id: str
    title: str
    content: str
    condition_id: str
    condition_label: str
    step_number: int | None = None
    similarity_score: float
    confidence_score: float
    is_contraindication: bool = False
    citation: CitationItem
    warning_note: str | None = None
    cpr_bpm: int | None = None
    legal_shield: str = "Section 134A Motor Vehicles (Amendment) Act 2019"


class RAGSearchResponse(BaseModel):
    """Collection of retrieved passages with citation details."""

    query: str
    total_results: int
    passages: list[RetrievedPassageResponse]
    latency_ms: float = 0.0


class RAGQueryRequest(BaseModel):
    """End-to-end RAG question answering request with clinical safety guardrails."""

    query: str = Field(..., description="Bystander query", examples=["Can I give water to an unconscious victim who fell down?"])
    condition_id: str | None = Field(default=None, description="Emergency condition identifier if known")
    language: str = Field(default="en", description="Preferred response language ('en', 'bn', 'hi')")
    top_k: int = Field(default=4, ge=1, le=10)


class RAGQueryResponse(BaseModel):
    """End-to-end grounded answer generated from retrieved protocols."""

    query: str
    answer: str
    highlight_tag: str
    citations: list[CitationItem]
    contraindications: list[ContraindicationAlert]
    grounded_passages: list[RetrievedPassageResponse]
    is_safe: bool = True
    latency_ms: float = 0.0


class RAGStatsResponse(BaseModel):
    """Vector store and knowledge base corpus statistics."""

    collection_name: str
    total_chunks: int
    vector_store: str
    embedding_dimension: int
    is_initialized: bool
    persist_directory: str
