import React from 'react';

interface EmptyStateProps {
  title: string;
  description?: string;
  icon?: React.ComponentType<{ className?: string }>;
  action?: { label: string; onClick: () => void };
}

export default function EmptyState({ title, description, icon: Icon, action }: EmptyStateProps) {
  return (
    <div className="bg-card border border-border rounded-xl p-8 shadow-sm flex flex-col items-center justify-center text-center">
      {Icon && (
        <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
          <Icon className="w-6 h-6 text-primary" />
        </div>
      )}
      <h3 className="text-white font-medium mb-1">{title}</h3>
      {description && <p className="text-muted-foreground text-sm max-w-sm mb-4">{description}</p>}
      {action && (
        <button
          onClick={action.onClick}
          className="bg-primary text-primary-foreground text-sm font-medium px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
        >
          {action.label}
        </button>
      )}
    </div>
  );
}
