"""NearHelp AI — AI Microservice Entrypoint (Gemini 2.5 + LangGraph + RAG)."""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="NearHelp AI — AI Triage & RAG Service",
    description="Multimodal Emergency Detection, Severity Scoring, and LangGraph First-Aid RAG Agent",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health", tags=["Health"])
async def health_check():
    """AI Service health check."""
    return {
        "status": "healthy",
        "service": "nearhelp-ai-service",
        "version": "1.0.0",
        "model": "Gemini 2.5 Flash",
        "rag_engine": "ChromaDB",
        "agent_framework": "LangGraph",
    }


@app.get("/", tags=["Root"])
async def root():
    return {
        "message": "NearHelp AI Microservice Active",
        "documentation": "/docs",
        "status": "online",
    }
