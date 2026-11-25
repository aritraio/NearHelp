# 5. Authentication Workflow

NearHelp AI supports four authentication branches that all converge on backend-issued HS256 JWT access and refresh tokens.

```mermaid
sequenceDiagram
    autonumber
    participant Client as "Android / Admin Web"
    participant API as "Auth API (/api/v1/auth/*)"
    participant Rate as "RateLimitMiddleware (30/min)"
    participant AuthSvc as "AuthService"
    participant Firebase as "Firebase / Google OIDC"
    participant DB as "PostgreSQL (users)"

    Client->>Rate: POST /auth/{register|login|google|phone/verify|anonymous}
    alt Rate limit exceeded
        Rate-->>Client: 429 Too Many Requests
    else Allowed
        Rate->>API: Forward request
        API->>AuthSvc: Dispatch by provider

        alt Email Register / Login
            AuthSvc->>DB: Lookup user by lower(email)
            AuthSvc->>AuthSvc: Hash or verify password with bcrypt
        else Google Sign-In
            AuthSvc->>Firebase: verify_firebase_id_token(id_token)
            Note right of AuthSvc: In dev/test mode, mock_* prefixes and unverified JWTs are accepted
            AuthSvc->>DB: Lookup or create user by firebase_uid / email
        else Phone OTP (Partial)
            AuthSvc->>AuthSvc: Check Firebase id_token OR len(otp_code) >= 4
            AuthSvc->>DB: Lookup or create user by phone
        else Anonymous Guest
            AuthSvc->>DB: Insert disposable User (is_anonymous=true)
        end

        DB-->>AuthSvc: Persisted User record
        AuthSvc->>AuthSvc: Sign HS256 Access JWT (15m) & Refresh JWT (7d)
        AuthSvc-->>Client: TokenResponse {access_token, refresh_token, user}
    end

    Note over Client,DB: Authenticated Request & Presence Update
    Client->>API: Request with Authorization: Bearer access_token
    API->>API: Decode HS256 JWT, check type=="access", check user.is_active
    API->>DB: Upsert user_presence (throttled to 30s)
```

## Key Source References

- Routes: [`backend/app/api/auth.py`](../../backend/app/api/auth.py)
- Service: [`backend/app/services/auth_service.py`](../../backend/app/services/auth_service.py)
- Firebase verification & dev fallbacks: [`backend/app/services/firebase_service.py`](../../backend/app/services/firebase_service.py)
- Token signing & bcrypt: [`backend/app/core/security.py`](../../backend/app/core/security.py)
- Request dependencies & presence: [`backend/app/core/dependencies.py`](../../backend/app/core/dependencies.py), [`backend/app/services/presence_service.py`](../../backend/app/services/presence_service.py)
