"""NearHelp AI — Master Crisis Types Taxonomy & 8 Curated Clinical Conditions Matrix."""

from dataclasses import dataclass, field
from typing import Any


@dataclass
class EmergencyProfile:
    """Clinical and operational definition for an emergency subtype."""

    id: str
    label: str
    crisis_type: str
    severity: int
    priority: str  # critical, high, medium, low
    description: str
    symptoms: list[str]
    reference_texts: list[str]
    suggested_skills: list[str]
    immediate_action: str
    recommended_radius_km: float
    emergency_number: str = "108"
    requires_professional: bool = True
    call_emergency_services: bool = True
    icon_name: str = "AlertCircle"


# ==============================================================================
# 8 CURATED CLINICAL CONDITIONS MATRIX
# ==============================================================================

CLINICAL_CONDITIONS_MATRIX: dict[str, EmergencyProfile] = {
    "cardiac_arrest": EmergencyProfile(
        id="cardiac_arrest",
        label="Cardiac / Chest Pain",
        crisis_type="medical",
        severity=5,
        priority="critical",
        description="Sudden collapse, unresponsive victim, severe crushing chest pain radiating to left arm/jaw, agonal gasping respiration, absence of carotid pulse.",
        symptoms=[
            "Unresponsive to verbal and tactile stimuli",
            "No palpable carotid pulse",
            "Agonal gasping respiration",
            "Sudden collapse",
            "Severe crushing chest pain",
            "Diaphoresis (cold sweats)",
        ],
        reference_texts=[
            "Patient collapsed suddenly, not responding, gasping for air, no pulse detected",
            "Severe crushing chest pressure radiating to arm, patient unconscious, agonal breathing",
            "Cardiac arrest, heart attack, myocardial infarction, victim unresponsive, CPR needed",
            "মাটিতে পড়ে গেছেন, ডাকলে সাড়া দিচ্ছেন না, শ্বাস নিচ্ছেন না, বুকে হাত দিয়ে পড়ে গেলেন",
            "Bystander collapsed in lobby, unresponsive, clutching chest, gasping for air",
            "Dil ka daura, behosh ho gaya, saans nahi chal rahi, chhati me tez dard",
        ],
        suggested_skills=["CPR_CERTIFIED", "DOCTOR", "EMT", "NURSE", "FIRST_AID"],
        immediate_action="Begin CPR immediately: compress center of chest 5-6 cm deep at 110-120 BPM. Send for AED. Never give water.",
        recommended_radius_km=3.5,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="HeartPulse",
    ),
    "severe_bleeding": EmergencyProfile(
        id="severe_bleeding",
        label="Severe Hemorrhage",
        crisis_type="medical",
        severity=4,
        priority="high",
        description="Pulsatile arterial bleed, deep traumatic laceration, rapid blood loss, cold clammy skin, impending hypovolemic shock.",
        symptoms=[
            "Pulsating bright red arterial bleed",
            "Open compound laceration",
            "Cold clammy skin & pallor",
            "Rapid weak pulse",
            "Pooling blood on floor",
        ],
        reference_texts=[
            "Heavy bleeding from arm laceration, bright red blood spurting, victim becoming pale and weak",
            "Arterial bleeding, deep wound, blood soaking through bandages rapidly, hypovolemic shock risk",
            "Severe hemorrhage, knife cut wound, uncontrolled blood loss, tourniquet required",
            "প্রচণ্ড রক্তপাত হচ্ছে, হাত কেটে ফিনকি দিয়ে রক্ত বের হচ্ছে, শরীর ঠান্ডা হয়ে যাচ্ছে",
            "Chot lagne se bohot khoon beh raha hai, artery cut ho gayi, patti se khoon ruk nahi raha",
        ],
        suggested_skills=["FIRST_AID", "EMT", "NURSE", "DOCTOR"],
        immediate_action="Apply firm, direct pressure with clean cloth or sterile gauze. If limb arterial bleed does not stop, apply tourniquet 5cm above wound.",
        recommended_radius_km=2.5,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Droplet",
    ),
    "respiratory_asthma": EmergencyProfile(
        id="respiratory_asthma",
        label="Respiratory Distress",
        crisis_type="medical",
        severity=5,
        priority="critical",
        description="Severe bronchospasm, acute airway obstruction, peripheral cyanosis, gasping, inability to speak, oxygen desaturation < 88%.",
        symptoms=[
            "Inability to speak in sentences",
            "Peripheral cyanosis (blue lips/fingertips)",
            "Severe expiratory wheezing and stridor",
            "Accessory muscle usage for breathing",
            "Oxygen saturation < 88%",
        ],
        reference_texts=[
            "Severe asthma attack, struggling to breathe, lips turning blue, inhaler not working",
            "Acute respiratory failure, choking, gasping for oxygen, wheezing loudly, cyanotic",
            "Airway obstruction, acute bronchospasm, victim unable to speak, rapid shallow gasps",
            "শ্বাস নিতে পারছে না, ঠোঁট নীল হয়ে গেছে, হাঁপানির তীব্র টান, কথা বলতে পারছে না",
            "Saans lene me bohot takleef ho rahi hai, hoth neele pad gaye, dum ghut raha hai",
        ],
        suggested_skills=["DOCTOR", "EMT", "NURSE", "FIRST_AID"],
        immediate_action="Sit patient upright. Assist with prescribed bronchodilator inhaler (with spacer if available). Keep calm and do not crowd.",
        recommended_radius_km=3.0,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Wind",
    ),
    "unconscious_seizure": EmergencyProfile(
        id="unconscious_seizure",
        label="Seizure / Fainting",
        crisis_type="medical",
        severity=5,
        priority="critical",
        description="Generalized tonic-clonic convulsions, oral frothing, loss of consciousness, post-ictal confusion, risk of head injury.",
        symptoms=[
            "Generalized violent muscle jerking",
            "Oral frothing and tongue bite risk",
            "Loss of consciousness",
            "Post-ictal unresponsiveness or confusion",
            "Incontinence",
        ],
        reference_texts=[
            "Person having violent seizure on floor, shaking uncontrollably, foaming at mouth, unconscious",
            "Tonic clonic epileptic fit, convulsion, unresponsive patient, head protection needed",
            "Epileptic seizure episode, sudden collapse with muscle jerks, postictal coma state",
            "হঠাৎ খিঁচুনি শুরু হয়েছে, মাটিতে পড়ে হাত পা কাঁপছে, মুখ দিয়ে ফেনা উঠছে, বেহুঁশ",
            "Mirgi ka daura pada hai, body akad rahi hai, muh se jhaag aa raha hai, behosh hai",
        ],
        suggested_skills=["FIRST_AID", "NURSE", "EMT", "DOCTOR"],
        immediate_action="Clear surrounding hard objects. Protect head with soft padding. Do NOT put anything in mouth. Place in recovery position once jerking stops.",
        recommended_radius_km=2.5,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Activity",
    ),
    "stroke": EmergencyProfile(
        id="stroke",
        label="Stroke (FAST Protocol)",
        crisis_type="medical",
        severity=4,
        priority="high",
        description="Acute neurological deficit: facial asymmetry/drooping, unilateral arm/leg weakness or numbness, severe dysarthria / slurred speech, visual loss.",
        symptoms=[
            "Asymmetrical facial drooping",
            "Unilateral arm drift or hemiparesis",
            "Severe dysarthria / slurred speech",
            "Acute visual disturbance or confusion",
            "Sudden loss of balance",
        ],
        reference_texts=[
            "Elderly person face drooping on one side, cannot lift right arm, speech is slurred and confused",
            "Stroke FAST symptoms, sudden unilateral weakness, unable to speak clearly, cerebrovascular accident",
            "Acute ischemic stroke suspected, asymmetrical smile, arm weakness, onset 20 minutes ago",
            "মুখ বেঁকে গেছে, কথা জড়িয়ে যাচ্ছে, একদিকের হাত তুলতে পারছে না, প্যারালাইসিস সন্দেহ",
            "Chehra tedha ho gaya, bol nahi pa raha hai, ek taraf ka hath paon sunn ho gaya, stroke",
        ],
        suggested_skills=["DOCTOR", "EMT", "NURSE"],
        immediate_action="Keep patient still with head elevated 15-30 degrees. Note exact time of symptom onset. Do NOT administer aspirin or fluids.",
        recommended_radius_km=3.0,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Brain",
    ),
    "severe_burns": EmergencyProfile(
        id="severe_burns",
        label="Thermal Burns",
        crisis_type="medical",
        severity=3,
        priority="medium",
        description="Second or third-degree thermal/chemical burns, charred or blistering skin > 10% BSA, inhalation injury risk, acute pain and shock.",
        symptoms=[
            "Blistered charred skin > 10% BSA",
            "Acute thermal or chemical trauma",
            "Airway smoke inhalation risk",
            "Severe pain or sensory loss from deep burn",
        ],
        reference_texts=[
            "Boiling oil spilled over hands and torso, skin peeling with huge blisters, severe agony",
            "Severe flame burns on arms and chest, charred tissue, smoke inhalation, chemical burn",
            "Thermal burns, scalding water accident, extensive second degree blistering skin injury",
            "গরম তেল বা আগুনে পুড়ে গেছে, চামড়া উঠে গেছে, ফোস্কা পড়েছে, অসহ্য যন্ত্রণা",
            "Aag ya garam tel se jal gaya hai, chhale pad gaye hain, chamdi jhulsi hui hai",
        ],
        suggested_skills=["FIRST_AID", "NURSE", "EMT"],
        immediate_action="Cool burn under cool running tap water for at least 15-20 minutes. Do NOT apply ice, toothpaste, or oil. Cover with clean cling film or sheet.",
        recommended_radius_km=2.0,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Flame",
    ),
    "fracture_trauma": EmergencyProfile(
        id="fracture_trauma",
        label="Compound Trauma",
        crisis_type="medical",
        severity=4,
        priority="high",
        description="Open compound bone fracture with bone protruding, severe limb angulation, spinal or pelvic trauma from high-impact fall or crash.",
        symptoms=[
            "Open fracture with bone protrusion",
            "Spinal immobilization indicated",
            "Severe limb deformity and swelling",
            "Inability to bear weight or move limb",
            "Severe localized tenderness",
        ],
        reference_texts=[
            "Fell from stairs, leg is bent at unnatural angle, bone visible through skin, cannot stand",
            "Compound fracture of femur, severe bone deformity, open wound, extreme agony upon movement",
            "Severe orthopedic trauma, broken arm after fall, open bleeding fracture, spinal precautions",
            "সিঁড়ি থেকে পড়ে পা ভেঙে গেছে, হাড় চামড়া ফুঁড়ে বেরিয়ে এসেছে, নড়াচড়া করতে পারছে না",
            "Girne se haddi toot gayi hai, haddi bahar dikh rahi hai, pair tedha ho gaya, hil nahi pa raha",
        ],
        suggested_skills=["FIRST_AID", "EMT", "DOCTOR", "NURSE"],
        immediate_action="Immobilize limb in current position without forcing bone back. Control surrounding bleed. Keep neck and spine aligned if fall from height.",
        recommended_radius_km=2.5,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Bone",
    ),
    "anaphylaxis_allergy": EmergencyProfile(
        id="anaphylaxis_allergy",
        label="Anaphylactic Shock",
        crisis_type="medical",
        severity=5,
        priority="critical",
        description="Acute systemic allergic reaction following allergen exposure: generalized hives/urticaria, throat tightness, laryngeal angioedema, hypotension.",
        symptoms=[
            "Acute diffuse urticaria & severe hives",
            "Laryngeal angioedema (throat swelling)",
            "Hypotensive collapse and dizziness",
            "Acute dyspnea and wheezing",
            "Swelling of tongue and lips",
        ],
        reference_texts=[
            "Ate peanut/seafood, throat closing up, severe hives all over body, struggling to breathe, fainting",
            "Anaphylactic shock after insect sting / medication, tongue swollen, airway closing, epinephrine needed",
            "Acute severe allergic reaction, systemic hives, stridor, rapidly declining blood pressure",
            "অ্যালার্জি থেকে গলা ফুলে বন্ধ হয়ে যাচ্ছে, সারা গায়ে চাকা চাকা লাল দাগ, শ্বাস বন্ধ হয়ে আসছে",
            "Allergy ki wajah se gala band ho raha hai, saans nahi aa rahi, poore sharir me rashes hain",
        ],
        suggested_skills=["DOCTOR", "EMT", "NURSE", "FIRST_AID"],
        immediate_action="Administer epinephrine auto-injector (EpiPen) into outer mid-thigh if available. Lie victim flat with legs elevated unless breathing is difficult.",
        recommended_radius_km=3.0,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="AlertCircle",
    ),
}


# ==============================================================================
# NON-MEDICAL CRISIS PROFILES (FIRE, GAS LEAK, ACCIDENT, CRIME, DISASTER)
# ==============================================================================

NON_MEDICAL_PROFILES: dict[str, EmergencyProfile] = {
    # --- FIRE ---
    "structural_fire": EmergencyProfile(
        id="structural_fire",
        label="Building / Structural Fire",
        crisis_type="fire",
        severity=5,
        priority="critical",
        description="Active flames in residential or commercial building, heavy toxic smoke, occupants trapped, structural collapse risk.",
        symptoms=["Thick black smoke", "Active flames", "People trapped inside", "Explosion hazard"],
        reference_texts=[
            "Apartment building on fire, heavy black smoke billowing from third floor, people trapped inside",
            "Fire breaking out in building, flames spreading fast, fire brigade needed immediately",
            "বাড়িতে ভয়াবহ আগুন লেগেছে, চারদিক ধোঁয়ায় ঢেকে গেছে, অনেকে ভেতরে আটকে আছে",
            "Building me aag lag gayi hai, bohot dhuaan hai, log andar phase hue hain, fire brigade bhejo",
        ],
        suggested_skills=["FIRE_SAFETY", "FIRST_AID", "EMT"],
        immediate_action="Evacuate immediately via stairwells (never use elevators). Stay low under smoke. Call 101 Fire Brigade.",
        recommended_radius_km=3.0,
        emergency_number="101",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Flame",
    ),
    "electrical_fire": EmergencyProfile(
        id="electrical_fire",
        label="Electrical Transformer / Meter Fire",
        crisis_type="fire",
        severity=4,
        priority="high",
        description="Electrical short circuit, sparking transformer, meter box explosion, live wire risk.",
        symptoms=["Blue sparks", "Electrical burning smell", "Sparking power lines", "Transformer explosion"],
        reference_texts=[
            "Electrical meter box on fire, sparks flying everywhere, live wires sparking in alley",
            "Transformer burst on street, electrical fire spreading, power line snapped on road",
            "ইলেকট্রিক খুঁটি বা ট্রান্সফর্মারে আগুন লেগেছে, স্পার্ক হচ্ছে, তার ছিঁড়ে পড়ে আছে",
            "Transformer fat gaya hai, bijli ke taar me aag lagi hai, current ka khatra hai",
        ],
        suggested_skills=["FIRE_SAFETY", "FIRST_AID"],
        immediate_action="Do NOT throw water on electrical fire. Use CO2 or dry powder extinguisher if safe. Cut main power switch.",
        recommended_radius_km=2.0,
        emergency_number="101",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Zap",
    ),
    # --- GAS LEAK ---
    "lpg_gas_leak": EmergencyProfile(
        id="lpg_gas_leak",
        label="LPG Cylinder / Gas Leak",
        crisis_type="gas_leak",
        severity=5,
        priority="critical",
        description="Strong ethyl mercaptan odor, hissing LPG cylinder, high risk of vapor cloud ignition or BLEVE explosion.",
        symptoms=["Pungent rotten cabbage odor", "Hissing regulator", "Dizziness in room", "Explosion hazard"],
        reference_texts=[
            "Strong smell of cooking gas in kitchen, LPG cylinder hissing loudly, fear of explosion",
            "Gas cylinder leaking heavily, smell everywhere in apartment, evacuation needed",
            "গ্যাস সিলিন্ডার লিক করছে, তীব্র গন্ধ বের হচ্ছে, আগুন জ্বালাবেন না, বিপদ",
            "Gas cylinder leak ho raha hai, bohot tez badboo aa rahi hai, blast hone ka darr hai",
        ],
        suggested_skills=["FIRE_SAFETY", "FIRST_AID"],
        immediate_action="Do NOT operate electrical switches, matchsticks, or lighters. Open all doors and windows for ventilation. Close cylinder regulator valve.",
        recommended_radius_km=2.0,
        emergency_number="101",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="AlertTriangle",
    ),
    # --- ACCIDENT ---
    "road_accident": EmergencyProfile(
        id="road_accident",
        label="Road / Traffic Collision",
        crisis_type="accident",
        severity=5,
        priority="critical",
        description="High-speed vehicular crash, pedestrian hit, motorcycle collision, trapped passengers, multiple casualties.",
        symptoms=["Multiple injured victims", "Shattered glass", "Vehicle rollover", "Unresponsive passengers"],
        reference_texts=[
            "Severe car crash on bypass, two vehicles collided head-on, passengers trapped and bleeding",
            "Motorbike hit by speeding truck, rider unconscious on road with multiple fractures and head trauma",
            "বড় সড়ক দুর্ঘটনা, বাস আর লরির সংঘর্ষ, অনেকেই রক্তাক্ত অবস্থায় আটকে আছে",
            "Gadi ka bura accident ho gaya hai, bike rider road pe behosh pada hai, ambulance chahiye",
        ],
        suggested_skills=["FIRST_AID", "EMT", "DOCTOR", "NURSE"],
        immediate_action="Ensure scene safety (hazard lights, divert traffic). Do NOT move victims unless fire hazard. Maintain spinal alignment.",
        recommended_radius_km=3.0,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Car",
    ),
    "drowning": EmergencyProfile(
        id="drowning",
        label="Water Submersion / Drowning",
        crisis_type="accident",
        severity=5,
        priority="critical",
        description="Submersion in pond, river, or pool, acute hypoxemia, water aspiration, cardiac arrest secondary to drowning.",
        symptoms=["Submerged in water", "Not breathing", "Cyanosis", "Hypothermia"],
        reference_texts=[
            "Person pulled out of water/pond, not breathing, swallowed water, unconscious and cold",
            "Drowning victim pulled from swimming pool/river, unresponsive, rescue breaths needed",
            "পুকুরে ডুবে গেছিল, জল থেকে তোলা হয়েছে কিন্তু শ্বাস নিচ্ছে না, পেট ফুলে গেছে",
            "Paani me doob gaya tha, bahar nikala hai par saans nahi le raha, behosh hai",
        ],
        suggested_skills=["CPR_CERTIFIED", "DOCTOR", "EMT", "FIRST_AID"],
        immediate_action="Provide 5 initial rescue breaths immediately, then begin standard CPR (30 compressions : 2 breaths). Keep patient warm.",
        recommended_radius_km=3.0,
        emergency_number="108",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="LifeBuoy",
    ),
    # --- CRIME ---
    "physical_assault": EmergencyProfile(
        id="physical_assault",
        label="Violent Assault / Weapon Attack",
        crisis_type="crime",
        severity=5,
        priority="critical",
        description="Active physical violence, stabbing, armed robbery, victim critically injured, immediate threat to personal safety.",
        symptoms=["Active assault in progress", "Weapon seen", "Stab wound", "Victim calling for help"],
        reference_texts=[
            "Violent physical assault in alley, knife attack, person stabbed and bleeding, attackers fled",
            "Armed robbery in progress, physical attack, victim injured and needs police and medical help",
            "মারপিট চলছে, ছুরি দিয়ে আঘাত করেছে, রক্তাক্ত অবস্থায় পড়ে আছে, পুলিশ দরকার",
            "Hamla hua hai, chaku mara hai, khoon beh raha hai, police aur ambulance turant chahiye",
        ],
        suggested_skills=["FIRST_AID", "EMT"],
        immediate_action="Stay at a safe distance. Call 100/112 Police immediately. Once safe, apply pressure to stop severe bleeding.",
        recommended_radius_km=2.5,
        emergency_number="100",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="ShieldAlert",
    ),
    # --- NATURAL DISASTER ---
    "urban_flood_collapse": EmergencyProfile(
        id="urban_flood_collapse",
        label="Structural Collapse / Flood",
        crisis_type="natural_disaster",
        severity=5,
        priority="critical",
        description="Building wall collapse, severe flash flooding, trapped citizens under rubble, electrocution risk.",
        symptoms=["Building rubble", "Trapped victims under debris", "Submerged streets", "Live power in water"],
        reference_texts=[
            "Old building balcony collapsed on street, people trapped under concrete debris and bricks",
            "Flash flood water entering homes, wall collapsed, people trapped in basement",
            "পুরনো বাড়ি ভেঙে পড়েছে, ধ্বংসস্তূপের নিচে মানুষ আটকে আছে, উদ্ধারকারী দল দরকার",
            "Deewar gir gayi hai, malbe ke neeche log dabe hue hain, disaster team chahiye",
        ],
        suggested_skills=["FIRE_SAFETY", "FIRST_AID", "EMT"],
        immediate_action="Alert municipal disaster authorities (112). Do NOT enter unstable rubble without safety gear. Clear access roads for rescue teams.",
        recommended_radius_km=4.0,
        emergency_number="112",
        requires_professional=True,
        call_emergency_services=True,
        icon_name="Home",
    ),
}

# Combine all profiles for full taxonomy
ALL_EMERGENCY_PROFILES: dict[str, EmergencyProfile] = {
    **CLINICAL_CONDITIONS_MATRIX,
    **NON_MEDICAL_PROFILES,
}

# Top-level crisis types list
CRISIS_TYPES_TAXONOMY: list[dict[str, Any]] = [
    {
        "id": "medical",
        "name": "Medical Emergency",
        "description": "Acute life-threatening or urgent medical conditions, trauma, and illnesses.",
        "default_emergency_number": "108",
        "sub_types": list(CLINICAL_CONDITIONS_MATRIX.keys()),
    },
    {
        "id": "fire",
        "name": "Fire Outbreak",
        "description": "Building fires, electrical fires, industrial blazes, and wildfire hazards.",
        "default_emergency_number": "101",
        "sub_types": ["structural_fire", "electrical_fire", "wildfire", "vehicle_fire"],
    },
    {
        "id": "gas_leak",
        "name": "Gas & Hazardous Leak",
        "description": "LPG cylinder leaks, chemical gas releases, and hazardous toxic fumes.",
        "default_emergency_number": "101",
        "sub_types": ["lpg_gas_leak", "industrial_gas_leak", "carbon_monoxide"],
    },
    {
        "id": "accident",
        "name": "Accident & Trauma",
        "description": "Road collisions, falls from height, drowning, and structural accidents.",
        "default_emergency_number": "108",
        "sub_types": ["road_accident", "drowning", "fall_from_height", "pedestrian_hit"],
    },
    {
        "id": "crime",
        "name": "Crime & Violence",
        "description": "Physical assault, armed robbery, domestic violence, and security threats.",
        "default_emergency_number": "100",
        "sub_types": ["physical_assault", "robbery_weapon", "domestic_violence", "harassment_stalking"],
    },
    {
        "id": "natural_disaster",
        "name": "Natural Disaster",
        "description": "Floods, earthquakes, building collapses, and severe storms.",
        "default_emergency_number": "112",
        "sub_types": ["urban_flood_collapse", "earthquake", "cyclone_storm", "landslide"],
    },
]
