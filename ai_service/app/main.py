"""NearHelp AI — AI Microservice Entrypoint (Gemini 2.5 + LangGraph + RAG)."""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.classify import router as classify_router
from app.api.severity import router as severity_router
from app.classifiers.embedding_service import embedding_service
from app.core.config import settings

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifecycle manager for warming up embedding models and caches."""
    logger.info("Initializing NearHelp AI Microservice & Pre-warming Vector Embeddings...")
    await embedding_service.initialize()
    logger.info("NearHelp AI Microservice Startup Complete.")
    yield
    logger.info("Shutting down NearHelp AI Microservice.")


app = FastAPI(
    title="NearHelp AI — AI Triage & RAG Service",
    description="Multimodal Emergency Detection, Severity Scoring, and LangGraph First-Aid RAG Agent",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Canonical and versioned router mounts
app.include_router(classify_router, prefix="/api/v1")
app.include_router(classify_router)
app.include_router(severity_router, prefix="/api/v1")
app.include_router(severity_router)


@app.get("/health", tags=["Health"])
async def health_check():
    """AI Service health check."""
    return {
        "status": "healthy",
        "service": "nearhelp-ai-service",
        "version": "1.0.0",
        "model": settings.GEMINI_MODEL,
        "embedding_model": settings.EMBEDDING_MODEL,
        "rag_engine": "ChromaDB",
        "agent_framework": "LangGraph",
    }


@app.get("/", tags=["Root"])
async def root():
    return {
        "message": "NearHelp AI Microservice Active",
        "documentation": "/docs",
        "status": "online",
        "version": "1.0.0",
    }
