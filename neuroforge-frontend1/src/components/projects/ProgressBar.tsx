import React from 'react';

interface ProgressBarProps {
  value: number;       // 0–100
  label?: string;
  showPercent?: boolean;
  color?: 'blue' | 'green' | 'amber' | 'red';
  size?: 'sm' | 'md' | 'lg';
}

export default function ProgressBar({
  value,
  label,
  showPercent = true,
  color = 'blue',
  size = 'md',
}: ProgressBarProps) {
  const clamp = Math.min(100, Math.max(0, value));

  const trackColors: Record<string, string> = {
    blue:  'bg-blue-600',
    green: 'bg-emerald-500',
    amber: 'bg-amber-500',
    red:   'bg-red-500',
  };

  const barH = size === 'sm' ? 'h-1.5' : size === 'lg' ? 'h-3' : 'h-2';

  // auto-color by value
  let autoColor = color;
  if (color === 'blue') {
    if (clamp >= 70) autoColor = 'green';
    else if (clamp >= 40) autoColor = 'amber';
    else autoColor = 'red';
  }

  return (
    <div className="w-full">
      {(label || showPercent) && (
        <div className="flex items-center justify-between mb-1.5">
          {label && <span className="text-xs text-muted-foreground">{label}</span>}
          {showPercent && (
            <span className="text-xs font-medium text-white">{clamp}%</span>
          )}
        </div>
      )}
      <div className={`w-full ${barH} bg-white/5 rounded-full overflow-hidden`}>
        <div
          className={`${barH} ${trackColors[autoColor]} rounded-full transition-all duration-500`}
          style={{ width: `${clamp}%` }}
        />
      </div>
    </div>
  );
}
