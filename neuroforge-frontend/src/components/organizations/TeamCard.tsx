import React from 'react';
import { Users2, UserCircle2, Pencil, Trash2 } from 'lucide-react';
import { Team } from '@/services/organizationService';

interface TeamCardProps {
  team: Team;
  canEdit?: boolean;
  onEdit?: (team: Team) => void;
  onDelete?: (team: Team) => void;
}

export default function TeamCard({ team, canEdit, onEdit, onDelete }: TeamCardProps) {
  return (
    <div className="bg-card border border-border rounded-xl p-5 hover:border-primary/30 transition-all">
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-purple-500/10 flex items-center justify-center">
            <Users2 className="w-4 h-4 text-purple-400" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-white">{team.name}</h3>
            {team.leadName && (
              <p className="text-xs text-muted-foreground flex items-center gap-1">
                <UserCircle2 className="w-3 h-3" /> {team.leadName}
              </p>
            )}
          </div>
        </div>
        {canEdit && (
          <div className="flex items-center gap-1">
            {onEdit && (
              <button onClick={() => onEdit(team)} className="p-1.5 text-muted-foreground hover:text-white hover:bg-white/5 rounded-lg transition-colors">
                <Pencil className="w-3.5 h-3.5" />
              </button>
            )}
            {onDelete && (
              <button onClick={() => onDelete(team)} className="p-1.5 text-muted-foreground hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors">
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        )}
      </div>

      {team.description && <p className="text-xs text-muted-foreground mb-3 line-clamp-2">{team.description}</p>}

      <div className="flex items-center gap-1 text-xs text-muted-foreground pt-3 border-t border-border/50">
        <Users2 className="w-3 h-3" />
        <span>{team.membersCount} members</span>
      </div>
    </div>
  );
}
