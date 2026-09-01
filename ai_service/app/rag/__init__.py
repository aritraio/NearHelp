"""NearHelp AI — RAG Knowledge Base Module."""

from app.rag.chunker import DocumentChunker, ProtocolChunk, document_chunker
from app.rag.guardrails import HallucinationGuardrails, hallucination_guardrails
from app.rag.retriever import RAGRetriever, RetrievedPassage, rag_retriever
from app.rag.store import ProtocolVectorStore, vector_store

__all__ = [
    "DocumentChunker",
    "ProtocolChunk",
    "document_chunker",
    "HallucinationGuardrails",
    "hallucination_guardrails",
    "ProtocolVectorStore",
    "vector_store",
    "RAGRetriever",
    "RetrievedPassage",
    "rag_retriever",
]
