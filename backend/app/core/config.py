"""NearHelp AI — Core Application Configuration."""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # Application Info
    PROJECT_NAME: str = "NearHelp AI"
    ENVIRONMENT: str = "development"
    DEBUG: bool = True
    API_V1_STR: str = "/api/v1"

    # CORS
    CORS_ORIGINS: list[str] = [
        "http://localhost:5173",
        "http://localhost:3000",
        "http://127.0.0.1:5173",
        "http://localhost:8000",
        "*"
    ]

    # JWT Authentication & Tokens
    SECRET_KEY: str = "nearhelp_super_secret_jwt_key_change_in_production_32bytes_min"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 15
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    ANONYMOUS_TOKEN_EXPIRE_MINUTES: int = 60

    # PostgreSQL / PostGIS Database
    POSTGRES_SERVER: str = "localhost"
    POSTGRES_PORT: int = 5432
    POSTGRES_USER: str = "postgres"
    POSTGRES_PASSWORD: str = "postgres"
    POSTGRES_DB: str = "nearhelp_db"
    DATABASE_URL: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/nearhelp_db"
    DATABASE_SYNC_URL: str = "postgresql://postgres:postgres@localhost:5432/nearhelp_db"

    # Redis Cache & Session Store
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    REDIS_PASSWORD: str = ""
    REDIS_URL: str = "redis://localhost:6379/0"

    # Firebase Authentication & FCM
    FIREBASE_PROJECT_ID: str = "nearhelp-ai"
    FIREBASE_CREDENTIALS_PATH: str = "./firebase_service_account.json"
    FCM_SERVER_KEY: str = ""

    # Rate Limiting & Resilience
    RATE_LIMIT_AUTH_PER_MINUTE: int = 30
    RATE_LIMIT_DEFAULT_PER_MINUTE: int = 100
    IDEMPOTENCY_EXPIRE_SECONDS: int = 86400  # 24 hours

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore",
    )


settings = Settings()
