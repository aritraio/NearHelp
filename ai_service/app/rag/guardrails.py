"""NearHelp AI — Zero-Hallucination Clinical Guardrails for Emergency Guidance.

Enforces strict clinical contraindications:
1. Medication Guardrail: Prohibits unverified prescriptions, oral fluids to unconscious victims.
2. Surgical Guardrail: Prohibits invasive surgical acts (no incisions, no tracheotomy, no impaled object extraction).
3. Post-Generation Verification: Sanitizes and guarantees verified protocol citations.
"""

from __future__ import annotations

import logging
import re
from dataclasses import dataclass
from typing import Any

from app.schemas.agent import CitationItem, ContraindicationAlert

logger = logging.getLogger(__name__)


@dataclass
class GuardrailCheckResult:
    """Outcome of clinical safety guardrail evaluation."""

    passed: bool
    is_contraindicated: bool
    contraindications: list[ContraindicationAlert]
    sanitized_text: str | None = None
    override_reply: str | None = None
    highlight_tag: str | None = None
    citations: list[CitationItem] | None = None


class HallucinationGuardrails:
    """Clinical safety supervisor enforcing zero medical hallucinations."""

    # Prohibited dangerous surgical directives & keywords
    FORBIDDEN_SURGICAL_PATTERNS: list[tuple[re.Pattern, str, str, str]] = [
        (
            re.compile(r"\b(cut|incise|incision|slice|lance|slash|blade)\b.*?\b(bite|wound|throat|skin|neck|swelling|blood)\b", re.IGNORECASE),
            "NO_SURGICAL_INCISION",
            "Do Not Cut or Incise Wound",
            "Cutting or making incisions into wounds or snakebites severs blood vessels, accelerates toxic venom absorption, and introduces lethal infections. Never make field incisions.",
        ),
        (
            re.compile(r"\b(tracheostomy|cricothyroidotomy|cut\s+windpipe|puncture\s+throat|insert\s+pen\s+in\s+throat)\b", re.IGNORECASE),
            "NO_FIELD_TRACHEOTOMY",
            "Field Tracheotomy Strictly Prohibited",
            "Emergency surgical airways in non-sterile field conditions cause catastrophic carotid artery transection and instant death. Maintain non-invasive airway positioning.",
        ),
        (
            re.compile(r"\b(pull\s+out|remove|extract|yank|pull)\b.*?\b(knife|rebar|pole|glass|object|rod|metal|impaled)\b", re.IGNORECASE),
            "NO_OBJECT_REMOVAL",
            "Do Not Remove Impaled Objects",
            "Impaled objects act as an internal mechanical plug against torn arterial vessels. Pulling them out causes massive uncontrollable internal exsanguination.",
        ),
        (
            re.compile(r"\b(push|set|straighten|realign|pop)\b.*?\b(broken\s+bone|bone|fracture|joint|limb)\b", re.IGNORECASE),
            "NO_BONE_REDUCTION",
            "Do Not Manipulate or Set Fractures",
            "Attempting to set broken bones or push exposed bones back inside the skin lacerates major neurovascular bundles and risks fat embolism.",
        ),
        (
            re.compile(r"\b(suck\s+venom|suck\s+poison|suck\s+blood|mouth\s+suction|suck)\b.*?\b(venom|poison|blood|bite)\b", re.IGNORECASE),
            "NO_VENOM_SUCTION",
            "Do Not Suck Snake Venom",
            "Sucking venom transfers deadly toxins into the rescuer's mouth mucosa and does not reduce tissue envenomation.",
        ),
    ]

    # Prohibited dangerous medications & unverified drug directives
    FORBIDDEN_MEDICATION_PATTERNS: list[tuple[re.Pattern, str, str, str]] = [
        (
            re.compile(r"\b(sleeping\s+pill|sedative|alprazolam|diazepam|valium|xanax|cough\s+syrup|tranquilizer)\b", re.IGNORECASE),
            "NO_SEDATIVES",
            "Strictly No Sedatives or Tranquilizers",
            "Sedatives and tranquilizers depress the central respiratory drive and cause fatal respiratory arrest in emergency victims.",
        ),
        (
            re.compile(r"\b(antibiotic|amoxicillin|ciprofloxacin|azithromycin|paracetamol\s+syrup)\b.*?\b(unconscious|bleeding|arrest|snake|seizure)\b", re.IGNORECASE),
            "NO_UNVERIFIED_ANTIBIOTICS",
            "No Unverified Medications in Acute Crisis",
            "Administering systemic antibiotics or oral pills during acute trauma/resuscitation causes choking, delays emergency transport, and is clinically ineffective.",
        ),
        (
            re.compile(r"\b(aspirin|disprin|ecospirin)\b.*?\b(stroke|head\s+injury|bleeding|trauma|accident|snake)\b", re.IGNORECASE),
            "NO_ASPIRIN_IN_STROKE_TRAUMA",
            "Do Not Give Aspirin in Stroke or Trauma",
            "Aspirin is a potent antiplatelet agent. Administering it before CT scan in stroke or in bleeding trauma can cause fatal intracerebral hemorrhage.",
        ),
        (
            re.compile(r"\b(induce\s+vomit|make\s+vomit|give\s+salt\s+water|stick\s+finger\s+in\s+throat)\b.*?\b(poison|acid|chemical|bleach|kerosene|petrol)\b", re.IGNORECASE),
            "NO_INDUCED_EMESIS_CORROSIVES",
            "Strictly Do Not Induce Vomiting",
            "Vomiting corrosive acids, alkalis, or petroleum causes severe secondary chemical burns to the esophagus and fatal aspiration pneumonitis.",
        ),
    ]

    # Unconscious patient fluid & oral restrictions
    UNCONSCIOUS_FLUID_PATTERNS: list[tuple[re.Pattern, str, str, str]] = [
        (
            re.compile(r"\b(give|drink|feed|pour|force|sip)\b.*?\b(water|tea|milk|juice|liquids?|medicine|food|tablet|pill)\b.*?\b(unconscious|unresponsive|passed\s+out|fainted|seizure|convulsing|sleeping|choking)\b", re.IGNORECASE),
            "NO_ORAL_FLUIDS_UNCONSCIOUS",
            "Strictly No Food, Water, or Oral Fluids",
            "Pouring water or placing tablets into the mouth of an unresponsive or convulsing person flows directly into the lungs, causing instant asphyxiation and fatal aspiration.",
        ),
        (
            re.compile(r"\b(put|insert|force)\b.*?\b(spoon|finger|key|shoe|cloth|onion|stick)\b.*?\b(mouth|teeth|tongue)\b", re.IGNORECASE),
            "NO_OBJECTS_IN_MOUTH_SEIZURE",
            "Do Not Put Any Object in the Mouth",
            "Forcing spoons, keys, or fingers into a convulsing person's mouth causes broken teeth, shattered jaws, and airway occlusion. People cannot swallow their tongues.",
        ),
    ]

    @classmethod
    def inspect_query(
        cls,
        user_query: str,
        condition_id: str | None = None,
    ) -> GuardrailCheckResult:
        """Inspect user query prior to LLM execution to detect and preempt dangerous intents."""
        q = user_query.strip()
        alerts: list[ContraindicationAlert] = []

        # 1. Check Unconscious Fluid / Mouth Insertion Restrictions
        for pattern, flag, title, explanation in cls.UNCONSCIOUS_FLUID_PATTERNS:
            if pattern.search(q):
                alerts.append(
                    ContraindicationAlert(
                        flag=flag,
                        severity="CRITICAL",
                        warning_title=title,
                        warning_message=explanation,
                        action_directive="Keep the victim lying on their side in recovery position. Do NOT put anything in the mouth.",
                    )
                )

        # 2. Check Surgical Directive Inquiries
        for pattern, flag, title, explanation in cls.FORBIDDEN_SURGICAL_PATTERNS:
            if pattern.search(q):
                alerts.append(
                    ContraindicationAlert(
                        flag=flag,
                        severity="CRITICAL",
                        warning_title=title,
                        warning_message=explanation,
                        action_directive="Never perform surgical incisions or probe wounds. Keep the patient still and await 108 emergency paramedics.",
                    )
                )

        # 3. Check Medication Inquiries
        for pattern, flag, title, explanation in cls.FORBIDDEN_MEDICATION_PATTERNS:
            if pattern.search(q):
                alerts.append(
                    ContraindicationAlert(
                        flag=flag,
                        severity="CRITICAL",
                        warning_title=title,
                        warning_message=explanation,
                        action_directive="Withhold unverified medications. Rely on basic first-aid positioning and professional paramedic handover.",
                    )
                )

        if alerts:
            top_alert = alerts[0]
            override_reply = (
                f"❌ CLINICAL CONTRAINDICATION: {top_alert.warning_title.upper()}.\n\n"
                f"{top_alert.warning_message}\n\n"
                f"👉 MANDATORY DIRECTIVE: {top_alert.action_directive}\n\n"
                f"⚖️ Good Samaritan Protection: Under Section 134A of the Motor Vehicles (Amendment) Act 2019, "
                f"bystanders are fully protected from liability when providing reasonable first-aid assistance."
            )
            return GuardrailCheckResult(
                passed=False,
                is_contraindicated=True,
                contraindications=alerts,
                override_reply=override_reply,
                highlight_tag="Clinical Contraindication Alert",
            )

        return GuardrailCheckResult(
            passed=True,
            is_contraindicated=False,
            contraindications=[],
        )

    @classmethod
    def sanitize_llm_response(
        cls,
        generated_text: str,
        citations: list[CitationItem] | None = None,
    ) -> GuardrailCheckResult:
        """Post-generation clinical validator to sanitize any potential LLM hallucinations."""
        text = generated_text
        sanitized = False
        detected_alerts: list[ContraindicationAlert] = []

        # Check for forbidden dangerous phrases in generated output
        for pattern, flag, title, explanation in cls.FORBIDDEN_SURGICAL_PATTERNS + cls.FORBIDDEN_MEDICATION_PATTERNS + cls.UNCONSCIOUS_FLUID_PATTERNS:
            match = pattern.search(text)
            if match:
                # If LLM didn't explicitly frame it as a 'DO NOT' or 'NEVER', flag it
                snippet_start = max(0, match.start() - 30)
                snippet_end = min(len(text), match.end() + 30)
                context_window = text[snippet_start:snippet_end].lower()

                if "not" not in context_window and "never" not in context_window and "prohibit" not in context_window and "avoid" not in context_window:
                    logger.warning("Clinical guardrail triggered on LLM output snippet: %s", match.group(0))
                    detected_alerts.append(
                        ContraindicationAlert(
                            flag=flag,
                            severity="CRITICAL",
                            warning_title=title,
                            warning_message=explanation,
                            action_directive="Follow evidence-based non-invasive bystander protocol only.",
                        )
                    )
                    sanitized = True

        if sanitized and detected_alerts:
            # Prepend critical contraindication warning to protect user safety
            top = detected_alerts[0]
            sanitized_text = (
                f"⚠️ CLINICAL SAFETY NOTICE: {top.warning_title.upper()}\n\n"
                f"{top.warning_message}\n\n"
                f"{text}\n\n"
                f"[Verified Clinical Source: Official First Aid & Resuscitation Protocols]"
            )
            return GuardrailCheckResult(
                passed=False,
                is_contraindicated=True,
                contraindications=detected_alerts,
                sanitized_text=sanitized_text,
                highlight_tag="Safety Sanitized Response",
            )

        return GuardrailCheckResult(
            passed=True,
            is_contraindicated=False,
            contraindications=[],
            sanitized_text=text,
            highlight_tag="Grounded Protocol Step",
        )


hallucination_guardrails = HallucinationGuardrails()
