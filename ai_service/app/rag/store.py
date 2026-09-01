"""NearHelp AI — ChromaDB Vector Store & Protocol Ingestion Engine."""

from __future__ import annotations

import logging
import os
from pathlib import Path
from typing import Any

from app.classifiers.embedding_service import embedding_service
from app.core.config import settings
from app.rag.chunker import ProtocolChunk, document_chunker

logger = logging.getLogger(__name__)


class ProtocolVectorStore:
    """ChromaDB-backed persistent vector store for clinical emergency protocols."""

    COLLECTION_NAME = "nearhelp_first_aid_rag"

    def __init__(self, persist_directory: str | None = None):
        self.persist_directory = persist_directory or settings.CHROMA_PERSIST_DIRECTORY
        self._client = None
        self._collection = None
        self._initialized = False

    def _get_client(self):
        """Initialize persistent ChromaDB client with directory fallback."""
        if self._client is None:
            try:
                import chromadb
                from chromadb.config import Settings as ChromaSettings

                # Ensure persist directory exists
                p_path = Path(self.persist_directory)
                p_path.mkdir(parents=True, exist_ok=True)

                self._client = chromadb.PersistentClient(
                    path=str(p_path),
                    settings=ChromaSettings(anonymized_telemetry=False),
                )
                logger.info("ChromaDB PersistentClient initialized at: %s", p_path)
            except Exception as e:
                logger.warning("Failed to initialize ChromaDB PersistentClient (%s). Using ephemeral Client.", e)
                import chromadb
                self._client = chromadb.EphemeralClient()
        return self._client

    def _get_collection(self):
        """Get or create the protocol collection."""
        if self._collection is None:
            client = self._get_client()
            self._collection = client.get_or_create_collection(
                name=self.COLLECTION_NAME,
                metadata={"hnsw:space": "cosine"},
            )
        return self._collection

    async def initialize(self, force_reload: bool = False):
        """Initialize vector store and auto-ingest protocol corpus if empty or forced."""
        if self._initialized and not force_reload:
            return

        collection = self._get_collection()
        count = collection.count()
        logger.info("Current ChromaDB '%s' collection count: %d", self.COLLECTION_NAME, count)

        if count == 0 or force_reload:
            logger.info("Indexing official emergency protocols into ChromaDB vector store...")
            protocol_dirs = [
                Path("./data/protocols"),
                Path("../data/protocols"),
                Path("/Users/aritra/Code/Projects/NearHelp/data/protocols"),
            ]
            target_dir = next((d for d in protocol_dirs if d.exists() and d.is_dir()), None)

            if target_dir:
                chunks = document_chunker.chunk_directory(target_dir)
                if chunks:
                    await self.add_chunks(chunks)
                    logger.info("Successfully ingested %d protocol chunks into ChromaDB.", len(chunks))
                else:
                    logger.warning("No protocol chunks generated from directory: %s", target_dir)
            else:
                logger.warning("Protocol directory not found in candidate paths.")

        self._initialized = True

    async def add_chunks(self, chunks: list[ProtocolChunk]):
        """Embed and batch upsert protocol chunks into ChromaDB."""
        if not chunks:
            return

        collection = self._get_collection()
        ids = [c.chunk_id for c in chunks]
        documents = [c.text for c in chunks]
        metadatas = [c.to_metadata() for c in chunks]

        # Generate dense embeddings for all chunk texts
        embeddings: list[list[float]] = []
        for chunk in chunks:
            emb = await embedding_service.generate_embedding(chunk.text)
            embeddings.append(emb)

        # Batch upsert into ChromaDB
        batch_size = 50
        for i in range(0, len(ids), batch_size):
            end = i + batch_size
            collection.upsert(
                ids=ids[i:end],
                documents=documents[i:end],
                embeddings=embeddings[i:end],
                metadatas=metadatas[i:end],
            )
        logger.info("Upserted %d chunks into ChromaDB collection '%s'.", len(ids), self.COLLECTION_NAME)

    async def search(
        self,
        query: str,
        top_k: int = 5,
        where: dict[str, Any] | None = None,
    ) -> list[tuple[ProtocolChunk, float]]:
        """Perform semantic similarity search with optional metadata filtering."""
        if not self._initialized:
            await self.initialize()

        collection = self._get_collection()
        query_emb = await embedding_service.generate_embedding(query)

        # Execute query
        results = collection.query(
            query_embeddings=[query_emb],
            n_results=min(top_k, max(1, collection.count())),
            where=where,
            include=["documents", "metadatas", "distances"],
        )

        matched_chunks: list[tuple[ProtocolChunk, float]] = []
        if not results or not results["ids"] or not results["ids"][0]:
            return matched_chunks

        ids = results["ids"][0]
        docs = results["documents"][0] if results.get("documents") else []
        metas = results["metadatas"][0] if results.get("metadatas") else []
        dists = results["distances"][0] if results.get("distances") else []

        for idx in range(len(ids)):
            meta = metas[idx] if idx < len(metas) else {}
            doc_text = docs[idx] if idx < len(docs) else ""
            dist = dists[idx] if idx < len(dists) else 0.0

            # Convert cosine distance to 0.0 - 1.0 similarity
            similarity_score = max(0.0, min(1.0, 1.0 - (dist / 2.0) if dist > 0 else 1.0))

            step_num = meta.get("step_number")
            chunk = ProtocolChunk(
                chunk_id=ids[idx],
                doc_id=meta.get("doc_id", "unknown_doc"),
                source=meta.get("source", "Medical Authority"),
                section=meta.get("section", "Guidelines"),
                guideline_name=meta.get("guideline_name", "Clinical Protocol"),
                authority=meta.get("authority", "Emergency Organization"),
                url=meta.get("url") or None,
                crisis_type=meta.get("crisis_type", "medical"),
                condition_id=meta.get("condition_id", "emergency"),
                condition_label=meta.get("condition_label", "Emergency Condition"),
                title=meta.get("title", "Clinical Protocol Step"),
                text=doc_text,
                step_number=step_num if step_num is not None and step_num >= 0 else None,
                warning_note=meta.get("warning_note"),
                is_contraindication=bool(meta.get("is_contraindication", False)),
                is_medication_restricted=bool(meta.get("is_medication_restricted", False)),
                is_surgical_restricted=bool(meta.get("is_surgical_restricted", False)),
                cpr_bpm=meta.get("cpr_bpm") if meta.get("cpr_bpm", 0) > 0 else None,
                legal_shield=meta.get("legal_shield", "Section 134A Motor Vehicles (Amendment) Act 2019"),
                token_count=meta.get("token_count", 0),
                tags=meta.get("tags_str", "").split(",") if meta.get("tags_str") else [],
            )
            matched_chunks.append((chunk, similarity_score))

        return matched_chunks

    def get_stats(self) -> dict[str, Any]:
        """Return vector store statistics."""
        collection = self._get_collection()
        count = collection.count()
        return {
            "collection_name": self.COLLECTION_NAME,
            "total_chunks": count,
            "persist_directory": self.persist_directory,
            "is_initialized": self._initialized,
            "embedding_dimension": 384,
            "vector_store": "ChromaDB",
        }

    async def reset(self):
        """Reset and re-index vector store."""
        client = self._get_client()
        try:
            client.delete_collection(self.COLLECTION_NAME)
        except Exception:
            pass
        self._collection = None
        self._initialized = False
        await self.initialize(force_reload=True)


vector_store = ProtocolVectorStore()
