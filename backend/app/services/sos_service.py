"""NearHelp AI — Smart SOS Engine Business Logic Service."""

import logging
import uuid
from datetime import UTC, datetime

from fastapi import HTTPException, status
from sqlalchemy import desc, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.response import SOSResponse
from app.models.sos_event import SOSEvent
from app.models.timeline_event import TimelineEvent
from app.models.user import User
from app.schemas.ai import SeverityRequest
from app.schemas.sos import (
    RankedResponderItem,
    SOSActiveListItem,
    SOSActiveListResponse,
    SOSCreateRequest,
    SOSCreateResponse,
    SOSDetailResponse,
    SOSEscalateRequest,
    SOSEscalationStatus,
    SOSResolveRequest,
    SOSResolveResponse,
    SOSResponseItem,
    SOSResponseRequest,
    TimelineEventItem,
)
from app.services.ai_client import ai_client
from app.services.geo_service import geo_service
from app.services.notification_service import notification_service
from app.services.ranking_service import ranking_service

logger = logging.getLogger(__name__)


class SOSService:
    """Core coordinator for Smart SOS emergency creation, PostGIS routing, and 3-layer escalation."""

    @staticmethod
    def _extract_skill_names(raw_skills: list | None) -> list[str]:
        """Normalize skills collection into a clean list of string names."""
        if not raw_skills or not isinstance(raw_skills, list):
            return []
        names = []
        for s in raw_skills:
            if isinstance(s, dict):
                n = s.get("name") or s.get("skill_type") or ""
                if n:
                    names.append(str(n))
            elif isinstance(s, str) and s:
                names.append(s)
        return names

    @classmethod
    def _build_escalation_status(cls, event: SOSEvent, elapsed_seconds: int = 0) -> SOSEscalationStatus:
        """Construct escalation status DTO based on active layer and elapsed time."""
        layer = event.escalation_layer or 1
        if elapsed_seconds >= 60:
            layer = 2

        descriptions = {
            1: f"Layer 1: Community Network Radial Dispatch ({int(event.current_radius_meters)}m)",
            2: "Layer 2: Direct 108/112 Municipal Ambulance Gateway Active",
            3: "Layer 3: Guided Self-Care AI Fallback (Cached RAG Protocol)",
        }

        emergency_number_map = {
            "fire": "101",
            "crime": "100",
            "gas_leak": "101",
            "medical": "108",
        }
        emergency_num = emergency_number_map.get(event.crisis_type, "108")

        return SOSEscalationStatus(
            current_layer=layer,
            current_radius_meters=float(event.current_radius_meters),
            max_radius_meters=float(event.max_radius_meters),
            elapsed_seconds=elapsed_seconds,
            auto_call_108_triggered=bool(event.auto_call_108_triggered or layer >= 2),
            recommended_emergency_number=emergency_num,
            offline_fallback_ready=True,
            layer_description=descriptions.get(layer, descriptions[1]),
        )

    @classmethod
    async def create_sos_event(
        cls,
        db: AsyncSession,
        user: User | None,
        req: SOSCreateRequest,
    ) -> SOSCreateResponse:
        """Create and dispatch a new SOS emergency event."""
        # 1. Parallel AI Triage Prediction
        severity_req = SeverityRequest(
            crisis_type=req.crisis_type,
            sub_type=req.sub_type,
            text=req.description or req.voice_transcript,
            symptoms=req.symptoms or [],
            photo_url=req.photo_url,
        )
        ai_triage = await ai_client.predict_severity(severity_req)

        # 2. Determine initial search radius based on clinical severity
        if req.initial_radius_meters:
            initial_radius = req.initial_radius_meters
        else:
            initial_radius = float(ai_triage.recommended_radius_km * 1000.0)

        # 3. Create SOSEvent DB Entity
        event_id = uuid.uuid4()
        broadcaster_id = user.id if (user and not req.is_anonymous) else None

        sos_event = SOSEvent(
            id=event_id,
            broadcaster_id=broadcaster_id,
            crisis_type=req.crisis_type,
            sub_type=req.sub_type or ai_triage.sub_type if hasattr(ai_triage, "sub_type") else req.sub_type,
            severity_score=ai_triage.severity_score,
            severity_level=ai_triage.severity_level,
            priority=ai_triage.priority,
            description=req.description or req.voice_transcript,
            symptoms=req.symptoms or [],
            immediate_action=ai_triage.recommended_actions[0] if ai_triage.recommended_actions else None,
            required_skills=ai_triage.required_responder_skills,
            latitude=req.latitude,
            longitude=req.longitude,
            address=req.address or "Salt Lake Sector V",
            sub_address=req.sub_address or "Kolkata, West Bengal",
            status="SOS_TRIGGERED",
            is_anonymous=req.is_anonymous,
            current_radius_meters=initial_radius,
            max_radius_meters=5000.0,
            escalation_layer=1,
            auto_call_108_triggered=ai_triage.auto_call_emergency_services,
            ai_triage_data=ai_triage.model_dump(),
            metadata_info={
                "voice_transcript": req.voice_transcript,
                "photo_url": req.photo_url,
                "created_via": "smart_sos_engine",
            },
        )

        # Set user location if known
        if user and not user.is_anonymous:
            await geo_service.update_user_location(db, user.id, req.latitude, req.longitude)

        db.add(sos_event)

        # 4. Insert Timeline Event: "sos_created"
        timeline_create = TimelineEvent(
            id=uuid.uuid4(),
            sos_event_id=event_id,
            actor_id=broadcaster_id,
            event_type="sos_created",
            details={
                "crisis_type": req.crisis_type,
                "sub_type": req.sub_type,
                "is_anonymous": req.is_anonymous,
                "initial_radius_meters": initial_radius,
            },
        )
        db.add(timeline_create)

        # 5. Insert Timeline Event: "ai_classified"
        timeline_ai = TimelineEvent(
            id=uuid.uuid4(),
            sos_event_id=event_id,
            actor_id=None,
            event_type="ai_classified",
            details={
                "severity_score": ai_triage.severity_score,
                "severity_level": ai_triage.severity_level,
                "priority": ai_triage.priority,
                "required_skills": ai_triage.required_responder_skills,
                "immediate_action": sos_event.immediate_action,
            },
        )
        db.add(timeline_ai)

        await db.commit()
        await db.refresh(sos_event)

        # 6. PostGIS Spatial Dispatch & Responder Ranking
        candidates = await geo_service.find_nearby_active_responders(
            db=db,
            latitude=req.latitude,
            longitude=req.longitude,
            radius_meters=initial_radius,
            exclude_user_id=broadcaster_id,
        )

        ranked_responders = ranking_service.rank_responders(
            candidates=candidates,
            max_radius_meters=sos_event.max_radius_meters,
            required_skills=sos_event.required_skills,
            severity_level=sos_event.severity_level,
        )

        # 7. Fan-out Push Notifications to Ranked Candidates
        for responder in ranked_responders:
            try:
                await notification_service.send_emergency_dispatch_alert(
                    user_id=responder.responder_id,
                    sos_event_id=event_id,
                    crisis_type=sos_event.crisis_type,
                    severity=sos_event.priority,
                    distance_str=f"{int(responder.distance_meters)}m",
                    eta_str=f"{responder.eta_minutes} min",
                    required_skills=sos_event.required_skills,
                )
            except Exception as e:
                logger.debug(f"Notification delivery log: {e}")

        escalation_status = cls._build_escalation_status(sos_event, elapsed_seconds=0)

        return SOSCreateResponse(
            id=sos_event.id,
            broadcaster_id=sos_event.broadcaster_id,
            status=sos_event.status,
            crisis_type=sos_event.crisis_type,
            sub_type=sos_event.sub_type,
            severity_score=sos_event.severity_score,
            severity_level=sos_event.severity_level,
            priority=sos_event.priority,
            immediate_action=sos_event.immediate_action,
            required_skills=sos_event.required_skills,
            latitude=sos_event.latitude,
            longitude=sos_event.longitude,
            address=sos_event.address,
            sub_address=sos_event.sub_address,
            is_anonymous=sos_event.is_anonymous,
            current_radius_meters=sos_event.current_radius_meters,
            escalation=escalation_status,
            top_ranked_responders=ranked_responders,
            candidates_notified_count=len(ranked_responders),
            created_at=sos_event.created_at,
        )

    @classmethod
    async def get_sos_details(cls, db: AsyncSession, sos_id: uuid.UUID) -> SOSDetailResponse:
        """Fetch complete details, response tracking, and timeline for an emergency."""
        stmt = select(SOSEvent).where(SOSEvent.id == sos_id)
        res = await db.execute(stmt)
        event = res.scalars().first()

        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"SOS emergency with ID '{sos_id}' was not found.",
            )

        # Fetch broadcaster name if present
        broadcaster_name = None
        if event.broadcaster_id:
            u_stmt = select(User).where(User.id == event.broadcaster_id)
            u_res = await db.execute(u_stmt)
            broadcaster = u_res.scalars().first()
            if broadcaster:
                broadcaster_name = broadcaster.name

        # Fetch responses
        resp_stmt = select(SOSResponse).where(SOSResponse.sos_event_id == sos_id).order_by(SOSResponse.joined_at.asc())
        resp_res = await db.execute(resp_stmt)
        responses = resp_res.scalars().all()

        response_items: list[SOSResponseItem] = []
        for r in responses:
            # Fetch responder name
            resp_user_stmt = select(User).where(User.id == r.responder_id)
            resp_user_res = await db.execute(resp_user_stmt)
            r_user = resp_user_res.scalars().first()
            r_name = r_user.name if r_user else f"Responder #{str(r.responder_id)[:6]}"
            r_trust = float(r_user.trust_score) if r_user else 50.0

            response_items.append(
                SOSResponseItem(
                    id=r.id,
                    sos_event_id=r.sos_event_id,
                    responder_id=r.responder_id,
                    responder_name=r_name,
                    responder_trust_score=r_trust,
                    responder_skills=cls._extract_skill_names(r_user.skills) if r_user else [],
                    status=r.status,
                    initial_distance_meters=r.initial_distance_meters,
                    initial_eta_seconds=r.initial_eta_seconds,
                    ranking_score=r.ranking_score,
                    joined_at=r.joined_at,
                    arrived_at=r.arrived_at,
                )
            )

        # Fetch timeline
        time_stmt = (
            select(TimelineEvent).where(TimelineEvent.sos_event_id == sos_id).order_by(TimelineEvent.timestamp.asc())
        )
        time_res = await db.execute(time_stmt)
        timeline_records = time_res.scalars().all()

        timeline_items: list[TimelineEventItem] = []
        for t in timeline_records:
            actor_name = None
            if t.actor_id:
                act_stmt = select(User).where(User.id == t.actor_id)
                act_res = await db.execute(act_stmt)
                actor = act_res.scalars().first()
                if actor:
                    actor_name = actor.name

            timeline_items.append(
                TimelineEventItem(
                    id=t.id,
                    sos_event_id=t.sos_event_id,
                    actor_id=t.actor_id,
                    actor_name=actor_name,
                    event_type=t.event_type,
                    details=t.details,
                    timestamp=t.timestamp,
                )
            )

        # Compute elapsed seconds
        now_utc = datetime.now(UTC)
        created_time = event.created_at.replace(tzinfo=UTC) if event.created_at.tzinfo is None else event.created_at
        elapsed_sec = int(max(0, (now_utc - created_time).total_seconds()))

        escalation_status = cls._build_escalation_status(event, elapsed_seconds=elapsed_sec)

        return SOSDetailResponse(
            id=event.id,
            broadcaster_id=event.broadcaster_id,
            broadcaster_name=broadcaster_name,
            status=event.status,
            crisis_type=event.crisis_type,
            sub_type=event.sub_type,
            severity_score=event.severity_score,
            severity_level=event.severity_level,
            priority=event.priority,
            description=event.description,
            symptoms=event.symptoms,
            immediate_action=event.immediate_action,
            required_skills=event.required_skills,
            latitude=event.latitude,
            longitude=event.longitude,
            address=event.address,
            sub_address=event.sub_address,
            is_anonymous=event.is_anonymous,
            current_radius_meters=event.current_radius_meters,
            escalation=escalation_status,
            responses=response_items,
            timeline=timeline_items,
            ai_triage_data=event.ai_triage_data,
            created_at=event.created_at,
            resolved_at=event.resolved_at,
        )

    @classmethod
    async def respond_to_sos(
        cls,
        db: AsyncSession,
        responder: User,
        sos_id: uuid.UUID,
        req: SOSResponseRequest,
    ) -> SOSResponseItem:
        """Handle responder acceptance or decline of an emergency alert."""
        # 1. Fetch SOS Event
        stmt = select(SOSEvent).where(SOSEvent.id == sos_id)
        res = await db.execute(stmt)
        event = res.scalars().first()

        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="SOS emergency event not found.",
            )

        if event.status in ("RESOLVED", "CANCELLED"):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Cannot respond to an emergency with status '{event.status}'.",
            )

        # 2. Update responder coordinate if provided
        if req.current_latitude is not None and req.current_longitude is not None:
            await geo_service.update_user_location(db, responder.id, req.current_latitude, req.current_longitude)
            dist_meters = geo_service.calculate_distance(
                req.current_latitude, req.current_longitude, event.latitude, event.longitude
            )
        else:
            # Fallback calculation
            dist_meters = 800.0

        # Calculate initial ETA seconds
        eta_seconds = int((req.eta_minutes * 60) if req.eta_minutes else (dist_meters / 5.55))

        # Check existing response record
        existing_resp_stmt = select(SOSResponse).where(
            SOSResponse.sos_event_id == sos_id,
            SOSResponse.responder_id == responder.id,
        )
        existing_res = await db.execute(existing_resp_stmt)
        response_record = existing_res.scalars().first()

        if response_record:
            response_record.status = req.status
            if req.status == "ARRIVED":
                response_record.arrived_at = datetime.now(UTC)
        else:
            # Compute score
            ranked_item = ranking_service.score_responder(
                user=responder,
                distance_meters=dist_meters,
                max_radius_meters=event.max_radius_meters,
                required_skills=event.required_skills,
            )

            response_record = SOSResponse(
                id=uuid.uuid4(),
                sos_event_id=sos_id,
                responder_id=responder.id,
                status=req.status,
                initial_distance_meters=round(dist_meters, 1),
                initial_eta_seconds=eta_seconds,
                ranking_score=ranked_item.total_ranking_score,
                joined_at=datetime.now(UTC),
                arrived_at=datetime.now(UTC) if req.status == "ARRIVED" else None,
            )
            db.add(response_record)

        # 3. Transition event status to RESPONDER_ACCEPTED if first responder
        if req.status in ("ACCEPTED", "EN_ROUTE") and event.status in ("SOS_TRIGGERED", "AI_TRIAGING"):
            event.status = "RESPONDER_ACCEPTED"

        # 4. Log Timeline Event: "response_accepted"
        timeline_entry = TimelineEvent(
            id=uuid.uuid4(),
            sos_event_id=sos_id,
            actor_id=responder.id,
            event_type="response_accepted" if req.status != "ARRIVED" else "responder_arrived",
            details={
                "responder_name": responder.name or "Volunteer",
                "status": req.status,
                "initial_distance_meters": dist_meters,
                "eta_minutes": req.eta_minutes or round(eta_seconds / 60.0, 1),
            },
        )
        db.add(timeline_entry)

        await db.commit()
        await db.refresh(response_record)

        # 5. Notify broadcaster if not anonymous
        if event.broadcaster_id:
            try:
                await notification_service.send_responder_arrival_notification(
                    victim_id=event.broadcaster_id,
                    sos_event_id=sos_id,
                    responder_name=responder.name or "A verified volunteer",
                    eta_str=f"{round(eta_seconds / 60.0, 1)} min",
                )
            except Exception as e:
                logger.debug(f"Victim notification delivery log: {e}")

        return SOSResponseItem(
            id=response_record.id,
            sos_event_id=response_record.sos_event_id,
            responder_id=response_record.responder_id,
            responder_name=responder.name or f"Volunteer #{str(responder.id)[:6]}",
            responder_trust_score=float(responder.trust_score),
            responder_skills=cls._extract_skill_names(responder.skills),
            status=response_record.status,
            initial_distance_meters=response_record.initial_distance_meters,
            initial_eta_seconds=response_record.initial_eta_seconds,
            ranking_score=response_record.ranking_score,
            joined_at=response_record.joined_at,
            arrived_at=response_record.arrived_at,
        )

    @classmethod
    async def escalate_sos(
        cls,
        db: AsyncSession,
        sos_id: uuid.UUID,
        req: SOSEscalateRequest,
    ) -> SOSEscalationStatus:
        """Evaluate and apply 3-Layer Escalation (radius expansion, 108/112 auto-call, AI fallback)."""
        stmt = select(SOSEvent).where(SOSEvent.id == sos_id)
        res = await db.execute(stmt)
        event = res.scalars().first()

        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="SOS emergency event not found.",
            )

        if event.status in ("RESOLVED", "CANCELLED"):
            return cls._build_escalation_status(event)

        elapsed = req.elapsed_seconds if req.elapsed_seconds is not None else 35
        old_radius = event.current_radius_meters
        new_radius = old_radius
        old_layer = event.escalation_layer
        new_layer = old_layer

        # Layer 1: Auto-radius expansion gates (30s: 2x, 45s: 3x)
        if 30 <= elapsed < 60:
            if elapsed < 45:
                # 2x radius expansion
                new_radius = min(float(event.max_radius_meters), old_radius * 2.0)
            else:
                # 3x radius expansion
                new_radius = min(float(event.max_radius_meters), old_radius * 3.0)
            new_layer = 1
        elif elapsed >= 60 or req.force_layer == 2:
            # Layer 2: Municipal Ambulance Gateway Auto-Call
            new_layer = 2
            event.auto_call_108_triggered = True
            new_radius = float(event.max_radius_meters)

        if req.force_layer == 3:
            new_layer = 3

        event.current_radius_meters = new_radius
        event.escalation_layer = new_layer

        # Log timeline expansion if radius changed
        if new_radius > old_radius:
            timeline_exp = TimelineEvent(
                id=uuid.uuid4(),
                sos_event_id=sos_id,
                actor_id=None,
                event_type="radius_expanded",
                details={
                    "old_radius_meters": old_radius,
                    "new_radius_meters": new_radius,
                    "elapsed_seconds": elapsed,
                },
            )
            db.add(timeline_exp)

        # Log timeline layer transition
        if new_layer != old_layer:
            timeline_layer = TimelineEvent(
                id=uuid.uuid4(),
                sos_event_id=sos_id,
                actor_id=None,
                event_type="escalation_layer_updated",
                details={
                    "layer": new_layer,
                    "reason": f"Elapsed {elapsed}s without on-scene volunteer response",
                },
            )
            db.add(timeline_layer)

        await db.commit()
        await db.refresh(event)

        # If radius expanded, notify newly covered responders
        if new_radius > old_radius:
            candidates = await geo_service.find_nearby_active_responders(
                db=db,
                latitude=event.latitude,
                longitude=event.longitude,
                radius_meters=new_radius,
                exclude_user_id=event.broadcaster_id,
            )
            ranked_new = ranking_service.rank_responders(
                candidates=candidates,
                max_radius_meters=event.max_radius_meters,
                required_skills=event.required_skills,
                severity_level=event.severity_level,
            )
            for resp in ranked_new:
                try:
                    await notification_service.send_emergency_dispatch_alert(
                        user_id=resp.responder_id,
                        sos_event_id=sos_id,
                        crisis_type=event.crisis_type,
                        severity=event.priority,
                        distance_str=f"{int(resp.distance_meters)}m",
                        eta_str=f"{resp.eta_minutes} min",
                        required_skills=event.required_skills,
                    )
                except Exception:
                    pass

        return cls._build_escalation_status(event, elapsed_seconds=elapsed)

    @classmethod
    async def resolve_sos(
        cls,
        db: AsyncSession,
        actor: User,
        sos_id: uuid.UUID,
        req: SOSResolveRequest,
    ) -> SOSResolveResponse:
        """Mark an SOS emergency resolved, recording post-incident feedback and updating reputation scores."""
        stmt = select(SOSEvent).where(SOSEvent.id == sos_id)
        res = await db.execute(stmt)
        event = res.scalars().first()

        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="SOS emergency event not found.",
            )

        event.status = "RESOLVED"
        event.resolved_at = datetime.now(UTC)

        # Fetch active responders to apply reputation updates
        resp_stmt = select(SOSResponse).where(
            SOSResponse.sos_event_id == sos_id,
            SOSResponse.status.in_(["ACCEPTED", "ARRIVED", "EN_ROUTE"]),
        )
        resp_res = await db.execute(resp_stmt)
        responses = resp_res.scalars().all()

        reputation_updates = []
        for r in responses:
            r.feedback_score = req.feedback_score or 5.0
            r.feedback_notes = req.resolution_notes

            # Reward responding volunteer: +3 for successful response, +2 for high feedback
            user_stmt = select(User).where(User.id == r.responder_id)
            user_res = await db.execute(user_stmt)
            responder_user = user_res.scalars().first()

            if responder_user:
                score_delta = 3.0
                if (req.feedback_score or 5.0) >= 4.0:
                    score_delta += 2.0
                old_score = responder_user.trust_score
                responder_user.trust_score = min(100.0, old_score + score_delta)
                reputation_updates.append({
                    "responder_id": str(responder_user.id),
                    "responder_name": responder_user.name,
                    "old_trust_score": old_score,
                    "new_trust_score": responder_user.trust_score,
                    "delta": score_delta,
                })

        # Insert Timeline Event: "sos_resolved"
        timeline_res = TimelineEvent(
            id=uuid.uuid4(),
            sos_event_id=sos_id,
            actor_id=actor.id,
            event_type="sos_resolved",
            details={
                "resolved_by": req.resolved_by,
                "notes": req.resolution_notes,
                "feedback_score": req.feedback_score,
            },
        )
        db.add(timeline_res)

        await db.commit()
        await db.refresh(event)

        return SOSResolveResponse(
            id=event.id,
            status="RESOLVED",
            resolved_at=event.resolved_at,
            message="Emergency successfully resolved. Responder trust scores updated.",
            reputation_updates=reputation_updates,
        )

    @classmethod
    async def get_active_events(
        cls,
        db: AsyncSession,
        user: User | None = None,
        latitude: float | None = None,
        longitude: float | None = None,
        limit: int = 50,
    ) -> SOSActiveListResponse:
        """Fetch list of all currently active SOS emergency broadcasts."""
        stmt = (
            select(SOSEvent)
            .where(SOSEvent.status.not_in(["RESOLVED", "CANCELLED"]))
            .order_by(desc(SOSEvent.created_at))
            .limit(limit)
        )
        res = await db.execute(stmt)
        events = res.scalars().all()

        active_items: list[SOSActiveListItem] = []
        for ev in events:
            dist = None
            if latitude is not None and longitude is not None:
                dist = round(geo_service.calculate_distance(latitude, longitude, ev.latitude, ev.longitude), 1)

            # Count responses
            cnt_stmt = select(SOSResponse).where(SOSResponse.sos_event_id == ev.id)
            cnt_res = await db.execute(cnt_stmt)
            resp_count = len(cnt_res.scalars().all())

            active_items.append(
                SOSActiveListItem(
                    id=ev.id,
                    crisis_type=ev.crisis_type,
                    sub_type=ev.sub_type,
                    severity_score=ev.severity_score,
                    severity_level=ev.severity_level,
                    priority=ev.priority,
                    status=ev.status,
                    latitude=ev.latitude,
                    longitude=ev.longitude,
                    address=ev.address,
                    distance_meters=dist,
                    responders_count=resp_count,
                    is_anonymous=ev.is_anonymous,
                    created_at=ev.created_at,
                )
            )

        return SOSActiveListResponse(active_events=active_items, total_count=len(active_items))


sos_service = SOSService()
