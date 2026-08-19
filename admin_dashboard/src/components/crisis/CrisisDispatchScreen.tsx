/* ==========================================================================
   NearHelp AI — Screen 2: Crisis Dispatch & 16-Category Matrix
   File: src/components/crisis/CrisisDispatchScreen.tsx
   Design Spec: docs/design.md (Right Screen Reference)
   ========================================================================== */

import React, { useState } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { EMERGENCY_CATEGORIES } from '../../mock/scenarios';
import type { CrisisCategoryId } from '../../mock/types';
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
  Check
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const CrisisDispatchScreen: React.FC = () => {
  const {
    currentScenario,
    incidentStatus,
    selectedCategoryId,
    streetAddress,
    subAddress,
    countdownSeconds,
    isCountingDown,
    elapsedSeconds,
    searchRadiusKm,
    cprMetronomeActive,
    selectCategory,
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

  const handleCategoryClick = (catId: CrisisCategoryId) => {
    selectCategory(catId);
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: 'var(--crisis-surface-bg)',
      color: 'var(--text-primary-dark)',
      padding: '12px 14px 14px 14px',
      gap: '10px',
      overflowY: 'auto',
      userSelect: 'none'
    }}>
      {/* 1. Top Segmented Navigation Pills */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '6px',
        backgroundColor: '#E2E8F0',
        padding: '3px',
        borderRadius: 'var(--radius-full)'
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
                gap: '4px',
                padding: '6px 4px',
                borderRadius: 'var(--radius-full)',
                backgroundColor: isActive ? '#FFFFFF' : 'transparent',
                color: isActive ? 'var(--emergency-crimson)' : 'var(--text-secondary-muted)',
                fontWeight: isActive ? 700 : 500,
                fontSize: '11px',
                boxShadow: isActive ? '0 2px 6px rgba(0,0,0,0.06)' : 'none',
                transition: 'all var(--transition-fast)'
              }}
            >
              <TabIcon size={12} color={isActive ? 'var(--emergency-crimson)' : 'var(--text-secondary-muted)'} />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* 2. Address Verification & Action Card (AddressConfirmCard.kt) */}
      <div style={{
        backgroundColor: 'var(--card-neomorphic-light)',
        borderRadius: 'var(--radius-lg)',
        padding: '14px',
        boxShadow: 'var(--card-neomorphic-shadow)',
        display: 'flex',
        flexDirection: 'column',
        gap: '10px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            backgroundColor: '#F1F5F9',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0
          }}>
            <MapPin size={18} color="#0F172A" />
          </div>

          <div style={{ flex: 1, minWidth: 0 }}>
            {isEditingAddress ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <input
                  type="text"
                  value={customStreet}
                  onChange={(e) => setCustomStreet(e.target.value)}
                  style={{
                    fontSize: '14px',
                    fontWeight: 700,
                    border: '1px solid #CBD5E1',
                    borderRadius: '4px',
                    padding: '2px 6px',
                    color: '#0F172A'
                  }}
                />
                <input
                  type="text"
                  value={customSub}
                  onChange={(e) => setCustomSub(e.target.value)}
                  style={{
                    fontSize: '11px',
                    border: '1px solid #CBD5E1',
                    borderRadius: '4px',
                    padding: '2px 6px',
                    color: '#64748B'
                  }}
                />
              </div>
            ) : (
              <>
                <div style={{
                  fontSize: '15px',
                  fontWeight: 800,
                  color: 'var(--text-primary-dark)',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                }}>
                  {customStreet || streetAddress}
                </div>
                <div style={{
                  fontSize: '11px',
                  color: 'var(--text-secondary-muted)',
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
              padding: '6px',
              borderRadius: '50%',
              backgroundColor: '#F8FAFC',
              color: 'var(--text-secondary-muted)'
            }}
            title="Edit Pinpoint Address"
          >
            {isEditingAddress ? <Check size={16} color="var(--emergency-crimson)" /> : <Edit3 size={15} />}
          </button>
        </div>

        {/* Confirm Address Button */}
        {!isEmergencyActive && !isCountingDown && (
          <button
            onClick={confirmAddress}
            style={{
              width: '100%',
              height: '42px',
              borderRadius: 'var(--radius-full)',
              backgroundColor: 'var(--emergency-crimson)',
              color: '#FFFFFF',
              fontWeight: 800,
              fontSize: '14px',
              letterSpacing: '0.02em',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              boxShadow: '0 4px 14px rgba(229, 37, 56, 0.3)',
              cursor: 'pointer'
            }}
          >
            <span>Confirm Address</span>
          </button>
        )}
      </div>

      {/* Main Content: 16-Category Matrix OR Active SOS Guidance */}
      {!isEmergencyActive ? (
        <>
          {/* 3. The 16-Category Emergency Matrix (4x4 Responsive Grid) */}
          <div style={{
            backgroundColor: '#E8ECEF',
            borderRadius: '20px',
            padding: '8px',
            boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.06)'
          }}>
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(4, 1fr)',
              gap: '6px'
            }}>
              {EMERGENCY_CATEGORIES.map((cat) => {
                const isSelected = selectedCategoryId === cat.id;
                return (
                  <button
                    key={cat.id}
                    onClick={() => handleCategoryClick(cat.id)}
                    style={{
                      aspectRatio: '0.94',
                      borderRadius: '14px',
                      backgroundColor: isSelected ? 'var(--emergency-crimson)' : '#FFFFFF',
                      color: isSelected ? '#FFFFFF' : '#334155',
                      boxShadow: isSelected 
                        ? '0 6px 16px rgba(229, 37, 56, 0.35)' 
                        : '0 2px 6px rgba(0, 0, 0, 0.04)',
                      transform: isSelected ? 'scale(1.04)' : 'scale(1)',
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      padding: '6px 2px',
                      gap: '4px',
                      transition: 'all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1)',
                      cursor: 'pointer'
                    }}
                    title={cat.description}
                  >
                    {/* Emoji Icon Container */}
                    <div style={{
                      width: '32px',
                      height: '32px',
                      borderRadius: '50%',
                      backgroundColor: isSelected ? 'rgba(255, 255, 255, 0.22)' : '#F1F5F9',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '16px'
                    }}>
                      {cat.emoji}
                    </div>

                    {/* Label */}
                    <span style={{
                      fontSize: '9.5px',
                      fontWeight: isSelected ? 800 : 600,
                      lineHeight: 1.1,
                      textAlign: 'center',
                      letterSpacing: '-0.01em',
                      maxWidth: '62px',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap'
                    }}>
                      {cat.label}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* 4. Dual-Action Countdown Dispatch Slider (CountdownDispatchSlider.kt) */}
          <div style={{
            marginTop: 'auto',
            width: '100%',
            height: '56px',
            borderRadius: 'var(--radius-full)',
            background: isCountingDown 
              ? 'linear-gradient(135deg, #34C759 0%, #E52538 100%)' 
              : 'linear-gradient(135deg, #34C759 0%, #E52538 100%)',
            padding: '4px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 8px 24px rgba(0, 0, 0, 0.15)'
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
                paddingLeft: '14px',
                gap: '4px',
                color: '#FFFFFF',
                fontWeight: 700,
                fontSize: '13px'
              }}
            >
              <X size={16} />
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
                width: '46px',
                height: '46px',
                borderRadius: '50%',
                backgroundColor: 'var(--emergency-crimson)',
                color: '#FFFFFF',
                fontWeight: 900,
                fontSize: isCountingDown ? '20px' : '13px',
                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.25)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
                border: '2px solid rgba(255, 255, 255, 0.4)'
              }}
              title={isCountingDown ? 'Click to Dispatch Immediately' : 'Start 3s Countdown'}
            >
              {isCountingDown ? countdownSeconds : <Zap size={18} />}
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
                paddingRight: '14px',
                gap: '2px',
                color: '#FFFFFF',
                fontWeight: 700,
                fontSize: '13px'
              }}
            >
              <span>Send SOS</span>
              <ChevronRight size={18} />
            </button>
          </div>
        </>
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
            backgroundColor: 'rgba(229, 37, 56, 0.1)',
            border: '1px solid rgba(229, 37, 56, 0.3)',
            borderRadius: 'var(--radius-md)',
            padding: '12px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
              <span style={{ fontSize: '12px', fontWeight: 800, color: 'var(--emergency-crimson)' }}>
                {currentScenario.severityLabel}
              </span>
              <span className="font-mono" style={{ fontSize: '12px', color: 'var(--action-amber)', fontWeight: 700 }}>
                T+{elapsedSeconds}s
              </span>
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary-muted)' }}>
              AI Confidence: <strong>{currentScenario.aiConfidence}%</strong> • Platinum Window: <strong>{currentScenario.survivalWindowMinutes}m</strong>
            </div>
          </div>

          {/* Spatial Escalation Bar */}
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: 'var(--radius-md)',
            padding: '10px 12px',
            boxShadow: 'var(--card-neomorphic-shadow)'
          }}>
            <div style={{ fontSize: '10px', fontWeight: 700, color: 'var(--text-secondary-muted)', marginBottom: '2px' }}>
              POSTGIS GIST RADIAL DISPATCH
            </div>
            <div style={{ fontSize: '13px', fontWeight: 800, color: '#15803D' }}>
              Searching radius: {searchRadiusKm} km
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary-muted)' }}>
              {currentScenario.responders.length} verified community volunteers notified
            </div>
          </div>

          {/* CPR Metronome Box (if medical) */}
          {currentScenario.category === 'medical' && (
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-md)',
              padding: '12px',
              boxShadow: 'var(--card-neomorphic-shadow)',
              border: `1px solid ${cprMetronomeActive ? 'var(--emergency-crimson)' : 'transparent'}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <div>
                <div style={{ fontSize: '13px', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Heart size={14} color="var(--emergency-crimson)" />
                  <span>CPR Rhythm Metronome</span>
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-secondary-muted)' }}>110 Compressions / Min</div>
              </div>

              <button
                onClick={toggleCprMetronome}
                className={cprMetronomeActive ? 'cpr-beat-active' : ''}
                style={{
                  padding: '6px 12px',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '11px',
                  fontWeight: 800,
                  backgroundColor: cprMetronomeActive ? 'var(--emergency-crimson)' : '#0F172A',
                  color: '#FFFFFF'
                }}
              >
                {cprMetronomeActive ? 'STOP' : 'START BEAT'}
              </button>
            </div>
          )}

          {/* First Aid Protocol */}
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: 'var(--radius-md)',
            padding: '12px',
            boxShadow: 'var(--card-neomorphic-shadow)'
          }}>
            <div style={{ fontSize: '10px', fontWeight: 800, color: '#2563EB', marginBottom: '4px' }}>
              WHO / RED CROSS RAG PROTOCOL
            </div>
            <div style={{ fontSize: '12px', fontWeight: 700, marginBottom: '4px' }}>
              {currentScenario.protocol.steps[0]?.title}
            </div>
            <p style={{ fontSize: '11px', color: 'var(--text-secondary-muted)', lineHeight: 1.4 }}>
              {currentScenario.protocol.steps[0]?.actionInstruction}
            </p>
          </div>

          {/* Cancel SOS */}
          <button
            onClick={cancelSos}
            style={{
              marginTop: 'auto',
              padding: '10px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: '#FFFFFF',
              color: 'var(--emergency-crimson)',
              border: '1px solid #CBD5E1',
              fontSize: '12px',
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
