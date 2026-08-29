"""NearHelp AI — Backend API Service Entrypoint."""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="NearHelp AI — Core Backend API",
    description="Emergency Response Coordination, Spatial PostGIS Dispatch, and WebSocket Location Streaming",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# Enable Cross-Origin Resource Sharing
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health", tags=["Health"])
async def health_check():
    """System health check endpoint."""
    return {
        "status": "healthy",
        "service": "nearhelp-backend",
        "version": "1.0.0",
        "spatial_engine": "PostGIS 3.4",
        "database": "connected",
    }


@app.get("/", tags=["Root"])
async def root():
    return {
        "message": "NearHelp AI Backend Service Active",
        "documentation": "/docs",
        "status": "online",
    }
