/* ==========================================================================
   NearHelp AI — Live Presenter Rehearsal Prompter & Dry Run Tour HUD
   File: src/components/demo/DryRunTourModal.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { MASTER_SLIDES_SYNC } from '../../mock/scenarios';
import { 
  Play, 
  Pause, 
  Square, 
  ChevronLeft, 
  ChevronRight, 
  Clock, 
  User, 
  ArrowRight, 
  Zap, 
  GraduationCap, 
  Timer 
} from 'lucide-react';

export const DryRunTourModal: React.FC = () => {
  const {
    isTourActive,
    tourSlideIndex,
    tourElapsedSeconds,
    tourPaceMode,
    isTourPaused,
    stopTour,
    toggleTourPause,
    nextTourSlide,
    prevTourSlide,
    setTourPaceMode,
    projectorMode
  } = useDemoStore();

  if (!isTourActive) return null;

  const currentSlide = MASTER_SLIDES_SYNC[tourSlideIndex] || MASTER_SLIDES_SYNC[0];
  const nextSlide = MASTER_SLIDES_SYNC[tourSlideIndex + 1];

  const getTargetDuration = () => {
    if (tourPaceMode === 'LIGHTNING_60S') return 8;
    if (tourPaceMode === 'EXPRESS_3M') return 22;
    const durations = [60, 75, 90, 75, 75, 75, 120, 90];
    return durations[tourSlideIndex] || 75;
  };

  const targetDuration = getTargetDuration();
  const progressPercent = Math.min(100, Math.round((tourElapsedSeconds / targetDuration) * 100));

  return (
    <div 
      className="animate-slide-in"
      style={{
        position: 'fixed',
        bottom: '20px',
        left: '50%',
        transform: 'translateX(-50%)',
        zIndex: 2000,
        width: 'calc(100% - 40px)',
        maxWidth: '1000px',
        backgroundColor: projectorMode ? 'rgba(5, 7, 10, 0.98)' : 'rgba(12, 15, 22, 0.96)',
        backdropFilter: 'blur(24px)',
        WebkitBackdropFilter: 'blur(24px)',
        border: `2px solid ${projectorMode ? 'var(--color-ai-cyan)' : 'var(--border-ai)'}`,
        borderRadius: 'var(--radius-xl)',
        padding: '16px 20px',
        boxShadow: '0 25px 60px rgba(0, 0, 0, 0.95), 0 0 35px rgba(0, 229, 255, 0.25)',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px'
      }}
    >
      {/* Top Bar: Slide Progress & Speaker Badge */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '10px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span style={{
            fontSize: '11px',
            fontWeight: 800,
            padding: '3px 9px',
            borderRadius: 'var(--radius-xs)',
            backgroundColor: 'var(--color-ai-cyan)',
            color: '#000',
            letterSpacing: '0.04em'
          }}>
            SLIDE {currentSlide.slideNumber} / 8
          </span>

          <span style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-primary)' }}>
            {currentSlide.title}
          </span>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
            ({currentSlide.topicNumber})
          </span>
        </div>

        {/* Pace Selector Pills */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{
            display: 'flex',
            backgroundColor: 'var(--bg-surface)',
            padding: '2px',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border-subtle)',
            gap: '2px'
          }}>
            <button
              onClick={() => setTourPaceMode('LIGHTNING_60S')}
              style={{
                padding: '4px 8px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '11px',
                fontWeight: tourPaceMode === 'LIGHTNING_60S' ? 800 : 500,
                backgroundColor: tourPaceMode === 'LIGHTNING_60S' ? 'var(--color-emergency-red)' : 'transparent',
                color: tourPaceMode === 'LIGHTNING_60S' ? '#fff' : 'var(--text-secondary)',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
              title="60-Second Lightning Showcase Tour"
            >
              <Zap size={11} />
              <span>60s Lightning</span>
            </button>

            <button
              onClick={() => setTourPaceMode('EXPRESS_3M')}
              style={{
                padding: '4px 8px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '11px',
                fontWeight: tourPaceMode === 'EXPRESS_3M' ? 800 : 500,
                backgroundColor: tourPaceMode === 'EXPRESS_3M' ? 'var(--color-action-amber)' : 'transparent',
                color: tourPaceMode === 'EXPRESS_3M' ? '#000' : 'var(--text-secondary)',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
              title="3-Minute Express Team Rehearsal"
            >
              <Timer size={11} />
              <span>3m Express</span>
            </button>

            <button
              onClick={() => setTourPaceMode('FULL_11M')}
              style={{
                padding: '4px 8px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '11px',
                fontWeight: tourPaceMode === 'FULL_11M' ? 800 : 500,
                backgroundColor: tourPaceMode === 'FULL_11M' ? 'var(--color-ai-cyan)' : 'transparent',
                color: tourPaceMode === 'FULL_11M' ? '#000' : 'var(--text-secondary)',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
              title="Full 11-Minute Official Review Choreography"
            >
              <GraduationCap size={11} />
              <span>11m Full Defense</span>
            </button>
          </div>
        </div>
      </div>

      {/* Center Row: Speaker Avatar & Verbatim Bullet Prompter */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '260px 1fr auto',
        gap: '16px',
        alignItems: 'center',
        backgroundColor: 'rgba(255, 255, 255, 0.02)',
        padding: '12px 16px',
        borderRadius: 'var(--radius-lg)',
        border: '1px solid var(--border-subtle)'
      }}>
        {/* Active Speaker Card */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{
              width: '32px',
              height: '32px',
              borderRadius: '50%',
              backgroundColor: 'rgba(0, 229, 255, 0.2)',
              border: '1.5px solid var(--color-ai-cyan)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-ai-cyan)'
            }}>
              <User size={16} />
            </div>

            <div>
              <div style={{ fontSize: '14px', fontWeight: 800, color: 'var(--color-ai-cyan)' }}>
                {currentSlide.presenter}
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                {currentSlide.presenterRole}
              </div>
            </div>
          </div>

          <div style={{ fontSize: '11px', color: 'var(--color-action-amber-bright)', fontWeight: 700, marginTop: '2px', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Clock size={11} />
            <span>Clock: {tourElapsedSeconds}s / {targetDuration}s</span>
          </div>
        </div>

        {/* Live Speaking Points Prompter */}
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '3px',
          overflow: 'hidden'
        }}>
          <div style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 800, letterSpacing: '0.04em' }}>
            Speaking Prompter ({currentSlide.presenter})
          </div>
          <div style={{
            fontSize: '13px',
            fontWeight: 600,
            color: 'var(--text-primary)',
            lineHeight: '1.4',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis'
          }}>
            {currentSlide.bulletPoints[0]}
          </div>
          <div style={{
            fontSize: '11px',
            color: 'var(--text-secondary)',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis'
          }}>
            {currentSlide.bulletPoints[1]}
          </div>
        </div>

        {/* Handoff Notice */}
        {nextSlide && (
          <div style={{
            padding: '6px 12px',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'var(--bg-surface-elevated)',
            border: '1px solid var(--border-subtle)',
            fontSize: '11px',
            textAlign: 'right',
            flexShrink: 0
          }}>
            <div style={{ color: 'var(--text-muted)', fontSize: '10px', textTransform: 'uppercase', fontWeight: 700 }}>
              Next Speaker
            </div>
            <div style={{ color: 'var(--text-primary)', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '4px' }}>
              <span>{nextSlide.presenter}</span>
              <ArrowRight size={11} style={{ color: 'var(--color-ai-cyan)' }} />
            </div>
          </div>
        )}
      </div>

      {/* Progress Bar & Tour Navigation Buttons */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '16px' }}>
        {/* Progress Fill Line */}
        <div style={{ flex: 1, height: '6px', backgroundColor: 'var(--bg-surface)', borderRadius: '3px', overflow: 'hidden' }}>
          <div 
            style={{
              height: '100%',
              width: `${progressPercent}%`,
              backgroundColor: 'var(--color-ai-cyan)',
              borderRadius: '3px',
              transition: 'width 1s linear',
              boxShadow: '0 0 10px var(--color-ai-cyan)'
            }}
          />
        </div>

        {/* Transport Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <button
            onClick={prevTourSlide}
            disabled={tourSlideIndex === 0}
            style={{
              padding: '6px 10px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              color: tourSlideIndex === 0 ? 'var(--text-disabled)' : 'var(--text-primary)',
              fontSize: '11px',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}
            title="Previous Slide (Left Arrow)"
          >
            <ChevronLeft size={13} />
            <span>Prev</span>
          </button>

          <button
            onClick={toggleTourPause}
            style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: isTourPaused ? 'var(--color-safe-green)' : 'var(--bg-surface-elevated)',
              border: '1px solid var(--border-medium)',
              color: isTourPaused ? '#000' : 'var(--text-primary)',
              fontSize: '11px',
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              gap: '5px'
            }}
            title="Pause or Resume Tour (Space)"
          >
            {isTourPaused ? <Play size={13} /> : <Pause size={13} />}
            <span>{isTourPaused ? 'Resume' : 'Pause'}</span>
          </button>

          <button
            onClick={nextTourSlide}
            disabled={tourSlideIndex === MASTER_SLIDES_SYNC.length - 1}
            style={{
              padding: '6px 10px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              color: tourSlideIndex === MASTER_SLIDES_SYNC.length - 1 ? 'var(--text-disabled)' : 'var(--text-primary)',
              fontSize: '11px',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}
            title="Next Slide (Right Arrow)"
          >
            <span>Next</span>
            <ChevronRight size={13} />
          </button>

          <button
            onClick={stopTour}
            style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'rgba(255, 23, 68, 0.15)',
              border: '1px solid var(--border-crimson)',
              color: 'var(--color-emergency-red-bright)',
              fontSize: '11px',
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}
            title="Exit Tour (Key: T or X)"
          >
            <Square size={12} />
            <span>Exit Tour</span>
          </button>
        </div>
      </div>
    </div>
  );
};
