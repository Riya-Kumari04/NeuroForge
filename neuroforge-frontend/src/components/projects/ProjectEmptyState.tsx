import React from 'react';
import { FolderKanban } from 'lucide-react';

interface Props {
  icon?: React.ElementType;
  title: string;
  message: string;
  action?: { label: string; onClick: () => void };
}

export default function ProjectEmptyState({ icon: Icon = FolderKanban, title, message, action }: Props) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-16 h-16 rounded-2xl bg-primary/10 flex items-center justify-center mb-4">
        <Icon className="w-8 h-8 text-primary/60" />
      </div>
      <h3 className="text-base font-semibold text-white mb-2">{title}</h3>
      <p className="text-sm text-muted-foreground max-w-sm">{message}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="mt-5 px-4 py-2 bg-primary text-white text-sm font-medium rounded-lg hover:bg-primary/90 transition-colors shadow-[0_0_15px_rgba(37,99,235,0.3)]"
        >
          {action.label}
        </button>
      )}
    </div>
  );
}
