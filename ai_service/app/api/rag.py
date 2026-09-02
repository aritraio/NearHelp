"""NearHelp AI — RAG Knowledge Base REST Endpoints."""

import logging
import time
from typing import Any

from fastapi import APIRouter, HTTPException, status

from app.agent.knowledge import get_grounded_protocol
from app.core.config import settings
from app.rag.guardrails import hallucination_guardrails
from app.rag.retriever import rag_retriever
from app.rag.store import vector_store
from app.schemas.agent import CitationItem
from app.schemas.rag import (
    RAGQueryRequest,
    RAGQueryResponse,
    RAGSearchRequest,
    RAGSearchResponse,
    RAGStatsResponse,
    RetrievedPassageResponse,
)

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/rag", tags=["RAG Knowledge Base"])


@router.post(
    "/search",
    response_model=RAGSearchResponse,
    status_code=status.HTTP_200_OK,
    summary="Semantic Search across Verified Clinical Protocols",
    description="Searches ChromaDB vector store for evidence-based first-aid protocols with hybrid semantic re-ranking.",
)
async def search_rag_knowledge_base(request: RAGSearchRequest) -> RAGSearchResponse:
    """Execute hybrid semantic search over clinical protocol vector store."""
    start_time = time.perf_counter()
    try:
        passages = await rag_retriever.retrieve(
            query=request.query,
            condition_id=request.condition_id,
            crisis_type=request.crisis_type,
            top_k=request.top_k,
        )

        passage_responses = [
            RetrievedPassageResponse(
                chunk_id=p.chunk_id,
                title=p.title,
                content=p.content,
                condition_id=p.condition_id,
                condition_label=p.condition_label,
                step_number=p.step_number,
                similarity_score=p.similarity_score,
                confidence_score=p.confidence_score,
                is_contraindication=p.is_contraindication,
                citation=p.citation,
                warning_note=p.warning_note,
                cpr_bpm=p.cpr_bpm,
                legal_shield=p.legal_shield,
            )
            for p in passages
        ]

        latency_ms = (time.perf_counter() - start_time) * 1000.0
        return RAGSearchResponse(
            query=request.query,
            total_results=len(passage_responses),
            passages=passage_responses,
            latency_ms=round(latency_ms, 2),
        )
    except Exception as e:
        logger.exception("RAG search error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"RAG search failed: {e!s}",
        ) from e


@router.post(
    "/query",
    response_model=RAGQueryResponse,
    status_code=status.HTTP_200_OK,
    summary="Generate Grounded First-Aid Guidance with Safety Guardrails",
    description="End-to-end RAG pipeline with hallucination guardrails, citation enforcement, and Section 134A legal protection.",
)
async def query_rag_knowledge_base(request: RAGQueryRequest) -> RAGQueryResponse:
    """Execute end-to-end RAG query answering with clinical guardrail validation."""
    start_time = time.perf_counter()
    try:
        # 1. Pre-execution Clinical Guardrail Check
        guardrail_result = hallucination_guardrails.inspect_query(
            user_query=request.query,
            condition_id=request.condition_id,
        )

        if not guardrail_result.passed and guardrail_result.override_reply:
            latency_ms = (time.perf_counter() - start_time) * 1000.0
            return RAGQueryResponse(
                query=request.query,
                answer=guardrail_result.override_reply,
                highlight_tag=guardrail_result.highlight_tag or "Clinical Contraindication Alert",
                citations=[
                    CitationItem(
                        source="Motor Vehicles (Amendment) Act 2019",
                        section="Section 134A Good Samaritan Protection",
                        guideline_name="National First Aid & Good Samaritan Safety Standard",
                        authority="Ministry of Road Transport & Highways, Govt of India",
                        url="https://morth.nic.in/good-samaritan-guidelines",
                    )
                ],
                contraindications=guardrail_result.contraindications,
                grounded_passages=[],
                is_safe=False,
                latency_ms=round(latency_ms, 2),
            )

        # 2. Retrieve Grounded Passages
        passages = await rag_retriever.retrieve(
            query=request.query,
            condition_id=request.condition_id,
            top_k=request.top_k,
        )

        passage_responses = [
            RetrievedPassageResponse(
                chunk_id=p.chunk_id,
                title=p.title,
                content=p.content,
                condition_id=p.condition_id,
                condition_label=p.condition_label,
                step_number=p.step_number,
                similarity_score=p.similarity_score,
                confidence_score=p.confidence_score,
                is_contraindication=p.is_contraindication,
                citation=p.citation,
                warning_note=p.warning_note,
                cpr_bpm=p.cpr_bpm,
                legal_shield=p.legal_shield,
            )
            for p in passages
        ]

        # Extract citations
        citations = [p.citation for p in passages] if passages else []

        # 3. Generate Grounded Guidance
        if passages:
            top_p = passages[0]
            answer_parts = [
                f"✅ {top_p.title.upper()}",
                f"{top_p.content}",
            ]
            if len(passages) > 1:
                answer_parts.append("\n👉 NEXT IMMEDIATE ACTIONS:")
                for p in passages[1:3]:
                    answer_parts.append(f"• {p.title}: {p.content.splitlines()[-1] if p.content else ''}")

            answer_parts.append(f"\n[Source: {top_p.citation.source} • {top_p.citation.section}]")
            raw_answer = "\n\n".join(answer_parts)
        else:
            raw_answer = (
                "Please keep the victim safe, still, and calm. Emergency ambulance 108 has been alerted. "
                "Do not administer unverified oral liquids or medications."
            )

        # 4. Post-generation Sanitization & Verification
        sanitized_res = hallucination_guardrails.sanitize_llm_response(raw_answer, citations=citations)
        final_answer = sanitized_res.sanitized_text or raw_answer
        highlight_tag = sanitized_res.highlight_tag or "Grounded Protocol Step"

        latency_ms = (time.perf_counter() - start_time) * 1000.0
        return RAGQueryResponse(
            query=request.query,
            answer=final_answer,
            highlight_tag=highlight_tag,
            citations=citations,
            contraindications=sanitized_res.contraindications,
            grounded_passages=passage_responses,
            is_safe=sanitized_res.passed,
            latency_ms=round(latency_ms, 2),
        )
    except Exception as e:
        logger.exception("RAG query answering error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"RAG query failed: {e!s}",
        ) from e


@router.get(
    "/stats",
    response_model=RAGStatsResponse,
    status_code=status.HTTP_200_OK,
    summary="Get RAG Vector Store Corpus Statistics",
)
async def get_rag_corpus_stats() -> RAGStatsResponse:
    """Retrieve vector store chunk count and index status."""
    try:
        stats = vector_store.get_stats()
        return RAGStatsResponse(**stats)
    except Exception as e:
        logger.exception("RAG stats error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to fetch RAG stats: {e!s}",
        ) from e


@router.post(
    "/ingest",
    status_code=status.HTTP_200_OK,
    summary="Trigger Re-Ingestion of Protocol Corpus",
)
async def reingest_rag_protocols() -> dict[str, Any]:
    """Force re-ingest all JSON/Markdown protocols in data/protocols/ into ChromaDB."""
    try:
        await vector_store.reset()
        stats = vector_store.get_stats()
        return {
            "status": "reingested",
            "total_chunks": stats["total_chunks"],
            "collection": stats["collection_name"],
            "vector_store": stats["vector_store"],
        }
    except Exception as e:
        logger.exception("RAG ingestion error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to reingest protocols: {e!s}",
        ) from e
