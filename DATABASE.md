# Database Documentation

## 1. Engine & Connection Configuration

Source: [`backend/app/db/session.py`](backend/app/db/session.py)

- **Production / Docker Engine:** PostgreSQL 16 + PostGIS 3.4 (`postgis/postgis:16-3.4-alpine`), accessed via `sqlalchemy.ext.asyncio` + `asyncpg`.
  - Pool: `AsyncAdaptedQueuePool` with `pool_size=10`, `max_overflow=20`, `pool_timeout=30`, `pool_recycle=1800`, `pool_pre_ping=True`, `statement_cache_size=0`, `prepared_statement_cache_size=0`.
  - URL normalization in `Settings.validate_db_urls` and `_get_async_url`: converts `postgres://` and `postgresql://` to `postgresql+asyncpg://`.
- **Unit Test Engine:** In-memory SQLite (`sqlite+aiosqlite:///:memory:`) with `StaticPool` in [`backend/tests/conftest.py`](backend/tests/conftest.py).
  - GeoAlchemy2 `Geometry` compiles to `BLOB` under SQLite (`@compiles(Geometry, "sqlite")`).
  - Spatial functions (`ST_DWithin`, `ST_Distance`, `ST_SetSRID`, `ST_MakePoint`, `AsEWKT`, `AsEWKB`) are registered as Python SQLite stubs (note: `ST_DWithin` always returns `1` and `ST_Distance` returns `0` in SQLite).
- **Schema Initialization & Migrations:** `alembic` is listed in `backend/requirements.txt`, **but no Alembic `migrations/` directory or `alembic.ini` exists**. Tables are created on startup via `Base.metadata.create_all`, followed by ad-hoc PostgreSQL `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` and `CREATE INDEX IF NOT EXISTS` statements in `init_db()`.

See [docs/diagrams/04-database-erd.md](docs/diagrams/04-database-erd.md) for the complete Mermaid Entity Relationship Diagram.

---

## 2. Tables & Schemas

### 2.1 `users` ([`backend/app/models/user.py`](backend/app/models/user.py))

Stores registered users, OAuth/phone users, administrators, and disposable anonymous SOS users.

| Column | Type | Constraints / Default | Notes |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | PK, `uuid4`, indexed | Primary identifier |
| `email` | `String(255)` | Unique, indexed, nullable | Lowercased on registration/login; null for anonymous/phone-only |
| `hashed_password` | `String(255)` | Nullable | Bcrypt hash (max 72 UTF-8 bytes) |
| `name` | `String(255)` | Nullable | Display name |
| `photo_url` | `String(1024)` | Nullable | Avatar URL (e.g. `/uploads/avatars/...`) |
| `phone` | `String(50)` | Unique, indexed, nullable | E.164 phone number |
| `phone_verified` | `Boolean` | Not null, `False` | Phone verification flag |
| `firebase_uid` | `String(255)` | Unique, indexed, nullable | External Firebase UID |
| `auth_provider` | `String(50)` | Not null, `'email'` | `'email'`, `'google'`, `'phone'`, `'anonymous'` |
| `is_anonymous` | `Boolean` | Not null, indexed, `False` | Disposable guest session flag |
| `is_active` | `Boolean` | Not null, `True` | Suspension control (`False` blocks HTTP auth) |
| `is_superuser` | `Boolean` | Not null, `False` | Admin Command Center privilege |
| `blood_group` | `String(10)` | Nullable | **Plaintext** blood type (e.g., `O+`) |
| `medical_conditions` | `JSON` | Not null, `[]` | **Encrypted at rest** (`aes256gcm:...` string stored in JSON column) |
| `known_allergies` | `JSON` | Not null, `[]` | **Encrypted at rest** (`aes256gcm:...`) |
| `has_pacemaker` | `Boolean` | Not null, `False` | Plaintext boolean flag |
| `is_organ_donor` | `Boolean` | Not null, `False` | Plaintext boolean flag |
| `medical_notes` | `String(2048)` | Nullable | **Encrypted at rest** (`aes256gcm:...`) |
| `emergency_contacts` | `JSON` | Not null, `[]` | Plaintext JSON array `[{id, name, phone, relationship, is_primary}]` (max 5) |
| `languages` | `JSON` | Not null, `["en"]` | ISO 639-1 / supported ISO 639-3 codes |
| `skills` | `JSON` | Not null, `[]` | `[{skill_type, verified, certificate_url, verified_at}]` |
| `trust_score` | `Float` | Not null, `50.0` | Reputation score `[0.0, 100.0]` |
| `badges` | `JSON` | Not null, `[]` | Earned badge strings (e.g. `CPR_CERTIFIED`, `VERIFIED_RESPONDER`) |
| `fcm_token` | `String(512)` | Nullable | Plaintext FCM registration token |
| `device_info` | `JSON` | Nullable | Stores `available`, `location_updated_at`, `last_latitude`, `last_longitude`, `last_heading`, `last_speed`, `last_updated`, `device_id`, `platform` |
| `location` | `Geometry(POINT, 4326)` | Nullable, GiST index `idx_users_location` | WGS84 PostGIS point (`POINT(lon lat)`) |
| `created_at`, `updated_at`, `last_login_at` | `DateTime(tz)` | `func.now()` | Audit timestamps |

### 2.2 `user_presence` ([`backend/app/models/user_presence.py`](backend/app/models/user_presence.py))

Tracks recent authenticated HTTP activity across workers.

| Column | Type | Constraints | Notes |
| :--- | :--- | :--- | :--- |
| `user_id` | `UUID` | PK, FK -> `users.id` (`ON DELETE CASCADE`) | 1-to-1 with `users` |
| `last_seen_at` | `DateTime(tz)` | Not null, indexed | Upserted with 30s throttle; "online" = within 300s |

### 2.3 `sos_events` ([`backend/app/models/sos_event.py`](backend/app/models/sos_event.py))

| Column | Type | Constraints / Default | Notes |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | PK, `uuid4` | Deterministic UUIDv5 when `Idempotency-Key` supplied |
| `broadcaster_id` | `UUID` | FK -> `users.id` (`ON DELETE SET NULL`), nullable, indexed | `NULL` when `is_anonymous=True` or guest without token |
| `crisis_type`, `sub_type` | `String(50)`, `String(100)` | `'medical'`, nullable | Category and subtype |
| `severity_score`, `severity_level`, `priority` | `Integer`, `Integer`, `String(50)` | `50`, `3`, `'high'` | AI triage outputs |
| `description`, `symptoms`, `immediate_action`, `required_skills` | `String(2048)`, `JSON`, `String(1024)`, `JSON` | Nullable / `[]` | Clinical details & recommended skills |
| `location`, `latitude`, `longitude`, `address`, `sub_address` | `Geometry(POINT, 4326)`, `Float`, `Float`, `String(512)` | GiST index `idx_sos_events_location` | Note: `SOSService.create_sos_event` writes `latitude`/`longitude` floats; `location` Geometry column is not populated on insert! |
| `status` | `String(50)` | `'SOS_TRIGGERED'`, indexed | `SOS_TRIGGERED`, `RESPONDER_ACCEPTED`, `RESOLVED`, `CANCELLED` |
| `is_anonymous` | `Boolean` | `False`, indexed | Anonymous mode flag |
| `current_radius_meters`, `max_radius_meters`, `escalation_layer`, `auto_call_108_triggered` | `Float`, `Float`, `Integer`, `Boolean` | `1500.0`, `5000.0`, `1`, `False` | 3-layer escalation state |
| `ai_triage_data`, `metadata_info` | `JSON`, `JSON` | `{}` | Full AI response JSON + `request_fingerprint` |
| `created_at`, `updated_at`, `resolved_at` | `DateTime(tz)` | `func.now()` | Lifecycle timestamps |

### 2.4 `responses` ([`backend/app/models/response.py`](backend/app/models/response.py))

| Column | Type | Constraints | Notes |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | PK | Engagement ID |
| `sos_event_id` | `UUID` | FK -> `sos_events.id` (`ON DELETE CASCADE`), indexed | Incident link |
| `responder_id` | `UUID` | FK -> `users.id` (`ON DELETE CASCADE`), indexed | Responder link |
| `status` | `String(50)` | `'ACCEPTED'`, indexed | `ACCEPTED`, `EN_ROUTE`, `ARRIVED`, `DECLINED`, `CANCELLED` |
| `initial_distance_meters`, `initial_eta_seconds`, `ranking_score` | `Float`, `Integer`, `Float` | Nullable | Captured at acceptance |
| `joined_at`, `arrived_at` | `DateTime(tz)` | `func.now()`, nullable | Response timestamps |
| `feedback_score`, `feedback_notes` | `Float`, `String(1024)` | Nullable | Recorded on resolution |

### 2.5 `timeline_events` ([`backend/app/models/timeline_event.py`](backend/app/models/timeline_event.py))

Immutable audit log + persisted participant chat.

| Column | Type | Constraints | Notes |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | PK | Event ID |
| `sos_event_id` | `UUID` | FK -> `sos_events.id` (`ON DELETE CASCADE`), indexed | Incident link |
| `actor_id` | `UUID` | FK -> `users.id` (`ON DELETE SET NULL`), nullable | User who acted (null for system events) |
| `event_type` | `String(100)` | Not null, indexed | e.g., `sos_created`, `ai_classified`, `response_accepted`, `RESPONDER_ARRIVED`, `chat_message`, `radius_expanded`, `escalation_layer_updated`, `sos_resolved`, `sos_cancelled` |
| `details` | `JSON` | Not null, `{}` | Event metadata or `{text, language}` for chat |
| `timestamp` | `DateTime(tz)` | `func.now()`, indexed | Event timestamp |

### 2.6 `skill_verifications` ([`backend/app/models/skill_verification.py`](backend/app/models/skill_verification.py))

| Column | Type | Constraints | Notes |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | PK | Verification request ID |
| `user_id` | `UUID` | FK -> `users.id` (`ON DELETE CASCADE`), indexed | Claimant |
| `skill_type` | `String(100)` | Not null, indexed | Normalized code (`CPR_CERTIFIED`, `DOCTOR`, `EMT`, `NURSE`, `FIRST_AID`, etc.) |
| `certificate_url` | `String(1024)` | Not null | Local static path (`/uploads/certificates/...`) |
| `status` | `String(50)` | `'PENDING'`, indexed | `PENDING`, `APPROVED`, `REJECTED` |
| `rejection_reason`, `notes` | `String(1024)` | Nullable | Reviewer feedback / credential notes |
| `reviewed_by` | `UUID` | FK -> `users.id` (`ON DELETE SET NULL`), nullable | Reviewing admin |
| `submitted_at`, `reviewed_at` | `DateTime(tz)` | `func.now()`, nullable | Audit timestamps |

### 2.7 `facilities` ([`backend/app/models/facility.py`](backend/app/models/facility.py))

Auto-seeded on startup from [`data/regional/kolkata_facilities.json`](data/regional/kolkata_facilities.json).

| Column | Type | Notes |
| :--- | :--- | :--- |
| `id`, `name`, `facility_type`, `address`, `phone`, `emergency_helpline`, `zone` | UUID / Strings | `hospital`, `aed`, `trauma_center`, `blood_bank`, `clinic` |
| `latitude`, `longitude`, `location` | `Float`, `Float`, `Geometry(POINT, 4326)` | GiST index `idx_facilities_location`; note `FacilityService.get_nearby_facilities` filters in Python using Haversine rather than PostGIS `ST_DWithin` |
| `bed_availability`, `total_beds`, `icu_availability`, `total_icu`, `trauma_level`, `has_cardiac_unit`, `has_burn_unit` | Integers / String / Booleans | Static dataset capacity metrics |
| `is_24_hours`, `is_verified` | Booleans | Operational flags |
| `aed_building_name`, `aed_location_description`, `aed_access_code`, `extra_metadata` | Strings / JSON | AED cabinet access instructions |

---

## 3. Data Integrity & Indexing Observations

1. **Spatial Index Mismatch:** `idx_users_location` is a `GIST (location)` index on a `Geometry(POINT, 4326)` column, while `GeoService` queries `WHERE ST_DWithin(location::geography, ...)`. PostgreSQL does not use a `geometry` GiST index for `::geography` casts unless a functional geography index is created.
2. **Unpopulated `sos_events.location`:** `SOSService.create_sos_event` saves `latitude` and `longitude`, but never constructs a `WKTElement` for `sos_events.location` (so `sos_events.location` remains `NULL` on newly created rows).
3. **Committed SQLite File:** `backend/nearhelp.db` exists in the repository directory (local dev/artifact file).
