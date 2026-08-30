"""NearHelp AI — Live LLM & Clinical Rule-Based Severity Prediction Engine."""

import json
import logging
import time
from typing import Any

from app.classifiers.crisis_types import (
    ALL_EMERGENCY_PROFILES,
    CLINICAL_CONDITIONS_MATRIX,
)
from app.core.config import settings
from app.schemas.severity import (
    SeverityRequest,
    SeverityResponse,
    SeverityScoreFactors,
)

logger = logging.getLogger(__name__)


class SeverityPredictor:
    """Predicts emergency severity (0-100 score, Level 1-5 triage, radius, auto-dial flags) using Gemini 2.5 and clinical heuristics."""

    async def predict(self, request: SeverityRequest) -> SeverityResponse:
        """Predict emergency severity using live Gemini LLM or resilient clinical fallback."""
        start_time = time.perf_counter()

        # 1. Attempt Live LLM Inference via Gemini 2.5 Flash if API Key is configured
        if settings.GEMINI_API_KEY and settings.GEMINI_API_KEY != "your_gemini_api_key_here":
            try:
                llm_response = await self._predict_gemini_llm(request, start_time)
                if llm_response:
                    return llm_response
            except Exception as e:
                logger.warning("Gemini LLM severity prediction failed: %s. Falling back to clinical rules.", e)

        # 2. Resilient Deterministic Clinical Rule Engine Fallback
        return self._predict_clinical_rules(request, start_time)

    async def _predict_gemini_llm(self, request: SeverityRequest, start_time: float) -> SeverityResponse | None:
        """Invoke Gemini 2.5 Flash for structured clinical triage scoring."""
        import google.generativeai as genai

        genai.configure(api_key=settings.GEMINI_API_KEY)
        model = genai.GenerativeModel(settings.GEMINI_MODEL)

        prompt_context = {
            "emergency_description": request.text or "",
            "emergency_type": request.emergency_type or "unknown",
            "sub_type": request.sub_type or "unknown",
            "detected_symptoms": request.detected_symptoms or [],
            "patient_age": request.patient_age,
            "unresponsive": request.unresponsive,
            "breathing_difficulty": request.breathing_difficulty,
            "severe_bleeding": request.severe_bleeding,
        }

        system_instruction = (
            "You are an expert AI Emergency Medical Triage Specialist (AHA ACLS/ATLS & Manchester Triage System certified) for NearHelp AI. "
            "Evaluate this emergency incident and determine its clinical severity.\n\n"
            "Severity Triage Framework:\n"
            "- Level 5 (Score 80–100): Critical Life Threat (Hypoxia risk <5 mins, cardiac arrest, unresponsiveness, severe respiratory failure, massive hemorrhage, active building fire, gas leak). Radius: 3.0-5.0 km, Auto-call: True.\n"
            "- Level 4 (Score 50–79): Urgent Trauma (Compound fractures, acute stroke FAST signs, arterial bleed controlled with pressure). Radius: 2.0-3.0 km, Auto-call: False (suggested).\n"
            "- Level 3 (Score 20–49): Moderate Emergency (Thermal burns >10%, localized asthma, non-compound fractures). Radius: 1.0-2.0 km, Auto-call: False.\n"
            "- Level 1–2 (Score 0–19): Low Priority / Minor (Minor laceration, mild sprain, abrasion). Radius: 0.5-1.0 km, Auto-call: False.\n\n"
            "Return ONLY a JSON object with keys:\n"
            "- severity_score: integer (0-100)\n"
            "- severity_level: integer (1-5)\n"
            "- priority: string ('critical', 'high', 'medium', 'low')\n"
            "- confidence: float (0.0 to 1.0)\n"
            "- reasoning: array of strings (2-4 bulleted clinical justifications)\n"
            "- factors: object with life_threat_score, time_sensitivity_score, casualty_risk_score, environmental_hazard_score (all floats 0-100)\n"
            "- recommended_radius_km: float\n"
            "- survival_window_minutes: integer or null (e.g. 5 for cardiac/hypoxia)\n"
            "- auto_call_emergency_services: boolean\n"
            "- suggested_call_action: string ('auto_dial', 'suggested', 'optional', 'none')\n"
            "- emergency_number: string ('108', '101', '100', '112')\n"
            "- recommended_actions: array of strings (actionable bystander first-aid steps)\n"
            "- required_responder_skills: array of strings (e.g. ['CPR_CERTIFIED', 'DOCTOR', 'EMT', 'FIRST_AID'])"
        )

        response = model.generate_content(
            f"{system_instruction}\n\nIncident Details:\n{json.dumps(prompt_context, indent=2)}"
        )

        if response and response.text:
            raw = response.text.strip()
            if raw.startswith("```json"):
                raw = raw[7:]
            if raw.endswith("```"):
                raw = raw[:-3]

            data = json.loads(raw.strip())
            total_latency_ms = (time.perf_counter() - start_time) * 1000.0

            factors_data = data.get("factors", {})
            factors = SeverityScoreFactors(
                life_threat_score=float(factors_data.get("life_threat_score", 85.0)),
                time_sensitivity_score=float(factors_data.get("time_sensitivity_score", 90.0)),
                casualty_risk_score=float(factors_data.get("casualty_risk_score", 30.0)),
                environmental_hazard_score=float(factors_data.get("environmental_hazard_score", 20.0)),
            )

            score = int(data.get("severity_score", 85))
            conf = float(data.get("confidence", 0.96))

            return SeverityResponse(
                severity_score=score,
                severity_level=int(data.get("severity_level", 5)),
                priority=str(data.get("priority", "critical")),
                confidence=round(conf, 4),
                confidence_percentage=round(conf * 100.0, 1),
                reasoning=list(data.get("reasoning", ["Emergency clinical triage assessment complete."])),
                factors=factors,
                recommended_radius_km=float(data.get("recommended_radius_km", 3.0)),
                survival_window_minutes=data.get("survival_window_minutes"),
                auto_call_emergency_services=bool(data.get("auto_call_emergency_services", score >= 80)),
                suggested_call_action=str(data.get("suggested_call_action", "auto_dial" if score >= 80 else "suggested")),
                emergency_number=str(data.get("emergency_number", "108")),
                recommended_actions=list(data.get("recommended_actions", [])),
                required_responder_skills=list(data.get("required_responder_skills", ["FIRST_AID"])),
                processing_time_ms=round(total_latency_ms, 2),
            )

        return None

    def _predict_clinical_rules(self, request: SeverityRequest, start_time: float) -> SeverityResponse:
        """Deterministic clinical heuristic triage engine based on medical urgency parameters."""
        raw_text = (request.text or "").lower()
        symptoms_text = " ".join(request.detected_symptoms or []).lower()
        full_text = f"{raw_text} {symptoms_text}"

        emergency_type = (request.emergency_type or "").lower()
        sub_type = (request.sub_type or "").lower()

        # Clinical factor baselines
        life_threat = 15.0
        time_sensitivity = 15.0
        casualty_risk = 10.0
        environmental_hazard = 10.0
        survival_window: int | None = None
        emergency_number = "108"
        suggested_skills: list[str] = ["FIRST_AID"]
        reasoning: list[str] = []
        recommended_actions: list[str] = []

        # ----------------------------------------------------------------------
        # 1. CARDIAC ARREST & ACUTE CARDIAC LIFE THREATS
        # ----------------------------------------------------------------------
        is_cardiac = (
            sub_type == "cardiac_arrest"
            or (
                any(k in full_text for k in [
                    "cardiac", "heart attack", "heart", "cpr", "myocardial",
                    "no pulse", "হৃদরোগ", "dil ka daura", "seene me dard"
                ])
                or (any(k in full_text for k in ["chest pain", "clutching chest", "বুকে", "chhati"]) and any(k in full_text for k in ["collapsed", "unresponsive", "gasping", "অজ্ঞান", "বেহুঁশ", "behosh", "severe", "crushing", "মাটিতে"]))
                or (request.unresponsive is True and not any(k in full_text for k in ["seizure", "epilepsy", "stroke", "paralysis", "allergy", "anaphylaxis"]))
            )
        )

        if is_cardiac:
            emergency_type = "medical"
            sub_type = "cardiac_arrest"
            life_threat = 98.0
            time_sensitivity = 99.0
            casualty_risk = 20.0
            environmental_hazard = 15.0
            survival_window = 5
            emergency_number = "108"
            suggested_skills = ["CPR_CERTIFIED", "DOCTOR", "EMT", "NURSE", "FIRST_AID"]
            reasoning = [
                "Unresponsive victim with sudden collapse indicates imminent cardiac arrest or acute ventricular arrhythmia.",
                "Absence of carotid pulse and agonal breathing represents an irreversible life threat without immediate intervention.",
                "Critical 5-minute Platinum Hypoxia Window: irreversible brain damage starts within 4-6 minutes without continuous CPR.",
            ]
            recommended_actions = [
                "Begin CPR immediately: compress center of chest 5-6 cm deep at 110-120 BPM.",
                "Send a bystander immediately to retrieve the nearest Automated External Defibrillator (AED).",
                "Call 108 Emergency Medical Services immediately.",
                "NEVER administer water, fluids, or oral medications to an unconscious patient.",
            ]

        # ----------------------------------------------------------------------
        # 2. SEVERE RESPIRATORY ARREST & ANAPHYLAXIS & DROWNING
        # ----------------------------------------------------------------------
        elif (
            sub_type in ("respiratory_asthma", "anaphylaxis_allergy", "drowning")
            or any(k in full_text for k in [
                "asthma", "stridor", "wheezing", "lips turning blue", "cyanosis", "cyanotic",
                "throat closing", "anaphylaxis", "urticaria", "hives", "epipen", "epinephrine",
                "drowning", "submerged", "swallowed water", "শ্বাস নিতে", "ঠোঁট নীল",
                "অ্যালার্জি", "গলা ফুলে", "পুকুরে", "পানিতে", "dum ghut", "gala band", "allergy"
            ])
            or (request.breathing_difficulty is True and ("severe" in full_text or "not breathing" in full_text))
        ):
            emergency_type = "medical"
            if "anaphylaxis" in full_text or "epipen" in full_text or "allergic" in full_text or "allergy" in full_text or "অ্যালার্জি" in full_text or "hives" in full_text or "urticaria" in full_text or sub_type == "anaphylaxis_allergy":
                sub_type = "anaphylaxis_allergy"
                life_threat = 95.0
                time_sensitivity = 96.0
                survival_window = 8
                suggested_skills = ["DOCTOR", "EMT", "NURSE", "FIRST_AID"]
                reasoning = [
                    "Acute systemic anaphylaxis with laryngeal angioedema threatening complete upper airway occlusion.",
                    "Severe bronchospasm and hypotension require immediate intramuscular epinephrine administration.",
                ]
                recommended_actions = [
                    "Administer Epinephrine auto-injector (EpiPen) into outer mid-thigh immediately if available.",
                    "Position patient supine with legs elevated unless breathing is difficult.",
                    "Call 108 Emergency Ambulance.",
                ]
            elif "drowning" in full_text or "submerged" in full_text or "পুকুরে" in full_text or "পানিতে" in full_text or sub_type == "drowning":
                sub_type = "drowning"
                life_threat = 97.0
                time_sensitivity = 98.0
                survival_window = 5
                suggested_skills = ["CPR_CERTIFIED", "DOCTOR", "EMT", "FIRST_AID"]
                reasoning = [
                    "Water submersion with acute hypoxia and alveolar fluid aspiration.",
                    "Cardiac arrest secondary to drowning requires immediate rescue breaths and CPR.",
                ]
                recommended_actions = [
                    "Provide 5 initial rescue breaths immediately.",
                    "Begin standard CPR (30 chest compressions : 2 rescue breaths) at 110-120 BPM.",
                    "Call 108 Ambulance and keep patient warm to prevent hypothermia.",
                ]
            else:
                sub_type = "respiratory_asthma"
                life_threat = 92.0
                time_sensitivity = 94.0
                survival_window = 10
                suggested_skills = ["DOCTOR", "EMT", "NURSE", "FIRST_AID"]
                reasoning = [
                    "Acute severe bronchospasm with peripheral cyanosis and oxygen desaturation.",
                    "Impending acute respiratory failure without bronchodilator therapy or oxygenation.",
                ]
                recommended_actions = [
                    "Sit patient upright in high Fowler's position. Do NOT lay them flat.",
                    "Assist with prescribed bronchodilator inhaler (with spacer if available).",
                    "Call 108 Emergency Ambulance.",
                ]

        # ----------------------------------------------------------------------
        # 3. SEVERE ARTERIAL BLEEDING & TRAUMA HEMORRHAGE
        # ----------------------------------------------------------------------
        elif (
            sub_type == "severe_bleeding"
            or any(k in full_text for k in [
                "arterial", "spurting", "pulsating", "bleeding profusely", "hemorrhage",
                "blood pooling", "hypovolemic", "tourniquet", "laceration", "ফিনকি",
                "রক্তপাত", "রক্ত বের", "khoon beh", "artery cut"
            ])
            or request.severe_bleeding is True
        ):
            emergency_type = "medical"
            sub_type = "severe_bleeding"
            life_threat = 78.0
            time_sensitivity = 85.0
            casualty_risk = 15.0
            environmental_hazard = 20.0
            survival_window = 15
            emergency_number = "108"
            suggested_skills = ["FIRST_AID", "EMT", "NURSE", "DOCTOR"]
            reasoning = [
                "Rapid arterial or high-volume venous blood loss with impending hypovolemic shock.",
                "Continuous hemorrhage requires immediate direct mechanical pressure and potential tourniquet application.",
            ]
            recommended_actions = [
                "Apply firm, continuous direct pressure over wound using clean cloth or sterile gauze.",
                "If arterial bleed on limb cannot be controlled with direct pressure, apply tourniquet 5 cm above wound.",
                "Elevate injured limb above heart level if no fracture is suspected.",
                "Keep patient warm and calm. Call 108 Ambulance.",
            ]

        # ----------------------------------------------------------------------
        # 4. ROAD ACCIDENT / VEHICULAR COLLISION
        # ----------------------------------------------------------------------
        elif (
            emergency_type == "accident"
            or sub_type == "road_accident"
            or any(k in full_text for k in [
                "car crash", "collision", "accident", "hit and run", "rollover",
                "trapped in vehicle", "pedestrian hit", "লরির", "সংঘর্ষ", "দুর্ঘটনা", "gadi ka accident"
            ])
        ):
            emergency_type = "accident"
            sub_type = "road_accident"
            life_threat = 82.0
            time_sensitivity = 80.0
            casualty_risk = 85.0
            environmental_hazard = 75.0
            survival_window = 20
            emergency_number = "108"
            suggested_skills = ["FIRST_AID", "EMT", "DOCTOR", "NURSE"]
            reasoning = [
                "High-velocity vehicular impact with suspected multi-system trauma, head injury, and spinal compromise.",
                "High risk of multiple casualties, ongoing active traffic hazards, and fuel ignition.",
            ]
            recommended_actions = [
                "Ensure scene safety: turn on hazard lights, set warning triangles, divert traffic.",
                "Do NOT move victims unless immediate danger of fire or explosion.",
                "Maintain strict cervical spine immobilization.",
                "Call 108 Ambulance and 100/112 Police.",
            ]

        # ----------------------------------------------------------------------
        # 5. STRUCTURAL FIRE & LPG GAS LEAK
        # ----------------------------------------------------------------------
        elif (
            emergency_type in ("fire", "gas_leak")
            or sub_type in ("structural_fire", "electrical_fire", "lpg_gas_leak")
            or any(k in full_text for k in [
                "fire", "flames", "smoke", "burning", "trapped inside", "gas leak", "lpg", "gas cylinder",
                "cylinder hissing", "explosion", "আগুন", "ধোঁয়া", "গ্যাস", "সিলিন্ডার", "লিক", "aag", "blast"
            ])
        ):
            if "gas" in full_text or "lpg" in full_text or "গ্যাস" in full_text or "সিলিন্ডার" in full_text or sub_type == "lpg_gas_leak":
                emergency_type = "gas_leak"
                sub_type = "lpg_gas_leak"
                life_threat = 86.0
                time_sensitivity = 88.0
                casualty_risk = 80.0
                environmental_hazard = 95.0
                survival_window = 10
                emergency_number = "101"
                suggested_skills = ["FIRE_SAFETY", "FIRST_AID"]
                reasoning = [
                    "Volatile hydrocarbon gas accumulation with severe BLEVE explosion and vapor cloud ignition risk.",
                    "Immediate toxicity and asphyxiation threat to building occupants.",
                ]
                recommended_actions = [
                    "Do NOT operate any electrical switches, sockets, matchsticks, or lighters.",
                    "Open all doors and windows for maximum cross-ventilation.",
                    "Close cylinder regulator knob immediately if safe to approach.",
                    "Evacuate building immediately and call 101 Fire & Disaster Management.",
                ]
            else:
                emergency_type = "fire"
                sub_type = "structural_fire"
                life_threat = 88.0
                time_sensitivity = 90.0
                casualty_risk = 85.0
                environmental_hazard = 92.0
                survival_window = 10
                emergency_number = "101"
                suggested_skills = ["FIRE_SAFETY", "FIRST_AID", "EMT"]
                reasoning = [
                    "Active structural combustion producing carbon monoxide, hydrogen cyanide, and thermal trauma.",
                    "Rapid flame propagation and smoke entrapment create an acute multi-casualty threat.",
                ]
                recommended_actions = [
                    "Evacuate immediately via stairwells (NEVER use elevators).",
                    "Stay low below the thermal smoke layer while escaping.",
                    "Call 101 Fire Brigade immediately.",
                    "Do NOT re-enter burning building under any circumstances.",
                ]

        # ----------------------------------------------------------------------
        # 6. STROKE (FAST PROTOCOL) & NEUROLOGICAL CRISIS
        # ----------------------------------------------------------------------
        elif (
            sub_type in ("stroke", "unconscious_seizure")
            or any(k in full_text for k in [
                "stroke", "facial droop", "slurred speech", "arm weakness", "hemiparesis",
                "seizure", "convulsion", "frothing", "মুখ বেঁকে", "প্যারালাইসিস", "খিঁচুনি", "mirgi", "chehra tedha"
            ])
        ):
            emergency_type = "medical"
            if "seizure" in full_text or "convulsion" in full_text or "খিঁচুনি" in full_text:
                sub_type = "unconscious_seizure"
                life_threat = 82.0
                time_sensitivity = 84.0
                survival_window = 15
                suggested_skills = ["FIRST_AID", "NURSE", "EMT", "DOCTOR"]
                reasoning = [
                    "Generalized tonic-clonic convulsions with loss of consciousness and airway compromise.",
                    "Risk of traumatic head injury, tongue biting, and status epilepticus.",
                ]
                recommended_actions = [
                    "Clear surrounding hard or sharp objects. Protect head with soft cushion.",
                    "Do NOT restrain the victim or force anything into their mouth.",
                    "Turn victim into recovery position once convulsions cease.",
                    "Call 108 Ambulance.",
                ]
            else:
                sub_type = "stroke"
                life_threat = 75.0
                time_sensitivity = 78.0
                survival_window = 45  # Golden window for thrombolysis
                suggested_skills = ["DOCTOR", "EMT", "NURSE"]
                reasoning = [
                    "Acute focal neurological deficit meeting FAST stroke criteria (Facial droop, Arm weakness, Slurred speech).",
                    "Time-critical cerebral ischemia requiring thrombolysis within the 3-4.5 hour therapeutic window.",
                ]
                recommended_actions = [
                    "Note exact time of symptom onset (critical for thrombolysis eligibility).",
                    "Keep patient calm and still with head elevated 15-30 degrees.",
                    "Do NOT administer aspirin, food, or fluids.",
                    "Call 108 Ambulance immediately for rapid stroke-ready hospital transit.",
                ]

        # ----------------------------------------------------------------------
        # 7. COMPOUND FRACTURES & SEVERE BURNS
        # ----------------------------------------------------------------------
        elif (
            sub_type in ("fracture_trauma", "severe_burns")
            or any(k in full_text for k in [
                "fracture", "broken bone", "bone protruding", "deformed leg", "burn", "burns",
                "blister", "blisters", "scalding", "hot oil", "হাড় ভেঙে", "পুড়ে গেছে", "haddi toot", "jal gaya"
            ])
        ):
            emergency_type = "medical"
            if "burn" in full_text or "পুড়ে" in full_text or "jal" in full_text or "blister" in full_text or "scald" in full_text or sub_type == "severe_burns":
                sub_type = "severe_burns"
                life_threat = 40.0
                time_sensitivity = 45.0
                environmental_hazard = 20.0
                casualty_risk = 15.0
                suggested_skills = ["FIRST_AID", "NURSE", "EMT"]
                reasoning = [
                    "Second or third-degree thermal dermal trauma with skin barrier breakdown and acute fluid loss.",
                ]
                recommended_actions = [
                    "Cool burn immediately under clean running tap water for 15-20 minutes.",
                    "Do NOT apply ice, butter, oil, or toothpaste to burns.",
                    "Cover loosely with clean non-adherent cling wrap or sheet.",
                    "Call 108 Ambulance.",
                ]
            else:
                sub_type = "fracture_trauma"
                life_threat = 65.0
                time_sensitivity = 60.0
                suggested_skills = ["FIRST_AID", "EMT", "DOCTOR", "NURSE"]
                reasoning = [
                    "Open compound fracture with high risk of osteomyelitis, vascular compromise, and severe hemorrhage.",
                ]
                recommended_actions = [
                    "Immobilize limb in found position; do NOT attempt to realign or push protruding bone back.",
                    "Cover open wound with sterile dressing to prevent contamination.",
                    "Check distal pulse and capillary refill.",
                    "Call 108 Ambulance.",
                ]

        # ----------------------------------------------------------------------
        # 8. CRIME & PHYSICAL VIOLENCE
        # ----------------------------------------------------------------------
        elif (
            emergency_type == "crime"
            or sub_type == "physical_assault"
            or any(k in full_text for k in [
                "assault", "stabbed", "knife", "weapon", "robbery", "attacker",
                "মারপিট", "ছুরি", "police", "chaku", "hamla"
            ])
        ):
            emergency_type = "crime"
            sub_type = "physical_assault"
            life_threat = 82.0
            time_sensitivity = 84.0
            casualty_risk = 60.0
            environmental_hazard = 85.0
            survival_window = 15
            emergency_number = "100"
            suggested_skills = ["FIRST_AID", "EMT"]
            reasoning = [
                "Active interpersonal violence with penetrating trauma or weapon injury.",
                "Immediate safety risk to bystanders and ongoing hostile environment.",
            ]
            recommended_actions = [
                "Maintain a safe distance and seek secure shelter.",
                "Call 100 / 112 Police immediately.",
                "Once scene is verified safe, apply firm pressure to bleeding wounds.",
            ]

        # ----------------------------------------------------------------------
        # 9. MINOR / NON-ACUTE / LOW PRIORITY SCENARIOS
        # ----------------------------------------------------------------------
        elif any(k in full_text for k in [
            "scratch", "scrape", "minor cut", "bruise", "mild sprain", "twisted ankle",
            "paper cut", "headache", "bandage", "একটু কেটে", "হালকা ব্যথা", "chhoti chot"
        ]):
            life_threat = 8.0
            time_sensitivity = 10.0
            casualty_risk = 5.0
            environmental_hazard = 5.0
            suggested_skills = ["FIRST_AID"]
            reasoning = [
                "Minor superficial injury or non-acute discomfort without systemic or hemodynamic compromise.",
                "Standard self-care first aid is appropriate.",
            ]
            recommended_actions = [
                "Clean wound with mild soap and clean water.",
                "Apply an adhesive bandage or sterile dressing.",
                "Rest, Ice, Compress, and Elevate (RICE) if joint sprain.",
            ]

        # ----------------------------------------------------------------------
        # DEFAULT / GENERAL EMERGENCY FALLBACK
        # ----------------------------------------------------------------------
        else:
            life_threat = 60.0
            time_sensitivity = 65.0
            casualty_risk = 20.0
            environmental_hazard = 20.0
            reasoning = [
                "Reported emergency situation requires on-scene evaluation.",
                "Bystander assistance and emergency readiness indicated.",
            ]
            recommended_actions = [
                "Assess scene safety before approaching.",
                "Check victim responsiveness and breathing.",
                "Call 108 Emergency Ambulance if condition deteriorates.",
            ]

        # ----------------------------------------------------------------------
        # CLINICAL SEVERITY SCORING FORMULA:
        # In medical triage (ESI/Manchester), acute life threats and extreme time sensitivities
        # dominate the triage severity score.
        # ----------------------------------------------------------------------
        if life_threat >= 85.0 or time_sensitivity >= 85.0:
            # Life threat and time urgency dominate for acute crises (Level 5)
            weighted_score = (
                0.55 * life_threat
                + 0.35 * time_sensitivity
                + 0.05 * casualty_risk
                + 0.05 * environmental_hazard
            )
        else:
            weighted_score = (
                0.45 * life_threat
                + 0.30 * time_sensitivity
                + 0.15 * casualty_risk
                + 0.10 * environmental_hazard
            )

        final_score = int(round(max(0.0, min(100.0, weighted_score))))

        # ----------------------------------------------------------------------
        # SEVERITY-TO-ACTION TRIAGE LEVEL MAPPING
        # ----------------------------------------------------------------------
        if final_score >= 80:
            severity_level = 5
            priority = "critical"
            # Radius 3.0 to 5.0 km scaled with score
            radius_km = round(3.0 + ((final_score - 80) / 20.0) * 2.0, 1)
            auto_call = True
            call_action = "auto_dial"
            conf = 0.984
        elif final_score >= 50:
            severity_level = 4
            priority = "high"
            # Radius 2.0 to 3.0 km
            radius_km = round(2.0 + ((final_score - 50) / 29.0) * 1.0, 1)
            auto_call = False
            call_action = "suggested"
            conf = 0.952
        elif final_score >= 20:
            severity_level = 3
            priority = "medium"
            # Radius 1.0 to 2.0 km
            radius_km = round(1.0 + ((final_score - 20) / 29.0) * 1.0, 1)
            auto_call = False
            call_action = "optional"
            conf = 0.915
        elif final_score >= 10:
            severity_level = 2
            priority = "low"
            radius_km = 0.8
            auto_call = False
            call_action = "none"
            conf = 0.880
        else:
            severity_level = 1
            priority = "low"
            radius_km = 0.5
            auto_call = False
            call_action = "none"
            conf = 0.850

        factors = SeverityScoreFactors(
            life_threat_score=round(life_threat, 1),
            time_sensitivity_score=round(time_sensitivity, 1),
            casualty_risk_score=round(casualty_risk, 1),
            environmental_hazard_score=round(environmental_hazard, 1),
        )

        total_latency_ms = (time.perf_counter() - start_time) * 1000.0

        return SeverityResponse(
            severity_score=final_score,
            severity_level=severity_level,
            priority=priority,
            confidence=conf,
            confidence_percentage=round(conf * 100.0, 1),
            reasoning=reasoning,
            factors=factors,
            recommended_radius_km=radius_km,
            survival_window_minutes=survival_window,
            auto_call_emergency_services=auto_call,
            suggested_call_action=call_action,
            emergency_number=emergency_number,
            recommended_actions=recommended_actions,
            required_responder_skills=suggested_skills,
            processing_time_ms=max(0.01, round(total_latency_ms, 2)),
        )


severity_predictor = SeverityPredictor()
