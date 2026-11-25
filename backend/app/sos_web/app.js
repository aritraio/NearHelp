(() => {
  "use strict";

  const STORAGE_KEY = "nearhelp_web_sos_v1";
  const HOLD_DURATION_MS = 2000;
  const PROMPT_DURATION_MS = 5000;
  const GENERIC_MEDICAL_SOS = "Medical emergency. I need urgent medical help at my live location.";
  const TERMINAL_STATUSES = new Set(["RESOLVED", "CANCELLED"]);
  const ARRIVED_STATUSES = new Set(["ARRIVED", "ON_SCENE", "HANDOVER_108"]);

  const $ = (id) => document.getElementById(id);

  const els = {
    viewHome: $("view-home"),
    viewTracking: $("view-tracking"),
    homeGpsDot: $("home-gps-dot"),
    homeGpsBadge: $("home-gps-badge"),
    homeLocTitle: $("home-loc-title"),
    homeLocCoords: $("home-loc-coords"),
    locSummaryRow: $("loc-summary-row"),
    locLabelDisplay: $("loc-label-display"),
    btnEditLoc: $("btn-edit-loc"),
    locEditPanel: $("loc-edit-panel"),
    landmarkInput: $("landmark-input"),
    btnSaveLoc: $("btn-save-loc"),
    btnCancelLoc: $("btn-cancel-loc"),
    btnAllowLoc: $("btn-allow-loc"),
    sosHoldBtn: $("sos-hold-btn"),
    sosHoldFill: $("sos-hold-fill"),
    sosHoldTitle: $("sos-hold-title"),
    sosHoldSubtitle: $("sos-hold-subtitle"),
    sosHoldBar: $("sos-hold-bar"),
    homeHint: $("home-hint"),

    trackingTopbarTitle: $("tracking-topbar-title"),
    trackingMapBadge: $("tracking-map-badge"),
    trackingLocTitle: $("tracking-loc-title"),
    trackingLocCoords: $("tracking-loc-coords"),
    trackingProgressBar: $("tracking-progress-bar"),
    trackingErrorBox: $("tracking-error-box"),
    trackingErrorTitle: $("tracking-error-title"),
    trackingErrorDetail: $("tracking-error-detail"),
    btnRetryAction: $("btn-retry-action"),

    statePending: $("state-pending"),
    pendingHeading: $("pending-heading"),
    pendingDescription: $("pending-description"),
    pendingGpsNote: $("pending-gps-note"),
    btnPendingGps: $("btn-pending-gps"),
    pendingRetryNote: $("pending-retry-note"),

    stateActive: $("state-active"),
    activePhaseTitle: $("active-phase-title"),
    activePhaseSubtitle: $("active-phase-subtitle"),
    activeRequestText: $("active-request-text"),
    escalationNote: $("escalation-note"),
    escalationTitle: $("escalation-title"),
    escalationDetail: $("escalation-detail"),
    escalationCallLink: $("escalation-call-link"),
    respondersList: $("responders-list"),

    stateEnded: $("state-ended"),
    endedTitle: $("ended-title"),
    endedSubtitle: $("ended-subtitle"),
    btnBackHome: $("btn-back-home"),
    trackingFooter: $("tracking-footer"),
    btnCancelSos: $("btn-cancel-sos"),

    promptDialog: $("prompt-dialog"),
    promptCountdownText: $("prompt-countdown-text"),
    promptProgressBar: $("prompt-progress-bar"),
    symptomGrid: $("symptom-grid"),
    promptDraft: $("prompt-draft"),
    btnSendSosNow: $("btn-send-sos-now"),
    btnPromptCancel: $("btn-prompt-cancel"),

    cancelDialog: $("cancel-dialog"),
    btnKeepSos: $("btn-keep-sos"),
    btnConfirmCancel: $("btn-confirm-cancel"),
  };

  const state = {
    location: {
      hasFix: false,
      latitude: null,
      longitude: null,
      accuracy: null,
      error: null,
      denied: false,
    },
    landmark: "",
    incidentId: null,
    idempotencyKey: null,
    cancelToken: null,
    pendingDescription: null,
    submission: "NONE", // NONE | CHOOSING | WAITING_FOR_LOCATION | SENDING | FAILED | SENT
    cancellationPending: false,
    busy: false,
    error: null,
    incident: null,
    ws: null,
    pollTimer: null,
    escalationTimer: null,
    lastEscalationGate: 0,
    holdRaf: null,
    holdStart: 0,
    isHolding: false,
    promptTimer: null,
    promptDeadline: 0,
    watchId: null,
  };

  function generateUuid() {
    if (window.crypto && typeof window.crypto.randomUUID === "function") {
      return window.crypto.randomUUID();
    }
    const bytes = new Uint8Array(16);
    if (window.crypto && typeof window.crypto.getRandomValues === "function") {
      window.crypto.getRandomValues(bytes);
    } else {
      for (let i = 0; i < 16; i += 1) bytes[i] = Math.floor(Math.random() * 256);
    }
    bytes[6] = (bytes[6] & 0x0f) | 0x40;
    bytes[8] = (bytes[8] & 0x3f) | 0x80;
    const hex = Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
    return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
  }

  function loadPersisted() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return;
      const saved = JSON.parse(raw);
      if (saved && typeof saved === "object") {
        state.landmark = typeof saved.landmark === "string" ? saved.landmark : "";
        state.incidentId = typeof saved.incidentId === "string" ? saved.incidentId : null;
        state.idempotencyKey = typeof saved.idempotencyKey === "string" ? saved.idempotencyKey : null;
        state.cancelToken = typeof saved.cancelToken === "string" ? saved.cancelToken : null;
        state.pendingDescription = typeof saved.pendingDescription === "string" ? saved.pendingDescription : null;
        if (state.incidentId) {
          state.submission = "SENT";
        } else if (state.pendingDescription && state.idempotencyKey) {
          state.submission = "FAILED";
        }
      }
    } catch {
      // Ignore corrupted local storage
    }
  }

  function persist() {
    try {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          landmark: state.landmark,
          incidentId: state.incidentId,
          idempotencyKey: state.idempotencyKey,
          cancelToken: state.cancelToken,
          pendingDescription: state.pendingDescription,
        })
      );
    } catch {
      // Ignore storage quota / private mode issues
    }
  }

  function clearActiveSosState() {
    stopLiveTracking();
    state.incidentId = null;
    state.idempotencyKey = null;
    state.cancelToken = null;
    state.pendingDescription = null;
    state.submission = "NONE";
    state.cancellationPending = false;
    state.busy = false;
    state.error = null;
    state.incident = null;
    state.lastEscalationGate = 0;
    persist();
  }

  function formatCoords(lat, lon, accuracy) {
    if (typeof lat !== "number" || typeof lon !== "number") return "";
    const acc = typeof accuracy === "number" ? ` · ±${Math.round(accuracy)}m` : "";
    return `${lat.toFixed(5)}, ${lon.toFixed(5)}${acc}`;
  }

  function requestLocation(forceFresh = false) {
    if (!("geolocation" in navigator)) {
      state.location.error = "Location is not supported by this browser.";
      state.location.denied = true;
      render();
      return;
    }

    const opts = {
      enableHighAccuracy: true,
      timeout: 12000,
      maximumAge: forceFresh ? 0 : 15000,
    };

    const onPos = (pos) => {
      state.location.hasFix = true;
      state.location.latitude = pos.coords.latitude;
      state.location.longitude = pos.coords.longitude;
      state.location.accuracy = pos.coords.accuracy;
      state.location.error = null;
      state.location.denied = false;
      render();

      if (state.submission === "WAITING_FOR_LOCATION") {
        dispatchSos();
      }
    };

    const onErr = (err) => {
      if (err && err.code === 1) {
        state.location.denied = true;
        state.location.error = "Location permission denied. Allow GPS access in your browser.";
      } else if (err && err.code === 3) {
        state.location.error = "GPS timed out. Tap to retry location.";
      } else {
        state.location.error = "Waiting for GPS signal…";
      }
      render();
    };

    navigator.geolocation.getCurrentPosition(onPos, onErr, opts);
    if (state.watchId === null) {
      state.watchId = navigator.geolocation.watchPosition(onPos, onErr, {
        enableHighAccuracy: true,
        maximumAge: 10000,
        timeout: 15000,
      });
    }
  }

  // 2-second hold gesture matching CareSosHoldButton
  function resetHoldVisuals() {
    els.sosHoldBtn.classList.remove("holding");
    els.sosHoldFill.style.width = "0%";
    els.sosHoldBar.style.width = "0%";
    els.sosHoldTitle.textContent = "SOS";
    els.sosHoldSubtitle.textContent = "Hold for 2 seconds to trigger it";
  }

  function startHold(event) {
    if (state.isHolding || state.incidentId || state.submission !== "NONE") return;
    if (event.type === "pointerdown" && event.pointerType === "mouse" && event.button !== 0) return;

    state.isHolding = true;
    state.holdStart = performance.now();
    els.homeHint.hidden = true;
    els.sosHoldBtn.classList.add("holding");
    if (navigator.vibrate) navigator.vibrate(25);

    const tick = (now) => {
      if (!state.isHolding) return;
      const elapsed = now - state.holdStart;
      const progress = Math.min(1, elapsed / HOLD_DURATION_MS);
      const remaining = Math.max(0, (HOLD_DURATION_MS - elapsed) / 1000);
      const pct = `${(progress * 100).toFixed(2)}%`;

      els.sosHoldFill.style.width = pct;
      els.sosHoldBar.style.width = pct;
      els.sosHoldSubtitle.textContent = `Keep holding · ${remaining.toFixed(1)}s`;

      if (progress >= 1) {
        state.isHolding = false;
        state.holdRaf = null;
        if (navigator.vibrate) navigator.vibrate([50, 40, 80]);
        resetHoldVisuals();
        openPromptDialog();
        return;
      }
      state.holdRaf = requestAnimationFrame(tick);
    };

    state.holdRaf = requestAnimationFrame(tick);
  }

  function cancelHold(showHint) {
    if (!state.isHolding) return;
    const elapsed = performance.now() - state.holdStart;
    state.isHolding = false;
    if (state.holdRaf !== null) {
      cancelAnimationFrame(state.holdRaf);
      state.holdRaf = null;
    }
    resetHoldVisuals();

    if (showHint && elapsed < HOLD_DURATION_MS) {
      els.homeHint.textContent = "Hold the SOS button for 2 seconds to trigger an emergency alert.";
      els.homeHint.hidden = false;
    }
  }

  // 5-second quick triage dialog matching MedicalEmergencyDialog
  function openPromptDialog() {
    state.submission = "CHOOSING";
    els.promptDraft.value = "";
    state.promptDeadline = Date.now() + PROMPT_DURATION_MS;
    updatePromptCountdown();

    if (typeof els.promptDialog.showModal === "function") {
      if (!els.promptDialog.open) els.promptDialog.showModal();
    } else {
      els.promptDialog.setAttribute("open", "");
    }

    clearInterval(state.promptTimer);
    state.promptTimer = setInterval(updatePromptCountdown, 100);
  }

  function closePromptDialog() {
    clearInterval(state.promptTimer);
    state.promptTimer = null;
    if (els.promptDialog.open && typeof els.promptDialog.close === "function") {
      els.promptDialog.close();
    } else {
      els.promptDialog.removeAttribute("open");
    }
  }

  function updatePromptCountdown() {
    const remainingMs = Math.max(0, state.promptDeadline - Date.now());
    const secs = Math.ceil(remainingMs / 1000);
    const ratio = remainingMs / PROMPT_DURATION_MS;

    els.promptCountdownText.textContent = `${secs}s until a general medical SOS`;
    els.promptProgressBar.style.transform = `scaleX(${ratio.toFixed(3)})`;

    if (remainingMs <= 0) {
      submitPrompt("");
    }
  }

  function submitPrompt(rawText) {
    if (state.submission !== "CHOOSING") return;
    closePromptDialog();
    const cleaned = (rawText || "").trim().slice(0, 180);
    const description = cleaned ? `Medical emergency: ${cleaned}` : GENERIC_MEDICAL_SOS;
    state.pendingDescription = description;
    state.idempotencyKey = state.idempotencyKey || generateUuid();
    state.error = null;
    persist();

    if (state.location.hasFix) {
      dispatchSos();
    } else {
      state.submission = "WAITING_FOR_LOCATION";
      requestLocation(true);
      render();
    }
  }

  function cancelPrompt() {
    closePromptDialog();
    clearActiveSosState();
    render();
  }

  async function dispatchSos() {
    if (state.busy || state.incidentId) return;
    if (!state.location.hasFix) {
      state.submission = "WAITING_FOR_LOCATION";
      render();
      return;
    }

    state.submission = "SENDING";
    state.busy = true;
    state.error = null;
    render();

    const payload = {
      latitude: state.location.latitude,
      longitude: state.location.longitude,
      crisis_type: "medical",
      description: state.pendingDescription || GENERIC_MEDICAL_SOS,
      address: state.landmark.trim() || formatCoords(state.location.latitude, state.location.longitude),
      is_anonymous: true,
    };

    try {
      const response = await fetch("/api/v1/sos/create", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Idempotency-Key": state.idempotencyKey || generateUuid(),
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errBody = await response.json().catch(() => ({}));
        throw new Error(errBody.detail || `Server responded with ${response.status}`);
      }

      const data = await response.json();
      state.incidentId = data.id;
      if (data.guest_cancel_token) {
        state.cancelToken = data.guest_cancel_token;
      }
      state.incident = data;
      state.submission = "SENT";
      state.busy = false;
      state.error = null;
      persist();
      render();
      startLiveTracking();
      await fetchIncidentDetails();
    } catch (err) {
      state.submission = "FAILED";
      state.busy = false;
      state.error = err instanceof Error ? err.message : "Network error while sending SOS.";
      persist();
      render();
    }
  }

  function startLiveTracking() {
    stopLiveTracking();
    if (!state.incidentId) return;

    connectWebSocket();
    state.pollTimer = setInterval(fetchIncidentDetails, 4000);
    state.escalationTimer = setInterval(evaluateEscalation, 5000);
  }

  function stopLiveTracking() {
    if (state.ws) {
      try {
        state.ws.close();
      } catch {
        // ignore
      }
      state.ws = null;
    }
    if (state.pollTimer) {
      clearInterval(state.pollTimer);
      state.pollTimer = null;
    }
    if (state.escalationTimer) {
      clearInterval(state.escalationTimer);
      state.escalationTimer = null;
    }
  }

  function connectWebSocket() {
    if (!state.incidentId) return;
    const proto = window.location.protocol === "https:" ? "wss:" : "ws:";
    const wsUrl = `${proto}//${window.location.host}/ws/tracking/${state.incidentId}`;

    try {
      const ws = new WebSocket(wsUrl);
      state.ws = ws;

      ws.onmessage = (event) => {
        try {
          const msg = JSON.parse(event.data);
          if (!msg || typeof msg !== "object") return;
          if (msg.type === "tracking_snapshot" && msg.status) {
            if (TERMINAL_STATUSES.has(msg.status)) {
              fetchIncidentDetails();
              return;
            }
          }
          fetchIncidentDetails();
        } catch {
          // ignore malformed frames
        }
      };

      ws.onclose = () => {
        if (state.ws === ws) state.ws = null;
      };
    } catch {
      // Polling handles updates when WebSocket cannot connect
    }
  }

  async function fetchIncidentDetails() {
    if (!state.incidentId) return;
    try {
      const res = await fetch(`/api/v1/sos/${state.incidentId}`, { cache: "no-store" });
      if (res.status === 404) {
        clearActiveSosState();
        render();
        return;
      }
      if (!res.ok) return;
      const detail = await res.json();
      state.incident = detail;
      state.error = null;
      if (TERMINAL_STATUSES.has(detail.status)) {
        stopLiveTracking();
      }
      render();
    } catch {
      // Keep polling silently on transient network drops
    }
  }

  async function evaluateEscalation() {
    if (!state.incidentId || !state.incident || TERMINAL_STATUSES.has(state.incident.status)) return;
    const createdMs = Date.parse(state.incident.created_at);
    if (Number.isNaN(createdMs)) return;
    const elapsedSec = Math.max(0, Math.floor((Date.now() - createdMs) / 1000));
    const gate = elapsedSec >= 60 ? 60 : elapsedSec >= 45 ? 45 : elapsedSec >= 30 ? 30 : 0;
    if (gate === 0 || gate <= state.lastEscalationGate) return;

    state.lastEscalationGate = gate;
    try {
      const res = await fetch(`/api/v1/sos/${state.incidentId}/escalate`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ elapsed_seconds: elapsedSec }),
      });
      if (res.ok) {
        const escalation = await res.json();
        state.incident.escalation = escalation;
        render();
      }
    } catch {
      // ignore transient escalation failures
    }
  }

  async function confirmCancelSos() {
    if (els.cancelDialog.open && typeof els.cancelDialog.close === "function") {
      els.cancelDialog.close();
    } else {
      els.cancelDialog.removeAttribute("open");
    }

    if (!state.incidentId) {
      clearActiveSosState();
      render();
      return;
    }

    state.cancellationPending = true;
    state.busy = true;
    state.error = null;
    render();

    const headers = {};
    if (state.cancelToken) {
      headers["X-SOS-Cancel-Token"] = state.cancelToken;
    }

    try {
      const res = await fetch(`/api/v1/sos/${state.incidentId}/cancel`, {
        method: "PUT",
        headers,
      });
      if (!res.ok) {
        const errBody = await res.json().catch(() => ({}));
        throw new Error(errBody.detail || `Could not cancel (${res.status})`);
      }
      stopLiveTracking();
      state.cancellationPending = false;
      state.busy = false;
      state.error = null;
      if (state.incident) {
        state.incident.status = "CANCELLED";
      } else {
        state.incident = { status: "CANCELLED", address: state.landmark };
      }
      persist();
      render();
    } catch (err) {
      state.busy = false;
      state.error = err instanceof Error ? err.message : "Could not confirm cancellation.";
      render();
    }
  }

  function getRescuePresentation() {
    const incident = state.incident;
    if (!incident) {
      return { phase: "SEARCHING", progress: 0, responders: [] };
    }
    if (incident.status === "RESOLVED") {
      return { phase: "RESOLVED", progress: 3, responders: [] };
    }
    if (incident.status === "CANCELLED") {
      return { phase: "CANCELLED", progress: 3, responders: [] };
    }

    const engaged = (incident.responses || []).filter((r) =>
      ["ACCEPTED", "EN_ROUTE", "ARRIVED", "ON_SCENE", "HANDOVER_108"].includes(r.status)
    );
    if (engaged.some((r) => ARRIVED_STATUSES.has(r.status))) {
      return { phase: "ARRIVED", progress: 3, responders: engaged };
    }
    if (engaged.some((r) => r.status === "EN_ROUTE")) {
      return { phase: "EN_ROUTE", progress: 2, responders: engaged };
    }
    if (engaged.length > 0) {
      return { phase: "ACCEPTED", progress: 1, responders: engaged };
    }
    return { phase: "SEARCHING", progress: 0, responders: [] };
  }

  function renderResponders(responders) {
    els.respondersList.replaceChildren();
    responders.forEach((r) => {
      const row = document.createElement("div");
      row.className = "responder-row";

      const info = document.createElement("div");
      const nameEl = document.createElement("strong");
      nameEl.textContent = r.responder_name || "Nearby responder";

      const statusEl = document.createElement("span");
      statusEl.className = "care-muted-sm";
      if (ARRIVED_STATUSES.has(r.status)) {
        statusEl.textContent = "Arrived";
      } else if (typeof r.initial_eta_seconds === "number" && r.initial_eta_seconds > 0) {
        const mins = Math.max(1, Math.ceil(r.initial_eta_seconds / 60));
        statusEl.textContent = `Estimated arrival: ${mins} min`;
      } else {
        statusEl.textContent = r.status === "EN_ROUTE" ? "On the way" : "Accepted";
      }

      info.appendChild(nameEl);
      info.appendChild(statusEl);
      row.appendChild(info);
      els.respondersList.appendChild(row);
    });
  }

  function render() {
    const inTracking = Boolean(state.incidentId) || state.submission !== "NONE";
    els.viewHome.hidden = inTracking;
    els.viewTracking.hidden = !inTracking;

    // Home location display
    if (state.location.hasFix) {
      els.homeGpsDot.className = "care-pin-dot live";
      els.homeGpsBadge.textContent = "Live location";
      els.homeLocTitle.textContent = state.landmark.trim() || "GPS ready";
      els.homeLocCoords.textContent = formatCoords(
        state.location.latitude,
        state.location.longitude,
        state.location.accuracy
      );
      els.btnAllowLoc.hidden = true;
    } else if (state.location.denied) {
      els.homeGpsDot.className = "care-pin-dot error";
      els.homeGpsBadge.textContent = "Location access needed";
      els.homeLocTitle.textContent = "Enable browser location";
      els.homeLocCoords.textContent = state.location.error || "Required to send your live coordinates";
      els.btnAllowLoc.hidden = false;
    } else {
      els.homeGpsDot.className = "care-pin-dot";
      els.homeGpsBadge.textContent = "Waiting for GPS";
      els.homeLocTitle.textContent = "Locating your position…";
      els.homeLocCoords.textContent = state.location.error || "Allow GPS access so responders can find you";
      els.btnAllowLoc.hidden = false;
    }

    els.locLabelDisplay.textContent = state.landmark.trim() || "Current GPS location";

    if (!inTracking) return;

    // Tracking view
    els.trackingProgressBar.hidden = !state.busy;

    const pres = getRescuePresentation();
    const ended = pres.phase === "RESOLVED" || pres.phase === "CANCELLED";

    const lat = state.incident ? state.incident.latitude : state.location.latitude;
    const lon = state.incident ? state.incident.longitude : state.location.longitude;
    const addr = (state.incident && state.incident.address) || state.landmark.trim();
    els.trackingLocTitle.textContent = addr || "Live GPS coordinates";
    els.trackingLocCoords.textContent =
      typeof lat === "number" && typeof lon === "number" ? formatCoords(lat, lon) : "Waiting for GPS fix…";

    if (state.error) {
      els.trackingErrorBox.hidden = false;
      els.trackingErrorTitle.textContent = state.cancellationPending
        ? "Cancellation not confirmed"
        : "We couldn’t complete that action";
      els.trackingErrorDetail.textContent = state.error;
      els.btnRetryAction.textContent = state.cancellationPending
        ? "Retry cancellation"
        : state.submission === "FAILED"
          ? "Retry sending medical SOS"
          : "Refresh status";
    } else {
      els.trackingErrorBox.hidden = true;
    }

    els.statePending.hidden = true;
    els.stateActive.hidden = true;
    els.stateEnded.hidden = true;
    els.trackingFooter.hidden = ended;

    if (ended) {
      els.trackingTopbarTitle.textContent = "Your medical SOS";
      els.stateEnded.hidden = false;
      els.endedTitle.textContent =
        pres.phase === "RESOLVED" ? "Medical request resolved" : "Medical SOS cancelled";
      els.endedSubtitle.textContent = addr || "";
      return;
    }

    if (!state.incidentId) {
      els.statePending.hidden = false;
      els.trackingTopbarTitle.textContent = "Your medical SOS";
      if (state.cancellationPending) {
        els.pendingHeading.textContent = "Cancelling your SOS…";
      } else if (state.submission === "SENDING") {
        els.pendingHeading.textContent = "Sending medical SOS…";
      } else if (state.submission === "FAILED") {
        els.pendingHeading.textContent = "Sending not confirmed";
      } else {
        els.pendingHeading.textContent = "Waiting for your location";
      }
      els.pendingDescription.textContent = state.pendingDescription ? `Your request: ${state.pendingDescription}` : "";
      els.pendingGpsNote.hidden = state.submission !== "WAITING_FOR_LOCATION";
      els.pendingRetryNote.hidden = state.submission !== "FAILED";
      return;
    }

    // Confirmed active incident
    els.trackingTopbarTitle.textContent = "Your medical SOS";
    els.stateActive.hidden = false;

    document.querySelectorAll(".rescue-step").forEach((node) => {
      const idx = Number(node.getAttribute("data-step"));
      node.classList.toggle("active", idx <= pres.progress);
    });

    const primary = pres.responders[0];
    if (pres.phase === "SEARCHING") {
      els.activePhaseTitle.textContent = "Searching for nearby responders";
      els.activePhaseSubtitle.textContent =
        "Your medical SOS is active. Stay where responders can reach you.";
    } else if (pres.phase === "ACCEPTED") {
      els.activePhaseTitle.textContent = "Responder accepted";
      els.activePhaseSubtitle.textContent = primary ? primary.responder_name : "";
    } else if (pres.phase === "EN_ROUTE") {
      const mins =
        primary && typeof primary.initial_eta_seconds === "number" && primary.initial_eta_seconds > 0
          ? `${Math.max(1, Math.ceil(primary.initial_eta_seconds / 60))} min`
          : null;
      els.activePhaseTitle.textContent = mins ? `Arriving in ${mins}` : "Your responder is on the way";
      els.activePhaseSubtitle.textContent = primary ? primary.responder_name : "";
    } else if (pres.phase === "ARRIVED") {
      els.activePhaseTitle.textContent = "Your responder has arrived";
      els.activePhaseSubtitle.textContent = primary ? primary.responder_name : "";
    }

    if (state.incident.description) {
      els.activeRequestText.textContent = `Your request: ${state.incident.description}`;
      els.activeRequestText.hidden = false;
    } else {
      els.activeRequestText.hidden = true;
    }

    const esc = state.incident.escalation;
    if (esc && esc.auto_call_108_triggered && pres.phase === "SEARCHING") {
      const num = esc.recommended_emergency_number || "108";
      els.escalationNote.hidden = false;
      els.escalationTitle.textContent = `Call ${num} / 112 directly`;
      els.escalationDetail.textContent =
        esc.layer_description || "Municipal emergency gateway recommended while community dispatch expands.";
      els.escalationCallLink.href = `tel:${num}`;
      els.escalationCallLink.textContent = `Call ${num} now`;
    } else {
      els.escalationNote.hidden = true;
    }

    renderResponders(pres.responders);
    els.btnCancelSos.disabled = state.cancellationPending || (state.busy && state.submission === "SENDING");
  }

  function bindEvents() {
    els.btnAllowLoc.addEventListener("click", () => requestLocation(true));
    els.btnPendingGps.addEventListener("click", () => requestLocation(true));

    els.btnEditLoc.addEventListener("click", () => {
      els.landmarkInput.value = state.landmark;
      els.locSummaryRow.hidden = true;
      els.locEditPanel.hidden = false;
      els.landmarkInput.focus();
    });

    els.btnSaveLoc.addEventListener("click", () => {
      state.landmark = els.landmarkInput.value.trim();
      persist();
      els.locEditPanel.hidden = true;
      els.locSummaryRow.hidden = false;
      render();
    });

    els.btnCancelLoc.addEventListener("click", () => {
      els.locEditPanel.hidden = true;
      els.locSummaryRow.hidden = false;
    });

    // Pointer + keyboard hold on SOS button
    els.sosHoldBtn.addEventListener("pointerdown", startHold);
    els.sosHoldBtn.addEventListener("pointerup", () => cancelHold(true));
    els.sosHoldBtn.addEventListener("pointercancel", () => cancelHold(false));
    els.sosHoldBtn.addEventListener("pointerleave", () => cancelHold(false));
    els.sosHoldBtn.addEventListener("contextmenu", (e) => e.preventDefault());
    els.sosHoldBtn.addEventListener("keydown", (e) => {
      if ((e.key === " " || e.key === "Enter") && !e.repeat) {
        e.preventDefault();
        startHold(e);
      }
    });
    els.sosHoldBtn.addEventListener("keyup", (e) => {
      if (e.key === " " || e.key === "Enter") {
        e.preventDefault();
        cancelHold(true);
      }
    });

    // 5s prompt actions
    els.symptomGrid.addEventListener("click", (e) => {
      const btn = e.target.closest("button[data-symptom]");
      if (!btn) return;
      submitPrompt(btn.getAttribute("data-symptom") || "");
    });
    els.btnSendSosNow.addEventListener("click", () => submitPrompt(els.promptDraft.value));
    els.btnPromptCancel.addEventListener("click", cancelPrompt);
    els.promptDialog.addEventListener("cancel", (e) => {
      e.preventDefault();
      cancelPrompt();
    });

    // Tracking actions
    els.btnRetryAction.addEventListener("click", () => {
      if (state.cancellationPending) {
        confirmCancelSos();
      } else if (state.submission === "FAILED") {
        dispatchSos();
      } else {
        fetchIncidentDetails();
      }
    });

    els.btnCancelSos.addEventListener("click", () => {
      if (state.submission === "WAITING_FOR_LOCATION") {
        clearActiveSosState();
        render();
        return;
      }
      if (typeof els.cancelDialog.showModal === "function") {
        els.cancelDialog.showModal();
      } else {
        els.cancelDialog.setAttribute("open", "");
      }
    });

    els.btnKeepSos.addEventListener("click", () => {
      if (els.cancelDialog.open && typeof els.cancelDialog.close === "function") {
        els.cancelDialog.close();
      } else {
        els.cancelDialog.removeAttribute("open");
      }
    });
    els.btnConfirmCancel.addEventListener("click", confirmCancelSos);

    els.btnBackHome.addEventListener("click", () => {
      clearActiveSosState();
      render();
      requestLocation(false);
    });

    document.addEventListener("visibilitychange", () => {
      if (!document.hidden && state.incidentId) {
        fetchIncidentDetails();
        if (!state.ws) connectWebSocket();
      }
    });
  }

  function init() {
    loadPersisted();
    bindEvents();
    requestLocation(false);
    render();

    if (state.incidentId) {
      startLiveTracking();
      fetchIncidentDetails();
    }
  }

  init();
})();
