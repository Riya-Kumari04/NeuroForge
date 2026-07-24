import React from 'react';

interface HealthBadgeProps {
  status: string;
  score?: number;
  size?: 'sm' | 'md' | 'lg';
}

export default function HealthBadge({ status, score, size = 'md' }: HealthBadgeProps) {
  const colors: Record<string, string> = {
    HEALTHY:  'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    AT_RISK:  'bg-amber-500/10  text-amber-400  border-amber-500/20',
    CRITICAL: 'bg-red-500/10    text-red-400    border-red-500/20',
    ACTIVE:   'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    INACTIVE: 'bg-slate-500/10  text-slate-400  border-slate-500/20',
    ON_HOLD:  'bg-amber-500/10  text-amber-400  border-amber-500/20',
    COMPLETED:'bg-blue-500/10   text-blue-400   border-blue-500/20',
    ARCHIVED: 'bg-slate-500/10  text-slate-400  border-slate-500/20',
  };

  const dotColors: Record<string, string> = {
    HEALTHY:   'bg-emerald-500',
    AT_RISK:   'bg-amber-500',
    CRITICAL:  'bg-red-500',
    ACTIVE:    'bg-emerald-500',
    INACTIVE:  'bg-slate-500',
    ON_HOLD:   'bg-amber-500',
    COMPLETED: 'bg-blue-500',
    ARCHIVED:  'bg-slate-500',
  };

  const sizeClass = size === 'sm' ? 'text-[10px] px-2 py-0.5' : size === 'lg' ? 'text-sm px-3 py-1.5' : 'text-xs px-2.5 py-1';

  const key = status?.toUpperCase() || 'ACTIVE';
  const colorClass = colors[key] || 'bg-slate-500/10 text-slate-400 border-slate-500/20';
  const dotClass = dotColors[key] || 'bg-slate-500';

  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border font-medium ${colorClass} ${sizeClass}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${dotClass}`} />
      {score !== undefined ? `${status} (${score}%)` : status}
    </span>
  );
}
