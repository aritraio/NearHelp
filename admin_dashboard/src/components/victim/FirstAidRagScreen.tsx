/* ==========================================================================
   NearHelp AI — Screen 3: Interactive Grounded First-Aid Protocol (RAG Assist)
   File: src/components/victim/FirstAidRagScreen.tsx
   ========================================================================== */

import React, { useState } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  Heart, 
  ShieldCheck, 
  Sparkles, 
  MessageSquare, 
  ChevronDown, 
  ChevronUp, 
  CheckCircle2, 
  AlertTriangle, 
  Volume2, 
  VolumeX, 
  Send, 
  X, 
  ArrowLeft
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const FirstAidRagScreen: React.FC = () => {
  const {
    currentScenario,
    cprMetronomeActive,
    cprBeatTick,
    audioMuted,
    completedRagSteps,
    isAiChatDrawerOpen,
    aiChatMessages,
    toggleCprMetronome,
    toggleAudioMute,
    toggleRagStep,
    setAiChatDrawerOpen,
    sendBystanderQuestion,
    setVictimSubScreen
  } = useDemoStore();

  const [expandedStepIndex, setExpandedStepIndex] = useState<number>(0);
  const [customQuestionInput, setCustomQuestionInput] = useState<string>('');

  const quickQuestions = [
    'Can I give water or oral medicine?',
    'How deep should chest compressions be?',
    'When and how do I use the AED?',
    'What if ribs crack during CPR?',
    'Am I legally protected if I help?'
  ];

  const handleSendCustomQuestion = (e: React.FormEvent) => {
    e.preventDefault();
    if (!customQuestionInput.trim()) return;
    sendBystanderQuestion(customQuestionInput.trim());
    setCustomQuestionInput('');
  };

  const steps = currentScenario.protocol.steps;
  const progressPercent = Math.round((completedRagSteps.length / steps.length) * 100);

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
      {/* 1. Header & Authority Tag */}
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
        <button
          onClick={() => {
            soundEngine.playClick();
            setVictimSubScreen('TRIAGE');
          }}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            color: '#00E5FF',
            fontSize: '11px',
            fontWeight: 800,
            cursor: 'pointer',
            backgroundColor: 'transparent',
            border: 'none',
            padding: 0
          }}
        >
          <ArrowLeft size={13} strokeWidth={2.8} />
          <span>Triage Status</span>
        </button>

        <div style={{
          fontSize: '9.5px',
          fontWeight: 700,
          color: '#00E5FF',
          backgroundColor: 'rgba(0, 229, 255, 0.12)',
          padding: '2px 8px',
          borderRadius: 'var(--radius-full)',
          border: '1px solid rgba(0, 229, 255, 0.3)'
        }}>
          WHO / IRC Protocol
        </div>
      </div>

      {/* 2. CPR Metronome Audio & Visual Bar (110 BPM) */}
      <div style={{
        backgroundColor: '#0C0E12',
        borderRadius: '14px',
        padding: '12px 14px',
        border: `1px solid ${cprMetronomeActive ? '#FF2A44' : 'rgba(255, 255, 255, 0.12)'}`,
        boxShadow: cprMetronomeActive ? '0 0 24px rgba(255, 42, 68, 0.35)' : '0 4px 14px rgba(0, 0, 0, 0.6)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexShrink: 0,
        transition: 'all 0.2s ease'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {/* Animated Heart Pulsing Badge */}
          <div
            className={cprMetronomeActive ? 'cpr-beat-active' : ''}
            style={{
              width: '42px',
              height: '42px',
              borderRadius: '50%',
              backgroundColor: cprMetronomeActive ? '#FF2A44' : '#1A1E26',
              border: `2px solid ${cprMetronomeActive ? '#FFFFFF' : 'rgba(255, 42, 68, 0.5)'}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0
            }}
          >
            <Heart size={20} fill={cprMetronomeActive ? '#FFFFFF' : '#FF2A44'} color="#FFFFFF" />
          </div>

          <div>
            <div style={{ fontSize: '13px', fontWeight: 900, display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span>CPR Rhythm Metronome</span>
              {cprMetronomeActive && (
                <span className="font-mono" style={{ fontSize: '10.5px', color: '#00E5FF' }}>
                  Beat #{cprBeatTick}
                </span>
              )}
            </div>
            <div style={{ fontSize: '10px', color: '#94A3B8' }}>
              110 Compressions / Min (AHA / ERC BLS Cadence)
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <button
            onClick={toggleAudioMute}
            style={{
              padding: '6px',
              borderRadius: '8px',
              backgroundColor: '#161922',
              border: '1px solid rgba(255, 255, 255, 0.08)',
              color: audioMuted ? '#FFA000' : '#94A3B8',
              cursor: 'pointer'
            }}
            title={audioMuted ? 'Unmute Audio' : 'Mute Audio'}
          >
            {audioMuted ? <VolumeX size={13} /> : <Volume2 size={13} />}
          </button>

          <button
            onClick={toggleCprMetronome}
            style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-full)',
              fontSize: '11px',
              fontWeight: 800,
              backgroundColor: cprMetronomeActive ? '#FF2A44' : '#00E676',
              color: cprMetronomeActive ? '#FFFFFF' : '#000000',
              boxShadow: cprMetronomeActive ? '0 0 12px rgba(255, 42, 68, 0.8)' : '0 2px 8px rgba(0, 230, 118, 0.4)',
              cursor: 'pointer'
            }}
          >
            {cprMetronomeActive ? 'STOP BEAT' : 'START BEAT'}
          </button>
        </div>
      </div>

      {/* 3. Interactive Protocol Checklist Progress Ring & Title */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 2px',
        flexShrink: 0
      }}>
        <div>
          <div style={{ fontSize: '12px', fontWeight: 900, color: '#FFFFFF' }}>
            STEP-BY-STEP ACTION PROTOCOL
          </div>
          <div style={{ fontSize: '10px', color: '#94A3B8' }}>
            Tap checkmark to verify completion ({completedRagSteps.length} of {steps.length} completed)
          </div>
        </div>

        <div style={{
          fontSize: '11px',
          fontWeight: 800,
          color: progressPercent === 100 ? '#00E676' : '#00E5FF',
          backgroundColor: '#0D0F14',
          padding: '3px 8px',
          borderRadius: 'var(--radius-full)',
          border: '1px solid rgba(255, 255, 255, 0.1)'
        }}>
          {progressPercent}% Complete
        </div>
      </div>

      {/* 4. Step-by-Step Interactive Cards */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        flexShrink: 0
      }}>
        {steps.map((step, idx) => {
          const isCompleted = completedRagSteps.includes(step.stepNumber);
          const isExpanded = expandedStepIndex === idx;

          return (
            <div
              key={step.stepNumber}
              style={{
                backgroundColor: '#0C0E12',
                borderRadius: '12px',
                padding: '10px 12px',
                border: isCompleted 
                  ? '1px solid rgba(0, 230, 118, 0.4)' 
                  : isExpanded 
                  ? '1px solid rgba(0, 229, 255, 0.4)' 
                  : '1px solid rgba(255, 255, 255, 0.08)',
                boxShadow: '0 4px 14px rgba(0, 0, 0, 0.5)',
                display: 'flex',
                flexDirection: 'column',
                gap: '6px',
                transition: 'all 0.18s ease'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1, minWidth: 0 }}>
                  {/* Interactive Checkbox */}
                  <button
                    onClick={() => toggleRagStep(step.stepNumber)}
                    style={{
                      width: '24px',
                      height: '24px',
                      borderRadius: '50%',
                      backgroundColor: isCompleted ? '#00E676' : '#1A1E26',
                      border: `1.5px solid ${isCompleted ? '#00E676' : 'rgba(255, 255, 255, 0.2)'}`,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      cursor: 'pointer',
                      flexShrink: 0
                    }}
                    title={isCompleted ? 'Mark step as incomplete' : 'Mark step as complete'}
                  >
                    {isCompleted ? <CheckCircle2 size={16} color="#000000" /> : <span style={{ fontSize: '10px', color: '#94A3B8', fontWeight: 800 }}>{step.stepNumber}</span>}
                  </button>

                  <div
                    onClick={() => setExpandedStepIndex(isExpanded ? -1 : idx)}
                    style={{ flex: 1, minWidth: 0, cursor: 'pointer' }}
                  >
                    <div style={{
                      fontSize: '12px',
                      fontWeight: 800,
                      color: isCompleted ? '#00E676' : '#FFFFFF',
                      textDecoration: isCompleted ? 'line-through' : 'none',
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis'
                    }}>
                      Step {step.stepNumber}: {step.title}
                    </div>
                  </div>
                </div>

                <button
                  onClick={() => setExpandedStepIndex(isExpanded ? -1 : idx)}
                  style={{
                    backgroundColor: 'transparent',
                    border: 'none',
                    color: '#94A3B8',
                    cursor: 'pointer',
                    padding: '2px'
                  }}
                >
                  {isExpanded ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
                </button>
              </div>

              {/* Expanded Action Instruction */}
              {isExpanded && (
                <div style={{
                  paddingTop: '6px',
                  borderTop: '1px solid rgba(255, 255, 255, 0.06)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '6px'
                }}>
                  <p style={{ fontSize: '11px', color: '#CBD5E1', lineHeight: 1.4, margin: 0 }}>
                    {step.actionInstruction}
                  </p>

                  {step.warningNote && (
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '5px',
                      padding: '5px 8px',
                      borderRadius: '6px',
                      backgroundColor: 'rgba(255, 160, 0, 0.12)',
                      border: '1px solid rgba(255, 160, 0, 0.3)',
                      fontSize: '10px',
                      color: '#FFD180'
                    }}>
                      <AlertTriangle size={12} color="#FFA000" style={{ flexShrink: 0 }} />
                      <span>{step.warningNote}</span>
                    </div>
                  )}

                  {step.isCprStep && (
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      backgroundColor: 'rgba(255, 42, 68, 0.1)',
                      padding: '6px 8px',
                      borderRadius: '6px',
                      border: '1px solid rgba(255, 42, 68, 0.25)'
                    }}>
                      <span style={{ fontSize: '10.5px', color: '#FF8090', fontWeight: 700 }}>
                        🫀 Continuous compression rate: 110–120 BPM
                      </span>
                      <button
                        onClick={toggleCprMetronome}
                        style={{
                          fontSize: '10px',
                          fontWeight: 800,
                          padding: '3px 8px',
                          borderRadius: 'var(--radius-full)',
                          backgroundColor: '#FF2A44',
                          color: '#FFFFFF',
                          cursor: 'pointer'
                        }}
                      >
                        {cprMetronomeActive ? 'Stop' : 'Play Metronome'}
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* 5. Good Samaritan Legal Shield Protection Badge (Section 134A) */}
      <div style={{
        borderRadius: '12px',
        padding: '10px 12px',
        border: '1px solid rgba(0, 230, 118, 0.3)',
        backgroundColor: 'rgba(0, 230, 118, 0.05)',
        display: 'flex',
        alignItems: 'flex-start',
        gap: '8px',
        flexShrink: 0
      }}>
        <ShieldCheck size={20} color="#00E676" style={{ flexShrink: 0, marginTop: '2px' }} />
        <div>
          <div style={{ fontSize: '11px', fontWeight: 800, color: '#00E676' }}>
            Good Samaritan Law Protection (Section 134A MV Act)
          </div>
          <div style={{ fontSize: '9.5px', color: '#CBD5E1', lineHeight: 1.35, marginTop: '2px' }}>
            Under Supreme Court of India 2016 Guidelines &amp; Section 134A, any bystander rendering emergency first-aid or CPR is fully immune from civil or criminal liability.
          </div>
        </div>
      </div>

      {/* 6. Quick AI Assistant Chat Trigger Pill */}
      <button
        onClick={() => setAiChatDrawerOpen(true)}
        style={{
          width: '100%',
          height: '42px',
          borderRadius: 'var(--radius-full)',
          backgroundColor: '#141720',
          border: '1px solid rgba(0, 229, 255, 0.4)',
          color: '#00E5FF',
          fontWeight: 800,
          fontSize: '12px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '6px',
          boxShadow: '0 4px 14px rgba(0, 229, 255, 0.2)',
          cursor: 'pointer',
          flexShrink: 0
        }}
      >
        <MessageSquare size={14} />
        <span>Ask Gemini First-Aid Assistant ("Can I give water?")</span>
        <Sparkles size={13} />
      </button>

      {/* ====================================================================
          Slide-Over AI Bystander Chat Drawer (Modal Overlay)
          ==================================================================== */}
      {isAiChatDrawerOpen && (
        <div style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.85)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)',
          zIndex: 100,
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'flex-end',
          padding: '10px'
        }}>
          <div style={{
            backgroundColor: '#0D0F14',
            borderRadius: '16px',
            border: '1px solid rgba(0, 229, 255, 0.35)',
            maxHeight: '90%',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            boxShadow: '0 10px 30px rgba(0, 0, 0, 0.9)'
          }}>
            {/* Drawer Header */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '10px 14px',
              borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
              backgroundColor: '#12151C'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Sparkles size={15} color="#00E5FF" />
                <span style={{ fontSize: '12.5px', fontWeight: 800, color: '#FFFFFF' }}>
                  Gemini First-Aid RAG Assistant
                </span>
              </div>

              <button
                onClick={() => setAiChatDrawerOpen(false)}
                style={{
                  padding: '4px',
                  borderRadius: '50%',
                  backgroundColor: '#1E232E',
                  color: '#FFFFFF',
                  cursor: 'pointer'
                }}
              >
                <X size={13} />
              </button>
            </div>

            {/* Chat Message List */}
            <div style={{
              flex: 1,
              overflowY: 'auto',
              padding: '12px',
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
              maxHeight: '260px'
            }}>
              {aiChatMessages.map((msg) => (
                <div
                  key={msg.id}
                  style={{
                    alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                    maxWidth: '88%',
                    backgroundColor: msg.sender === 'user' ? '#FF2A44' : '#161922',
                    color: '#FFFFFF',
                    padding: '8px 12px',
                    borderRadius: '12px',
                    border: msg.sender === 'gemini' ? '1px solid rgba(0, 229, 255, 0.25)' : 'none',
                    fontSize: '11px',
                    lineHeight: 1.35
                  }}
                >
                  {msg.highlightText && (
                    <div style={{
                      fontSize: '9px',
                      fontWeight: 800,
                      color: '#00E5FF',
                      marginBottom: '3px',
                      textTransform: 'uppercase'
                    }}>
                      • {msg.highlightText}
                    </div>
                  )}
                  <div>{msg.text}</div>
                </div>
              ))}
            </div>

            {/* Pre-Baked Quick Question Chips */}
            <div style={{
              padding: '8px 12px',
              borderTop: '1px solid rgba(255, 255, 255, 0.06)',
              backgroundColor: '#0F1218',
              display: 'flex',
              gap: '4px',
              overflowX: 'auto'
            }}>
              {quickQuestions.map((q, idx) => (
                <button
                  key={idx}
                  onClick={() => sendBystanderQuestion(q)}
                  style={{
                    padding: '4px 8px',
                    borderRadius: 'var(--radius-full)',
                    backgroundColor: '#1A1E26',
                    border: '1px solid rgba(0, 229, 255, 0.3)',
                    color: '#E2E8F0',
                    fontSize: '10px',
                    fontWeight: 700,
                    whiteSpace: 'nowrap',
                    cursor: 'pointer'
                  }}
                >
                  {q}
                </button>
              ))}
            </div>

            {/* Custom Question Input Form */}
            <form
              onSubmit={handleSendCustomQuestion}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '8px 12px',
                borderTop: '1px solid rgba(255, 255, 255, 0.08)',
                backgroundColor: '#12151C'
              }}
            >
              <input
                type="text"
                value={customQuestionInput}
                onChange={(e) => setCustomQuestionInput(e.target.value)}
                placeholder="Ask emergency bystander question..."
                style={{
                  flex: 1,
                  backgroundColor: '#0A0C10',
                  border: '1px solid rgba(255, 255, 255, 0.15)',
                  borderRadius: '8px',
                  padding: '6px 10px',
                  fontSize: '11px',
                  color: '#FFFFFF',
                  outline: 'none'
                }}
              />

              <button
                type="submit"
                style={{
                  padding: '6px 10px',
                  borderRadius: '8px',
                  backgroundColor: '#00E5FF',
                  color: '#000000',
                  fontWeight: 800,
                  fontSize: '11px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  cursor: 'pointer'
                }}
              >
                <Send size={12} />
                <span>Ask</span>
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
