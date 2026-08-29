"""NearHelp AI — API Routers Registry."""

from fastapi import APIRouter

from app.api.admin import router as admin_router
from app.api.auth import router as auth_router
from app.api.users import router as users_router

api_router = APIRouter()

# Canonical versioned routes: /api/v1/auth, /api/v1/users, and /api/v1/admin
api_router.include_router(auth_router, prefix="/auth")
api_router.include_router(users_router, prefix="/users")
api_router.include_router(admin_router, prefix="/admin")
