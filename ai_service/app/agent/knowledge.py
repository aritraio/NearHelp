"""NearHelp AI — Evidence-Based First-Aid Knowledge Base & Citation Repository.

Contains official clinical resuscitation guidelines (AHA, ERC, WHO, IRC, GINA, ILAE, ASA, BBA, NDMA)
and statutory protections (Section 134A Motor Vehicles (Amendment) Act 2019).
"""

from typing import Any

from app.schemas.agent import (
    CitationItem,
    ContraindicationAlert,
    GroundedProtocolResponse,
    ProtocolStepItem,
)

# ==============================================================================
# STATUTORY CITATIONS & AUTHORITY DEFINITIONS
# ==============================================================================

CITATIONS_CATALOG: dict[str, CitationItem] = {
    "good_samaritan_134a": CitationItem(
        source="Motor Vehicles (Amendment) Act 2019",
        section="Section 134A & Supreme Court WP(Civil) 235/2012",
        guideline_name="Protection of Good Samaritans from Civil and Criminal Liability",
        authority="Ministry of Road Transport and Highways (MoRTH), Govt of India",
        url="https://morth.nic.in/good-samaritan-guidelines",
    ),
    "aha_cpr_2020": CitationItem(
        source="AHA Guidelines for CPR and ECC 2020",
        section="Part 3: Adult Basic Life Support §3.2 (Chest Compressions & Rhythm)",
        guideline_name="2020 American Heart Association Guidelines for Cardiopulmonary Resuscitation",
        authority="American Heart Association (AHA)",
        url="https://cpr.heart.org/en/resuscitation-science/cpr-and-ecc-guidelines",
    ),
    "irc_bls_2020": CitationItem(
        source="Indian Resuscitation Council (IRC) Comprehensive Guidelines 2020",
        section="Section 2: Cardiopulmonary Resuscitation in Adults (110 BPM Metronome Standard)",
        guideline_name="National CPR Guidelines for Lay Responders & Health Professionals",
        authority="Indian Resuscitation Council (IRC)",
        url="https://cprindia.in",
    ),
    "who_bleeding_control": CitationItem(
        source="WHO Emergency Trauma Care & Stop The Bleed Protocol",
        section="Guideline 4.1: Direct Pressure, Wound Packing & Arterial Tourniquet Protocol",
        guideline_name="Guidelines for Essential Trauma Care & Mass Casualty Management",
        authority="World Health Organization (WHO) & American College of Surgeons",
        url="https://www.who.int/emergencies/trauma-care",
    ),
    "gina_asthma_2023": CitationItem(
        source="Global Initiative for Asthma (GINA) 2023",
        section="Chapter 4: Acute Asthma Exacerbations & Bronchodilator Spacer Regimens",
        guideline_name="Global Strategy for Asthma Management and Prevention",
        authority="Global Initiative for Asthma (GINA) & WHO",
        url="https://ginasthma.org",
    ),
    "ilae_seizure_protocol": CitationItem(
        source="International League Against Epilepsy (ILAE) & NHS First Aid",
        section="Protocol: Generalised Convulsive Status Epilepticus Pre-Hospital Safety",
        guideline_name="First Aid for Tonic-Clonic Seizures and Recovery Positioning",
        authority="International League Against Epilepsy (ILAE)",
        url="https://www.ilae.org",
    ),
    "asa_stroke_fast": CitationItem(
        source="American Stroke Association (ASA) / AHA Guidelines 2019",
        section="Section 1.2: Pre-Hospital FAST Screening & Golden Hour Neuroprotection",
        guideline_name="Guidelines for the Early Management of Patients With Acute Ischemic Stroke",
        authority="American Heart Association / American Stroke Association",
        url="https://www.stroke.org",
    ),
    "bba_burns_2021": CitationItem(
        source="British Burn Association (BBA) & WHO Burn Trauma Guide",
        section="Protocol: 20-Minute Cool Running Water & Clean Cling Film Dressing",
        guideline_name="First Aid Clinical Practice Guidelines for Burn Injuries",
        authority="British Burn Association (BBA) & International Society for Burn Injuries (ISBI)",
        url="https://www.britishburnassociation.org",
    ),
    "atls_trauma_ndma": CitationItem(
        source="National Disaster Management Authority (NDMA) & ATLS 10th Ed.",
        section="Module 8: Pre-Hospital Spinal Immobilization and Splinting in Situ",
        guideline_name="National Disaster Response Trauma & Orthopedic Guidelines",
        authority="NDMA, Govt of India & American College of Surgeons ATLS",
        url="https://ndma.gov.in",
    ),
}


# ==============================================================================
# 8 CURATED EVIDENCE-BASED PROTOCOLS
# ==============================================================================

GROUNDED_PROTOCOLS: dict[str, GroundedProtocolResponse] = {
    "cardiac_arrest": GroundedProtocolResponse(
        condition_id="cardiac_arrest",
        condition_label="Cardiac / Chest Pain",
        crisis_type="medical",
        severity_level=5,
        priority="critical",
        protocol_title="AHA / Indian Resuscitation Council Basic Life Support (BLS) Protocol",
        authority="American Heart Association (AHA) & Indian Resuscitation Council (IRC)",
        disclaimers="Emergency interim bystander protocol. Municipal 108 ambulance dispatched.",
        legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019 (Zero Civil/Criminal Liability).",
        recommended_radius_km=3.5,
        emergency_number="108",
        cpr_bpm=110,
        steps=[
            ProtocolStepItem(
                step_number=1,
                title="Check Safety & Confirm Unresponsiveness",
                action_instruction='Ensure scene is safe. Tap victim firmly on both shoulders and shout loudly "Are you okay?". Check carotid pulse in neck groove for NO MORE than 10 seconds.',
                warning_note="If victim is unresponsive and not breathing (or only agonal gasping), immediately start CPR.",
                is_cpr_step=False,
                icon="AlertCircle",
            ),
            ProtocolStepItem(
                step_number=2,
                title="Begin High-Quality Chest Compressions (110 BPM Metronome)",
                action_instruction="Place heel of one hand on lower half of breastbone (center of chest). Interlock fingers of second hand. Push hard and fast at a depth of 5–6 cm (2–2.4 inches). Allow full chest recoil.",
                warning_note="Maintain uninterrupted cadence of 110–120 BPM. Do not lean on chest between compressions.",
                is_cpr_step=True,
                beat_bpm=110,
                icon="HeartPulse",
            ),
            ProtocolStepItem(
                step_number=3,
                title="Maintain 30:2 Ratio or Continuous Hands-Only CPR",
                action_instruction="Deliver 30 compressions followed by 2 rescue breaths (tilt head, lift chin, pinch nose). If untrained or without barrier mask, perform continuous Hands-Only CPR without stopping.",
                warning_note="Minimize interruptions to chest compressions to less than 10 seconds.",
                is_cpr_step=True,
                beat_bpm=110,
                icon="Activity",
            ),
            ProtocolStepItem(
                step_number=4,
                title="Retrieve & Apply Automated External Defibrillator (AED)",
                action_instruction="Send a designated bystander to fetch nearest AED. Turn AED ON immediately. Peel pads and adhere to bare dry chest: Upper right chest below collarbone, Lower left chest below armpit. Follow spoken prompts.",
                warning_note="STAND CLEAR of patient when AED announces 'Analyzing rhythm' or 'Delivering shock'.",
                is_cpr_step=False,
                icon="Zap",
            ),
        ],
        citations=[
            CITATIONS_CATALOG["aha_cpr_2020"],
            CITATIONS_CATALOG["irc_bls_2020"],
            CITATIONS_CATALOG["good_samaritan_134a"],
        ],
    ),
    "severe_bleeding": GroundedProtocolResponse(
        condition_id="severe_bleeding",
        condition_label="Severe Hemorrhage",
        crisis_type="medical",
        severity_level=4,
        priority="high",
        protocol_title="WHO / IRC Stop The Bleed & Hemorrhage Control Protocol",
        authority="World Health Organization (WHO) & Indian Red Cross Society",
        disclaimers="High-volume hemorrhage is time-critical. Maintain constant direct pressure until EMS arrival.",
        legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
        recommended_radius_km=2.5,
        emergency_number="108",
        steps=[
            ProtocolStepItem(
                step_number=1,
                title="Expose Wound & Identify Bleeding Source",
                action_instruction="Cut or tear clothing to fully visualize bleeding site. Identify if blood is spurting bright red (arterial) or steadily flowing (venous).",
                warning_note="Do NOT remove deeply embedded impaled objects; stabilize them in place.",
                icon="Eye",
            ),
            ProtocolStepItem(
                step_number=2,
                title="Apply Continuous Direct Mechanical Pressure",
                action_instruction="Cover wound with sterile gauze or clean cloth. Press firmly with both hands directly over the bleeding vessel using your body weight.",
                warning_note="Do NOT lift gauze to check if bleeding has stopped. If blood soaks through, add more layers on top.",
                icon="Droplet",
            ),
            ProtocolStepItem(
                step_number=3,
                title="Pack Deep Cavity Wounds (Junctional / Groin / Neck)",
                action_instruction="For deep puncture wounds, tightly pack gauze directly into the wound cavity down to the bleeding vessel, then maintain hard pressure for 3+ minutes.",
                warning_note="Maintain continuous pressure without easing off.",
                icon="Layers",
            ),
            ProtocolStepItem(
                step_number=4,
                title="Apply Limb Tourniquet for Life-Threatening Arterial Bleed",
                action_instruction="If limb spurting bleed does not stop with direct pressure, place a commercial tourniquet 5–7 cm (2–3 inches) above wound (never directly on a joint). Tighten windlass until bleeding stops and pulse disappears. Note time applied.",
                warning_note="Never loosen or remove a tourniquet once applied. Let hospital surgical team handle removal.",
                icon="ShieldAlert",
            ),
        ],
        citations=[
            CITATIONS_CATALOG["who_bleeding_control"],
            CITATIONS_CATALOG["good_samaritan_134a"],
        ],
    ),
    "respiratory_asthma": GroundedProtocolResponse(
        condition_id="respiratory_asthma",
        condition_label="Respiratory Distress",
        crisis_type="medical",
        severity_level=5,
        priority="critical",
        protocol_title="GINA / WHO Acute Respiratory & Asthma Crisis Protocol",
        authority="Global Initiative for Asthma (GINA) & WHO",
        disclaimers="Acute hypoxic event. Keep calm, keep patient upright, and maintain clear airflow.",
        legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
        recommended_radius_km=3.0,
        emergency_number="108",
        steps=[
            ProtocolStepItem(
                step_number=1,
                title="Position Patient Upright",
                action_instruction="Help patient sit completely upright leaning slightly forward (tripod position). Loosen tight collar or clothing. Do NOT allow patient to lie down.",
                warning_note="Lying flat increases diaphragmatic pressure and worsens airway obstruction.",
                icon="UserCheck",
            ),
            ProtocolStepItem(
                step_number=2,
                title="Administer Reliever Inhaler (Salbutamol / Albuterol)",
                action_instruction="Shake blue reliever inhaler. Use spacer if available. Give 4 separate puffs with 4 slow, deep breaths after each puff. Wait 4 minutes; if no improvement, give another 4 puffs (up to 10 puffs).",
                warning_note="Stay with patient and keep crowd away to provide fresh ventilation.",
                icon="Wind",
            ),
            ProtocolStepItem(
                step_number=3,
                title="Monitor for Severe Cyanosis & Airway Failure",
                action_instruction="Check lips and fingernails for blue/gray discoloration (cyanosis). If patient becomes drowsy, stops wheezing (silent chest), or loses consciousness, prepare for CPR.",
                warning_note="Silent chest with severe exhaustion indicates imminent respiratory arrest.",
                icon="AlertTriangle",
            ),
        ],
        citations=[
            CITATIONS_CATALOG["gina_asthma_2023"],
            CITATIONS_CATALOG["good_samaritan_134a"],
        ],
    ),
    "unconscious_seizure": GroundedProtocolResponse(
        condition_id="unconscious_seizure",
        condition_label="Seizure / Fainting",
        crisis_type="medical",
        severity_level=5,
        priority="critical",
        protocol_title="ILAE & NHS Seizure & Convulsion Safety Protocol",
        authority="International League Against Epilepsy (ILAE) & NHS First Aid",
        disclaimers="Protect victim from traumatic impact. Do not restrain movements.",
        legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
        recommended_radius_km=2.5,
        emergency_number="108",
        steps=[
            ProtocolStepItem(
                step_number=1,
                title="Protect Head & Clear Dangerous Surroundings",
                action_instruction="Clear away hard, sharp, or hot objects (chairs, glass, electrical cords). Place a soft folded jacket or pillow gently under the victim's head.",
                warning_note="Do NOT physically restrain or pin down the victim's shaking limbs.",
                icon="Shield",
            ),
            ProtocolStepItem(
                step_number=2,
                title="NEVER Insert Anything into Mouth",
                action_instruction="Keep mouth completely clear. Do NOT put fingers, spoons, cloths, water, or medication into the mouth. The tongue cannot be swallowed.",
                warning_note="Placing objects in mouth causes broken teeth, severe biting injury, and airway blockage.",
                icon="AlertCircle",
            ),
            ProtocolStepItem(
                step_number=3,
                title="Time Duration & Turn into Recovery Position Post-Seizure",
                action_instruction="Time the seizure on your watch. Once active shaking stops, gently roll victim onto their side into the recovery position to allow oral saliva/vomit to drain and maintain open airway.",
                warning_note="If seizure lasts longer than 5 minutes or repeats without regaining consciousness, status epilepticus is occurring.",
                icon="Clock",
            ),
        ],
        citations=[
            CITATIONS_CATALOG["ilae_seizure_protocol"],
            CITATIONS_CATALOG["good_samaritan_134a"],
        ],
    ),
    "stroke": GroundedProtocolResponse(
        condition_id="stroke",
        condition_label="Stroke (FAST Protocol)",
        crisis_type="medical",
        severity_level=4,
        priority="high",
        protocol_title="ASA / AHA FAST Stroke Acute Evaluation Protocol",
        authority="American Stroke Association (ASA) & Indian Stroke Association (ISA)",
        disclaimers="Time lost is brain lost. Immediate CT scan and thrombolysis window is under 4.5 hours.",
        legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
        recommended_radius_km=3.0,
        emergency_number="108",
        steps=[
            ProtocolStepItem(
                step_number=1,
                title="Evaluate FAST Symptoms Rapidly",
                action_instruction="F (Face): Ask patient to smile — does one side droop? A (Arms): Ask patient to raise both arms — does one arm drift down? S (Speech): Ask patient to repeat a simple sentence — is speech slurred? T (Time): Call 108 immediately.",
                warning_note="Note the exact time symptoms were first noticed by patient or family.",
                icon="Brain",
            ),
            ProtocolStepItem(
                step_number=2,
                title="Position with Head Elevated 15–30 Degrees",
                action_instruction="Lie patient down with head and shoulders slightly elevated on pillows to reduce intracranial pressure. Loosen restrictive clothing around neck.",
                warning_note="Do NOT allow patient to walk or exert themselves.",
                icon="Activity",
            ),
            ProtocolStepItem(
                step_number=3,
                title="Strictly Withhold All Food, Water, and Oral Medications",
                action_instruction="Do NOT give water, food, or aspirin. Stroke can impair swallowing reflexes causing fatal aspiration, and aspirin is dangerous if stroke is hemorrhagic.",
                warning_note="Aspirin must only be administered after a hospital CT scan confirms ischemic stroke.",
                icon="AlertTriangle",
            ),
        ],
        citations=[
            CITATIONS_CATALOG["asa_stroke_fast"],
            CITATIONS_CATALOG["good_samaritan_134a"],
        ],
    ),
    "severe_burns": GroundedProtocolResponse(
        condition_id="severe_burns",
        condition_label="Thermal Burns",
        crisis_type="medical",
        severity_level=3,
        priority="medium",
        protocol_title="BBA & WHO Thermal Burn First-Aid Protocol",
        authority="British Burn Association (BBA) & WHO Burn Trauma Division",
        disclaimers="Immediate cooling halts deep tissue necrosis. Keep victim warm after cooling.",
        legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
        recommended_radius_km=2.0,
        emergency_number="108",
        steps=[
            ProtocolStepItem(
                step_number=1,
                title="Cool Burn with Cool Running Tap Water for 20 Minutes",
                action_instruction="Immediately place burned area under gentle, cool running tap water (15–20°C) for a minimum of 20 full minutes. This dissipates heat trapped in tissue.",
                warning_note="Do NOT use ice, ice water, toothpaste, butter, raw egg, or turmeric. Ice causes vasoconstriction and tissue necrosis.",
                icon="Droplets",
            ),
            ProtocolStepItem(
                step_number=2,
                title="Remove Constrictive Jewelry & Non-Adherent Clothing",
                action_instruction="Quickly and gently remove rings, watches, bracelets, and belts before severe swelling sets in. Remove loose clothing around burn.",
                warning_note="Do NOT pull away clothing that is melted or stuck to burned flesh.",
                icon="ShieldAlert",
            ),
            ProtocolStepItem(
                step_number=3,
                title="Cover Loosely with Clean Cling Film or Sterile Dressing",
                action_instruction="Apply clean plastic food cling wrap in layers (do not wrap tightly around limbs) or cover with a clean non-fluffy sheet. Keep patient warm to prevent hypothermia.",
                warning_note="Do NOT pop or puncture blistered skin.",
                icon="FileText",
            ),
        ],
        citations=[
            CITATIONS_CATALOG["bba_burns_2021"],
            CITATIONS_CATALOG["good_samaritan_134a"],
        ],
    ),
    "fracture_trauma": GroundedProtocolResponse(
        condition_id="fracture_trauma",
        condition_label="Compound Trauma",
        crisis_type="medical",
        severity_level=4,
        priority="high",
        protocol_title="ATLS & NDMA Orthopedic & Spinal Trauma Protocol",
        authority="National Disaster Management Authority (NDMA) & ATLS",
        disclaimers="Prevent secondary neurovascular injury. Splint in position found.",
        legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
        recommended_radius_km=2.5,
        emergency_number="108",
        steps=[
            ProtocolStepItem(
                step_number=1,
                title="Immobilize Head & Spine (In-Line Manual Stabilization)",
                action_instruction="If victim fell from height or suffered vehicular collision, immediately kneel at their head and hold head and neck still in neutral alignment.",
                warning_note="Do NOT move or bend neck or spine unless immediate fire, explosion, or structural collapse threatens life.",
                icon="Bone",
            ),
            ProtocolStepItem(
                step_number=2,
                title="Control Bleeding Around Open Compound Fracture",
                action_instruction="If bone is protruding through skin, cover protruding bone with sterile moist dressing. Apply pressure around wound edges to control bleeding.",
                warning_note="NEVER attempt to push exposed bone ends back into the wound.",
                icon="Droplet",
            ),
            ProtocolStepItem(
                step_number=3,
                title="Splint Limb in Position Found",
                action_instruction="Support injured limb with rolled blankets, magazines, or rigid boards above and below the fracture site. Secure with cloth ties without cutting circulation.",
                warning_note="Check foot or hand for warmth, sensation, and pulse after splinting.",
                icon="Layers",
            ),
        ],
        citations=[
            CITATIONS_CATALOG["atls_trauma_ndma"],
            CITATIONS_CATALOG["good_samaritan_134a"],
        ],
    ),
    "anaphylaxis_allergy": GroundedProtocolResponse(
        condition_id="anaphylaxis_allergy",
        condition_label="Anaphylactic Shock",
        crisis_type="medical",
        severity_level=5,
        priority="critical",
        protocol_title="World Allergy Organization (WAO) Anaphylaxis Protocol",
        authority="World Allergy Organization (WAO) & Resuscitation Council UK",
        disclaimers="Epinephrine is the only first-line life-saving treatment for anaphylaxis.",
        legal_shield="Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
        recommended_radius_km=3.0,
        emergency_number="108",
        steps=[
            ProtocolStepItem(
                step_number=1,
                title="Administer Epinephrine Auto-Injector (EpiPen) Immediately",
                action_instruction="Remove safety cap. Hold outer mid-thigh firmly. Push injector perpendicular into outer thigh until click is heard. Hold in place for 5–10 seconds, then massage area.",
                warning_note="Epinephrine can be administered through clothing if necessary.",
                icon="Zap",
            ),
            ProtocolStepItem(
                step_number=2,
                title="Lie Victim Flat with Legs Elevated",
                action_instruction="Lie victim flat on their back and elevate legs 30 cm to maintain venous return and blood pressure. If breathing is difficult, allow them to sit up slightly.",
                warning_note="Do NOT allow patient to suddenly stand or walk; rapid standing can cause fatal circulatory collapse.",
                icon="UserCheck",
            ),
            ProtocolStepItem(
                step_number=3,
                title="Prepare Second Dose if No Improvement in 5 Minutes",
                action_instruction="If symptoms persist or worsen after 5–10 minutes, administer a second epinephrine dose in the opposite thigh.",
                warning_note="Keep airway open and monitor for respiratory arrest.",
                icon="Repeat",
            ),
        ],
        citations=[
            CITATIONS_CATALOG["who_bleeding_control"],
            CITATIONS_CATALOG["good_samaritan_134a"],
        ],
    ),
}


# ==============================================================================
# CLINICAL CONTRAINDICATION RULES & DETECTOR
# ==============================================================================

CONTRAINDICATION_RULES: list[dict[str, Any]] = [
    {
        "flag": "NO_ORAL_FLUIDS_UNCONSCIOUS",
        "keywords": ["water", "liquid", "drink", "milk", "juice", "chai", "tea", "jal", "pani", "জল", "পানি", "খাওয়া"],
        "trigger_conditions": ["cardiac_arrest", "unconscious_seizure", "stroke", "accident", "fracture_trauma"],
        "severity": "CRITICAL",
        "warning_title": "NEVER Administer Oral Fluids to Unconscious / Gasping Patient",
        "warning_message": "Giving water, fluids, or oral items to an unresponsive or choking victim directly enters the trachea, causing fatal airway obstruction and pulmonary aspiration.",
        "action_directive": "DO NOT give any water or liquid. Keep airway clear and position on side (recovery position) only if breathing normally.",
    },
    {
        "flag": "NO_MOVING_SPINAL_TRAUMA",
        "keywords": ["move", "stand", "walk", "lift", "carry", "shift", "sit up", "হাঁটানো", "তোলা", "chalana", "uthao"],
        "trigger_conditions": ["fracture_trauma", "road_accident", "accident"],
        "severity": "CRITICAL",
        "warning_title": "DO NOT Move or Shift Suspected Spinal Trauma Victim",
        "warning_message": "Moving a victim after high-speed crash or fall can transect the cervical spinal cord, causing permanent quadriplegia or respiratory arrest.",
        "action_directive": "Maintain manual in-line stabilization of head and neck. Move ONLY if immediate fire, toxic gas, or structural explosion threatens life.",
    },
    {
        "flag": "NO_OBJECTS_IN_SEIZURE_MOUTH",
        "keywords": ["mouth", "spoon", "finger", "cloth", "stick", "tongue", "bite", "দাঁত", "চামচ", "muh", "chammach"],
        "trigger_conditions": ["unconscious_seizure"],
        "severity": "CRITICAL",
        "warning_title": "NEVER Insert Fingers or Objects into Mouth During Seizure",
        "warning_message": "Patients cannot swallow their tongue during a convulsion. Inserting objects causes severe broken teeth, shattered enamel airway obstruction, and finger amputation.",
        "action_directive": "Keep mouth completely clear. Cushion head with soft jacket and turn victim onto side once jerking stops.",
    },
    {
        "flag": "NO_ICE_OR_PASTE_ON_BURNS",
        "keywords": ["ice", "toothpaste", "butter", "oil", "turmeric", "haldi", "egg", "বরফ", "টুথপেস্ট", "হলুদ", "baraf"],
        "trigger_conditions": ["severe_burns", "fire"],
        "severity": "WARNING",
        "warning_title": "DO NOT Apply Ice, Toothpaste, Oil, or Turmeric to Burns",
        "warning_message": "Ice induces vasoconstriction and causes frostbite/ischemic necrosis. Toothpaste and home ointments trap heat and cause deep wound infections.",
        "action_directive": "Cool ONLY with clean, cool running tap water for 20 minutes. Cover loosely with clean plastic cling wrap.",
    },
    {
        "flag": "DO_NOT_STOP_CPR_FOR_RIBS",
        "keywords": ["crack", "pop", "rib", "break", "bone", "হাড় ভাঙা", "শব্দ", "toot gayi"],
        "trigger_conditions": ["cardiac_arrest"],
        "severity": "CAUTION",
        "warning_title": "Do NOT Stop CPR Compressions if Ribs or Cartilage Crack",
        "warning_message": "Costochondral separation or rib cracking occurs in up to 30% of effective adult CPR. Halting compressions guarantees fatal cerebral hypoxia.",
        "action_directive": "CONTINUE CPR without hesitation. Restoring cerebral oxygenation is the absolute sole priority.",
    },
    {
        "flag": "NO_REMOVING_IMPALED_OBJECT",
        "keywords": ["remove knife", "pull out", "take out knife", "pull object", "knife", "glass pull", "ছুরি বের", "nikalna"],
        "trigger_conditions": ["severe_bleeding", "fracture_trauma", "accident"],
        "severity": "CRITICAL",
        "warning_title": "NEVER Pull Out or Remove an Impaled Object",
        "warning_message": "The embedded object is acting as a mechanical plug on severed blood vessels. Pulling it out triggers catastrophic uncontrolled arterial hemorrhage.",
        "action_directive": "Leave object in place. Stabilize it with bulky rolled dressings around the perimeter and apply pressure around the base.",
    },
]


def evaluate_contraindications(user_query: str, condition_id: str | None = None) -> list[ContraindicationAlert]:
    """Scan user query and context against clinical contraindications knowledge base."""
    text_lower = user_query.lower()
    alerts: list[ContraindicationAlert] = []

    for rule in CONTRAINDICATION_RULES:
        # Match keywords
        keyword_match = any(kw in text_lower for kw in rule["keywords"])
        if not keyword_match:
            continue

        # If rule is condition-specific, check condition match
        cond_match = True
        if rule.get("trigger_conditions") and condition_id:
            cond_match = condition_id in rule["trigger_conditions"]

        if keyword_match and (cond_match or condition_id is None):
            alerts.append(
                ContraindicationAlert(
                    flag=rule["flag"],
                    severity=rule["severity"],
                    warning_title=rule["warning_title"],
                    warning_message=rule["warning_message"],
                    action_directive=rule["action_directive"],
                )
            )

    return alerts


def get_grounded_protocol(condition_id: str) -> GroundedProtocolResponse | None:
    """Retrieve full evidence-based protocol for a condition."""
    if condition_id in GROUNDED_PROTOCOLS:
        return GROUNDED_PROTOCOLS[condition_id]

    # Fallback to cardiac arrest if unknown medical condition
    return GROUNDED_PROTOCOLS.get("cardiac_arrest")
