"""NearHelp AI — Clinical Hallucination Guardrails Unit Tests."""

import pytest
from app.rag.guardrails import HallucinationGuardrails


def test_guardrail_unconscious_water_rejection():
    """Verify strict prohibition of administering water/liquids to unconscious victims."""
    queries = [
        "Can I give him water to drink since he is unconscious?",
        "Should I pour tea or milk in his mouth while passed out?",
        "Give water to unconscious person who fell",
    ]
    for q in queries:
        res = HallucinationGuardrails.inspect_query(q)
        assert not res.passed, f"Failed on query: {q}"
        assert res.is_contraindicated
        assert len(res.contraindications) > 0
        assert "NO_ORAL_FLUIDS" in res.contraindications[0].flag or "UNCONSCIOUS" in res.contraindications[0].flag
        assert "Section 134A" in (res.override_reply or "")


def test_guardrail_snakebite_incision_rejection():
    """Verify strict prohibition of cutting snakebites or sucking venom."""
    queries = [
        "Can I cut the snake bite mark with a blade?",
        "Should I make an incision on the bite to let poisoned blood out?",
        "Can I suck venom out with my mouth?",
    ]
    for q in queries:
        res = HallucinationGuardrails.inspect_query(q)
        assert not res.passed, f"Failed on query: {q}"
        assert res.is_contraindicated
        assert any("INCISION" in c.flag or "SUCTION" in c.flag or "SURGICAL" in c.flag for c in res.contraindications)


def test_guardrail_forbidden_surgical_acts():
    """Verify prohibition against tracheostomy, pulling impaled rods, or setting fractures."""
    tracheo_query = "Can I do a tracheostomy or cut his windpipe with a pen?"
    res1 = HallucinationGuardrails.inspect_query(tracheo_query)
    assert not res1.passed
    assert any("TRACHEOTOMY" in c.flag for c in res1.contraindications)

    impaled_query = "Should I pull out the knife stuck in his stomach?"
    res2 = HallucinationGuardrails.inspect_query(impaled_query)
    assert not res2.passed
    assert any("OBJECT_REMOVAL" in c.flag for c in res2.contraindications)

    fracture_query = "Can I push the broken bone back inside his leg and straighten it?"
    res3 = HallucinationGuardrails.inspect_query(fracture_query)
    assert not res3.passed
    assert any("BONE_REDUCTION" in c.flag for c in res3.contraindications)


def test_guardrail_forbidden_medications():
    """Verify prohibition against unverified sedatives or aspirin in stroke/head injury."""
    sedative_q = "Can I give him a sleeping pill or diazepam to calm his breathing?"
    res1 = HallucinationGuardrails.inspect_query(sedative_q)
    assert not res1.passed
    assert any("SEDATIVE" in c.flag for c in res1.contraindications)

    aspirin_q = "Should I give aspirin to a victim with acute stroke symptoms?"
    res2 = HallucinationGuardrails.inspect_query(aspirin_q)
    assert not res2.passed
    assert any("ASPIRIN" in c.flag for c in res2.contraindications)


def test_guardrail_safe_queries_pass():
    """Verify safe clinical questions pass without false-positive contraindications."""
    safe_queries = [
        "How deep should chest compressions be during adult CPR?",
        "Where should I place the AED pads on the chest?",
        "How to apply direct pressure to a bleeding thigh wound?",
        "What is the recommended 20 minute water cooling for burns?",
    ]
    for q in safe_queries:
        res = HallucinationGuardrails.inspect_query(q)
        assert res.passed, f"Safe query wrongly blocked: {q}"
        assert not res.is_contraindicated
        assert len(res.contraindications) == 0


def test_response_sanitizer():
    """Verify post-generation sanitizer catches dangerous directives in generated text."""
    dangerous_output = "You should cut the wound open with a sterilized blade to remove debris."
    res = HallucinationGuardrails.sanitize_llm_response(dangerous_output)
    assert not res.passed
    assert res.is_contraindicated
    assert "CLINICAL SAFETY NOTICE" in (res.sanitized_text or "")
