# ADR-009: Same-Origin Static Admin Panel Served by FastAPI

- **Status:** Implemented (Diverged from Planned Next.js App)
- **Date:** 2026-11-02 (commits `d594ab5`, `176bf5d`, `a960d85`); verified 2026-11-25 against code

## Context

Administrators need to review pending responder skill certifications, manage user accounts (create, edit, suspend, delete, purge demo/anonymous users), and monitor platform statistics. Earlier plans (`docs/architecture.md`, `.github/workflows/ci.yml`) envisioned a separate Next.js `admin_dashboard/` app.

## Decision

Build a dependency-free, same-origin web SPA (`index.html`, `app.js`, `style.css`) under `backend/app/admin_web/` and serve it directly from FastAPI at `/admin` with strict CSP headers (`script-src 'self'; frame-ancestors 'none'`) and superuser-protected APIs under `/api/v1/admin/*`. Track online user activity in a shared `user_presence` database table (5-minute window, 30s write throttle).

## Evidence in Code

- [`backend/app/main.py`](../../backend/app/main.py)
- [`backend/app/admin_web/index.html`](../../backend/app/admin_web/index.html)
- [`backend/app/admin_web/app.js`](../../backend/app/admin_web/app.js)
- [`backend/app/api/admin.py`](../../backend/app/api/admin.py)
- [`backend/app/api/admin_users.py`](../../backend/app/api/admin_users.py)
- [`backend/app/services/presence_service.py`](../../backend/app/services/presence_service.py)
- [`backend/app/cli.py`](../../backend/app/cli.py)

## Consequences & Trade-offs

- **Pros:** Zero separate frontend build/hosting pipeline; immune to localStorage XSS token theft because tokens are kept strictly in JavaScript page memory.
- **Cons / Risks:**
  - `.github/workflows/ci.yml` still does not reflect any `admin_dashboard/` (and `docs/architecture.md` still references Next.js `admin_dashboard/`).
  - `init_db()` in [`backend/app/db/session.py`](../../backend/app/db/session.py) auto-bootstraps a default superuser (`admin@nearhelp.ai` / `AdminPassword123!`) from `settings.INITIAL_ADMIN_*` defaults if not overridden in `.env`, contradicting the `README.md` claim that there are no default admin credentials.
  - Uploaded certificates in `/uploads/certificates/` are served via unauthenticated `StaticFiles`.
