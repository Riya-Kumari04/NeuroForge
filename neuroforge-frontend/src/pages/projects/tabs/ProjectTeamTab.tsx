import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Users, Search, Loader2 } from 'lucide-react';
import { organizationService, TeamMember } from '@/services/organizationService';
import { Project } from '@/services/projectService';

interface Props { project: Project }

const roleColors: Record<string, string> = {
  SUPER_ADMIN:     'bg-purple-500/10 text-purple-400 border-purple-500/20',
  ORG_ADMIN:       'bg-blue-500/10   text-blue-400   border-blue-500/20',
  PROJECT_MANAGER: 'bg-amber-500/10  text-amber-400  border-amber-500/20',
  DEVELOPER:       'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  TESTER:          'bg-rose-500/10   text-rose-400   border-rose-500/20',
  CLIENT:          'bg-slate-500/10  text-slate-400  border-slate-500/20',
};

export default function ProjectTeamTab({ project }: Props) {
  const [search, setSearch] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['org-members', project.organizationId],
    queryFn: () => organizationService.getMembers(project.organizationId!).then(r => r.data),
    enabled: !!project.organizationId,
  });
  const members: TeamMember[] = data?.data || [];

  const filtered = members.filter(m =>
    !search ||
    m.userName.toLowerCase().includes(search.toLowerCase()) ||
    m.userEmail.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h3 className="text-base font-semibold text-white">Organization Members ({members.length})</h3>
      </div>

      <div className="relative">
        <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
        <input
          type="text"
          placeholder="Search members..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="w-full max-w-sm bg-background border border-border rounded-lg pl-9 pr-4 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all"
        />
      </div>

      {isLoading && (
        <div className="flex justify-center py-10">
          <Loader2 className="w-5 h-5 animate-spin text-primary" />
        </div>
      )}

      {!isLoading && filtered.length === 0 && (
        <div className="bg-card border border-dashed border-border rounded-xl p-10 text-center">
          <Users className="w-8 h-8 text-muted-foreground mx-auto mb-3" />
          <p className="text-sm font-medium text-white mb-1">No members found</p>
          <p className="text-xs text-muted-foreground">Invite team members through the Organization settings.</p>
        </div>
      )}

      {filtered.length > 0 && (
        <div className="bg-card border border-border rounded-xl overflow-hidden">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-background/50 border-b border-border text-xs uppercase tracking-wider text-muted-foreground">
                <th className="px-5 py-3 font-medium">Member</th>
                <th className="px-5 py-3 font-medium">Email</th>
                <th className="px-5 py-3 font-medium">Role</th>
                <th className="px-5 py-3 font-medium">Joined</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/50 text-sm">
              {filtered.map(member => (
                <tr key={member.id} className="hover:bg-white/5 transition-colors">
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-xs flex-shrink-0">
                        {member.userName?.charAt(0) || '?'}
                      </div>
                      <span className="font-medium text-white">{member.userName}</span>
                    </div>
                  </td>
                  <td className="px-5 py-3.5 text-muted-foreground">{member.userEmail}</td>
                  <td className="px-5 py-3.5">
                    <span className={`text-xs px-2.5 py-1 rounded-full border font-medium ${roleColors[member.role] || 'bg-slate-500/10 text-slate-400 border-slate-500/20'}`}>
                      {member.role?.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-muted-foreground text-xs">
                    {member.joinedAt ? new Date(member.joinedAt).toLocaleDateString() : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
