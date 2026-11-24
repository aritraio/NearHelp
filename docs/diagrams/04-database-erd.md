# 4. Database Entity Relationship Diagram

This ERD reflects the exact SQLAlchemy models defined in [`backend/app/models/`](../../backend/app/models/). Note that `messages` and `ai_summaries` tables shown in earlier planning docs do not exist; participant chat is persisted in `timeline_events` and AI triage output is stored in `sos_events.ai_triage_data`.

```mermaid
erDiagram
    users {
        UUID id PK
        String email UK "nullable"
        String hashed_password "nullable, bcrypt"
        String name "nullable"
        String photo_url "nullable"
        String phone UK "nullable"
        Boolean phone_verified
        String firebase_uid UK "nullable"
        String auth_provider "email|google|phone|anonymous"
        Boolean is_anonymous
        Boolean is_active
        Boolean is_superuser
        String blood_group "nullable"
        JSON medical_conditions "AES-256-GCM ciphertext"
        JSON known_allergies "AES-256-GCM ciphertext"
        Boolean has_pacemaker
        Boolean is_organ_donor
        String medical_notes "AES-256-GCM ciphertext"
        JSON emergency_contacts "max 5 items"
        JSON languages
        JSON skills
        Float trust_score "default 50.0"
        JSON badges
        String fcm_token "nullable"
        JSON device_info "availability & last coords"
        Geometry location "PostGIS POINT SRID 4326"
        DateTime created_at
        DateTime updated_at
        DateTime last_login_at "nullable"
    }

    user_presence {
        UUID user_id PK,FK "-> users.id CASCADE"
        DateTime last_seen_at
    }

    sos_events {
        UUID id PK
        UUID broadcaster_id FK "-> users.id SET NULL"
        String crisis_type
        String sub_type "nullable"
        Integer severity_score "0-100"
        Integer severity_level "1-5"
        String priority
        String description "nullable"
        JSON symptoms
        String immediate_action "nullable"
        JSON required_skills
        Geometry location "PostGIS POINT SRID 4326"
        Float latitude
        Float longitude
        String address "nullable"
        String sub_address "nullable"
        String status "SOS_TRIGGERED|RESPONDER_ACCEPTED|RESOLVED|CANCELLED"
        Boolean is_anonymous
        Float current_radius_meters
        Float max_radius_meters
        Integer escalation_layer
        Boolean auto_call_108_triggered
        JSON ai_triage_data
        JSON metadata_info
        DateTime created_at
        DateTime updated_at
        DateTime resolved_at "nullable"
    }

    responses {
        UUID id PK
        UUID sos_event_id FK "-> sos_events.id CASCADE"
        UUID responder_id FK "-> users.id CASCADE"
        String status "ACCEPTED|EN_ROUTE|ARRIVED|DECLINED"
        Float initial_distance_meters "nullable"
        Integer initial_eta_seconds "nullable"
        Float ranking_score "nullable"
        DateTime joined_at
        DateTime arrived_at "nullable"
        Float feedback_score "nullable"
        String feedback_notes "nullable"
    }

    timeline_events {
        UUID id PK
        UUID sos_event_id FK "-> sos_events.id CASCADE"
        UUID actor_id FK "-> users.id SET NULL"
        String event_type
        JSON details
        DateTime timestamp
    }

    skill_verifications {
        UUID id PK
        UUID user_id FK "-> users.id CASCADE"
        String skill_type
        String certificate_url
        String status "PENDING|APPROVED|REJECTED"
        String rejection_reason "nullable"
        String notes "nullable"
        UUID reviewed_by FK "-> users.id SET NULL"
        DateTime submitted_at
        DateTime reviewed_at "nullable"
    }

    facilities {
        UUID id PK
        String name
        String facility_type "hospital|aed|trauma_center|blood_bank|clinic"
        String address
        String phone "nullable"
        String emergency_helpline "nullable"
        Float latitude
        Float longitude
        Geometry location "PostGIS POINT SRID 4326"
        String zone "nullable"
        Integer bed_availability
        Integer total_beds
        Integer icu_availability
        Integer total_icu
        String trauma_level "nullable"
        Boolean has_cardiac_unit
        Boolean has_burn_unit
        Boolean is_24_hours
        Boolean is_verified
        String aed_building_name "nullable"
        String aed_location_description "nullable"
        String aed_access_code "nullable"
        JSON extra_metadata
        DateTime created_at
        DateTime updated_at
    }

    users ||--o| user_presence : "tracks activity"
    users ||--o{ sos_events : "broadcasts"
    users ||--o{ responses : "responds"
    sos_events ||--o{ responses : "receives"
    sos_events ||--o{ timeline_events : "logs"
    users ||--o{ timeline_events : "acts in"
    users ||--o{ skill_verifications : "submits"
    users ||--o{ skill_verifications : "reviews"
```
