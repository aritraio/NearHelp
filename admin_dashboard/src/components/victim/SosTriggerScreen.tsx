/* ==========================================================================
   NearHelp AI — Screen 1: One-Tap SOS Trigger Screen (Panic-Resilient UX)
   File: src/components/victim/SosTriggerScreen.tsx
   ========================================================================== */

import React, { useState, useRef, useEffect } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import type { MultimodalInputMode, CrisisCategory } from '../../mock/types';
import { 
  MapPin, 
  ShieldAlert, 
  Flame, 
  Car, 
  HeartPulse, 
  Mic, 
  Camera, 
  Layers, 
  Sparkles, 
  X, 
  EyeOff, 
  Eye, 
  Check, 
  Edit3, 
  AlertTriangle,
  ChevronRight,
  Zap
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const SosTriggerScreen: React.FC = () => {
  const {
    selectedCrisisCategory,
    anonymousEmergencyMode,
    intakeInputMode,
    voiceTranscript,
    textInputNotes,
    isVoiceRecording,
    photoAttached,
    photoUrl,
    localityName,
    safetyIndexScore,
    streetAddress,
    subAddress,
    countdownSeconds,
    isCountingDown,
    setSelectedCrisisCategory,
    toggleAnonymousEmergencyMode,
    setIntakeInputMode,
    toggleVoiceRecording,
    setTextInputNotes,
    attachSamplePhoto,
    removePhoto,
    startCountdown,
    cancelCountdown,
    confirmAddress,
    triggerSos
  } = useDemoStore();

  const [isHoldingSos, setIsHoldingSos] = useState<boolean>(false);
  const [holdProgress, setHoldProgress] = useState<number>(0);
  const [isEditingAddress, setIsEditingAddress] = useState<boolean>(false);
  const [customStreet, setCustomStreet] = useState<string>(streetAddress);
  const [customSub, setCustomSub] = useState<string>(subAddress);
  const holdIntervalRef = useRef<number | null>(null);

  // 3-second hold to trigger SOS
  const handleHoldStart = () => {
    soundEngine.playCountdownBeep(880);
    setIsHoldingSos(true);
    setHoldProgress(0);

    const startTime = Date.now();
    const duration = 2500; // 2.5s hold
    holdIntervalRef.current = window.setInterval(() => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(100, (elapsed / duration) * 100);
      setHoldProgress(progress);

      if (progress >= 100) {
        if (holdIntervalRef.current) {
          clearInterval(holdIntervalRef.current);
          holdIntervalRef.current = null;
        }
        setIsHoldingSos(false);
        setHoldProgress(0);
        triggerSos();
      }
    }, 40);
  };

  const handleHoldEnd = () => {
    if (holdIntervalRef.current) {
      clearInterval(holdIntervalRef.current);
      holdIntervalRef.current = null;
    }
    setIsHoldingSos(false);
    setHoldProgress(0);
  };

  useEffect(() => {
    return () => {
      if (holdIntervalRef.current) {
        clearInterval(holdIntervalRef.current);
      }
    };
  }, []);

  const crisisCategories: { id: CrisisCategory; label: string; icon: React.FC<{ size?: number; color?: string }> }[] = [
    { id: 'medical', label: 'Medical', icon: HeartPulse },
    { id: 'fire', label: 'Fire Hazard', icon: Flame },
    { id: 'crime', label: 'Crime / Threat', icon: ShieldAlert },
    { id: 'accident', label: 'Road Collision', icon: Car },
  ];

  const quickSymptoms = [
    'Sudden Collapse', 
    'Chest Pressure', 
    'Unresponsive', 
    'Agonal Breathing', 
    'Arterial Bleed', 
    'Blue Lips'
  ];

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: '#000000',
      color: '#FFFFFF',
      padding: '12px 14px 14px 14px',
      gap: '10px',
      overflowY: 'auto',
      overflowX: 'hidden',
      userSelect: 'none',
      position: 'relative'
    }}>
      {/* 1. Top Header: Locality, Safe Zone Score & Anonymous Mode Toggle */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '6px 10px',
        backgroundColor: '#0D0F14',
        borderRadius: '12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div className="telemetry-dot telemetry-dot-emergency" style={{ width: '8px', height: '8px' }} />
          <div>
            <div style={{ fontSize: '12px', fontWeight: 800, color: '#FFFFFF', lineHeight: 1.1 }}>
              {localityName}
            </div>
            <div style={{ fontSize: '10px', color: '#00E676', fontWeight: 700 }}>
              Safety Index {safetyIndexScore}% • Active Radar
            </div>
          </div>
        </div>

        {/* Anonymous Emergency Mode Toggle */}
        <button
          onClick={toggleAnonymousEmergencyMode}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '5px',
            padding: '4px 8px',
            borderRadius: 'var(--radius-full)',
            backgroundColor: anonymousEmergencyMode ? 'rgba(255, 42, 68, 0.2)' : 'rgba(255, 255, 255, 0.06)',
            color: anonymousEmergencyMode ? '#FF2A44' : '#94A3B8',
            border: `1px solid ${anonymousEmergencyMode ? 'rgba(255, 42, 68, 0.4)' : 'rgba(255, 255, 255, 0.1)'}`,
            fontSize: '10px',
            fontWeight: 700,
            cursor: 'pointer',
            transition: 'all 0.2s ease'
          }}
          title="Toggle Anonymous Emergency Mode (Hides Personal Contact Information)"
        >
          {anonymousEmergencyMode ? <EyeOff size={12} color="#FF2A44" /> : <Eye size={12} />}
          <span>{anonymousEmergencyMode ? 'Anonymous ON' : 'Privacy Mode'}</span>
        </button>
      </div>

      {/* Anonymous Warning Pill */}
      {anonymousEmergencyMode && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          padding: '6px 10px',
          borderRadius: '8px',
          backgroundColor: 'rgba(255, 42, 68, 0.12)',
          border: '1px solid rgba(255, 42, 68, 0.3)',
          fontSize: '10px',
          color: '#FFA0A8',
          lineHeight: 1.3,
          flexShrink: 0
        }}>
          <AlertTriangle size={13} color="#FF2A44" style={{ flexShrink: 0 }} />
          <span>Anonymous Mode enabled: Personal identity and contact numbers masked from public volunteers.</span>
        </div>
      )}

      {/* 2. Address Verification & GPS Pin */}
      <div style={{
        backgroundColor: '#0C0E12',
        borderRadius: '12px',
        padding: '8px 10px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        flexShrink: 0
      }}>
        <div style={{
          width: '28px',
          height: '28px',
          borderRadius: '50%',
          backgroundColor: '#161922',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0
        }}>
          <MapPin size={14} color="#FF2A44" />
        </div>

        <div style={{ flex: 1, minWidth: 0 }}>
          {isEditingAddress ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
              <input
                type="text"
                value={customStreet}
                onChange={(e) => setCustomStreet(e.target.value)}
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  backgroundColor: '#161922',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '4px',
                  padding: '2px 5px',
                  color: '#FFFFFF'
                }}
              />
              <input
                type="text"
                value={customSub}
                onChange={(e) => setCustomSub(e.target.value)}
                style={{
                  fontSize: '9.5px',
                  backgroundColor: '#161922',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '4px',
                  padding: '2px 5px',
                  color: '#94A3B8'
                }}
              />
            </div>
          ) : (
            <>
              <div style={{
                fontSize: '12px',
                fontWeight: 800,
                color: '#FFFFFF',
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis'
              }}>
                {customStreet || streetAddress}
              </div>
              <div style={{
                fontSize: '10px',
                color: '#94A3B8',
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis'
              }}>
                {customSub || subAddress}
              </div>
            </>
          )}
        </div>

        <button
          onClick={() => {
            soundEngine.playClick();
            setIsEditingAddress(!isEditingAddress);
          }}
          style={{
            padding: '4px',
            borderRadius: '50%',
            backgroundColor: '#161922',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            color: '#94A3B8'
          }}
          title="Edit Address"
        >
          {isEditingAddress ? <Check size={13} color="#00E676" /> : <Edit3 size={12} />}
        </button>
      </div>

      {/* 3. Crisis Category Selector Chips */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', flexShrink: 0 }}>
        <div style={{ fontSize: '10px', fontWeight: 800, color: '#94A3B8', letterSpacing: '0.04em' }}>
          CRISIS CATEGORY
        </div>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: '4px',
          backgroundColor: '#0D0F14',
          padding: '3px',
          borderRadius: '10px',
          border: '1px solid rgba(255, 255, 255, 0.08)'
        }}>
          {crisisCategories.map((cat) => {
            const isSelected = selectedCrisisCategory === cat.id;
            const CategoryIcon = cat.icon;
            return (
              <button
                key={cat.id}
                onClick={() => setSelectedCrisisCategory(cat.id)}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '3px',
                  padding: '6px 2px',
                  borderRadius: '8px',
                  backgroundColor: isSelected ? 'var(--emergency-crimson)' : 'transparent',
                  color: isSelected ? '#FFFFFF' : '#94A3B8',
                  fontWeight: isSelected ? 800 : 600,
                  fontSize: '9.5px',
                  boxShadow: isSelected ? '0 2px 8px rgba(255, 42, 68, 0.4)' : 'none',
                  border: isSelected ? '1px solid rgba(255, 255, 255, 0.2)' : '1px solid transparent',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                <CategoryIcon size={14} color={isSelected ? '#FFFFFF' : '#94A3B8'} />
                <span>{cat.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* 4. Giant Central SOS Button with Breathing Glow and Hold Ring */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '10px 0',
        position: 'relative',
        flexShrink: 0
      }}>
        {/* Multi-layered Animated Pulse Glow Container */}
        <div style={{ position: 'relative', width: '170px', height: '170px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          {/* Radial Concentric Wave 1 */}
          <div
            className="radar-ring-pulse"
            style={{
              position: 'absolute',
              width: '166px',
              height: '166px',
              borderRadius: '50%',
              border: '2px solid rgba(255, 42, 68, 0.25)',
              backgroundColor: 'rgba(255, 42, 68, 0.04)',
              pointerEvents: 'none'
            }}
          />

          {/* Radial Concentric Wave 2 */}
          <div
            style={{
              position: 'absolute',
              width: '146px',
              height: '146px',
              borderRadius: '50%',
              border: '2px solid rgba(255, 42, 68, 0.45)',
              backgroundColor: 'rgba(255, 42, 68, 0.08)',
              pointerEvents: 'none'
            }}
          />

          {/* Hold Progress SVG Ring */}
          {isHoldingSos && (
            <svg
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '170px',
                height: '170px',
                transform: 'rotate(-90deg)',
                pointerEvents: 'none'
              }}
            >
              <circle
                cx="85"
                cy="85"
                r="74"
                fill="none"
                stroke="rgba(255, 255, 255, 0.2)"
                strokeWidth="6"
              />
              <circle
                cx="85"
                cy="85"
                r="74"
                fill="none"
                stroke="#00E5FF"
                strokeWidth="6"
                strokeDasharray={2 * Math.PI * 74}
                strokeDashoffset={2 * Math.PI * 74 * (1 - holdProgress / 100)}
                strokeLinecap="round"
              />
            </svg>
          )}

          {/* Central SOS Trigger Button */}
          <button
            onMouseDown={handleHoldStart}
            onMouseUp={handleHoldEnd}
            onTouchStart={handleHoldStart}
            onTouchEnd={handleHoldEnd}
            onClick={() => {
              // 1-Tap fallback
              if (!isHoldingSos) {
                confirmAddress();
              }
            }}
            className="sos-breathing"
            style={{
              width: '124px',
              height: '124px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #FF3B4E 0%, #D70020 100%)',
              color: '#FFFFFF',
              border: '3px solid #FFFFFF',
              boxShadow: '0 0 35px rgba(255, 42, 68, 0.8), inset 0 2px 10px rgba(255, 255, 255, 0.4)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '2px',
              cursor: 'pointer',
              zIndex: 10,
              transform: isHoldingSos ? 'scale(0.96)' : 'scale(1)',
              transition: 'transform 0.1s ease'
            }}
          >
            <Zap size={32} fill="#FFFFFF" />
            <span style={{ fontSize: '22px', fontWeight: 900, letterSpacing: '0.06em', lineHeight: 1 }}>SOS</span>
            <span style={{ fontSize: '9px', fontWeight: 800, letterSpacing: '0.04em', textTransform: 'uppercase', opacity: 0.9 }}>
              {isHoldingSos ? `${Math.round(holdProgress)}% HOLD` : 'HOLD OR TAP'}
            </span>
          </button>
        </div>

        <div style={{ textAlign: 'center', marginTop: '4px' }}>
          <div style={{ fontSize: '11px', fontWeight: 700, color: '#E2E8F0' }}>
            Hold 2.5s or Tap to Dispatch Community Responders
          </div>
          <div style={{ fontSize: '9.5px', color: '#94A3B8' }}>
            Includes 3-second grace buffer to cancel false alarms
          </div>
        </div>
      </div>

      {/* 5. Multimodal Intake Simulator Bar */}
      <div style={{
        backgroundColor: '#0C0E12',
        borderRadius: '12px',
        padding: '10px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '11px', fontWeight: 800, color: '#FFFFFF', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Sparkles size={12} color="#00E5FF" />
            <span>Multimodal AI Intake</span>
          </span>

          {/* Mode Switcher */}
          <div style={{
            display: 'flex',
            backgroundColor: '#14171E',
            padding: '2px',
            borderRadius: '6px',
            gap: '2px'
          }}>
            {[
              { id: 'VOICE', label: 'Voice', icon: Mic },
              { id: 'PHOTO', label: 'Photo', icon: Camera },
              { id: 'PRESETS', label: 'Chips', icon: Layers },
            ].map((tab) => {
              const isSelected = intakeInputMode === tab.id;
              const TabIcon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setIntakeInputMode(tab.id as MultimodalInputMode)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '3px',
                    padding: '3px 6px',
                    borderRadius: '4px',
                    backgroundColor: isSelected ? '#FF2A44' : 'transparent',
                    color: isSelected ? '#FFFFFF' : '#94A3B8',
                    fontWeight: isSelected ? 800 : 600,
                    fontSize: '10px',
                    cursor: 'pointer'
                  }}
                >
                  <TabIcon size={10} color={isSelected ? '#FFFFFF' : '#94A3B8'} />
                  <span>{tab.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Voice Sub-Panel */}
        {intakeInputMode === 'VOICE' && (
          <div style={{
            backgroundColor: '#12151C',
            borderRadius: '10px',
            padding: '8px',
            border: '1px solid rgba(255, 42, 68, 0.25)',
            display: 'flex',
            flexDirection: 'column',
            gap: '6px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <button
                onClick={toggleVoiceRecording}
                className={isVoiceRecording ? 'sos-breathing' : ''}
                style={{
                  width: '38px',
                  height: '38px',
                  borderRadius: '50%',
                  backgroundColor: isVoiceRecording ? '#FF2A44' : '#1A1E26',
                  border: `2px solid ${isVoiceRecording ? '#FFFFFF' : '#FF2A44'}`,
                  color: '#FFFFFF',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                  cursor: 'pointer'
                }}
                title={isVoiceRecording ? 'Stop Recording' : 'Tap to Record Voice SOS'}
              >
                <Mic size={18} color={isVoiceRecording ? '#FFFFFF' : '#FF2A44'} />
              </button>

              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: '10.5px', fontWeight: 800, color: isVoiceRecording ? '#FF2A44' : '#FFFFFF' }}>
                  {isVoiceRecording ? 'AI Listening & Transcribing...' : 'Hold or Tap Mic to Speak'}
                </div>

                {/* Waveform visualizer */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '2px', height: '18px', marginTop: '2px' }}>
                  {[6, 14, 22, 10, 24, 12, 18, 8, 20, 14, 7, 16].map((h, idx) => (
                    <div
                      key={idx}
                      style={{
                        width: '3px',
                        height: isVoiceRecording ? `${h}px` : '4px',
                        backgroundColor: isVoiceRecording ? '#FF2A44' : '#334155',
                        borderRadius: '2px',
                        transition: 'height 0.12s ease'
                      }}
                    />
                  ))}
                </div>
              </div>
            </div>

            {/* Transcript */}
            <div style={{
              backgroundColor: '#0A0C10',
              borderRadius: '6px',
              padding: '6px 8px',
              border: '1px solid rgba(255, 255, 255, 0.08)',
              fontSize: '10.5px',
              color: '#CBD5E1',
              lineHeight: 1.3
            }}>
              <span style={{ fontWeight: 800, color: '#00E5FF', marginRight: '4px' }}>AI Transcript:</span>
              <span>"{voiceTranscript}"</span>
            </div>
          </div>
        )}

        {/* Photo Sub-Panel */}
        {intakeInputMode === 'PHOTO' && (
          <div style={{
            backgroundColor: '#12151C',
            borderRadius: '10px',
            padding: '8px',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            display: 'flex',
            flexDirection: 'column',
            gap: '6px'
          }}>
            {photoAttached && photoUrl ? (
              <div style={{
                position: 'relative',
                width: '100%',
                height: '80px',
                borderRadius: '6px',
                overflow: 'hidden',
                border: '2px solid #FF2A44'
              }}>
                <img
                  src={photoUrl}
                  alt="Medical Scene"
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />

                {/* AI Bounding Box Detection Overlay */}
                <div style={{
                  position: 'absolute',
                  top: '6px',
                  left: '6px',
                  backgroundColor: 'rgba(255, 42, 68, 0.95)',
                  color: '#FFFFFF',
                  padding: '2px 6px',
                  borderRadius: '4px',
                  fontSize: '9px',
                  fontWeight: 800,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '3px'
                }}>
                  <Sparkles size={10} />
                  <span>AI Vision: Acute Trauma / Hypoxia (98.2%)</span>
                </div>

                <button
                  onClick={removePhoto}
                  style={{
                    position: 'absolute',
                    top: '6px',
                    right: '6px',
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    color: '#FFFFFF',
                    borderRadius: '50%',
                    width: '18px',
                    height: '18px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer'
                  }}
                  title="Remove Photo"
                >
                  <X size={10} />
                </button>
              </div>
            ) : (
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                backgroundColor: '#0A0C10',
                borderRadius: '6px',
                padding: '6px 8px',
                border: '1px dashed rgba(255, 255, 255, 0.2)'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Camera size={16} color="#00E5FF" />
                  <div style={{ fontSize: '10.5px', color: '#94A3B8' }}>AI Vision classifies scene trauma</div>
                </div>
                <button
                  onClick={attachSamplePhoto}
                  style={{
                    padding: '4px 8px',
                    borderRadius: 'var(--radius-full)',
                    backgroundColor: '#FF2A44',
                    color: '#FFFFFF',
                    fontSize: '10px',
                    fontWeight: 800,
                    cursor: 'pointer'
                  }}
                >
                  Snap Photo
                </button>
              </div>
            )}
          </div>
        )}

        {/* Preset Chips Sub-Panel */}
        {intakeInputMode === 'PRESETS' && (
          <div style={{ display: 'flex', gap: '4px', overflowX: 'auto', paddingBottom: '2px' }}>
            {quickSymptoms.map((symp, idx) => (
              <button
                key={idx}
                onClick={() => {
                  soundEngine.playClick();
                  setTextInputNotes(textInputNotes ? `${textInputNotes}, ${symp}` : symp);
                }}
                style={{
                  padding: '3px 8px',
                  borderRadius: 'var(--radius-full)',
                  backgroundColor: '#1E232E',
                  border: '1px solid rgba(255, 255, 255, 0.1)',
                  fontSize: '10px',
                  fontWeight: 700,
                  color: '#E2E8F0',
                  whiteSpace: 'nowrap',
                  cursor: 'pointer'
                }}
              >
                + {symp}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* 6. Bottom Dispatch Slider with 3-Second Grace Countdown */}
      <div style={{
        marginTop: 'auto',
        width: '100%',
        height: '48px',
        borderRadius: 'var(--radius-full)',
        background: 'linear-gradient(135deg, #00E676 0%, #FF2A44 100%)',
        padding: '3px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 4px 16px rgba(0, 0, 0, 0.6)',
        border: '1px solid rgba(255, 255, 255, 0.2)',
        flexShrink: 0
      }}>
        {/* Cancel Wing */}
        <button
          onClick={() => {
            if (isCountingDown) {
              cancelCountdown();
            }
          }}
          style={{
            flex: 1,
            height: '100%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-start',
            paddingLeft: '12px',
            gap: '4px',
            color: '#FFFFFF',
            fontWeight: 800,
            fontSize: '12px',
            cursor: 'pointer'
          }}
        >
          <X size={14} strokeWidth={2.8} />
          <span>{isCountingDown ? 'Cancel' : 'Safe Standby'}</span>
        </button>

        {/* Center Countdown Badge */}
        <button
          onClick={() => {
            if (!isCountingDown) {
              startCountdown();
            } else {
              triggerSos();
            }
          }}
          className={isCountingDown ? 'countdown-pulse' : ''}
          style={{
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            backgroundColor: '#FF2A44',
            color: '#FFFFFF',
            fontWeight: 900,
            fontSize: isCountingDown ? '16px' : '12px',
            boxShadow: '0 0 14px rgba(255, 42, 68, 0.8)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
            border: '2px solid #FFFFFF',
            cursor: 'pointer'
          }}
          title={isCountingDown ? 'Click to Dispatch Immediately' : 'Start 3s Grace Countdown'}
        >
          {isCountingDown ? countdownSeconds : <Zap size={14} fill="#FFFFFF" />}
        </button>

        {/* Instant SOS Wing */}
        <button
          onClick={triggerSos}
          style={{
            flex: 1,
            height: '100%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            paddingRight: '12px',
            gap: '3px',
            color: '#FFFFFF',
            fontWeight: 800,
            fontSize: '12px',
            cursor: 'pointer'
          }}
        >
          <span>Send SOS</span>
          <ChevronRight size={15} strokeWidth={2.8} />
        </button>
      </div>
    </div>
  );
};
