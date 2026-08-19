/* ==========================================================================
   NearHelp AI — Presentation Slide Synchronizer & Examiner Defense HUD
   File: src/components/demo/SlideSyncHUD.tsx
   ========================================================================== */

import React, { useState } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { MASTER_SLIDES_SYNC, EXAMINER_QA_ITEMS } from '../../mock/scenarios';
import { 
  X, 
  Play, 
  Clock, 
  User, 
  HelpCircle, 
  Keyboard, 
  ShieldCheck, 
  Zap, 
  CheckCircle2, 
  BookOpen
} from 'lucide-react';

export const SlideSyncHUD: React.FC = () => {
  const { 
    isSlideSyncOpen, 
    setIsSlideSyncOpen, 
    jumpToSlideView, 
    projectorMode 
  } = useDemoStore();

  const [activeTab, setActiveTab] = useState<'SLIDES' | 'EXAMINER_QA' | 'SHORTCUTS'>('SLIDES');
  const [selectedSlideNumber, setSelectedSlideNumber] = useState<number>(1);

  if (!isSlideSyncOpen) return null;

  return (
    <div className="animate-fade-in" style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.85)',
      backdropFilter: 'blur(16px)',
      WebkitBackdropFilter: 'blur(16px)',
      zIndex: 2000,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px'
    }}>
      <div 
        className="glass-panel-elevated animate-slide-in"
        style={{
          width: '100%',
          maxWidth: '1080px',
          maxHeight: '90vh',
          backgroundColor: projectorMode ? '#07090e' : '#0c0f16',
          border: `1.5px solid ${projectorMode ? 'rgba(255, 255, 255, 0.35)' : 'var(--border-medium)'}`,
          borderRadius: 'var(--radius-xl)',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          boxShadow: '0 25px 60px rgba(0, 0, 0, 0.95)'
        }}
      >
        {/* Modal Header */}
        <div style={{
          padding: '16px 24px',
          borderBottom: '1px solid var(--border-medium)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          backgroundColor: 'rgba(255, 255, 255, 0.02)',
          flexShrink: 0
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{
              width: '36px',
              height: '36px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'rgba(0, 229, 255, 0.15)',
              border: '1px solid var(--border-ai)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-ai-cyan)'
            }}>
              <BookOpen size={20} />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <h2 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--text-primary)' }}>
                  Presenter HUD & Examiner Defense Assistant
                </h2>
                <span style={{
                  fontSize: '11px',
                  padding: '2px 8px',
                  borderRadius: 'var(--radius-full)',
                  backgroundColor: 'var(--color-ai-subtle)',
                  color: 'var(--color-ai-cyan)',
                  border: '1px solid var(--border-ai)',
                  fontWeight: 700
                }}>
                  8-SLIDE SYNC
                </span>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                Saturday 22 Aug Review (8:00–11:00 AM • Room 401) • 11:00 Min Master Choreography
              </p>
            </div>
          </div>

          {/* Navigation Tabs in Header */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{
              display: 'flex',
              backgroundColor: 'var(--bg-surface)',
              padding: '3px',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--border-subtle)',
              gap: '4px'
            }}>
              <button
                onClick={() => setActiveTab('SLIDES')}
                style={{
                  padding: '6px 12px',
                  borderRadius: 'var(--radius-xs)',
                  fontSize: '12px',
                  fontWeight: activeTab === 'SLIDES' ? 700 : 500,
                  backgroundColor: activeTab === 'SLIDES' ? 'var(--color-ai-cyan)' : 'transparent',
                  color: activeTab === 'SLIDES' ? '#000' : 'var(--text-secondary)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <Play size={13} />
                <span>Master 8 Slides</span>
              </button>

              <button
                onClick={() => setActiveTab('EXAMINER_QA')}
                style={{
                  padding: '6px 12px',
                  borderRadius: 'var(--radius-xs)',
                  fontSize: '12px',
                  fontWeight: activeTab === 'EXAMINER_QA' ? 700 : 500,
                  backgroundColor: activeTab === 'EXAMINER_QA' ? 'var(--color-emergency-red)' : 'transparent',
                  color: activeTab === 'EXAMINER_QA' ? '#fff' : 'var(--text-secondary)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <HelpCircle size={13} />
                <span>Examiner Q&A Defense</span>
              </button>

              <button
                onClick={() => setActiveTab('SHORTCUTS')}
                style={{
                  padding: '6px 12px',
                  borderRadius: 'var(--radius-xs)',
                  fontSize: '12px',
                  fontWeight: activeTab === 'SHORTCUTS' ? 700 : 500,
                  backgroundColor: activeTab === 'SHORTCUTS' ? 'var(--bg-card-elevated)' : 'transparent',
                  color: activeTab === 'SHORTCUTS' ? 'var(--text-primary)' : 'var(--text-secondary)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <Keyboard size={13} />
                <span>Hotkeys</span>
              </button>
            </div>

            <button
              onClick={() => setIsSlideSyncOpen(false)}
              style={{
                width: '34px',
                height: '34px',
                borderRadius: '50%',
                backgroundColor: 'var(--bg-surface)',
                border: '1px solid var(--border-subtle)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--text-muted)'
              }}
              title="Close Presenter HUD (Esc / S)"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Modal Content Body */}
        <div style={{
          flex: 1,
          overflowY: 'auto',
          padding: '20px 24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px'
        }}>
          {activeTab === 'SLIDES' && (
            <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: '20px', alignItems: 'start' }}>
              {/* Left Slide List */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Deck Slide Sequence (11:00 Mins)
                </div>

                {MASTER_SLIDES_SYNC.map(slide => {
                  const isSelected = selectedSlideNumber === slide.slideNumber;
                  return (
                    <button
                      key={slide.slideNumber}
                      onClick={() => setSelectedSlideNumber(slide.slideNumber)}
                      style={{
                        padding: '12px',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: isSelected ? 'rgba(0, 229, 255, 0.12)' : 'var(--bg-card-dark)',
                        border: `1.5px solid ${isSelected ? 'var(--color-ai-cyan)' : 'var(--border-subtle)'}`,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '6px',
                        textAlign: 'left',
                        transition: 'all var(--transition-fast)'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <span style={{
                            fontSize: '11px',
                            fontWeight: 800,
                            padding: '2px 6px',
                            borderRadius: 'var(--radius-xs)',
                            backgroundColor: isSelected ? 'var(--color-ai-cyan)' : 'var(--bg-surface-elevated)',
                            color: isSelected ? '#000' : 'var(--text-primary)'
                          }}>
                            SLIDE {slide.slideNumber}
                          </span>
                          <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600 }}>
                            {slide.topicNumber}
                          </span>
                        </div>

                        <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--color-action-amber-bright)', fontWeight: 700 }}>
                          <Clock size={11} />
                          <span>{slide.duration}</span>
                        </div>
                      </div>

                      <div style={{ fontSize: '13px', fontWeight: 700, color: isSelected ? '#fff' : 'var(--text-secondary)' }}>
                        {slide.title}
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '2px' }}>
                        <span style={{ fontSize: '11px', color: 'var(--color-ai-cyan)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px' }}>
                          <User size={11} />
                          {slide.presenter}
                        </span>
                        <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                          {slide.timeWindow}
                        </span>
                      </div>
                    </button>
                  );
                })}
              </div>

              {/* Right Slide Detail & Action Controller */}
              {(() => {
                const currentSlide = MASTER_SLIDES_SYNC.find(s => s.slideNumber === selectedSlideNumber) || MASTER_SLIDES_SYNC[0];
                return (
                  <div style={{
                    padding: '20px',
                    borderRadius: 'var(--radius-lg)',
                    backgroundColor: 'var(--bg-card-dark)',
                    border: '1.5px solid var(--border-medium)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '16px'
                  }}>
                    {/* Header bar of detail */}
                    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '16px' }}>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                          <span style={{
                            fontSize: '12px',
                            fontWeight: 800,
                            padding: '3px 8px',
                            borderRadius: 'var(--radius-xs)',
                            backgroundColor: 'var(--color-ai-cyan)',
                            color: '#000'
                          }}>
                            SLIDE {currentSlide.slideNumber} of 8
                          </span>
                          <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600 }}>
                            {currentSlide.topicNumber}
                          </span>
                        </div>
                        <h3 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--text-primary)' }}>
                          {currentSlide.title}
                        </h3>
                      </div>

                      {/* Speaker Badge */}
                      <div style={{
                        padding: '8px 14px',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: 'rgba(0, 229, 255, 0.1)',
                        border: '1px solid var(--border-ai)',
                        textAlign: 'right'
                      }}>
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>
                          Speaker & Role
                        </div>
                        <div style={{ fontSize: '14px', fontWeight: 800, color: 'var(--color-ai-cyan)' }}>
                          {currentSlide.presenter}
                        </div>
                        <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
                          {currentSlide.presenterRole}
                        </div>
                      </div>
                    </div>

                    {/* Target Visual Banner */}
                    <div style={{
                      padding: '12px 16px',
                      borderRadius: 'var(--radius-md)',
                      backgroundColor: 'var(--bg-surface-elevated)',
                      border: '1px solid var(--border-subtle)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{
                          width: '10px',
                          height: '10px',
                          borderRadius: '50%',
                          backgroundColor: 'var(--color-safe-green-bright)',
                          boxShadow: '0 0 10px var(--color-safe-green-bright)'
                        }} />
                        <div>
                          <div style={{ fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>
                            Live Screen Target
                          </div>
                          <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary)' }}>
                            {currentSlide.keyVisual}
                          </div>
                        </div>
                      </div>

                      <button
                        onClick={() => {
                          jumpToSlideView(currentSlide.slideNumber);
                          setIsSlideSyncOpen(false);
                        }}
                        style={{
                          padding: '8px 16px',
                          borderRadius: 'var(--radius-sm)',
                          backgroundColor: 'var(--color-emergency-red)',
                          color: '#fff',
                          fontSize: '12px',
                          fontWeight: 700,
                          display: 'flex',
                          alignItems: 'center',
                          gap: '6px',
                          boxShadow: '0 0 16px rgba(255, 23, 68, 0.4)'
                        }}
                      >
                        <Zap size={14} />
                        <span>Activate View & Close</span>
                      </button>
                    </div>

                    {/* 30-Second Speaker Key-Bullet Cues */}
                    <div>
                      <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: '8px' }}>
                        Verbatim Speaker Notes & Key Cues
                      </div>

                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {currentSlide.bulletPoints.map((pt, idx) => (
                          <div
                            key={idx}
                            style={{
                              padding: '10px 14px',
                              borderRadius: 'var(--radius-sm)',
                              backgroundColor: 'rgba(255, 255, 255, 0.02)',
                              border: '1px solid var(--border-subtle)',
                              display: 'flex',
                              alignItems: 'flex-start',
                              gap: '10px',
                              fontSize: '13px',
                              lineHeight: '1.45',
                              color: 'var(--text-secondary)'
                            }}
                          >
                            <span style={{
                              color: 'var(--color-ai-cyan)',
                              fontWeight: 800,
                              fontSize: '12px',
                              marginTop: '1px'
                            }}>
                              {idx + 1}.
                            </span>
                            <span>{pt}</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* Direct Quick Jump Action */}
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: '8px', borderTop: '1px solid var(--border-subtle)' }}>
                      <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                        Alloted Speaking Clock: <strong style={{ color: 'var(--color-action-amber-bright)' }}>{currentSlide.timeWindow}</strong> ({currentSlide.duration})
                      </span>

                      <button
                        onClick={() => jumpToSlideView(currentSlide.slideNumber)}
                        style={{
                          padding: '6px 14px',
                          borderRadius: 'var(--radius-xs)',
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
                        <Play size={12} />
                        <span>Preview Slide Screen State</span>
                      </button>
                    </div>
                  </div>
                );
              })()}
            </div>
          )}

          {activeTab === 'EXAMINER_QA' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{
                padding: '12px 16px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'rgba(255, 23, 68, 0.1)',
                border: '1px solid var(--border-crimson)',
                display: 'flex',
                alignItems: 'center',
                gap: '12px'
              }}>
                <ShieldCheck size={22} style={{ color: 'var(--color-emergency-red-bright)', flexShrink: 0 }} />
                <div>
                  <div style={{ fontSize: '14px', fontWeight: 800, color: 'var(--text-primary)' }}>
                    Top 5 Examiner Viva Defense Responses (Authoritative Reference)
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                    Pre-baked mathematical, architectural, and legal answers to ace cross-questioning from review professors.
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {EXAMINER_QA_ITEMS.map((item, idx) => (
                  <div
                    key={idx}
                    style={{
                      padding: '16px',
                      borderRadius: 'var(--radius-lg)',
                      backgroundColor: 'var(--bg-card-dark)',
                      border: '1px solid var(--border-medium)',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '10px'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '12px' }}>
                      <h4 style={{ fontSize: '14px', fontWeight: 800, color: 'var(--color-action-amber-bright)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <span>Q{idx + 1}:</span>
                        <span>{item.question}</span>
                      </h4>

                      <span style={{
                        fontSize: '11px',
                        padding: '2px 8px',
                        borderRadius: 'var(--radius-full)',
                        backgroundColor: 'var(--bg-surface-elevated)',
                        color: 'var(--color-ai-cyan)',
                        border: '1px solid var(--border-ai)',
                        fontWeight: 700,
                        whiteSpace: 'nowrap'
                      }}>
                        Defender: {item.relevantSpeaker}
                      </span>
                    </div>

                    <div style={{
                      fontSize: '12px',
                      color: 'var(--text-muted)',
                      fontStyle: 'italic',
                      backgroundColor: 'rgba(0,0,0,0.3)',
                      padding: '6px 10px',
                      borderRadius: 'var(--radius-xs)',
                      borderLeft: '3px solid var(--color-action-amber)'
                    }}>
                      Examiner Doubt: {item.examinerDoubt}
                    </div>

                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {item.coreAnswer}
                    </div>

                    <div style={{
                      padding: '8px 12px',
                      borderRadius: 'var(--radius-sm)',
                      backgroundColor: 'rgba(0, 230, 118, 0.08)',
                      border: '1px solid var(--border-safe)',
                      fontSize: '12px',
                      color: 'var(--color-safe-green-bright)',
                      fontWeight: 600,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}>
                      <CheckCircle2 size={14} />
                      <span><strong>Key Metric / Proof:</strong> {item.technicalMetrics}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'SHORTCUTS' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                Use these hotkeys on any screen during the live presentation for lightning-fast zero-latency switching without having to click small controls.
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
                gap: '12px'
              }}>
                {[
                  { key: 'Space', desc: 'Toggle Auto-Progression Simulation (Play / Pause)' },
                  { key: 'ArrowRight', desc: 'Step Forward to Next Emergency Milestone' },
                  { key: '1', desc: 'Load Scenario 1: Cardiac Arrest in Salt Lake' },
                  { key: '2', desc: 'Load Scenario 2: Severe Arterial Bleed on EM Bypass' },
                  { key: '3', desc: 'Load Scenario 3: Offline BLE/SMS Mesh Fallback' },
                  { key: 'V', desc: 'Switch to Victim SOS & AI Triage Screen' },
                  { key: 'R', desc: 'Switch to Responder Alert & Navigation Screen' },
                  { key: 'C', desc: 'Switch to Command Center & Telemetry Screen' },
                  { key: 'M', desc: 'Switch to Dynamic Community Geo-Map' },
                  { key: 'G', desc: 'Switch to Guardian Radar Safe Zone' },
                  { key: 'P', desc: 'Toggle Projector High-Contrast 1080p Mode' },
                  { key: 'S', desc: 'Open / Close this Slide Sync HUD' },
                  { key: 'X', desc: 'Reset Demo to Initial Clean Standby' },
                ].map((item, idx) => (
                  <div
                    key={idx}
                    style={{
                      padding: '12px 16px',
                      borderRadius: 'var(--radius-md)',
                      backgroundColor: 'var(--bg-card-dark)',
                      border: '1px solid var(--border-subtle)',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px'
                    }}
                  >
                    <kbd style={{
                      padding: '4px 10px',
                      borderRadius: 'var(--radius-xs)',
                      backgroundColor: 'var(--bg-surface-elevated)',
                      border: '1px solid var(--border-medium)',
                      color: 'var(--color-ai-cyan)',
                      fontFamily: 'var(--font-mono)',
                      fontWeight: 800,
                      fontSize: '13px',
                      boxShadow: '0 2px 0 rgba(255,255,255,0.1)'
                    }}>
                      {item.key}
                    </kbd>
                    <span style={{ fontSize: '12px', color: 'var(--text-secondary)', fontWeight: 500 }}>
                      {item.desc}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div style={{
          padding: '12px 24px',
          borderTop: '1px solid var(--border-medium)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          backgroundColor: 'rgba(255, 255, 255, 0.02)',
          fontSize: '12px',
          color: 'var(--text-muted)'
        }}>
          <div>
            NearHelp AI Review Presentation • Saturday 22 Aug 2026 • CSE Dept Room 401
          </div>
          <button
            onClick={() => setIsSlideSyncOpen(false)}
            style={{
              padding: '6px 14px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'var(--bg-surface)',
              color: 'var(--text-primary)',
              border: '1px solid var(--border-subtle)',
              fontWeight: 600
            }}
          >
            Close (Esc)
          </button>
        </div>
      </div>
    </div>
  );
};
