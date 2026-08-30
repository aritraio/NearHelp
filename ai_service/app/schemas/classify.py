"""NearHelp AI — Classification & Multimodal Pydantic Schemas."""

from typing import Any

from pydantic import BaseModel, Field


class ClassificationRequest(BaseModel):
    """Input payload for emergency classification from text, voice audio, or photo."""

    text: str | None = Field(
        None,
        description="Free-text emergency description or bystander notes",
        examples=["Man collapsed in lobby, unresponsive, clutching chest, gasping for air"],
    )
    audio_base64: str | None = Field(
        None,
        description="Base64-encoded audio byte stream from voice SOS or microphone",
    )
    audio_format: str | None = Field(
        "wav",
        description="Audio format encoding (wav, mp3, ogg, m4a, flac, webm)",
    )
    image_base64: str | None = Field(
        None,
        description="Base64-encoded photo of emergency scene or injuries",
    )
    image_mime_type: str | None = Field(
        "image/jpeg",
        description="MIME type of attached image (image/jpeg, image/png, image/webp)",
    )
    language_code: str | None = Field(
        None,
        description="Spoken or written language hint (en-IN, bn-IN, hi-IN, auto)",
    )
    location: list[float] | None = Field(
        None,
        description="GPS coordinates of the incident [latitude, longitude]",
        examples=[[22.5726, 88.4312]],
    )
    metadata: dict[str, Any] | None = Field(
        default_factory=dict,
        description="Optional auxiliary metadata (victim profile, caller info, etc.)",
    )


class ClassificationResponse(BaseModel):
    """Structured clinical and operational triage output from the AI emergency detector."""

    emergency_type: str = Field(
        ...,
        description="Primary crisis classification category (medical, fire, gas_leak, accident, crime, natural_disaster)",
        examples=["medical"],
    )
    sub_type: str = Field(
        ...,
        description="Granular emergency subtype or clinical condition identifier",
        examples=["cardiac_arrest"],
    )
    priority: str = Field(
        ...,
        description="Triage priority level (critical, high, medium, low)",
        examples=["critical"],
    )
    severity_level: int = Field(
        ...,
        ge=1,
        le=5,
        description="Quantified emergency triage severity level (1=Lowest, 5=Life Threat)",
        examples=[5],
    )
    confidence: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Cosine similarity and clinical inference confidence score (0.00 to 1.00)",
        examples=[0.984],
    )
    confidence_percentage: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Human-readable confidence percentage (e.g. 98.4%)",
        examples=[98.4],
    )
    recommended_radius_km: float = Field(
        ...,
        ge=0.1,
        le=20.0,
        description="Calculated spatial dispatch search perimeter in kilometers",
        examples=[3.0],
    )
    suggested_responder_skills: list[str] = Field(
        default_factory=list,
        description="List of verified skills prioritized for responder recruitment",
        examples=[["CPR_CERTIFIED", "DOCTOR", "EMT"]],
    )
    immediate_action: str = Field(
        ...,
        description="Direct AHA/IRC first-aid action directive for on-scene bystanders",
        examples=["Begin CPR immediately. Push hard and fast in center of chest at 110-120 BPM."],
    )
    requires_professional: bool = Field(
        ...,
        description="Indicates whether advance emergency medical services/fire brigade are mandatory",
        examples=[True],
    )
    call_emergency_services: bool = Field(
        ...,
        description="Triggers automatic or recommended 108/101/100 emergency dispatch dialer",
        examples=[True],
    )
    emergency_number: str = Field(
        ...,
        description="Applicable statutory Indian emergency helpline number (108, 101, 100, 112)",
        examples=["108"],
    )
    detected_symptoms: list[str] = Field(
        default_factory=list,
        description="Extracted key clinical symptom markers and danger indicators",
        examples=[["Unresponsive", "Agonal Breathing", "Sudden Collapse"]],
    )
    transcription: str | None = Field(
        None,
        description="Transcribed text from voice audio input, if provided",
    )
    image_description: str | None = Field(
        None,
        description="Multimodal scene understanding summary from photo input, if provided",
    )
    processing_time_ms: float = Field(
        ...,
        description="Total classification inference and vector matching time in milliseconds",
        examples=[42.5],
    )


class TranscribeRequest(BaseModel):
    """Voice audio transcription request."""

    audio_base64: str = Field(..., description="Base64-encoded audio stream")
    audio_format: str = Field("wav", description="Audio format encoding")
    language_code: str | None = Field("en-IN", description="Language code hint")


class TranscribeResponse(BaseModel):
    """Voice audio transcription response."""

    transcription: str = Field(..., description="Transcribed plain text")
    language_detected: str = Field(..., description="Detected language code")
    confidence: float = Field(..., description="Speech recognition confidence score")
    processing_time_ms: float = Field(..., description="Transcription latency in milliseconds")


class VisionRequest(BaseModel):
    """Photo emergency scene analysis request."""

    image_base64: str = Field(..., description="Base64-encoded photo")
    image_mime_type: str = Field("image/jpeg", description="MIME type")
    prompt: str | None = Field(None, description="Optional guided prompt for vision model")


class VisionResponse(BaseModel):
    """Photo emergency scene analysis response."""

    scene_description: str = Field(..., description="Structured clinical scene description")
    inferred_emergency_type: str = Field(..., description="Visually detected emergency type")
    detected_hazards: list[str] = Field(default_factory=list, description="Visual hazard markers")
    detected_injuries: list[str] = Field(default_factory=list, description="Visual injury indicators")
    confidence: float = Field(..., description="Vision classification confidence")
    processing_time_ms: float = Field(..., description="Vision processing latency in ms")


class ClinicalConditionItem(BaseModel):
    """One of the 8 curated clinical conditions in the emergency matrix."""

    id: str
    label: str
    icon_name: str
    severity: int
    priority: str
    description: str
    symptoms: list[str]
    suggested_skills: list[str]
    immediate_action: str
    recommended_radius_km: float
    emergency_number: str


class CrisisTypeItem(BaseModel):
    """Top-level crisis category definition."""

    id: str
    name: str
    description: str
    default_emergency_number: str
    sub_types: list[str]


class TaxonomyResponse(BaseModel):
    """Full crisis taxonomy and 8 clinical conditions matrix."""

    crisis_types: list[CrisisTypeItem]
    clinical_conditions: list[ClinicalConditionItem]
    version: str = "1.0.0"
