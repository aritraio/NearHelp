"""NearHelp AI — API Routers Registry."""

from fastapi import APIRouter

from app.api.admin import router as admin_router
from app.api.ai import router as ai_router
from app.api.auth import router as auth_router
from app.api.facilities import router as facilities_router
from app.api.sos import router as sos_router
from app.api.tracking_ws import router as tracking_router
from app.api.users import router as users_router

api_router = APIRouter()

# Canonical versioned routes: /api/v1/auth, /api/v1/users, /api/v1/admin, /api/v1/ai, /api/v1/sos, /api/v1/facilities
api_router.include_router(auth_router, prefix="/auth")
api_router.include_router(users_router, prefix="/users")
api_router.include_router(admin_router, prefix="/admin")
api_router.include_router(ai_router, prefix="/ai")
api_router.include_router(sos_router, prefix="/sos")
api_router.include_router(facilities_router, prefix="/facilities")
api_router.include_router(tracking_router)
