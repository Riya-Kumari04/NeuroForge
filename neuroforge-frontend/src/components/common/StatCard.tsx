import React from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  badge?: string;
  badgeColor?: 'green' | 'red' | 'yellow' | 'blue';
  icon: React.ComponentType<{ className?: string }>;
  iconColor?: string;
}

export default function StatCard({ title, value, badge, badgeColor = 'blue', icon: Icon, iconColor = 'text-primary' }: StatCardProps) {
  const badgeColors = {
    green: 'bg-emerald-500/10 text-emerald-500',
    red: 'bg-rose-500/10 text-rose-500',
    yellow: 'bg-amber-500/10 text-amber-500',
    blue: 'bg-blue-500/10 text-blue-500',
  };

  return (
    <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
      <div className="flex items-center justify-between mb-4">
        <div className={`w-10 h-10 rounded-lg flex items-center justify-center bg-background border border-border`}>
          <Icon className={`w-5 h-5 ${iconColor}`} />
        </div>
        {badge && (
          <span className={`text-xs font-medium px-2.5 py-0.5 rounded-full ${badgeColors[badgeColor]}`}>
            {badge}
          </span>
        )}
      </div>
      <div>
        <p className="text-muted-foreground text-sm font-medium mb-1">{title}</p>
        <p className="text-3xl font-bold text-white">{value}</p>
      </div>
    </div>
  );
}
