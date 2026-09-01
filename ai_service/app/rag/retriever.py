"""NearHelp AI — Hybrid RAG Retriever and Semantic Re-Ranking Engine."""

from __future__ import annotations

import logging
import re
from dataclasses import dataclass
from typing import Any

from app.rag.chunker import ProtocolChunk
from app.rag.store import vector_store
from app.schemas.agent import CitationItem

logger = logging.getLogger(__name__)


@dataclass
class RetrievedPassage:
    """Structured passage retrieved from the clinical RAG vector store."""

    chunk_id: str
    content: str
    title: str
    condition_id: str
    condition_label: str
    step_number: int | None
    similarity_score: float
    confidence_score: float
    is_contraindication: bool
    citation: CitationItem
    warning_note: str | None = None
    cpr_bpm: int | None = None
    legal_shield: str = "Section 134A Motor Vehicles (Amendment) Act 2019"

    def to_dict(self) -> dict[str, Any]:
        return {
            "chunk_id": self.chunk_id,
            "content": self.content,
            "title": self.title,
            "condition_id": self.condition_id,
            "condition_label": self.condition_label,
            "step_number": self.step_number,
            "similarity_score": round(self.similarity_score, 4),
            "confidence_score": round(self.confidence_score, 4),
            "is_contraindication": self.is_contraindication,
            "citation": self.citation.model_dump(),
            "warning_note": self.warning_note,
            "cpr_bpm": self.cpr_bpm,
            "legal_shield": self.legal_shield,
        }


class RAGRetriever:
    """Hybrid semantic retriever and re-ranker for evidence-based emergency protocols."""

    def __init__(self):
        self.store = vector_store

    async def retrieve(
        self,
        query: str,
        condition_id: str | None = None,
        crisis_type: str | None = None,
        top_k: int = 5,
    ) -> list[RetrievedPassage]:
        """Perform hybrid retrieval: dense vector similarity + metadata filtering + keyword re-ranking."""
        if not query or not query.strip():
            query = condition_id or "emergency first aid resuscitation protocol"

        # 1. Fetch top candidates from vector store
        where_clause = None
        if condition_id and condition_id != "general":
            where_clause = {"condition_id": condition_id}

        # Query vector store with fallback to unfiltered if filtered yields zero
        raw_matches = await self.store.search(query=query, top_k=top_k * 2, where=where_clause)
        if not raw_matches and where_clause:
            raw_matches = await self.store.search(query=query, top_k=top_k * 2, where=None)

        if not raw_matches:
            logger.warning("No RAG passages retrieved for query: '%s'", query)
            return []

        # 2. Re-ranking: compute hybrid score combining dense similarity and clinical keyword overlap
        q_tokens = set(re.findall(r"\w+", query.lower()))
        re_ranked: list[RetrievedPassage] = []

        for chunk, sim_score in raw_matches:
            chunk_tokens = set(re.findall(r"\w+", chunk.text.lower()))
            overlap_count = len(q_tokens.intersection(chunk_tokens))
            keyword_bonus = min(0.30, overlap_count * 0.05)

            condition_bonus = 0.15 if (condition_id and chunk.condition_id == condition_id) else 0.0
            contraindication_bonus = 0.10 if (chunk.is_contraindication and any(w in query.lower() for w in ["water", "cut", "suck", "medicine", "pill", "can i", "should i", "give"])) else 0.0

            # Composite calibrated confidence
            composite_confidence = min(0.99, max(0.10, sim_score * 0.65 + keyword_bonus + condition_bonus + contraindication_bonus))

            citation = CitationItem(
                source=chunk.source,
                section=chunk.section,
                guideline_name=chunk.guideline_name,
                authority=chunk.authority,
                url=chunk.url,
            )

            re_ranked.append(
                RetrievedPassage(
                    chunk_id=chunk.chunk_id,
                    content=chunk.text,
                    title=chunk.title,
                    condition_id=chunk.condition_id,
                    condition_label=chunk.condition_label,
                    step_number=chunk.step_number,
                    similarity_score=sim_score,
                    confidence_score=composite_confidence,
                    is_contraindication=chunk.is_contraindication,
                    citation=citation,
                    warning_note=chunk.warning_note,
                    cpr_bpm=chunk.cpr_bpm,
                    legal_shield=chunk.legal_shield,
                )
            )

        # Sort by calibrated confidence descending
        re_ranked.sort(key=lambda p: p.confidence_score, reverse=True)
        return re_ranked[:top_k]

    def format_context_for_prompt(self, passages: list[RetrievedPassage]) -> str:
        """Format retrieved passages into a structured clinical grounding context for Gemini 2.5."""
        if not passages:
            return "No verified protocol passages retrieved. Advise bystander to await professional emergency services (108)."

        context_blocks = []
        for idx, p in enumerate(passages, start=1):
            block = (
                f"--- [VERIFIED PASSAGE {idx}] ---\n"
                f"Source Authority: {p.citation.authority} ({p.citation.source})\n"
                f"Guideline: {p.citation.guideline_name} • {p.citation.section}\n"
                f"Condition: {p.condition_label}\n"
                f"{p.content}\n"
            )
            if p.warning_note:
                block += f"Clinical Precaution: {p.warning_note}\n"
            if p.cpr_bpm:
                block += f"CPR Standard: {p.cpr_bpm} BPM Metronome Rhythm\n"
            context_blocks.append(block)

        return "\n".join(context_blocks)


rag_retriever = RAGRetriever()
