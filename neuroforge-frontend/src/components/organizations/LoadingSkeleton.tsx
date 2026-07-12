import React from 'react';

interface LoadingSkeletonProps {
  rows?: number;
  className?: string;
}

export default function LoadingSkeleton({ rows = 3, className = '' }: LoadingSkeletonProps) {
  return (
    <div className={`space-y-4 ${className}`}>
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="bg-card border border-border rounded-xl p-5 animate-pulse">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-muted/30" />
            <div className="flex-1 space-y-2">
              <div className="h-4 bg-muted/30 rounded w-3/4" />
              <div className="h-3 bg-muted/20 rounded w-1/2" />
            </div>
          </div>
          <div className="mt-4 grid grid-cols-3 gap-3">
            <div className="h-3 bg-muted/20 rounded" />
            <div className="h-3 bg-muted/20 rounded" />
            <div className="h-3 bg-muted/20 rounded" />
          </div>
        </div>
      ))}
    </div>
  );
}
