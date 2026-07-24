import React from 'react';

interface Props {
  rows?: number;
  variant?: 'cards' | 'table' | 'detail';
}

export default function ProjectLoadingSkeleton({ rows = 6, variant = 'cards' }: Props) {
  if (variant === 'table') {
    return (
      <div className="bg-card border border-border rounded-xl overflow-hidden">
        <div className="bg-background/50 border-b border-border px-5 py-3 flex gap-10">
          {['Project', 'Organization', 'Status', 'Start', 'End', 'Actions'].map(h => (
            <div key={h} className="h-3 bg-white/5 rounded w-16 animate-pulse" />
          ))}
        </div>
        {Array.from({ length: rows }).map((_, i) => (
          <div key={i} className="border-b border-border/50 px-5 py-4 flex gap-10 items-center">
            <div className="flex-1 space-y-1.5">
              <div className="h-3.5 bg-white/5 rounded animate-pulse w-40" />
              <div className="h-2.5 bg-white/5 rounded animate-pulse w-64" />
            </div>
            <div className="h-3 bg-white/5 rounded animate-pulse w-24" />
            <div className="h-5 bg-white/5 rounded-full animate-pulse w-16" />
            <div className="h-3 bg-white/5 rounded animate-pulse w-20" />
            <div className="h-3 bg-white/5 rounded animate-pulse w-20" />
            <div className="h-7 bg-white/5 rounded animate-pulse w-20" />
          </div>
        ))}
      </div>
    );
  }

  if (variant === 'detail') {
    return (
      <div className="space-y-6">
        <div className="bg-card border border-border rounded-xl p-6 animate-pulse">
          <div className="h-6 bg-white/5 rounded w-48 mb-3" />
          <div className="h-4 bg-white/5 rounded w-full mb-2" />
          <div className="h-4 bg-white/5 rounded w-3/4" />
        </div>
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="bg-card border border-border rounded-xl p-4 animate-pulse">
            <div className="h-5 bg-white/5 rounded w-32 mb-3" />
            <div className="h-3 bg-white/5 rounded w-full mb-2" />
            <div className="h-3 bg-white/5 rounded w-2/3" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="bg-card border border-border rounded-xl p-5 animate-pulse">
          <div className="flex items-start justify-between mb-3">
            <div className="space-y-1.5 flex-1">
              <div className="h-4 bg-white/5 rounded w-3/4" />
              <div className="h-3 bg-white/5 rounded w-1/2" />
            </div>
            <div className="h-5 bg-white/5 rounded-full w-16 ml-3" />
          </div>
          <div className="space-y-1.5 mb-4">
            <div className="h-3 bg-white/5 rounded w-full" />
            <div className="h-3 bg-white/5 rounded w-4/5" />
          </div>
          <div className="h-2 bg-white/5 rounded-full w-full mb-4" />
          <div className="h-3 bg-white/5 rounded w-2/3" />
        </div>
      ))}
    </div>
  );
}
