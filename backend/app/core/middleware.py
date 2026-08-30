"""NearHelp AI — Custom Middleware: Idempotency & Rate Limiting."""

import json
import logging
import time
from collections.abc import Callable

import redis.asyncio as aioredis
from fastapi import Request, Response, status
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.types import ASGIApp

from app.core.config import settings

logger = logging.getLogger(__name__)

# In-memory fallback stores for when Redis is disconnected or during standalone unit testing
_in_memory_idempotency: dict[str, tuple[float, int, bytes, dict[str, str]]] = {}
_in_memory_rate_limit: dict[str, list] = {}

_redis_client: aioredis.Redis | None = None
_redis_checked: bool = False

async def get_redis_client() -> aioredis.Redis | None:
    """Acquire or initialize asynchronous Redis client."""
    global _redis_client, _redis_checked
    if _redis_client is not None:
        return _redis_client
    if _redis_checked:
        return None
    try:
        client = aioredis.from_url(
            settings.REDIS_URL,
            encoding="utf-8",
            decode_responses=False,
            socket_timeout=0.2,
        )
        await client.ping()
        _redis_client = client
        _redis_checked = True
        return _redis_client
    except Exception as e:
        _redis_checked = True
        logger.debug(f"Redis connection unavailable, utilizing fast in-memory store: {e}")
        return None


class IdempotencyMiddleware(BaseHTTPMiddleware):
    """Middleware enforcing idempotent request handling for mutating API operations."""

    def __init__(self, app: ASGIApp, ttl_seconds: int = 86400):
        super().__init__(app)
        self.ttl_seconds = ttl_seconds

    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        # Only check idempotency on mutating operations (POST, PUT, PATCH)
        if request.method not in ("POST", "PUT", "PATCH"):
            return await call_next(request)

        idempotency_key = request.headers.get("Idempotency-Key") or request.headers.get("X-Idempotency-Key")
        if not idempotency_key:
            return await call_next(request)

        cache_key = f"idempotency:{idempotency_key}"
        redis = await get_redis_client()

        # Check Redis cache
        if redis:
            try:
                cached_data = await redis.get(cache_key)
                if cached_data:
                    payload = json.loads(cached_data.decode("utf-8"))
                    headers = payload.get("headers", {})
                    headers["X-Idempotent-Replay"] = "true"
                    return Response(
                        content=payload.get("body", "").encode("utf-8"),
                        status_code=payload.get("status_code", 200),
                        headers=headers,
                        media_type="application/json",
                    )
            except Exception as e:
                logger.warning(f"Error checking Redis idempotency key: {e}")
        else:
            # Check in-memory store
            now = time.time()
            if idempotency_key in _in_memory_idempotency:
                exp, status_code, body, headers = _in_memory_idempotency[idempotency_key]
                if now < exp:
                    h = dict(headers)
                    h["X-Idempotent-Replay"] = "true"
                    return Response(
                        content=body,
                        status_code=status_code,
                        headers=h,
                        media_type="application/json",
                    )
                else:
                    del _in_memory_idempotency[idempotency_key]

        # Execute downstream request
        response = await call_next(request)

        # Cache successful or client-error responses (avoid caching transient 500 server crashes)
        if response.status_code < 500:
            # Read response body
            response_body = [section async for section in response.body_iterator]
            full_body = b"".join(response_body)

            # Reconstruct response to return to client
            headers_dict = dict(response.headers)
            new_response = Response(
                content=full_body,
                status_code=response.status_code,
                headers=headers_dict,
                media_type=response.media_type,
            )

            # Store in Redis
            if redis:
                try:
                    payload = {
                        "status_code": response.status_code,
                        "body": full_body.decode("utf-8", errors="ignore"),
                        "headers": headers_dict,
                    }
                    await redis.setex(cache_key, self.ttl_seconds, json.dumps(payload))
                except Exception as e:
                    logger.warning(f"Error caching idempotency response to Redis: {e}")
            else:
                # Store in memory
                _in_memory_idempotency[idempotency_key] = (
                    time.time() + self.ttl_seconds,
                    response.status_code,
                    full_body,
                    headers_dict,
                )

            return new_response

        return response


class RateLimitMiddleware(BaseHTTPMiddleware):
    """Middleware enforcing sliding rate limits on sensitive authentication and general endpoints."""

    def __init__(self, app: ASGIApp, auth_limit: int = 30, window_seconds: int = 60):
        super().__init__(app)
        self.auth_limit = auth_limit
        self.window_seconds = window_seconds

    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        path = request.url.path

        # Apply specific rate limiting to authentication endpoints
        is_auth_route = "/auth/" in path and request.method == "POST"
        if not is_auth_route:
            return await call_next(request)

        client_ip = request.client.host if request.client else "127.0.0.1"
        rate_key = f"ratelimit:{client_ip}:{path}"
        now = time.time()
        redis = await get_redis_client()

        if redis:
            try:
                # Sliding window log in Redis
                pipe = redis.pipeline()
                pipe.zremrangebyscore(rate_key, 0, now - self.window_seconds)
                pipe.zadd(rate_key, {f"{now}:{time.time_ns()}": now})
                pipe.zcard(rate_key)
                pipe.expire(rate_key, self.window_seconds)
                results = await pipe.execute()

                current_count = results[2]
                if current_count > self.auth_limit:
                    return JSONResponse(
                        status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                        content={
                            "detail": "Too many authentication attempts. Please try again shortly.",
                            "retry_after_seconds": self.window_seconds,
                        },
                        headers={"Retry-After": str(self.window_seconds)},
                    )
            except Exception as e:
                logger.debug(f"Redis rate limit check bypassed: {e}")
        else:
            # In-memory sliding window
            timestamps = _in_memory_rate_limit.get(rate_key, [])
            # Filter timestamps within window
            timestamps = [ts for ts in timestamps if ts > (now - self.window_seconds)]
            timestamps.append(now)
            _in_memory_rate_limit[rate_key] = timestamps

            if len(timestamps) > self.auth_limit:
                return JSONResponse(
                    status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                    content={
                        "detail": "Too many authentication attempts. Please try again shortly.",
                        "retry_after_seconds": self.window_seconds,
                    },
                    headers={"Retry-After": str(self.window_seconds)},
                )

        return await call_next(request)
