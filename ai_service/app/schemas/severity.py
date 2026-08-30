"""NearHelp AI — AI Severity Prediction Pydantic Schemas."""

from typing import Any

from pydantic import BaseModel, Field


class SeverityScoreFactors(BaseModel):
    """Detailed breakdown of clinical severity scoring factors (0 to 100)."""

    life_threat_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Immediacy of threat to victim's life and vital organ failure risk (0-100)",
        examples=[95.0],
    )
    time_sensitivity_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Time sensitivity and urgency of intervention before irreversible damage/hypoxia (0-100)",
        examples=[98.0],
    )
    casualty_risk_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Risk of multiple casualties or situation escalating to more victims (0-100)",
        examples=[30.0],
    )
    environmental_hazard_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Hazards present on scene such as fire, toxic gas, collapse, live electrical wires, or traffic (0-100)",
        examples=[20.0],
    )


class SeverityRequest(BaseModel):
    """Input payload for emergency severity evaluation and clinical triage."""

    text: str | None = Field(
        None,
        description="Free-text emergency description or bystander narration",
        examples=["Victim collapsed suddenly in office lobby, unresponsive, no pulse detected, gasping for air"],
    )
    emergency_type: str | None = Field(
        None,
        description="Optional pre-classified crisis category (medical, fire, gas_leak, accident, crime, natural_disaster)",
        examples=["medical"],
    )
    sub_type: str | None = Field(
        None,
        description="Optional granular clinical sub-type (cardiac_arrest, severe_bleeding, structural_fire, etc.)",
        examples=["cardiac_arrest"],
    )
    detected_symptoms: list[str] | None = Field(
        default_factory=list,
        description="List of detected or observed symptoms and clinical markers",
        examples=[["Unresponsive", "No pulse", "Agonal breathing"]],
    )
    patient_age: int | None = Field(
        None,
        ge=0,
        le=130,
        description="Age of the patient/victim if known",
        examples=[58],
    )
    unresponsive: bool | None = Field(
        None,
        description="Whether the victim is unresponsive or unconscious",
        examples=[True],
    )
    breathing_difficulty: bool | None = Field(
        None,
        description="Whether the victim has severe difficulty breathing or agonal gasping",
        examples=[True],
    )
    severe_bleeding: bool | None = Field(
        None,
        description="Whether there is active pulsatile or heavy bleeding",
        examples=[False],
    )
    location: list[float] | None = Field(
        None,
        description="GPS coordinates of the emergency [latitude, longitude]",
        examples=[[22.5726, 88.4312]],
    )
    metadata: dict[str, Any] | None = Field(
        default_factory=dict,
        description="Optional auxiliary metadata (responder count, caller info, etc.)",
    )


class SeverityResponse(BaseModel):
    """Structured clinical severity score, Level 1-5 triage, radius, auto-dial flags, and reasoning."""

    severity_score: int = Field(
        ...,
        ge=0,
        le=100,
        description="Quantified overall severity score on a 0 to 100 continuous scale",
        examples=[96],
    )
    severity_level: int = Field(
        ...,
        ge=1,
        le=5,
        description="Standardized discrete triage level (Level 5=Critical Life Threat, 4=Urgent Trauma, 3=Moderate Emergency, 2=Minor/Low, 1=Non-acute)",
        examples=[5],
    )
    priority: str = Field(
        ...,
        description="Operational triage priority tier (critical, high, medium, low)",
        examples=["critical"],
    )
    confidence: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Clinical confidence score of the AI assessment (0.00 to 1.00)",
        examples=[0.984],
    )
    confidence_percentage: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Human-readable confidence percentage (e.g. 98.4%)",
        examples=[98.4],
    )
    reasoning: list[str] = Field(
        ...,
        description="Bulleted clinical justifications and observations explaining the severity score",
        examples=[[
            "Unresponsive victim indicates possible cardiac arrest or acute neurological catastrophe.",
            "Absence of pulse and agonal breathing represents an immediate, irreversible life threat.",
            "CPR and defibrillation must be initiated within the 4-5 minute hypoxia window for brain survival.",
        ]],
    )
    factors: SeverityScoreFactors = Field(
        ...,
        description="Breakdown of individual risk factor components",
    )
    recommended_radius_km: float = Field(
        ...,
        ge=0.1,
        le=20.0,
        description="Dynamically calculated spatial responder recruitment radius in kilometers",
        examples=[3.5],
    )
    survival_window_minutes: int | None = Field(
        None,
        description="Estimated critical survival window before irreversible brain damage or mortality (e.g. 5 min hypoxia window)",
        examples=[5],
    )
    auto_call_emergency_services: bool = Field(
        ...,
        description="Indicates whether the system automatically dials or mandates statutory emergency services (108/101/100/112)",
        examples=[True],
    )
    suggested_call_action: str = Field(
        ...,
        description="Action directive for calling emergency services: 'auto_dial', 'suggested', 'optional', 'none'",
        examples=["auto_dial"],
    )
    emergency_number: str = Field(
        ...,
        description="Applicable statutory Indian emergency helpline number (108, 101, 100, 112)",
        examples=["108"],
    )
    recommended_actions: list[str] = Field(
        default_factory=list,
        description="Immediate life-saving clinical first-aid or safety action directives for on-scene bystanders",
        examples=[[
            "Begin CPR immediately: compress center of chest 5-6 cm deep at 110-120 BPM.",
            "Send a bystander to fetch the nearest Automated External Defibrillator (AED).",
            "Call 108 Emergency Medical Services immediately.",
            "Never administer water, oral fluids, or food to an unresponsive person.",
        ]],
    )
    required_responder_skills: list[str] = Field(
        default_factory=list,
        description="List of verified skills prioritized for responder recruitment",
        examples=[["CPR_CERTIFIED", "DOCTOR", "EMT", "NURSE"]],
    )
    processing_time_ms: float = Field(
        ...,
        description="Total AI severity assessment inference and scoring latency in milliseconds",
        examples=[24.6],
    )
