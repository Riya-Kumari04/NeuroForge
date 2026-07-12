import React from 'react';
import { UserCircle2, Trash2 } from 'lucide-react';
import { TeamMember, OrgRole } from '@/services/organizationService';

interface MemberCardProps {
  member: TeamMember;
  canRemove?: boolean;
  onRemove?: (member: TeamMember) => void;
}

const roleColors: Record<OrgRole, string> = {
  SUPER_ADMIN:     'bg-red-500/20 text-red-400 border-red-500/30',
  ORG_ADMIN:       'bg-purple-500/20 text-purple-400 border-purple-500/30',
  PROJECT_MANAGER: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
  DEVELOPER:       'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
  TESTER:          'bg-orange-500/20 text-orange-400 border-orange-500/30',
  CLIENT:          'bg-slate-500/20 text-slate-400 border-slate-500/30',
};

const roleLabels: Record<OrgRole, string> = {
  SUPER_ADMIN:     'Super Admin',
  ORG_ADMIN:       'Org Admin',
  PROJECT_MANAGER: 'Project Manager',
  DEVELOPER:       'Developer',
  TESTER:          'Tester',
  CLIENT:          'Client',
};

export default function MemberCard({ member, canRemove, onRemove }: MemberCardProps) {
  const roleClass = roleColors[member.role] || 'bg-slate-500/20 text-slate-400 border-slate-500/30';
  const roleLabel = roleLabels[member.role] || member.role;
  return (
    <div className="flex items-center gap-3 px-4 py-3 bg-card border border-border rounded-xl hover:border-border/80 transition-all">
      <div className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-sm flex-shrink-0">
        {member.userName?.charAt(0) || <UserCircle2 className="w-5 h-5" />}
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-white truncate">{member.userName}</p>
        <p className="text-xs text-muted-foreground truncate">{member.userEmail}</p>
      </div>
      <div className="flex items-center gap-2 flex-shrink-0">
        <span className={`text-xs font-medium px-2 py-0.5 rounded-full border ${roleClass}`}>{roleLabel}</span>
        {canRemove && onRemove && (
          <button onClick={() => onRemove(member)} className="p-1.5 text-muted-foreground hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors">
            <Trash2 className="w-3.5 h-3.5" />
          </button>
        )}
      </div>
    </div>
  );
}
