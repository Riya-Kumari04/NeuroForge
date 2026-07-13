import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Users, Loader2, X, Trash2 } from 'lucide-react';
import { projectService, ProjectMember, Project } from '@/services/projectService';
import { organizationService, TeamMember } from '@/services/organizationService';
import ConfirmDialog from '@/components/projects/ConfirmDialog';
import { useToast } from '@/hooks/use-toast';

interface Props { project: Project }

// ─── Assign modal — pick an org TeamMember (Module 2) not yet on the project ──
function AssignMemberModal({ project, existingTeamMemberIds, onClose }: {
  project: Project;
  existingTeamMemberIds: number[];
  onClose: () => void;
}) {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [teamMemberId, setTeamMemberId] = useState('');

  const { data: orgMembersData, isLoading } = useQuery({
    queryKey: ['org-members', project.organizationId],
    queryFn: () => organizationService.getMembers(project.organizationId!).then(r => r.data),
    enabled: !!project.organizationId,
  });
  const orgMembers: TeamMember[] = orgMembersData?.data || [];
  const available = orgMembers.filter(m => !existingTeamMemberIds.includes(m.id));

  const mutation = useMutation({
    mutationFn: () => projectService.assignMember({
      projectId: project.id,
      teamMemberId: Number(teamMemberId),
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-members', project.id] });
      queryClient.invalidateQueries({ queryKey: ['project-stats', project.id] });
      toast({ title: 'Member assigned to project' });
      onClose();
    },
    onError: () => toast({ title: 'Error', description: 'Failed to assign member.', variant: 'destructive' }),
  });

  const inputClass = 'w-full bg-background border border-border rounded-lg px-3 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-[#111827] border border-border rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl">
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-base font-semibold text-white">Assign Team Member</h3>
          <button onClick={onClose}><X className="w-4 h-4 text-muted-foreground" /></button>
        </div>
        <div className="space-y-4">
          <div>
            <label className="text-xs font-medium text-white block mb-1.5">Organization Member</label>
            {isLoading ? (
              <div className="flex justify-center py-3"><Loader2 className="w-4 h-4 animate-spin text-primary" /></div>
            ) : available.length === 0 ? (
              <p className="text-xs text-muted-foreground">Every organization member is already assigned to this project.</p>
            ) : (
              <select className={inputClass} value={teamMemberId} onChange={e => setTeamMemberId(e.target.value)}>
                <option value="">Select a member…</option>
                {available.map(m => (
                  <option key={m.id} value={m.id}>{m.userName} — {m.userEmail}</option>
                ))}
              </select>
            )}
          </div>
          <button
            onClick={() => mutation.mutate()}
            disabled={!teamMemberId || mutation.isPending}
            className="w-full bg-primary text-white text-sm font-medium py-2.5 rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-60 flex items-center justify-center gap-2"
          >
            {mutation.isPending && <Loader2 className="w-4 h-4 animate-spin" />}
            Assign to Project
          </button>
        </div>
      </div>
    </div>
  );
}

export default function ProjectMembersTab({ project }: Props) {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [showAssign, setShowAssign] = useState(false);
  const [removeTarget, setRemoveTarget] = useState<ProjectMember | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['project-members', project.id],
    queryFn: () => projectService.getProjectMembers(project.id).then(r => r.data),
  });
  const members: ProjectMember[] = data?.data || [];

  const removeMutation = useMutation({
    mutationFn: (id: number) => projectService.removeMember(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-members', project.id] });
      queryClient.invalidateQueries({ queryKey: ['project-stats', project.id] });
      toast({ title: 'Member removed from project' });
      setRemoveTarget(null);
    },
  });

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h3 className="text-base font-semibold text-white">Project Members ({members.length})</h3>
        <button
          onClick={() => setShowAssign(true)}
          className="flex items-center gap-1.5 bg-primary text-white text-xs font-medium px-3 py-1.5 rounded-lg hover:bg-primary/90 transition-colors"
        >
          <Plus className="w-3.5 h-3.5" /> Assign Member
        </button>
      </div>

      {isLoading && <div className="flex justify-center py-10"><Loader2 className="w-5 h-5 animate-spin text-primary" /></div>}

      {!isLoading && members.length === 0 && (
        <div className="bg-card border border-dashed border-border rounded-xl p-10 text-center">
          <Users className="w-8 h-8 text-muted-foreground mx-auto mb-3" />
          <p className="text-sm font-medium text-white mb-1">No members assigned yet</p>
          <p className="text-xs text-muted-foreground">Assign organization members to this project so tasks can be delegated to them.</p>
        </div>
      )}

      {members.length > 0 && (
        <div className="bg-card border border-border rounded-xl overflow-hidden">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-background/50 border-b border-border text-xs uppercase tracking-wider text-muted-foreground">
                <th className="px-5 py-3 font-medium">Member</th>
                <th className="px-5 py-3 font-medium">Project Role</th>
                <th className="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/50 text-sm">
              {members.map(member => (
                <tr key={member.id} className="hover:bg-white/5 transition-colors">
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-xs flex-shrink-0">
                        {member.memberName?.charAt(0) || '?'}
                      </div>
                      <span className="font-medium text-white">{member.memberName}</span>
                    </div>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className="text-xs px-2.5 py-1 rounded-full border font-medium bg-blue-500/10 text-blue-400 border-blue-500/20">
                      {member.role}
                    </span>
                  </td>
                  <td className="px-5 py-3.5">
                    <div className="flex items-center justify-end">
                      <button
                        onClick={() => setRemoveTarget(member)}
                        className="p-1.5 rounded hover:bg-red-500/10 text-muted-foreground hover:text-red-400 transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showAssign && (
        <AssignMemberModal
          project={project}
          existingTeamMemberIds={members.map(m => m.teamMemberId)}
          onClose={() => setShowAssign(false)}
        />
      )}
      <ConfirmDialog
        open={!!removeTarget}
        title="Remove Member"
        message={`Remove "${removeTarget?.memberName}" from this project?`}
        confirmLabel="Remove"
        onConfirm={() => removeTarget && removeMutation.mutate(removeTarget.id)}
        onCancel={() => setRemoveTarget(null)}
        isLoading={removeMutation.isPending}
      />
    </div>
  );
}
