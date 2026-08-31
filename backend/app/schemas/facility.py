"""NearHelp AI — Facility Pydantic Schemas."""

import uuid
from datetime import datetime
from enum import Enum
from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class FacilityType(str, Enum):
    """Emergency facility category."""
    HOSPITAL = "hospital"
    AED = "aed"
    TRAUMA_CENTER = "trauma_center"
    BLOOD_BANK = "blood_bank"
    CLINIC = "clinic"


class FacilityBase(BaseModel):
    """Base schema attributes for emergency facilities."""
    name: str = Field(..., max_length=255, description="Official facility or AED site name")
    facility_type: FacilityType = Field(default=FacilityType.HOSPITAL, description="Category of emergency facility")
    address: str = Field(..., max_length=512, description="Street address and landmark")
    phone: str | None = Field(default=None, max_length=50, description="Telephone number")
    emergency_helpline: str | None = Field(default=None, max_length=50, description="Trauma/Emergency desk direct line")
    latitude: float = Field(..., ge=-90.0, le=90.0, description="Latitude in WGS 84 decimal degrees")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="Longitude in WGS 84 decimal degrees")
    zone: str | None = Field(default=None, max_length=100, description="Locality or sector zone")
    bed_availability: int = Field(default=0, ge=0, description="Vacant acute/general beds")
    total_beds: int = Field(default=0, ge=0, description="Total licensed bed capacity")
    icu_availability: int = Field(default=0, ge=0, description="Vacant critical care ICU beds")
    total_icu: int = Field(default=0, ge=0, description="Total critical care ICU beds")
    trauma_level: str | None = Field(default=None, max_length=100, description="Clinical trauma readiness classification")
    has_cardiac_unit: bool = Field(default=False, description="Presence of 24/7 cardiac cath lab")
    has_burn_unit: bool = Field(default=False, description="Presence of specialized burn unit")
    is_24_hours: bool = Field(default=True, description="24/7 emergency readiness")
    is_verified: bool = Field(default=True, description="Verified by operations")
    aed_building_name: str | None = Field(default=None, max_length=255, description="Host building name for AED")
    aed_location_description: str | None = Field(default=None, max_length=512, description="Precise floor/room retrieval instructions")
    aed_access_code: str | None = Field(default=None, max_length=100, description="Cabinet lock code or security instruction")
    extra_metadata: dict[str, Any] = Field(default_factory=dict, description="Extended facility metadata")


class FacilityCreate(FacilityBase):
    """Schema for registering a new facility."""
    pass


class FacilityUpdate(BaseModel):
    """Schema for updating facility details or live bed/ICU occupancy."""
    name: str | None = Field(default=None, max_length=255)
    facility_type: FacilityType | None = None
    address: str | None = Field(default=None, max_length=512)
    phone: str | None = Field(default=None, max_length=50)
    emergency_helpline: str | None = Field(default=None, max_length=50)
    latitude: float | None = Field(default=None, ge=-90.0, le=90.0)
    longitude: float | None = Field(default=None, ge=-180.0, le=180.0)
    zone: str | None = Field(default=None, max_length=100)
    bed_availability: int | None = Field(default=None, ge=0)
    total_beds: int | None = Field(default=None, ge=0)
    icu_availability: int | None = Field(default=None, ge=0)
    total_icu: int | None = Field(default=None, ge=0)
    trauma_level: str | None = Field(default=None, max_length=100)
    has_cardiac_unit: bool | None = None
    has_burn_unit: bool | None = None
    is_24_hours: bool | None = None
    is_verified: bool | None = None
    aed_building_name: str | None = Field(default=None, max_length=255)
    aed_location_description: str | None = Field(default=None, max_length=512)
    aed_access_code: str | None = Field(default=None, max_length=100)
    extra_metadata: dict[str, Any] | None = None


class FacilityResponse(FacilityBase):
    """Schema representing an emergency facility with calculated geodesic distance."""
    id: uuid.UUID
    created_at: datetime | None = None
    updated_at: datetime | None = None
    distance_meters: float | None = Field(default=None, description="Distance from origin query coordinates in meters")
    distance_km: float | None = Field(default=None, description="Distance from origin query coordinates in kilometers")
    eta_minutes: int | None = Field(default=None, description="Estimated ambulance/transit arrival time in minutes")

    model_config = ConfigDict(from_attributes=True)


class NearbyFacilitiesResponse(BaseModel):
    """Response payload for spatial nearby facility queries."""
    count: int = Field(..., description="Number of facilities matching query criteria")
    center_latitude: float = Field(..., description="Query epicenter latitude")
    center_longitude: float = Field(..., description="Query epicenter longitude")
    radius_km: float = Field(..., description="Search perimeter radius in kilometers")
    facilities: list[FacilityResponse] = Field(..., description="Sorted list of nearby emergency facilities")


class FacilitySeedResponse(BaseModel):
    """Response payload for database facility seeding."""
    status: str = "success"
    message: str
    inserted_count: int
    updated_count: int
    total_count: int
