"""NearHelp AI — Responder Ranking Algorithm Service."""

import logging

from app.models.user import User
from app.schemas.sos import RankedResponderItem

logger = logging.getLogger(__name__)


class RankingService:
    """Intelligent weighted ranking algorithm for optimal responder selection.
    
    Mathematical Formulation:
      Score(responder, emergency) = w1 * D + w2 * S + w3 * R
      
      Where:
        w1 = 0.40 (Distance / Proximity)
        w2 = 0.35 (Skill Matching)
        w3 = 0.25 (Reliability / Trust Score)
        
        D = 1.0 - (distance / max_radius)   [0, 1]
        S = |user_skills ∩ required_skills| / |required_skills| + (+0.2 if verified)  [0, 1.2]
        R = trust_score / 100.0             [0, 1]
    """

    DEFAULT_W1_DISTANCE: float = 0.40
    DEFAULT_W2_SKILL: float = 0.35
    DEFAULT_W3_RELIABILITY: float = 0.25

    @classmethod
    def compute_distance_score(cls, distance_meters: float, max_radius_meters: float) -> float:
        """Compute normalized inverse distance score D in [0.0, 1.0]."""
        if max_radius_meters <= 0:
            return 1.0 if distance_meters <= 0 else 0.0
        normalized = 1.0 - (distance_meters / max_radius_meters)
        return max(0.0, min(1.0, normalized))

    @classmethod
    def compute_skill_score(
        cls, user: User, required_skills: list[str]
    ) -> tuple[float, list[str], list[str]]:
        """Compute skill match score S in [0.0, 1.20] along with matched and verified skill lists."""
        if not required_skills:
            return 0.5, [], []

        # Standardize required skills
        req_set = {s.upper().strip() for s in required_skills}

        # Extract user skills and verification status
        user_skills: set[str] = set()
        verified_skills: set[str] = set()

        raw_skills = user.skills or []
        if isinstance(raw_skills, list):
            for item in raw_skills:
                if isinstance(item, dict):
                    skill_name = str(item.get("skill_type") or item.get("name") or "").upper().strip()
                    is_verified = bool(item.get("verified", False) or item.get("status") == "APPROVED")
                    if skill_name:
                        user_skills.add(skill_name)
                        if is_verified:
                            verified_skills.add(skill_name)
                elif isinstance(item, str):
                    skill_name = item.upper().strip()
                    if skill_name:
                        user_skills.add(skill_name)

        # Also check user badges
        raw_badges = user.badges or []
        if isinstance(raw_badges, list):
            for badge in raw_badges:
                b_str = str(badge).upper().strip()
                if "CPR" in b_str:
                    user_skills.add("CPR_CERTIFIED")
                    verified_skills.add("CPR_CERTIFIED")
                if "DOCTOR" in b_str:
                    user_skills.add("DOCTOR")
                    verified_skills.add("DOCTOR")
                if "NURSE" in b_str:
                    user_skills.add("NURSE")
                    verified_skills.add("NURSE")
                if "EMT" in b_str or "PARAMEDIC" in b_str:
                    user_skills.add("EMT")
                    verified_skills.add("EMT")
                if "FIRST_AID" in b_str:
                    user_skills.add("FIRST_AID")
                    verified_skills.add("FIRST_AID")

        # Compute intersection
        matching_skills = user_skills.intersection(req_set)
        matching_verified = verified_skills.intersection(req_set)

        match_ratio = len(matching_skills) / float(len(req_set)) if req_set else 0.0

        # Verified bonus: +0.20 if any matching skill is officially verified
        verified_bonus = 0.20 if len(matching_verified) > 0 else 0.0

        total_skill_score = min(1.20, match_ratio + verified_bonus)
        return total_skill_score, sorted(user_skills), sorted(verified_skills)

    @classmethod
    def compute_reliability_score(cls, trust_score: float) -> float:
        """Compute normalized reliability score R in [0.0, 1.0]."""
        return max(0.0, min(1.0, float(trust_score) / 100.0))

    @classmethod
    def score_responder(
        cls,
        user: User,
        distance_meters: float,
        max_radius_meters: float,
        required_skills: list[str],
        w1: float = DEFAULT_W1_DISTANCE,
        w2: float = DEFAULT_W2_SKILL,
        w3: float = DEFAULT_W3_RELIABILITY,
    ) -> RankedResponderItem:
        """Compute composite ranking score for a candidate responder."""
        d_score = cls.compute_distance_score(distance_meters, max_radius_meters)
        s_score, user_skills, user_verified = cls.compute_skill_score(user, required_skills)
        r_score = cls.compute_reliability_score(user.trust_score)

        # Composite score
        total_score = (w1 * d_score) + (w2 * s_score) + (w3 * r_score)

        # Estimated ETA: assume average emergency transit speed 20 km/h (~333 meters/min)
        eta_min = max(0.5, round(distance_meters / 333.33, 1))

        return RankedResponderItem(
            responder_id=user.id,
            name=user.name or f"Volunteer #{str(user.id)[:6]}",
            distance_meters=round(distance_meters, 1),
            distance_score=round(d_score, 4),
            skill_match_score=round(s_score, 4),
            reliability_score=round(r_score, 4),
            total_ranking_score=round(total_score, 4),
            skills=user_skills,
            verified_skills=user_verified,
            trust_score=float(user.trust_score),
            eta_minutes=eta_min,
            fcm_token_available=bool(user.fcm_token),
        )

    @classmethod
    def rank_responders(
        cls,
        candidates: list[tuple[User, float]],
        max_radius_meters: float,
        required_skills: list[str],
        severity_level: int = 3,
        w1: float = DEFAULT_W1_DISTANCE,
        w2: float = DEFAULT_W2_SKILL,
        w3: float = DEFAULT_W3_RELIABILITY,
    ) -> list[RankedResponderItem]:
        """Rank candidates using composite scoring and return top-N responders sorted by score descending."""
        scored_items: list[RankedResponderItem] = []

        for user, distance in candidates:
            item = cls.score_responder(
                user=user,
                distance_meters=distance,
                max_radius_meters=max_radius_meters,
                required_skills=required_skills,
                w1=w1,
                w2=w2,
                w3=w3,
            )
            scored_items.append(item)

        # Sort descending by total composite score
        scored_items.sort(key=lambda x: x.total_ranking_score, reverse=True)

        # Determine target N based on triage severity level
        limit_map = {
            5: 15,  # Level 5 Critical Life Threat (broadcast to top 15)
            4: 8,   # Level 4 Urgent Trauma (top 8)
            3: 5,   # Level 3 Moderate (top 5)
            2: 3,   # Level 2 Mild (top 3)
            1: 2,   # Level 1 Low (top 2)
        }
        target_limit = limit_map.get(severity_level, 5)

        return scored_items[:target_limit]


ranking_service = RankingService()
