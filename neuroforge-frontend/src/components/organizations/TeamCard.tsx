import React from 'react';
import { Users2, UserCircle2, Pencil, Trash2, UserPlus, UserMinus, Eye, Calendar } from 'lucide-react';
import { Team } from '@/services/organizationService';

interface TeamCardProps {
  team: Team;
  canEdit?: boolean;
  onView?: (team: Team) => void;
  onEdit?: (team: Team) => void;
  onDelete?: (team: Team) => void;
  onAssignMembers?: (team: Team) => void;
  onRemoveMembers?: (team: Team) => void;
}

function formatDate(dateStr?: string) {
  if (!dateStr) return '—';
  try {
    return new Date(dateStr).toLocaleDateString(undefined, {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  } catch {
    return '—';
  }
}

export default function TeamCard({
  team,
  canEdit,
  onView,
  onEdit,
  onDelete,
  onAssignMembers,
  onRemoveMembers,
}: TeamCardProps) {
  return (
    <div className="bg-card border border-border rounded-xl p-5 hover:border-primary/30 transition-all flex flex-col gap-3">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-purple-500/10 flex items-center justify-center flex-shrink-0">
            <Users2 className="w-4 h-4 text-purple-400" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-white leading-tight">{team.name}</h3>
            {team.leadName && (
              <p className="text-xs text-muted-foreground flex items-center gap-1 mt-0.5">
                <UserCircle2 className="w-3 h-3" />
                {team.leadName}
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Description */}
      {team.description && (
        <p className="text-xs text-muted-foreground line-clamp-2 leading-relaxed">
          {team.description}
        </p>
      )}

      {/* Stats row */}
      <div className="flex items-center gap-4 text-xs text-muted-foreground border-t border-border/50 pt-3">
        <span className="flex items-center gap-1">
          <Users2 className="w-3 h-3" />
          {team.membersCount} {team.membersCount === 1 ? 'member' : 'members'}
        </span>
        {team.createdAt && (
          <span className="flex items-center gap-1">
            <Calendar className="w-3 h-3" />
            {formatDate(team.createdAt)}
          </span>
        )}
      </div>

      {/* Action buttons */}
      {canEdit && (
        <div className="flex items-center gap-1 flex-wrap border-t border-border/50 pt-3">
          {onView && (
            <button
              onClick={() => onView(team)}
              title="View Details"
              className="flex items-center gap-1.5 px-2.5 py-1.5 text-xs text-muted-foreground hover:text-white hover:bg-white/5 rounded-lg transition-colors"
            >
              <Eye className="w-3.5 h-3.5" /> Details
            </button>
          )}
          {onAssignMembers && (
            <button
              onClick={() => onAssignMembers(team)}
              title="Assign Members"
              className="flex items-center gap-1.5 px-2.5 py-1.5 text-xs text-muted-foreground hover:text-primary hover:bg-primary/10 rounded-lg transition-colors"
            >
              <UserPlus className="w-3.5 h-3.5" /> Assign
            </button>
          )}
          {onRemoveMembers && (
            <button
              onClick={() => onRemoveMembers(team)}
              title="Remove Members"
              className="flex items-center gap-1.5 px-2.5 py-1.5 text-xs text-muted-foreground hover:text-orange-400 hover:bg-orange-500/10 rounded-lg transition-colors"
            >
              <UserMinus className="w-3.5 h-3.5" /> Remove
            </button>
          )}
          <div className="ml-auto flex items-center gap-1">
            {onEdit && (
              <button
                onClick={() => onEdit(team)}
                title="Edit Team"
                className="p-1.5 text-muted-foreground hover:text-white hover:bg-white/5 rounded-lg transition-colors"
              >
                <Pencil className="w-3.5 h-3.5" />
              </button>
            )}
            {onDelete && (
              <button
                onClick={() => onDelete(team)}
                title="Delete Team"
                className="p-1.5 text-muted-foreground hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
