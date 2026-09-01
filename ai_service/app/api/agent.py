"""NearHelp AI — Emergency Crisis Assistant Agent REST Endpoints."""

import logging
from typing import Any

from app.agent.gemini_agent import emergency_agent_service, handover_node
from app.agent.knowledge import GROUNDED_PROTOCOLS, get_grounded_protocol
from app.agent.state import EmergencyAgentState
from app.schemas.agent import (
    AgentChatRequest,
    AgentChatResponse,
    AgentInitRequest,
    ClinicalHandoverSummary,
    GroundedProtocolResponse,
    StepProgressRequest,
    StepProgressResponse,
)
from fastapi import APIRouter, HTTPException, Path, status

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/agent", tags=["AI Crisis Assistant Agent"])


@router.post(
    "/chat",
    response_model=AgentChatResponse,
    status_code=status.HTTP_200_OK,
    summary="Execute Agent Dialogue Turn (LangGraph + Gemini 2.5)",
    description="Processes bystander questions through the LangGraph Emergency Agent, enforcing clinical safety guardrails, AHA 110 BPM CPR rhythm, and statutory citations.",
)
async def chat_with_agent(request: AgentChatRequest) -> AgentChatResponse:
    """Run dialogue turn through LangGraph Emergency Agent."""
    try:
        response = await emergency_agent_service.execute_turn(request)
        return response
    except Exception as e:
        logger.exception("Agent chat execution error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Agent execution failed: {e!s}",
        ) from e


@router.post(
    "/init",
    status_code=status.HTTP_200_OK,
    summary="Initialize or Reset Emergency Agent Session",
)
async def init_agent_session(request: AgentInitRequest) -> dict[str, Any]:
    """Initialize stateful agent session with specific clinical condition profile."""
    try:
        emergency_agent_service.get_or_create_session(
            session_id=request.session_id,
            condition_id=request.condition_id,
            role=request.role,
        )
        protocol = get_grounded_protocol(request.condition_id)
        return {
            "status": "initialized",
            "session_id": request.session_id,
            "condition_id": request.condition_id,
            "protocol_title": protocol.protocol_title,
            "total_steps": len(protocol.steps),
            "cpr_bpm": protocol.cpr_bpm,
            "legal_shield": protocol.legal_shield,
        }
    except Exception as e:
        logger.exception("Agent session init error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to initialize session: {e!s}",
        ) from e


@router.post(
    "/step",
    response_model=StepProgressResponse,
    status_code=status.HTTP_200_OK,
    summary="Toggle or Update Protocol Step Completion",
)
async def update_step_progress(request: StepProgressRequest) -> StepProgressResponse:
    """Update completed steps list and compute progress percentage."""
    try:
        completed_steps, total_steps, progress_pct, all_done = emergency_agent_service.toggle_step(
            session_id=request.session_id,
            step_number=request.step_number,
            completed=request.completed,
        )
        current_idx = min(len(completed_steps), total_steps - 1)
        return StepProgressResponse(
            session_id=request.session_id,
            completed_steps=completed_steps,
            total_steps=total_steps,
            progress_percentage=progress_pct,
            current_step_index=current_idx,
            all_completed=all_done,
        )
    except Exception as e:
        logger.exception("Step progress update error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to update step progress: {e!s}",
        ) from e


@router.get(
    "/protocols",
    response_model=list[GroundedProtocolResponse],
    status_code=status.HTTP_200_OK,
    summary="Get All Curated Evidence-Based First-Aid Protocols",
)
async def get_all_protocols() -> list[GroundedProtocolResponse]:
    """Retrieve full catalog of grounded resuscitation protocols."""
    return list(GROUNDED_PROTOCOLS.values())


@router.get(
    "/protocols/{condition_id}",
    response_model=GroundedProtocolResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Evidence-Based Protocol by Condition ID",
)
async def get_protocol_by_condition(
    condition_id: str = Path(..., description="Condition identifier (e.g. 'cardiac_arrest', 'severe_bleeding')"),
) -> GroundedProtocolResponse:
    """Retrieve specific grounded protocol with citations."""
    protocol = get_grounded_protocol(condition_id)
    if not protocol:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Protocol for condition '{condition_id}' not found.",
        )
    return protocol


@router.post(
    "/handover",
    response_model=ClinicalHandoverSummary,
    status_code=status.HTTP_200_OK,
    summary="Generate Clinical Handover Summary for 108 Paramedics",
)
async def generate_handover_report(request: AgentChatRequest) -> ClinicalHandoverSummary:
    """Generate structured clinical handover summary with digital audit signature."""
    try:
        session = emergency_agent_service.get_or_create_session(request.session_id)
        state: EmergencyAgentState = {
            "session_id": request.session_id,
            "condition_id": session.get("condition_id", "cardiac_arrest"),
            "completed_steps": session.get("completed_steps", [1, 2]),
            "cpr_metronome_active": session.get("cpr_metronome_active", True),
            "aed_attached": session.get("aed_attached", True),
            "severity_level": 5,
        }
        res = handover_node(state)
        return ClinicalHandoverSummary(**res["handover_report"])
    except Exception as e:
        logger.exception("Handover report generation error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to generate clinical handover: {e!s}",
        ) from e
