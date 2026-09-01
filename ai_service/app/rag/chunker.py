"""NearHelp AI — Procedure-Level Document Chunking Pipeline for Emergency Protocols.

Splits verified clinical documents (WHO, Red Cross, NDMA, AHA, AIIMS) into
high-signal, procedure-level semantic chunks preserving step sequences,
clinical warnings, and statutory citations.
"""

from __future__ import annotations

import hashlib
import json
import logging
import re
from dataclasses import asdict, dataclass, field
from pathlib import Path
from typing import Any

logger = logging.getLogger(__name__)


def estimate_tokens(text: str) -> int:
    """Estimate token count for a text string using tiktoken or word heuristic."""
    if not text:
        return 0
    try:
        import tiktoken
        enc = tiktoken.get_encoding("cl100k_base")
        return len(enc.encode(text))
    except Exception:
        # Fallback: average 1.33 tokens per word + punctuation
        words = text.split()
        return max(1, int(len(words) * 1.33))


@dataclass
class ProtocolChunk:
    """Actionable, procedure-level semantic chunk with clinical metadata."""

    chunk_id: str
    doc_id: str
    source: str
    section: str
    guideline_name: str
    authority: str
    url: str | None
    crisis_type: str
    condition_id: str
    condition_label: str
    title: str
    text: str
    step_number: int | None = None
    warning_note: str | None = None
    is_contraindication: bool = False
    is_medication_restricted: bool = False
    is_surgical_restricted: bool = False
    cpr_bpm: int | None = None
    legal_shield: str = "Section 134A Motor Vehicles (Amendment) Act 2019"
    token_count: int = 0
    tags: list[str] = field(default_factory=list)

    def to_metadata(self) -> dict[str, Any]:
        """Convert chunk metadata to ChromaDB-compatible primitive dictionary."""
        return {
            "chunk_id": self.chunk_id,
            "doc_id": self.doc_id,
            "source": self.source,
            "section": self.section,
            "guideline_name": self.guideline_name,
            "authority": self.authority,
            "url": self.url or "",
            "crisis_type": self.crisis_type,
            "condition_id": self.condition_id,
            "condition_label": self.condition_label,
            "step_number": self.step_number if self.step_number is not None else -1,
            "is_contraindication": self.is_contraindication,
            "is_medication_restricted": self.is_medication_restricted,
            "is_surgical_restricted": self.is_surgical_restricted,
            "cpr_bpm": self.cpr_bpm if self.cpr_bpm is not None else 0,
            "legal_shield": self.legal_shield,
            "token_count": self.token_count,
            "tags_str": ",".join(self.tags),
        }


class DocumentChunker:
    """Procedure-level document parser and chunking engine."""

    TARGET_MIN_TOKENS: int = 150
    TARGET_MAX_TOKENS: int = 450
    CONTEXT_OVERLAP_TOKENS: int = 50

    def chunk_json_file(self, file_path: str | Path) -> list[ProtocolChunk]:
        """Parse structured protocol JSON file into procedure-level chunks."""
        path = Path(file_path)
        if not path.exists():
            logger.warning("Protocol file not found: %s", path)
            return []

        try:
            with open(path, encoding="utf-8") as f:
                data = json.load(f)
        except Exception as e:
            logger.error("Failed to parse JSON protocol %s: %s", path, e)
            return []

        doc_id = data.get("document_id", path.stem)
        authority = data.get("authority", "Emergency Authority")
        organization = data.get("organization", "Emergency Organization")
        doc_title = data.get("title", "Clinical Protocol")
        source_url = data.get("source_url")
        statutory_shield = data.get("statutory_shield", "Section 134A Motor Vehicles (Amendment) Act 2019")

        chunks: list[ProtocolChunk] = []

        for proto in data.get("protocols", []):
            cond_id = proto.get("condition_id", "emergency")
            cond_label = proto.get("condition_label", cond_id.replace("_", " ").title())
            crisis_type = proto.get("crisis_type", "medical")
            proto_name = proto.get("protocol_name", doc_title)
            section = proto.get("section", "Standard Guidelines")
            cpr_bpm = proto.get("cpr_bpm")

            # 1. Overview chunk for condition
            steps_data = proto.get("steps", [])
            steps_summary = " ".join(f"Step {s.get('step_number', idx+1)}: {s.get('title', '')}." for idx, s in enumerate(steps_data))
            overview_text = (
                f"Protocol: {proto_name}\n"
                f"Authority: {authority} ({organization})\n"
                f"Section: {section}\n"
                f"Condition: {cond_label} (Type: {crisis_type})\n"
                f"Summary of Resuscitation Steps: {steps_summary}\n"
                f"Statutory Shield: {statutory_shield}"
            )
            overview_id = hashlib.sha256(f"{doc_id}_{cond_id}_overview".encode()).hexdigest()[:16]
            chunks.append(
                ProtocolChunk(
                    chunk_id=overview_id,
                    doc_id=doc_id,
                    source=organization,
                    section=section,
                    guideline_name=proto_name,
                    authority=authority,
                    url=source_url,
                    crisis_type=crisis_type,
                    condition_id=cond_id,
                    condition_label=cond_label,
                    title=f"{cond_label} — Protocol Overview",
                    text=overview_text,
                    step_number=0,
                    cpr_bpm=cpr_bpm,
                    legal_shield=statutory_shield,
                    token_count=estimate_tokens(overview_text),
                    tags=[cond_id, crisis_type, "overview", "first_aid"],
                )
            )

            # 2. Step-by-step procedure chunks
            for step in steps_data:
                step_num = step.get("step_number", 1)
                step_title = step.get("title", f"Step {step_num}")
                action = step.get("action_instruction", "")
                warning = step.get("warning_note")
                is_contra = step.get("is_contraindication", False)
                is_med = step.get("is_medication_restricted", False)
                is_surg = step.get("is_surgical_restricted", False)

                # Context-enriched passage text
                step_text_parts = [
                    f"[Protocol: {proto_name} • {authority}]",
                    f"Condition: {cond_label} | Step {step_num}: {step_title}",
                    f"ACTION DIRECTIVE: {action}",
                ]
                if warning:
                    step_text_parts.append(f"CRITICAL PRECAUTION: {warning}")
                if cpr_bpm and ("cpr" in step_title.lower() or "compression" in action.lower()):
                    step_text_parts.append(f"METRONOME CADENCE: Maintain {cpr_bpm} BPM rhythmic compressions (545.45ms period).")
                step_text_parts.append(f"LEGAL PROTECTION: {statutory_shield}")

                step_text = "\n".join(step_text_parts)
                chunk_id = hashlib.sha256(f"{doc_id}_{cond_id}_step_{step_num}".encode()).hexdigest()[:16]

                chunks.append(
                    ProtocolChunk(
                        chunk_id=chunk_id,
                        doc_id=doc_id,
                        source=organization,
                        section=section,
                        guideline_name=proto_name,
                        authority=authority,
                        url=source_url,
                        crisis_type=crisis_type,
                        condition_id=cond_id,
                        condition_label=cond_label,
                        title=f"{cond_label} — Step {step_num}: {step_title}",
                        text=step_text,
                        step_number=step_num,
                        warning_note=warning,
                        is_contraindication=is_contra,
                        is_medication_restricted=is_med,
                        is_surgical_restricted=is_surg,
                        cpr_bpm=cpr_bpm,
                        legal_shield=statutory_shield,
                        token_count=estimate_tokens(step_text),
                        tags=[cond_id, crisis_type, f"step_{step_num}", "action_directive"],
                    )
                )

            # 3. Clinical Contraindication Chunks
            for contra in proto.get("contraindications", []):
                flag = contra.get("flag", "CONTRAINDICATION")
                c_title = contra.get("warning_title", "Clinical Contraindication")
                c_msg = contra.get("warning_message", "")
                c_dir = contra.get("action_directive", "")

                contra_text = (
                    f"[CLINICAL CONTRAINDICATION ALERT • {authority}]\n"
                    f"Condition: {cond_label}\n"
                    f"Warning: {c_title} ({flag})\n"
                    f"Clinical Rationale: {c_msg}\n"
                    f"MANDATORY DIRECTIVE: {c_dir}\n"
                    f"Source: {proto_name} • {section}"
                )
                contra_id = hashlib.sha256(f"{doc_id}_{cond_id}_contra_{flag}".encode()).hexdigest()[:16]

                chunks.append(
                    ProtocolChunk(
                        chunk_id=contra_id,
                        doc_id=doc_id,
                        source=organization,
                        section=section,
                        guideline_name=proto_name,
                        authority=authority,
                        url=source_url,
                        crisis_type=crisis_type,
                        condition_id=cond_id,
                        condition_label=cond_label,
                        title=f"Contraindication Alert: {c_title}",
                        text=contra_text,
                        step_number=None,
                        warning_note=c_msg,
                        is_contraindication=True,
                        is_medication_restricted=("MED" in flag or "ORAL" in flag or "ASPIRIN" in flag or "SEDATIVE" in flag),
                        is_surgical_restricted=("SURG" in flag or "INCISION" in flag or "TRACHEOTOMY" in flag or "REDUCTION" in flag),
                        cpr_bpm=cpr_bpm,
                        legal_shield=statutory_shield,
                        token_count=estimate_tokens(contra_text),
                        tags=[cond_id, crisis_type, "contraindication", flag.lower()],
                    )
                )

        logger.info("Parsed %d chunks from %s", len(chunks), path.name)
        return chunks

    def chunk_markdown_file(self, file_path: str | Path) -> list[ProtocolChunk]:
        """Parse structured markdown protocol file into procedure-level chunks."""
        path = Path(file_path)
        if not path.exists():
            return []

        try:
            with open(path, encoding="utf-8") as f:
                content = f.read()
        except Exception as e:
            logger.error("Failed to read markdown %s: %s", path, e)
            return []

        doc_id = path.stem
        lines = content.splitlines()

        # Extract top-level metadata
        title = "Clinical First Aid Manual"
        authority = "Health Authority"
        source = "Emergency Standard"
        shield = "Section 134A Motor Vehicles (Amendment) Act 2019"

        sections: list[tuple[str, list[str]]] = []
        current_section = "General Overview"
        current_lines: list[str] = []

        for line in lines:
            if line.startswith("# "):
                title = line.replace("# ", "").strip()
            elif "**Authority**:" in line:
                authority = line.split("**Authority**:", 1)[1].strip()
            elif "**Source**:" in line:
                source = line.split("**Source**:", 1)[1].strip()
            elif "**Statutory Shield**:" in line:
                shield = line.split("**Statutory Shield**:", 1)[1].strip()
            elif line.startswith("## "):
                if current_lines:
                    sections.append((current_section, current_lines))
                current_section = line.replace("## ", "").strip()
                current_lines = []
            else:
                current_lines.append(line)

        if current_lines:
            sections.append((current_section, current_lines))

        chunks: list[ProtocolChunk] = []

        for sec_title, sec_lines in sections:
            sec_text_raw = "\n".join(sec_lines).strip()
            if not sec_text_raw:
                continue

            cond_id = re.sub(r"[^a-z0-9]+", "_", sec_title.lower()).strip("_")
            chunk_text = (
                f"[Protocol: {title} • {authority}]\n"
                f"Section: {sec_title}\n"
                f"{sec_text_raw}\n"
                f"Legal Shield: {shield}"
            )
            chunk_id = hashlib.sha256(f"{doc_id}_{cond_id}".encode()).hexdigest()[:16]

            chunks.append(
                ProtocolChunk(
                    chunk_id=chunk_id,
                    doc_id=doc_id,
                    source=source,
                    section=sec_title,
                    guideline_name=title,
                    authority=authority,
                    url=None,
                    crisis_type="medical" if "cpr" in sec_title.lower() or "bleeding" in sec_title.lower() or "snake" in sec_title.lower() or "poison" in sec_title.lower() else "accident",
                    condition_id=cond_id,
                    condition_label=sec_title,
                    title=f"{title} — {sec_title}",
                    text=chunk_text,
                    step_number=None,
                    legal_shield=shield,
                    token_count=estimate_tokens(chunk_text),
                    tags=[cond_id, "markdown_protocol"],
                )
            )

        logger.info("Parsed %d markdown chunks from %s", len(chunks), path.name)
        return chunks

    def chunk_directory(self, dir_path: str | Path) -> list[ProtocolChunk]:
        """Ingest all JSON and Markdown protocol files in the specified directory."""
        directory = Path(dir_path)
        if not directory.exists() or not directory.is_dir():
            logger.warning("Directory %s does not exist.", directory)
            return []

        all_chunks: list[ProtocolChunk] = []
        for file in sorted(directory.iterdir()):
            if file.suffix.lower() == ".json":
                all_chunks.extend(self.chunk_json_file(file))
            elif file.suffix.lower() in [".md", ".markdown"]:
                all_chunks.extend(self.chunk_markdown_file(file))

        logger.info("Ingested %d total chunks from %s", len(all_chunks), directory)
        return all_chunks


document_chunker = DocumentChunker()
