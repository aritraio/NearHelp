/* ==========================================================================
   NearHelp AI — Phase 1 Design System & Architecture Showcase
   File: src/components/demo/Phase1Showcase.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { soundEngine } from '../../utils/audio';
import { 
  Activity, 
  Heart, 
  Zap, 
  Radio, 
  MapPin, 
  Volume2, 
  VolumeX, 
  Sparkles, 
  Server,
  ArrowRight,
  Database,
  Cpu,
  Layers
} from 'lucide-react';

export const Phase1Showcase: React.FC = () => {
  const {
    currentScenario,
    incidentStatus,
    searchRadiusKm,
    cprMetronomeActive,
    cprBeatTick,
    telemetry,
    audioMuted,
    triggerSos,
    cancelSos,
    advanceStep,
    toggleCprMetronome,
    toggleAudioMute,
    setIncidentStatus,
  } = useDemoStore();

  const lifecycleStages = [
    { key: 'IDLE', label: '1. Idle Standby' },
    { key: 'SOS_TRIGGERED', label: '2. SOS Trigger' },
    { key: 'AI_TRIAGING', label: '3. Multimodal Triage' },
    { key: 'AI_TRIAGED', label: '4. Level 1-5 Score' },
    { key: 'SEARCHING_RESPONDERS', label: '5. PostGIS Dispatch' },
    { key: 'RESPONDER_ACCEPTED', label: '6. Responder Accept' },
    { key: 'RESPONDER_EN_ROUTE', label: '7. Live GPS Nav' },
    { key: 'RESPONDER_ARRIVED', label: '8. On Scene' },
    { key: 'HANDOVER_108', label: '9. 108 Handover' },
    { key: 'RESOLVED', label: '10. Resolved' },
  ];

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      gap: '24px',
      padding: '24px',
      maxWidth: '1360px',
      margin: '0 auto',
      width: '100%'
    }}>
      {/* Hero Banner / Architecture Status */}
      <div className="glass-panel-elevated" style={{
        padding: '24px',
        borderRadius: 'var(--radius-xl)',
        background: 'linear-gradient(135deg, rgba(26, 27, 30, 0.95) 0%, rgba(18, 18, 20, 0.98) 100%)',
        border: '1px solid var(--border-medium)',
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '20px'
      }}>
        <div style={{ maxWidth: '680px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <span style={{
              fontSize: '11px',
              fontWeight: 800,
              padding: '3px 8px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'rgba(255, 23, 68, 0.15)',
              color: 'var(--color-emergency-red-bright)',
              border: '1px solid var(--border-emergency)',
              letterSpacing: '0.05em'
            }}>
              PHASE 1 COMPLETE • WED NIGHT
            </span>
            <span style={{
              fontSize: '11px',
              fontWeight: 700,
              padding: '3px 8px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'rgba(0, 229, 255, 0.12)',
              color: 'var(--color-ai-cyan)',
              border: '1px solid var(--border-ai)',
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}>
              <Sparkles size={11} />
              SHOWCASE DESIGN SYSTEM &amp; TOKENS
            </span>
          </div>

          <h1 style={{
            fontSize: '28px',
            fontWeight: 800,
            letterSpacing: '-0.03em',
            marginBottom: '6px',
            color: 'var(--text-primary)'
          }}>
            NearHelp AI Design System &amp; Simulation Engine
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px', lineHeight: 1.6 }}>
            High-contrast dark theme tokens, zero-dependency Web Audio synthesizers (110 BPM CPR metronome), 
            Kolkata emergency scenarios (Salt Lake Sector V &amp; EM Bypass), and deterministic state store.
          </p>
        </div>

        {/* Live Telemetry Pill Cluster */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(2, 1fr)',
          gap: '10px',
          minWidth: '280px'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '10px 14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Database size={11} />
              <span>PostGIS Spatial SLA</span>
            </div>
            <div className="font-mono" style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-safe-green-bright)' }}>
              {telemetry.spatialQueryLatencyMs}ms
            </div>
          </div>

          <div style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '10px 14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Cpu size={11} />
              <span>RAG Accuracy</span>
            </div>
            <div className="font-mono" style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-ai-cyan)' }}>
              {telemetry.ragAccuracyScore}%
            </div>
          </div>

          <div style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '10px 14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Server size={11} />
              <span>Active Bystanders</span>
            </div>
            <div className="font-mono" style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-action-amber-bright)' }}>
              {telemetry.availableVolunteersCount}
            </div>
          </div>

          <div style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '10px 14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Radio size={11} />
              <span>WS Telemetry Stream</span>
            </div>
            <div className="font-mono" style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary)' }}>
              {telemetry.websocketConnectionsCount} live
            </div>
          </div>
        </div>
      </div>

      {/* Grid: Color Tokens + Animation Utilities + Audio Cues */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
        gap: '20px'
      }}>
        {/* Card 1: High-Contrast Color Tokens */}
        <div className="glass-panel" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
            <Layers size={16} color="var(--color-ai-cyan)" />
            <h3 style={{ fontSize: '15px', fontWeight: 700 }}>High-Contrast Theme Tokens</h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '8px 12px',
              backgroundColor: 'var(--color-emergency-red-subtle)',
              border: '1px solid var(--border-emergency)',
              borderRadius: 'var(--radius-sm)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ width: '18px', height: '18px', borderRadius: '4px', backgroundColor: 'var(--color-emergency-red-bright)' }} />
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-emergency-red-bright)' }}>Emergency Red</div>
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>#E53935 • Level 5 SOS &amp; Cardiac</div>
                </div>
              </div>
              <span className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>--color-emergency-red</span>
            </div>

            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '8px 12px',
              backgroundColor: 'var(--color-action-amber-subtle)',
              border: '1px solid rgba(255, 152, 0, 0.3)',
              borderRadius: 'var(--radius-sm)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ width: '18px', height: '18px', borderRadius: '4px', backgroundColor: 'var(--color-action-amber-bright)' }} />
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-action-amber-bright)' }}>Action Amber</div>
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>#FF9800 • Level 3 &amp; Pending</div>
                </div>
              </div>
              <span className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>--color-action-amber</span>
            </div>

            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '8px 12px',
              backgroundColor: 'var(--color-safe-green-subtle)',
              border: '1px solid var(--border-safe)',
              borderRadius: 'var(--radius-sm)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ width: '18px', height: '18px', borderRadius: '4px', backgroundColor: 'var(--color-safe-green-bright)' }} />
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-safe-green-bright)' }}>Safe Green</div>
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>#4CAF50 • Verified Responders</div>
                </div>
              </div>
              <span className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>--color-safe-green</span>
            </div>

            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '8px 12px',
              backgroundColor: 'var(--color-ai-subtle)',
              border: '1px solid var(--border-ai)',
              borderRadius: 'var(--radius-sm)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ width: '18px', height: '18px', borderRadius: '4px', backgroundColor: 'var(--color-ai-cyan)' }} />
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-ai-cyan)' }}>AI Cyan / Blue</div>
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>#00E5FF • Gemini Triage &amp; RAG</div>
                </div>
              </div>
              <span className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>--color-ai-cyan</span>
            </div>
          </div>
        </div>

        {/* Card 2: Interactive Animations & Pulse Showcase */}
        <div className="glass-panel" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
            <Activity size={16} color="var(--color-emergency-red-bright)" />
            <h3 style={{ fontSize: '15px', fontWeight: 700 }}>Radar, Pulse &amp; Waveform Utilities</h3>
          </div>

          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '16px',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '210px',
            backgroundColor: 'var(--bg-surface)',
            borderRadius: 'var(--radius-md)',
            padding: '16px',
            border: '1px solid var(--border-subtle)',
            position: 'relative',
            overflow: 'hidden'
          }}>
            {/* SOS Trigger Button with breathing pulse */}
            <button
              onClick={incidentStatus === 'IDLE' ? triggerSos : cancelSos}
              className={incidentStatus !== 'IDLE' ? 'sos-breathing' : ''}
              style={{
                width: '90px',
                height: '90px',
                borderRadius: '50%',
                backgroundColor: incidentStatus === 'IDLE' ? 'var(--color-emergency-red)' : 'var(--color-emergency-red-bright)',
                color: '#ffffff',
                fontWeight: 800,
                fontSize: '20px',
                letterSpacing: '0.04em',
                boxShadow: 'var(--shadow-md)',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '2px',
                zIndex: 10
              }}
            >
              <Zap size={22} />
              <span>{incidentStatus === 'IDLE' ? 'SOS' : 'CANCEL'}</span>
            </button>

            {/* Audio Waveform Simulator */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', height: '36px' }}>
              {[18, 30, 12, 28, 34, 20, 32, 14, 24, 32, 16].map((h, i) => (
                <div
                  key={i}
                  style={{
                    width: '4px',
                    height: incidentStatus !== 'IDLE' ? `${h}px` : '6px',
                    backgroundColor: incidentStatus !== 'IDLE' ? 'var(--color-ai-cyan)' : 'var(--text-disabled)',
                    borderRadius: '2px',
                    transition: 'height 0.2s ease',
                    animation: incidentStatus !== 'IDLE' ? `audioWaveform ${0.6 + (i % 5) * 0.15}s infinite ease-in-out` : 'none'
                  }}
                />
              ))}
            </div>

            <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
              {incidentStatus === 'IDLE' ? 'Click SOS to initiate auto-progression pulse' : `Simulating: ${searchRadiusKm}km PostGIS GiST Search Wave`}
            </div>
          </div>
        </div>

        {/* Card 3: Web Audio Synthesizer & CPR Metronome */}
        <div className="glass-panel" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Heart size={16} color="var(--color-emergency-red-bright)" />
              <h3 style={{ fontSize: '15px', fontWeight: 700 }}>Web Audio Synthesizer</h3>
            </div>
            <button
              onClick={toggleAudioMute}
              style={{
                fontSize: '11px',
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
                color: audioMuted ? 'var(--color-action-amber)' : 'var(--color-safe-green-bright)'
              }}
            >
              {audioMuted ? <VolumeX size={13} /> : <Volume2 size={13} />}
              <span>{audioMuted ? 'Muted' : 'Audio On'}</span>
            </button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {/* Metronome Beat Box */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '12px 14px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'var(--bg-surface)',
              border: `1px solid ${cprMetronomeActive ? 'var(--color-emergency-red)' : 'var(--border-subtle)'}`
            }}>
              <div>
                <div style={{ fontSize: '13px', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <span>CPR Metronome (110 BPM)</span>
                  {cprMetronomeActive && (
                    <span className="font-mono" style={{ fontSize: '11px', color: 'var(--color-emergency-red-bright)' }}>
                      #{cprBeatTick}
                    </span>
                  )}
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>AHA/ERC guidelines cadence sound</div>
              </div>

              <button
                onClick={toggleCprMetronome}
                className={cprMetronomeActive ? 'cpr-beat-active' : ''}
                style={{
                  padding: '6px 14px',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '12px',
                  fontWeight: 700,
                  backgroundColor: cprMetronomeActive ? 'var(--color-emergency-red)' : 'var(--bg-surface-elevated)',
                  color: '#ffffff',
                  border: '1px solid var(--border-medium)'
                }}
              >
                {cprMetronomeActive ? 'STOP' : 'START'}
              </button>
            </div>

            {/* Individual Audio Tests */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
              <button
                onClick={() => soundEngine.playEmergencyAlert()}
                style={{
                  padding: '8px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '12px',
                  fontWeight: 600,
                  color: 'var(--color-emergency-red-bright)'
                }}
              >
                🚨 Alert Tone
              </button>

              <button
                onClick={() => soundEngine.playSuccessChime()}
                style={{
                  padding: '8px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '12px',
                  fontWeight: 600,
                  color: 'var(--color-safe-green-bright)'
                }}
              >
                ✨ Arrival Chime
              </button>

              <button
                onClick={() => soundEngine.playCprClick()}
                style={{
                  padding: '8px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '12px',
                  fontWeight: 600,
                  color: 'var(--color-action-amber-bright)'
                }}
              >
                🫀 CPR Click
              </button>

              <button
                onClick={() => soundEngine.playClick()}
                style={{
                  padding: '8px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '12px',
                  fontWeight: 600,
                  color: 'var(--text-secondary)'
                }}
              >
                🔘 Haptic Tap
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Scenario Breakdown Card */}
      <div className="glass-panel" style={{ padding: '22px' }}>
        <div style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '16px',
          gap: '10px'
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
              <MapPin size={15} color="var(--color-emergency-red-bright)" />
              <h2 style={{ fontSize: '18px', fontWeight: 800 }}>{currentScenario.title}</h2>
              <span style={{
                fontSize: '11px',
                fontWeight: 700,
                padding: '2px 8px',
                borderRadius: 'var(--radius-full)',
                backgroundColor: currentScenario.severity === 5 ? 'rgba(255, 23, 68, 0.2)' : 'rgba(255, 152, 0, 0.2)',
                color: currentScenario.severity === 5 ? 'var(--color-emergency-red-bright)' : 'var(--color-action-amber-bright)',
                border: `1px solid ${currentScenario.severity === 5 ? 'var(--border-emergency)' : 'rgba(255,152,0,0.4)'}`
              }}>
                LEVEL {currentScenario.severity}
              </span>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
              {currentScenario.locationName} • Coordinates: [{currentScenario.coordinates[0]}, {currentScenario.coordinates[1]}]
            </p>
          </div>

          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <div style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              fontSize: '12px'
            }}>
              <span style={{ color: 'var(--text-muted)' }}>AI Confidence: </span>
              <span style={{ fontWeight: 700, color: 'var(--color-ai-cyan)' }}>{currentScenario.aiConfidence}%</span>
            </div>
            <div style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              fontSize: '12px'
            }}>
              <span style={{ color: 'var(--text-muted)' }}>Platinum Window: </span>
              <span style={{ fontWeight: 700, color: 'var(--color-emergency-red-bright)' }}>{currentScenario.survivalWindowMinutes} Mins</span>
            </div>
          </div>
        </div>

        {/* Symptoms & Victim Medical Profile */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
          gap: '16px'
        }}>
          {/* Symptoms */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '8px' }}>
              EXTRACTED CLINICAL SYMPTOMS
            </div>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              {currentScenario.reportedSymptoms.map((symptom, idx) => (
                <li key={idx} style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
                  <div style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--color-emergency-red)' }} />
                  <span>{symptom}</span>
                </li>
              ))}
            </ul>
          </div>

          {/* Encrypted Medical ID */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '8px' }}>
              ENCRYPTED MEDICAL ID PROFILE
            </div>
            <div style={{ fontSize: '13px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <div><strong>Victim:</strong> {currentScenario.victim.name} ({currentScenario.victim.age}y/o {currentScenario.victim.gender})</div>
              <div><strong>Blood Group:</strong> <span style={{ color: 'var(--color-emergency-red-bright)', fontWeight: 700 }}>{currentScenario.victim.bloodType}</span></div>
              <div><strong>Allergies:</strong> {currentScenario.victim.allergies.length > 0 ? currentScenario.victim.allergies.join(', ') : 'None Reported'}</div>
              <div><strong>Emergency Contact:</strong> {currentScenario.victim.emergencyContactName} ({currentScenario.victim.emergencyContactPhone})</div>
            </div>
          </div>

          {/* Responders */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '8px' }}>
              NEARBY RANKED RESPONDERS ({currentScenario.responders.length})
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {currentScenario.responders.map((resp) => (
                <div key={resp.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '12px' }}>
                  <div>
                    <div style={{ fontWeight: 700, color: 'var(--text-primary)' }}>{resp.name}</div>
                    <div style={{ color: 'var(--text-muted)' }}>{resp.role}</div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ color: 'var(--color-safe-green-bright)', fontWeight: 700 }}>{resp.distanceMeters}m ({resp.etaMinutes}m ETA)</div>
                    <div style={{ color: 'var(--text-muted)' }}>Trust: {resp.trustScore}%</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* State Machine Progression Flow */}
      <div className="glass-panel" style={{ padding: '22px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
          <div>
            <h3 style={{ fontSize: '16px', fontWeight: 800 }}>Deterministic State Machine Progression</h3>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              Simulates live T+0s to T+40s incident lifecycle with zero backend failure risk during viva defense.
            </p>
          </div>
          <button
            onClick={advanceStep}
            style={{
              padding: '6px 14px',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: 'var(--color-ai-subtle)',
              color: 'var(--color-ai-cyan)',
              border: '1px solid var(--border-ai)',
              fontSize: '12px',
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <span>Advance Next State</span>
            <ArrowRight size={14} />
          </button>
        </div>

        {/* Lifecycle Stage Steps */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(115px, 1fr))',
          gap: '8px'
        }}>
          {lifecycleStages.map((stage) => {
            const isActive = incidentStatus === stage.key;
            return (
              <button
                key={stage.key}
                onClick={() => setIncidentStatus(stage.key as any)}
                style={{
                  padding: '10px 8px',
                  borderRadius: 'var(--radius-md)',
                  backgroundColor: isActive ? 'var(--color-emergency-red)' : 'var(--bg-surface)',
                  color: isActive ? '#ffffff' : 'var(--text-muted)',
                  border: `1px solid ${isActive ? 'var(--color-emergency-red-bright)' : 'var(--border-subtle)'}`,
                  fontSize: '11px',
                  fontWeight: isActive ? 800 : 500,
                  textAlign: 'center',
                  transition: 'all var(--transition-fast)'
                }}
              >
                {stage.label}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
