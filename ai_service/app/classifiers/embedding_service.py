"""NearHelp AI — Embedding Generation & Cosine Similarity Service."""

import hashlib
import logging
import math
import re
from typing import Any

from app.classifiers.crisis_types import ALL_EMERGENCY_PROFILES, EmergencyProfile
from app.core.config import settings

logger = logging.getLogger(__name__)


def cosine_similarity(vec_a: list[float], vec_b: list[float]) -> float:
    """Calculate mathematical cosine similarity between two float vectors."""
    if not vec_a or not vec_b or len(vec_a) != len(vec_b):
        return 0.0

    dot_product = sum(a * b for a, b in zip(vec_a, vec_b, strict=False))
    norm_a = math.sqrt(sum(a * a for a in vec_a))
    norm_b = math.sqrt(sum(b * b for b in vec_b))

    if norm_a == 0.0 or norm_b == 0.0:
        return 0.0

    sim = dot_product / (norm_a * norm_b)
    return max(0.0, min(1.0, (sim + 1.0) / 2.0 if sim < 0 else sim))


class ClinicalSemanticVectorizer:
    """Fast, deterministic 384-dimensional clinical embedding generator.

    Provides sub-millisecond local embedding generation with clinical keyword expansion,
    subword n-grams, and multi-lingual Indian emergency token weighting.
    """

    DIMENSION = 384

    # High-weight clinical token dictionary
    CLINICAL_LEXICON: dict[str, float] = {
        # Cardiac & Respiratory
        "cardiac": 12.0, "heart": 12.0, "attack": 10.0, "chest": 10.0, "cpr": 15.0, "pulse": 12.0,
        "arrest": 12.0, "unresponsive": 12.0, "collapse": 10.0, "collapsed": 10.0, "gasping": 12.0, "breathing": 10.0,
        "asthma": 12.0, "respiratory": 12.0, "choking": 12.0, "inhaler": 10.0, "cyanosis": 12.0, "cyanotic": 12.0,
        "blue": 8.0, "wheezing": 10.0, "hypoxia": 12.0, "aed": 12.0, "defibrillator": 12.0,
        # Bleeding & Trauma
        "bleeding": 12.0, "bleed": 11.0, "hemorrhage": 15.0, "arterial": 14.0, "artery": 12.0,
        "spurting": 14.0, "laceration": 12.0, "wound": 10.0, "tourniquet": 14.0, "blood": 10.0,
        "fracture": 14.0, "bone": 12.0, "broken": 10.0, "protrusion": 14.0, "orthopedic": 10.0, "ladder": 8.0,
        # Neurological & Allergic
        "seizure": 14.0, "convulsion": 14.0, "epileptic": 14.0, "fit": 10.0, "frothing": 12.0, "shaking": 10.0,
        "stroke": 14.0, "fast": 10.0, "facial": 10.0, "droop": 12.0, "drooping": 12.0, "slurred": 12.0, "speech": 10.0,
        "anaphylaxis": 15.0, "allergic": 12.0, "allergy": 12.0, "hives": 12.0, "epipen": 15.0,
        "epinephrine": 15.0, "swelling": 10.0, "throat": 10.0, "peanut": 10.0,
        # Burns & Hazards
        "burn": 12.0, "burns": 12.0, "scald": 10.0, "flame": 10.0, "charred": 12.0, "blister": 10.0, "blistered": 10.0, "oil": 8.0,
        "fire": 14.0, "smoke": 12.0, "flames": 12.0, "explosion": 14.0, "transformer": 12.0, "billowing": 10.0,
        "gas": 14.0, "leak": 14.0, "lpg": 15.0, "cylinder": 14.0, "smell": 10.0, "odor": 10.0, "hissing": 12.0,
        # Accidents & Crime
        "accident": 12.0, "crash": 12.0, "collision": 12.0, "car": 8.0, "truck": 8.0, "drowning": 14.0,
        "water": 8.0, "submerged": 12.0, "assault": 14.0, "knife": 14.0, "stab": 15.0, "stabbed": 15.0, "weapon": 12.0,
        "flood": 12.0, "collapse": 12.0, "rubble": 12.0, "trapped": 12.0, "debris": 10.0, "balcony": 10.0,
        # Bengali Clinical Tokens
        "পড়ে": 10.0, "শ্বাস": 14.0, "বুক": 12.0, "বুকে": 14.0, "ব্যথা": 12.0, "প্রচণ্ড": 12.0, "রক্ত": 12.0, "রক্তপাত": 14.0,
        "আগুন": 14.0, "ধোঁয়া": 12.0, "ধোঁয়ায়": 12.0, "গ্যাস": 14.0, "সিলিন্ডার": 14.0, "লিক": 14.0,
        "দুর্ঘটনা": 12.0, "বেহুঁশ": 14.0, "খিঁচুনি": 14.0, "মুখ": 10.0, "হাড়": 12.0, "পুড়ে": 12.0,
        "সাড়া": 12.0, "ডাকলে": 10.0, "মাটিতে": 10.0, "বাড়ি": 8.0, "বাড়িতে": 10.0,
        # Hindi Transliterated Tokens
        "behosh": 14.0, "saans": 14.0, "chhati": 12.0, "dard": 10.0, "khoon": 12.0, "aag": 14.0,
        "cylinder": 14.0, "mirgi": 14.0, "daura": 12.0, "jal": 10.0, "haddi": 12.0, "toot": 10.0,
        "chot": 10.0, "ruk": 8.0, "patti": 8.0,
    }

    @classmethod
    def embed_text(cls, text: str) -> list[float]:
        """Generate a normalized 384-dimensional vector embedding for input text."""
        vec = [0.0] * cls.DIMENSION
        if not text:
            return vec

        cleaned = re.sub(r"[^\w\s]", " ", text.lower())
        tokens = cleaned.split()

        for token in tokens:
            weight = cls.CLINICAL_LEXICON.get(token, 1.0)

            # Multiple hash buckets for rich token representation
            h = int(hashlib.sha256(token.encode("utf-8")).hexdigest(), 16)
            dim_1 = h % cls.DIMENSION
            dim_2 = (h >> 8) % cls.DIMENSION
            dim_3 = (h >> 16) % cls.DIMENSION

            vec[dim_1] += 2.0 * weight
            vec[dim_2] += 1.5 * weight
            vec[dim_3] += 1.0 * weight

        # Add bigram context
        for i in range(len(tokens) - 1):
            bigram = f"{tokens[i]}_{tokens[i+1]}"
            h_bi = int(hashlib.md5(bigram.encode("utf-8")).hexdigest(), 16)
            dim_bi = h_bi % cls.DIMENSION
            vec[dim_bi] += 3.0

        # L2-normalize vector
        norm = math.sqrt(sum(x * x for x in vec))
        if norm > 0:
            vec = [x / norm for x in vec]

        return vec


class EmbeddingService:
    """High-performance embedding generation and cosine similarity matching engine."""

    def __init__(self):
        # Maps profile_id to a list of reference phrase embeddings
        self._profile_reference_embeddings: dict[str, list[list[float]]] = {}
        self._profile_centroid_embeddings: dict[str, list[float]] = {}
        self._initialized = False

    async def initialize(self):
        """Precompute and cache reference embeddings for all crisis profiles."""
        if self._initialized:
            return

        logger.info("Initializing NearHelp AI Embedding Service Reference Matrix...")
        for profile_id, profile in ALL_EMERGENCY_PROFILES.items():
            phrase_embeddings: list[list[float]] = []

            # Embed each reference text, symptom, and description individually
            phrases_to_embed = [
                profile.label,
                profile.description,
                *profile.symptoms,
                *profile.reference_texts,
            ]

            for phrase in phrases_to_embed:
                emb = await self.generate_embedding(phrase)
                phrase_embeddings.append(emb)

            self._profile_reference_embeddings[profile_id] = phrase_embeddings

            # Compute centroid embedding
            dim = len(phrase_embeddings[0])
            centroid = [0.0] * dim
            for emb in phrase_embeddings:
                for d in range(dim):
                    centroid[d] += emb[d]
            c_norm = math.sqrt(sum(x * x for x in centroid))
            if c_norm > 0:
                centroid = [x / c_norm for x in centroid]
            self._profile_centroid_embeddings[profile_id] = centroid

        self._initialized = True
        logger.info(
            "Embedding Service Initialized with %d reference profiles.",
            len(self._profile_reference_embeddings),
        )

    async def generate_embedding(self, text: str) -> list[float]:
        """Generate embedding vector using Gemini API or local vectorizer."""
        if settings.GEMINI_API_KEY and settings.GEMINI_API_KEY != "your_gemini_api_key_here":
            try:
                import google.generativeai as genai

                genai.configure(api_key=settings.GEMINI_API_KEY)
                result = genai.embed_content(
                    model=settings.EMBEDDING_MODEL,
                    content=text,
                    task_type="classification",
                )
                if "embedding" in result and result["embedding"]:
                    emb = result["embedding"]
                    norm = math.sqrt(sum(x * x for x in emb))
                    if norm > 0:
                        return [x / norm for x in emb]
            except Exception as e:
                logger.warning(
                    "Gemini embedding call failed, falling back to local vectorizer: %s",
                    e,
                )

        return ClinicalSemanticVectorizer.embed_text(text)

    async def match_emergency(
        self,
        input_text: str,
        top_k: int = 3,
    ) -> list[tuple[EmergencyProfile, float, list[str]]]:
        """Match input text against reference crisis profiles using multi-reference cosine similarity.

        Returns list of (EmergencyProfile, similarity_score, matched_symptoms).
        """
        if not self._initialized:
            await self.initialize()

        if not input_text or not input_text.strip():
            default_profile = ALL_EMERGENCY_PROFILES.get("cardiac_arrest")
            return [(default_profile, 0.20, [])]

        input_emb = await self.generate_embedding(input_text)
        lowered_text = input_text.lower()

        scores: list[tuple[EmergencyProfile, float, list[str]]] = []

        for profile_id, phrase_embeddings in self._profile_reference_embeddings.items():
            profile = ALL_EMERGENCY_PROFILES[profile_id]

            # 1. Cosine similarity against each reference phrase (take top matches)
            phrase_sims = [cosine_similarity(input_emb, p_emb) for p_emb in phrase_embeddings]
            phrase_sims.sort(reverse=True)

            # Max similarity & top-3 average similarity
            max_sim = phrase_sims[0] if phrase_sims else 0.0
            top_3_avg = sum(phrase_sims[:3]) / min(len(phrase_sims), 3) if phrase_sims else 0.0

            # Centroid similarity
            centroid_sim = cosine_similarity(input_emb, self._profile_centroid_embeddings[profile_id])

            # Composite vector similarity
            composite_sim = max_sim * 0.60 + top_3_avg * 0.25 + centroid_sim * 0.15

            # 2. Symptom detection & keyword match bonus
            matched_symptoms: list[str] = []
            for symptom in profile.symptoms:
                symp_tokens = [t.lower() for t in re.sub(r"[^\w\s]", " ", symptom).split() if len(t) >= 2]
                match_count = sum(1 for tok in symp_tokens if tok in lowered_text)
                if match_count >= 1:
                    matched_symptoms.append(symptom)

            # Reference text keyword overlap
            ref_overlap_count = 0
            total_overlap_words = 0
            for ref_t in profile.reference_texts:
                ref_words = [w.lower() for w in re.sub(r"[^\w\s]", " ", ref_t).split() if len(w) >= 2]
                overlap = sum(1 for w in ref_words if w in lowered_text)
                total_overlap_words = max(total_overlap_words, overlap)

            # Calibrated clinical confidence score
            symptom_boost = min(0.35, len(matched_symptoms) * 0.10)
            overlap_boost = min(0.35, total_overlap_words * 0.06)

            # Base score scaled to high confidence when matching strongly
            if composite_sim > 0.35 or matched_symptoms or total_overlap_words >= 2:
                raw_confidence = composite_sim * 0.65 + symptom_boost + overlap_boost + 0.20
            else:
                raw_confidence = composite_sim

            final_confidence = max(0.10, min(0.99, raw_confidence))
            scores.append((profile, final_confidence, matched_symptoms))

        # Sort by confidence descending
        scores.sort(key=lambda x: x[1], reverse=True)
        return scores[:top_k]


# Global embedding service singleton
embedding_service = EmbeddingService()
