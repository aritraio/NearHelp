"""NearHelp AI — Backend API Service Entrypoint."""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import api_router
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

# Route Mounts
# Versioned API routes: /api/v1/auth, /api/v1/users
app.include_router(api_router, prefix=settings.API_V1_STR)

# Direct compatibility aliases: /api/auth, /api/users
app.include_router(auth_compat_router, prefix="/api/auth")
app.include_router(users_compat_router, prefix="/api/users")


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
