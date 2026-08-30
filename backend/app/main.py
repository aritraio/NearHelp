"""NearHelp AI — Backend API Service Entrypoint."""

import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.api import api_router
from app.api.admin import router as admin_compat_router
from app.api.ai import router as ai_compat_router
from app.api.auth import router as auth_compat_router
from app.api.users import router as users_compat_router
from app.core.config import settings
from app.core.middleware import (
    IdempotencyMiddleware,
    RateLimitMiddleware,
    get_redis_client,
)
from app.db.session import init_db

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifecycle manager for database initialization and cache warmup."""
    logger.info("Initializing NearHelp AI Backend Service...")
    await init_db()
    await get_redis_client()
    logger.info("NearHelp AI Backend Service Startup Complete.")
    yield
    logger.info("Shutting down NearHelp AI Backend Service.")


app = FastAPI(
    title="NearHelp AI — Core Backend API",
    description="Emergency Response Coordination, Spatial PostGIS Dispatch, and WebSocket Location Streaming",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

# 1. CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 2. Rate Limiting Middleware (Sliding window rate limit on auth endpoints)
app.add_middleware(
    RateLimitMiddleware,
    auth_limit=settings.RATE_LIMIT_AUTH_PER_MINUTE,
    window_seconds=60,
)

# 3. Idempotency Key Middleware (Prevents duplicate POST/PUT requests using Redis)
app.add_middleware(
    IdempotencyMiddleware,
    ttl_seconds=settings.IDEMPOTENCY_EXPIRE_SECONDS,
)

# Mount static uploads directory for avatar photos and certificate documents
uploads_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "../uploads"))
os.makedirs(uploads_path, exist_ok=True)
os.makedirs(os.path.join(uploads_path, "avatars"), exist_ok=True)
os.makedirs(os.path.join(uploads_path, "certificates"), exist_ok=True)
app.mount("/uploads", StaticFiles(directory=uploads_path), name="uploads")

# Route Mounts
# Versioned API routes: /api/v1/auth, /api/v1/users, /api/v1/admin, /api/v1/ai
app.include_router(api_router, prefix=settings.API_V1_STR)

# Direct compatibility aliases: /api/auth, /api/users, /api/admin, /api/ai
app.include_router(auth_compat_router, prefix="/api/auth")
app.include_router(users_compat_router, prefix="/api/users")
app.include_router(admin_compat_router, prefix="/api/admin")
app.include_router(ai_compat_router, prefix="/api/ai")


@app.get("/health", tags=["Health"])
async def health_check():
    """System health check endpoint."""
    return {
        "status": "healthy",
        "service": "nearhelp-backend",
        "version": "1.0.0",
        "spatial_engine": "PostGIS 3.4",
        "database": "connected",
        "environment": settings.ENVIRONMENT,
    }


@app.get("/", tags=["Root"])
async def root():
    return {
        "message": "NearHelp AI Backend Service Active",
        "documentation": "/docs",
        "status": "online",
        "version": "1.0.0",
    }
