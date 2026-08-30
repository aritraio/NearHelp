"""NearHelp AI — AI Microservice Configuration."""

from pydantic_settings import BaseSettings, SettingsConfigDict


class AISettings(BaseSettings):
    # Application Info
    PROJECT_NAME: str = "NearHelp AI — AI Service"
    ENVIRONMENT: str = "development"
    DEBUG: bool = True
    API_V1_STR: str = "/api/v1"

    # Server Ports & Host
    AI_SERVICE_HOST: str = "0.0.0.0"
    AI_SERVICE_PORT: int = 8001

    # Gemini & Generative AI
    GEMINI_API_KEY: str = ""
    GEMINI_MODEL: str = "gemini-2.5-flash"
    EMBEDDING_MODEL: str = "models/text-embedding-004"

    # Google Cloud & Speech-to-Text
    GOOGLE_CLOUD_PROJECT: str = "nearhelp-ai"
    GOOGLE_APPLICATION_CREDENTIALS: str = ""

    # ChromaDB Vector Store
    CHROMA_PERSIST_DIRECTORY: str = "./data/chroma_db"

    # CORS
    CORS_ORIGINS: list[str] = [
        "http://localhost:5173",
        "http://localhost:3000",
        "http://127.0.0.1:5173",
        "http://localhost:8000",
        "*",
    ]

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore",
    )


settings = AISettings()
