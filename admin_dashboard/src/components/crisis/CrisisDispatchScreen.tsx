/* ==========================================================================
   NearHelp AI — Screen 2: Medical Emergency Intake (AMOLED & Overflow Fixed)
   File: src/components/crisis/CrisisDispatchScreen.tsx
   ========================================================================== */

import React, { useState } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { MEDICAL_CONDITIONS } from '../../mock/scenarios';
import type { MedicalConditionId, MultimodalInputMode } from '../../mock/types';
import { 
  MapPin, 
  Edit3, 
  X, 
  ChevronRight, 
  Users, 
  Share2, 
  MessageSquare, 
  Bell, 
  Heart,
  Zap,
  Check,
  Mic,
  Keyboard,
  Camera,
  Layers,
  Sparkles,
  Image as ImageIcon
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const CrisisDispatchScreen: React.FC = () => {
  const {
    currentScenario,
    incidentStatus,
    selectedMedicalCondition,
    intakeInputMode,
    voiceTranscript,
    textInputNotes,
    isVoiceRecording,
    photoAttached,
    photoUrl,
    streetAddress,
    subAddress,
    countdownSeconds,
    isCountingDown,
    elapsedSeconds,
    searchRadiusKm,
    cprMetronomeActive,
    selectMedicalCondition,
    setIntakeInputMode,
    toggleVoiceRecording,
    setTextInputNotes,
    attachSamplePhoto,
    removePhoto,
    startCountdown,
    cancelCountdown,
    confirmAddress,
    triggerSos,
    cancelSos,
    toggleCprMetronome,
    setScreenMode
  } = useDemoStore();

  const [activeTab, setActiveTab] = useState<'Community' | 'Sharing' | 'Message' | 'Alert'>('Community');
  const [isEditingAddress, setIsEditingAddress] = useState<boolean>(false);
  const [customStreet, setCustomStreet] = useState<string>(streetAddress);
  const [customSub, setCustomSub] = useState<string>(subAddress);

  const isEmergencyActive = incidentStatus !== 'IDLE' && incidentStatus !== 'COUNTDOWN';

  const handleConditionClick = (condId: MedicalConditionId) => {
    selectMedicalCondition(condId);
  };

  const quickSymptoms = [
    'Chest Pain', 
    'Unresponsive', 
    'Severe Bleed', 
    'Blue Lips', 
    'Seizures', 
    'Head Trauma'
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
      {/* 1. Top Segmented Navigation Pills */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '4px',
        backgroundColor: '#0D0F14',
        padding: '3px',
        borderRadius: 'var(--radius-full)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        flexShrink: 0
      }}>
        {[
          { key: 'Community', label: 'Community', icon: Users },
          { key: 'Sharing', label: 'Sharing', icon: Share2 },
          { key: 'Message', label: 'Message', icon: MessageSquare },
          { key: 'Alert', label: 'Alert', icon: Bell },
        ].map((tab) => {
          const isActive = activeTab === tab.key;
          const TabIcon = tab.icon;
          return (
            <button
              key={tab.key}
              onClick={() => {
                soundEngine.playClick();
                setActiveTab(tab.key as any);
              }}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '3px',
                padding: '5px 2px',
                borderRadius: 'var(--radius-full)',
                backgroundColor: isActive ? '#1A1E26' : 'transparent',
                color: isActive ? '#FF2A44' : '#64748B',
                fontWeight: isActive ? 800 : 500,
                fontSize: '10.5px',
                border: isActive ? '1px solid rgba(255, 42, 68, 0.3)' : '1px solid transparent',
                transition: 'all var(--transition-fast)'
              }}
            >
              <TabIcon size={11} color={isActive ? '#FF2A44' : '#64748B'} />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* 2. Address Verification & Action Card */}
      <div style={{
        backgroundColor: '#0C0E12',
        borderRadius: '14px',
        padding: '10px 12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        boxShadow: '0 4px 14px rgba(0, 0, 0, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{
            width: '32px',
            height: '32px',
            borderRadius: '50%',
            backgroundColor: '#161922',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0
          }}>
            <MapPin size={15} color="#FF2A44" />
          </div>

          <div style={{ flex: 1, minWidth: 0 }}>
            {isEditingAddress ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
                <input
                  type="text"
                  value={customStreet}
                  onChange={(e) => setCustomStreet(e.target.value)}
                  style={{
                    fontSize: '12px',
                    fontWeight: 700,
                    backgroundColor: '#161922',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    borderRadius: '4px',
                    padding: '2px 6px',
                    color: '#FFFFFF'
                  }}
                />
                <input
                  type="text"
                  value={customSub}
                  onChange={(e) => setCustomSub(e.target.value)}
                  style={{
                    fontSize: '10px',
                    backgroundColor: '#161922',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    borderRadius: '4px',
                    padding: '2px 6px',
                    color: '#94A3B8'
                  }}
                />
              </div>
            ) : (
              <>
                <div style={{
                  fontSize: '13.5px',
                  fontWeight: 800,
                  color: '#FFFFFF',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                }}>
                  {customStreet || streetAddress}
                </div>
                <div style={{
                  fontSize: '10.5px',
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
              padding: '5px',
              borderRadius: '50%',
              backgroundColor: '#161922',
              border: '1px solid rgba(255, 255, 255, 0.08)',
              color: '#94A3B8'
            }}
            title="Edit Address"
          >
            {isEditingAddress ? <Check size={14} color="#00E676" /> : <Edit3 size={13} />}
          </button>
        </div>

        {/* Confirm Address Button */}
        {!isEmergencyActive && !isCountingDown && (
          <button
            onClick={confirmAddress}
            style={{
              width: '100%',
              height: '36px',
              borderRadius: 'var(--radius-full)',
              backgroundColor: 'var(--emergency-crimson)',
              color: '#FFFFFF',
              fontWeight: 800,
              fontSize: '13px',
              letterSpacing: '0.02em',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              boxShadow: '0 4px 14px rgba(255, 42, 68, 0.4)',
              border: '1px solid rgba(255, 255, 255, 0.15)',
              cursor: 'pointer'
            }}
          >
            <span>Confirm Address</span>
          </button>
        )}
      </div>

      {/* 3. Medical Emergency Intake & Multimodal Area */}
      {!isEmergencyActive ? (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '8px',
          flex: 1
        }}>
          {/* Multimodal Options Card */}
          <div style={{
            backgroundColor: '#0C0E12',
            borderRadius: '14px',
            padding: '10px 12px',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            boxShadow: '0 4px 14px rgba(0, 0, 0, 0.5)',
            display: 'flex',
            flexDirection: 'column',
            gap: '8px',
            flexShrink: 0
          }}>
            {/* Header & Intake Selector Tabs */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <span style={{ fontSize: '12px', fontWeight: 800, color: '#FFFFFF', display: 'flex', alignItems: 'center', gap: '5px' }}>
                <Heart size={13} color="#FF2A44" />
                <span>Specify Problem</span>
              </span>
              <span style={{ fontSize: '10px', color: '#00E5FF', fontWeight: 700, backgroundColor: 'rgba(0, 229, 255, 0.12)', padding: '1px 6px', borderRadius: 'var(--radius-full)', border: '1px solid rgba(0, 229, 255, 0.3)' }}>
                3 Input Modes
              </span>
            </div>

            {/* Mode Switcher Tabs */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(4, 1fr)',
              gap: '3px',
              backgroundColor: '#14171E',
              padding: '2px',
              borderRadius: '10px',
              border: '1px solid rgba(255, 255, 255, 0.06)'
            }}>
              {[
                { id: 'PRESETS', label: 'Presets', icon: Layers },
                { id: 'VOICE', label: 'Voice', icon: Mic },
                { id: 'TEXT', label: 'Typing', icon: Keyboard },
                { id: 'PHOTO', label: 'Photo', icon: Camera },
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
                      justifyContent: 'center',
                      gap: '4px',
                      padding: '5px 2px',
                      borderRadius: '8px',
                      backgroundColor: isSelected ? '#FF2A44' : 'transparent',
                      color: isSelected ? '#FFFFFF' : '#94A3B8',
                      fontWeight: isSelected ? 800 : 600,
                      fontSize: '11px',
                      boxShadow: isSelected ? '0 2px 8px rgba(255, 42, 68, 0.4)' : 'none',
                      transition: 'all 0.18s ease'
                    }}
                  >
                    <TabIcon size={12} color={isSelected ? '#FFFFFF' : '#94A3B8'} />
                    <span>{tab.label}</span>
                  </button>
                );
              })}
            </div>

            {/* --- Multimodal Panel Content --- */}

            {/* A. Voice Input Mode */}
            {intakeInputMode === 'VOICE' && (
              <div style={{
                backgroundColor: '#12151C',
                borderRadius: '12px',
                padding: '10px 12px',
                border: '1px solid rgba(255, 42, 68, 0.3)',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '8px'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', width: '100%' }}>
                  <button
                    onClick={toggleVoiceRecording}
                    className={isVoiceRecording ? 'sos-breathing' : ''}
                    style={{
                      width: '46px',
                      height: '46px',
                      borderRadius: '50%',
                      backgroundColor: isVoiceRecording ? '#FF2A44' : '#1A1E26',
                      border: `2px solid ${isVoiceRecording ? '#FFFFFF' : '#FF2A44'}`,
                      color: '#FFFFFF',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0,
                      boxShadow: isVoiceRecording ? '0 0 20px rgba(255, 42, 68, 0.8)' : 'none',
                      cursor: 'pointer'
                    }}
                    title={isVoiceRecording ? 'Stop Recording' : 'Tap to Record Voice SOS'}
                  >
                    <Mic size={22} color={isVoiceRecording ? '#FFFFFF' : '#FF2A44'} />
                  </button>

                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: '11px', fontWeight: 800, color: isVoiceRecording ? '#FF2A44' : '#FFFFFF', display: 'flex', alignItems: 'center', gap: '4px' }}>
                      {isVoiceRecording ? (
                        <>
                          <div className="telemetry-dot telemetry-dot-emergency" style={{ width: '6px', height: '6px' }} />
                          <span>Listening &amp; Transcribing...</span>
                        </>
                      ) : (
                        <span>Tap Mic to Speak Medical Emergency</span>
                      )}
                    </div>

                    {/* Waveform Visualizer */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '3px', height: '22px', marginTop: '4px' }}>
                      {[8, 18, 26, 12, 28, 14, 22, 10, 26, 16, 9].map((h, idx) => (
                        <div
                          key={idx}
                          style={{
                            width: '3px',
                            height: isVoiceRecording ? `${h}px` : '4px',
                            backgroundColor: isVoiceRecording ? '#FF2A44' : '#334155',
                            borderRadius: '2px',
                            transition: 'height 0.15s ease'
                          }}
                        />
                      ))}
                    </div>
                  </div>
                </div>

                {/* AI Speech Transcript Preview */}
                <div style={{
                  width: '100%',
                  backgroundColor: '#0A0C10',
                  borderRadius: '8px',
                  padding: '7px 10px',
                  border: '1px solid rgba(255, 255, 255, 0.08)',
                  fontSize: '11px',
                  color: '#CBD5E1',
                  lineHeight: 1.35
                }}>
                  <span style={{ fontWeight: 800, color: '#00E5FF', marginRight: '4px' }}>AI Transcript:</span>
                  <span>"{voiceTranscript}"</span>
                </div>
              </div>
            )}

            {/* B. Text Typing Mode */}
            {intakeInputMode === 'TEXT' && (
              <div style={{
                backgroundColor: '#12151C',
                borderRadius: '12px',
                padding: '8px 10px',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                display: 'flex',
                flexDirection: 'column',
                gap: '6px'
              }}>
                <textarea
                  value={textInputNotes}
                  onChange={(e) => setTextInputNotes(e.target.value)}
                  placeholder="Type patient symptoms..."
                  rows={2}
                  style={{
                    width: '100%',
                    backgroundColor: '#0A0C10',
                    border: '1px solid rgba(255, 255, 255, 0.15)',
                    borderRadius: '8px',
                    padding: '6px 8px',
                    fontSize: '11.5px',
                    color: '#FFFFFF',
                    resize: 'none',
                    outline: 'none',
                    fontFamily: 'inherit'
                  }}
                />

                {/* Quick Symptom Chips */}
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
                        whiteSpace: 'nowrap'
                      }}
                    >
                      + {symp}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* C. Photo Intake Mode */}
            {intakeInputMode === 'PHOTO' && (
              <div style={{
                backgroundColor: '#12151C',
                borderRadius: '12px',
                padding: '8px 10px',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                display: 'flex',
                flexDirection: 'column',
                gap: '6px'
              }}>
                {photoAttached && photoUrl ? (
                  <div style={{
                    position: 'relative',
                    width: '100%',
                    height: '85px',
                    borderRadius: '8px',
                    overflow: 'hidden',
                    border: '2px solid #FF2A44'
                  }}>
                    <img 
                      src={photoUrl} 
                      alt="Medical Scene" 
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }} 
                    />
                    
                    <div style={{
                      position: 'absolute',
                      top: '6px',
                      left: '6px',
                      backgroundColor: 'rgba(255, 42, 68, 0.95)',
                      color: '#FFFFFF',
                      padding: '2px 6px',
                      borderRadius: '4px',
                      fontSize: '9.5px',
                      fontWeight: 800,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '4px'
                    }}>
                      <Sparkles size={10} />
                      <span>AI Vision: Acute Trauma (98.2%)</span>
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
                        width: '20px',
                        height: '20px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                      title="Remove Photo"
                    >
                      <X size={11} />
                    </button>
                  </div>
                ) : (
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    backgroundColor: '#0A0C10',
                    borderRadius: '8px',
                    padding: '8px 10px',
                    border: '1px dashed rgba(255, 255, 255, 0.2)'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <ImageIcon size={18} color="#00E5FF" />
                      <div>
                        <div style={{ fontSize: '11px', fontWeight: 800, color: '#FFFFFF' }}>Attach Scene Photo</div>
                        <div style={{ fontSize: '10px', color: '#94A3B8' }}>AI Vision classifies trauma</div>
                      </div>
                    </div>

                    <button
                      onClick={attachSamplePhoto}
                      style={{
                        padding: '4px 10px',
                        borderRadius: 'var(--radius-full)',
                        backgroundColor: '#FF2A44',
                        color: '#FFFFFF',
                        fontSize: '10.5px',
                        fontWeight: 800,
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px'
                      }}
                    >
                      <Camera size={11} />
                      <span>Snap Photo</span>
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Quick Medical Problem Cards (2x4 Grid — Responsive, Compact, Zero Overflow) */}
          <div style={{
            backgroundColor: '#0C0E12',
            borderRadius: '14px',
            padding: '10px',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            boxShadow: '0 4px 14px rgba(0, 0, 0, 0.5)',
            display: 'flex',
            flexDirection: 'column',
            gap: '6px'
          }}>
            <div style={{ fontSize: '11px', fontWeight: 800, color: '#94A3B8', paddingLeft: '2px' }}>
              SELECT MEDICAL CONDITION:
            </div>

            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
              gap: '6px'
            }}>
              {MEDICAL_CONDITIONS.map((cond) => {
                const isSelected = selectedMedicalCondition === cond.id;
                return (
                  <button
                    key={cond.id}
                    onClick={() => handleConditionClick(cond.id)}
                    style={{
                      minWidth: 0,
                      borderRadius: '10px',
                      backgroundColor: isSelected ? '#FF2A44' : '#14171F',
                      color: '#FFFFFF',
                      border: isSelected ? '1px solid #FF8090' : '1px solid rgba(255, 255, 255, 0.08)',
                      boxShadow: isSelected ? '0 3px 10px rgba(255, 42, 68, 0.45)' : 'none',
                      display: 'flex',
                      alignItems: 'center',
                      padding: '7px 8px',
                      gap: '6px',
                      transition: 'all 0.15s ease',
                      cursor: 'pointer',
                      textAlign: 'left',
                      overflow: 'hidden'
                    }}
                    title={cond.description}
                  >
                    {/* Emoji Icon Container */}
                    <div style={{
                      width: '26px',
                      height: '26px',
                      borderRadius: '50%',
                      backgroundColor: isSelected ? 'rgba(255, 255, 255, 0.25)' : '#1E232E',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '14px',
                      flexShrink: 0
                    }}>
                      {cond.emoji}
                    </div>

                    {/* Text Details with strict overflow protection */}
                    <div style={{ minWidth: 0, flex: 1, overflow: 'hidden' }}>
                      <div style={{
                        fontSize: '11px',
                        fontWeight: 800,
                        lineHeight: 1.1,
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        color: '#FFFFFF'
                      }}>
                        {cond.label}
                      </div>
                      <div style={{
                        fontSize: '9px',
                        color: isSelected ? 'rgba(255,255,255,0.9)' : '#94A3B8',
                        fontWeight: 700,
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis'
                      }}>
                        Level {cond.severity} • {cond.severity === 5 ? 'Critical' : 'Urgent'}
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* 4. Dual-Action Countdown Dispatch Slider (Sticky Bottom) */}
          <div style={{
            marginTop: 'auto',
            width: '100%',
            height: '52px',
            borderRadius: 'var(--radius-full)',
            background: 'linear-gradient(135deg, #00E676 0%, #FF2A44 100%)',
            padding: '3px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 6px 20px rgba(0, 0, 0, 0.6)',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            flexShrink: 0
          }}>
            {/* Cancel Wing */}
            <button
              onClick={() => {
                if (isCountingDown) {
                  cancelCountdown();
                } else {
                  setScreenMode('GUARDIAN');
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
                fontSize: '13px'
              }}
            >
              <X size={15} strokeWidth={2.8} />
              <span>{isCountingDown ? 'Cancel' : 'Back'}</span>
            </button>

            {/* Center Pulsing Countdown Badge */}
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
                width: '42px',
                height: '42px',
                borderRadius: '50%',
                backgroundColor: '#FF2A44',
                color: '#FFFFFF',
                fontWeight: 900,
                fontSize: isCountingDown ? '18px' : '13px',
                boxShadow: '0 0 14px rgba(255, 42, 68, 0.8)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
                border: '2px solid #FFFFFF'
              }}
              title={isCountingDown ? 'Click to Dispatch Immediately' : 'Start 3s Countdown'}
            >
              {isCountingDown ? countdownSeconds : <Zap size={16} fill="#FFFFFF" />}
            </button>

            {/* Instant Send SOS Wing */}
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
                fontSize: '13px'
              }}
            >
              <span>Send SOS</span>
              <ChevronRight size={17} strokeWidth={2.8} />
            </button>
          </div>
        </div>
      ) : (
        /* Active Emergency State UI */
        <div style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          gap: '10px',
          overflowY: 'auto'
        }}>
          {/* Diagnostic Badge */}
          <div style={{
            backgroundColor: 'rgba(255, 42, 68, 0.16)',
            border: '1px solid rgba(255, 42, 68, 0.4)',
            borderRadius: '12px',
            padding: '12px 14px',
            boxShadow: '0 4px 18px rgba(0,0,0,0.6)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '3px' }}>
              <span style={{ fontSize: '12.5px', fontWeight: 800, color: '#FF2A44' }}>
                {currentScenario.severityLabel}
              </span>
              <span className="font-mono" style={{ fontSize: '11.5px', color: '#FFA000', fontWeight: 700 }}>
                T+{elapsedSeconds}s
              </span>
            </div>
            <div style={{ fontSize: '11px', color: '#94A3B8' }}>
              AI Confidence: <strong style={{ color: '#00E5FF' }}>{currentScenario.aiConfidence}%</strong> • Platinum Window: <strong style={{ color: '#FF2A44' }}>{currentScenario.survivalWindowMinutes}m</strong>
            </div>
          </div>

          {/* Spatial Escalation Bar */}
          <div style={{
            backgroundColor: '#0C0E12',
            borderRadius: '12px',
            padding: '10px 12px',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            boxShadow: '0 4px 14px rgba(0, 0, 0, 0.6)'
          }}>
            <div style={{ fontSize: '10px', fontWeight: 800, color: '#94A3B8', marginBottom: '2px' }}>
              POSTGIS GIST MEDICAL DISPATCH
            </div>
            <div style={{ fontSize: '13px', fontWeight: 800, color: '#00E676' }}>
              Searching radius: {searchRadiusKm} km
            </div>
            <div style={{ fontSize: '10.5px', color: '#94A3B8' }}>
              {currentScenario.responders.length} verified CPR &amp; medical volunteers notified
            </div>
          </div>

          {/* CPR Metronome Box */}
          <div style={{
            backgroundColor: '#0C0E12',
            borderRadius: '12px',
            padding: '12px 14px',
            border: `1px solid ${cprMetronomeActive ? '#FF2A44' : 'rgba(255, 255, 255, 0.08)'}`,
            boxShadow: '0 4px 14px rgba(0, 0, 0, 0.6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div>
              <div style={{ fontSize: '12.5px', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '5px', color: '#FFFFFF' }}>
                <Heart size={14} color="#FF2A44" />
                <span>CPR Rhythm Metronome</span>
              </div>
              <div style={{ fontSize: '10.5px', color: '#94A3B8' }}>110 Compressions / Min (AHA)</div>
            </div>

            <button
              onClick={toggleCprMetronome}
              className={cprMetronomeActive ? 'cpr-beat-active' : ''}
              style={{
                padding: '6px 12px',
                borderRadius: 'var(--radius-sm)',
                fontSize: '11px',
                fontWeight: 800,
                backgroundColor: cprMetronomeActive ? '#FF2A44' : '#1A1E26',
                color: '#FFFFFF',
                border: '1px solid rgba(255, 255, 255, 0.2)'
              }}
            >
              {cprMetronomeActive ? 'STOP' : 'START BEAT'}
            </button>
          </div>

          {/* First Aid Protocol */}
          <div style={{
            backgroundColor: '#0C0E12',
            borderRadius: '12px',
            padding: '12px 14px',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            boxShadow: '0 4px 14px rgba(0, 0, 0, 0.6)'
          }}>
            <div style={{ fontSize: '10px', fontWeight: 800, color: '#00E5FF', marginBottom: '3px' }}>
              WHO / AHA RAG PROTOCOL
            </div>
            <div style={{ fontSize: '12px', fontWeight: 800, marginBottom: '3px', color: '#FFFFFF' }}>
              {currentScenario.protocol.steps[0]?.title}
            </div>
            <p style={{ fontSize: '11px', color: '#94A3B8', lineHeight: 1.4 }}>
              {currentScenario.protocol.steps[0]?.actionInstruction}
            </p>
          </div>

          {/* Cancel SOS */}
          <button
            onClick={cancelSos}
            style={{
              marginTop: 'auto',
              padding: '10px',
              borderRadius: '12px',
              backgroundColor: '#12151C',
              color: '#FF2A44',
              border: '1px solid rgba(255, 42, 68, 0.4)',
              fontSize: '12.5px',
              fontWeight: 800
            }}
          >
            Cancel SOS (False Alarm)
          </button>
        </div>
      )}
    </div>
  );
};
