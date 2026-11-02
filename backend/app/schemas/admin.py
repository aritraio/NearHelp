"""Explicit admin field allowlists; credentials and medical records are never listed."""

import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict, EmailStr, Field, field_validator, model_validator


class AdminUserUpdate(BaseModel):
    model_config = ConfigDict(extra="forbid")

    name: str | None = Field(None, min_length=1, max_length=100)
    email: EmailStr | None = None
    phone: str | None = Field(None, pattern=r"^\+[1-9]\d{7,14}$")
    password: str | None = Field(None, min_length=8, max_length=72)
    is_active: bool | None = None
    is_superuser: bool | None = None
    trust_score: float | None = Field(None, ge=0, le=100, allow_inf_nan=False)

    @field_validator("name", "email", "phone", mode="before")
    @classmethod
    def strip_identity(cls, value):
        return value.strip() if isinstance(value, str) else value

    @field_validator("password")
    @classmethod
    def password_bytes(cls, value):
        if value is not None and len(value.encode("utf-8")) > 72:
            raise ValueError("Password must be at most 72 UTF-8 bytes")
        return value

    @model_validator(mode="after")
    def reject_null_controls(self):
        for key in {"name", "password", "is_active", "is_superuser", "trust_score"} & self.model_fields_set:
            if getattr(self, key) is None:
                raise ValueError(f"{key} cannot be null")
        return self


class AdminUserCreate(AdminUserUpdate):
    name: str = Field(..., min_length=1, max_length=100)
    email: EmailStr
    password: str = Field(..., min_length=8, max_length=72)
    is_active: bool = True
    is_superuser: bool = False
    trust_score: float = Field(50, ge=0, le=100, allow_inf_nan=False)


class AdminUserResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    name: str | None
    email: str | None
    phone: str | None
    auth_provider: str
    is_anonymous: bool
    is_active: bool
    is_superuser: bool
    trust_score: float
    created_at: datetime
    last_login_at: datetime | None
    last_seen_at: datetime | None = None
    is_online: bool = False


class AdminUserList(BaseModel):
    users: list[AdminUserResponse]
    total: int
    limit: int
    offset: int
