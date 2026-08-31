"""NearHelp AI — Facility Model Definition."""

import uuid

from geoalchemy2 import Geometry
from sqlalchemy import (
    JSON,
    Boolean,
    Column,
    DateTime,
    Float,
    Integer,
    String,
    func,
)
from sqlalchemy.dialects.postgresql import UUID as PG_UUID

from app.db.base import Base


class Facility(Base):
    """Emergency facility entity for NearHelp AI.
    
    Represents physical emergency infrastructure including hospitals, trauma centers,
    verified automated external defibrillator (AED) locator nodes, and blood banks.
    Tracks live capacity metrics (general bed and ICU availability) and spatial geometry.
    """
    __tablename__ = "facilities"

    # Primary Key
    id = Column(
        PG_UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
        doc="Unique facility identifier (UUID v4)",
    )

    # Identifiers & Categorization
    name = Column(
        String(255),
        nullable=False,
        index=True,
        doc="Official name of the medical hospital or AED deployment location",
    )
    facility_type = Column(
        String(50),
        nullable=False,
        index=True,
        default="hospital",
        doc="Type of emergency facility: 'hospital', 'aed', 'trauma_center', 'blood_bank', 'clinic'",
    )
    address = Column(
        String(512),
        nullable=False,
        doc="Physical street address and postal landmark",
    )
    phone = Column(
        String(50),
        nullable=True,
        doc="General reception or desk telephone number",
    )
    emergency_helpline = Column(
        String(50),
        nullable=True,
        doc="Dedicated emergency/trauma desk hotline",
    )

    # Geospatial Coordinates & PostGIS Point
    latitude = Column(
        Float,
        nullable=False,
        index=True,
        doc="Latitude in WGS 84 decimal degrees",
    )
    longitude = Column(
        Float,
        nullable=False,
        index=True,
        doc="Longitude in WGS 84 decimal degrees",
    )
    location = Column(
        Geometry(geometry_type="POINT", srid=4326),
        nullable=True,
        doc="PostGIS Point geometry for ST_DWithin and ST_Distance spatial indexes",
    )
    zone = Column(
        String(100),
        nullable=True,
        index=True,
        doc="Geographic locality / administrative zone (e.g. 'Salt Lake Sector V', 'EM Bypass')",
    )

    # Hospital Capacity Metrics
    bed_availability = Column(
        Integer,
        default=0,
        nullable=False,
        doc="Number of currently available acute/general inpatient beds",
    )
    total_beds = Column(
        Integer,
        default=0,
        nullable=False,
        doc="Total licensed bed capacity",
    )
    icu_availability = Column(
        Integer,
        default=0,
        nullable=False,
        doc="Number of currently vacant critical care / ICU beds",
    )
    total_icu = Column(
        Integer,
        default=0,
        nullable=False,
        doc="Total ICU bed capacity",
    )
    trauma_level = Column(
        String(100),
        nullable=True,
        doc="Clinical trauma readiness designation (e.g. 'Level 1 Trauma Center', 'Cardiac ICU')",
    )
    has_cardiac_unit = Column(
        Boolean,
        default=False,
        nullable=False,
        doc="Flag indicating presence of 24/7 cath lab and cardiac emergency team",
    )
    has_burn_unit = Column(
        Boolean,
        default=False,
        nullable=False,
        doc="Flag indicating dedicated burn ICU capability",
    )

    # Operational Flags
    is_24_hours = Column(
        Boolean,
        default=True,
        nullable=False,
        doc="Whether emergency facility operates 24/7",
    )
    is_verified = Column(
        Boolean,
        default=True,
        nullable=False,
        doc="Whether facility coordinates and credentials have been verified by NearHelp operations",
    )

    # AED Mesh Specific Attributes
    aed_building_name = Column(
        String(255),
        nullable=True,
        doc="Building or complex name hosting the AED device",
    )
    aed_location_description = Column(
        String(512),
        nullable=True,
        doc="Detailed floor/room access instructions for bystanders retrieving the AED",
    )
    aed_access_code = Column(
        String(100),
        nullable=True,
        doc="Cabinet access passcode or security override instruction",
    )

    # Flexible Extensible Metadata
    extra_metadata = Column(
        JSON,
        default=dict,
        nullable=False,
        doc="Extended attributes (ambulance fleet size, accreditation, battery health, etc.)",
    )

    # Timestamps
    created_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
        doc="Record creation timestamp",
    )
    updated_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
        doc="Last capacity or telemetry update timestamp",
    )
