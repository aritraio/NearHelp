# ADR-002: PostgreSQL 16 + PostGIS 3.4 with SQLite Test Stubs

- **Status:** Implemented (Schema Migrations Partial)
- **Date:** 2026-08-12 (original); verified 2026-11-25 against code

## Context

SOS responder dispatch and facility lookup require transactional ACID guarantees for incident creation, relational integrity across users/incidents/responses/timelines, and geospatial radius filtering (`ST_DWithin`, `ST_Distance`).

## Decision

Use **PostgreSQL 16 with PostGIS 3.4** (`postgis/postgis:16-3.4-alpine` in Docker Compose) via SQLAlchemy 2.0 async sessions (`asyncpg`) and GeoAlchemy2 (`Geometry("POINT", srid=4326)`). For lightweight unit testing without a running PostGIS container, compile `Geometry` columns to `BLOB` in SQLite, register stub SQLite spatial functions, and provide a Haversine geodesic fallback in `GeoService`.

## Evidence in Code

- [`docker-compose.yml`](../../docker-compose.yml)
- [`backend/app/db/session.py`](../../backend/app/db/session.py)
- [`backend/app/services/geo_service.py`](../../backend/app/services/geo_service.py)
- [`backend/tests/conftest.py`](../../backend/tests/conftest.py)

## Consequences & Trade-offs

- **Pros:** Strong relational consistency with row locking (`with_for_update()`) during resolution/cancellation; GiST spatial indexes on `users.location`, `sos_events.location`, and `facilities.location`; fast in-memory SQLite test execution.
- **Cons / Risks:**
  - `alembic` is in `requirements.txt`, but **no Alembic migrations directory exists**; schema management relies on `Base.metadata.create_all` plus ad-hoc `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` statements in `init_db()`.
  - `GeoService.find_nearby_active_responders` casts `location::geography` in `ST_DWithin` / `ST_Distance`, while the GiST indexes are created on the `geometry` column, which may bypass the geometry GiST index without a functional geography index.
  - SQLite tests do not exercise real PostGIS behavior (`ST_DWithin` stub always returns `1`!).
