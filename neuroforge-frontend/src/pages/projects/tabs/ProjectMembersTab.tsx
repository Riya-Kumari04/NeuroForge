import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Users, Loader2, X, Trash2, UserCircle2, Shield, Users2, Calendar } from 'lucide-react';
import { projectService, ProjectMember, Project } from '@/services/projectService';
import { organizationService, TeamMember } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import { canManageMembers } from '@/lib/roleUtils';
import ConfirmDialog from '@/components/projects/ConfirmDialog';
import { useToast } from '@/hooks/use-toast';

interface Props { project: Project }

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

// ─── Assign Member Modal ──────────────────────────────────────────────────────
function AssignMemberModal({
  project,
  existingTeamMemberIds,
  onClose,
}: {
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
    mutationFn: () =>
      projectService.assignMember({
        projectId: project.id,
        teamMemberId: Number(teamMemberId),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-members', project.id] });
      queryClient.invalidateQueries({ queryKey: ['project-stats', project.id] });
      toast({ title: 'Member assigned to project' });
      onClose();
    },
    onError: (e: any) =>
      toast({ title: 'Error', description: e?.response?.data?.message || 'Failed to assign member.', variant: 'destructive' }),
  });

  const inputClass =
    'w-full bg-background border border-border rounded-lg px-3 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all';

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
              <div className="flex justify-center py-3">
                <Loader2 className="w-4 h-4 animate-spin text-primary" />
              </div>
            ) : !project.organizationId ? (
              <p className="text-xs text-muted-foreground">
                This project has no linked organization. Cannot assign members.
              </p>
            ) : available.length === 0 ? (
              <p className="text-xs text-muted-foreground">
                Every organization member is already assigned to this project.
              </p>
            ) : (
              <select
                className={inputClass}
                value={teamMemberId}
                onChange={e => setTeamMemberId(e.target.value)}
              >
                <option value="">Select a member…</option>
                {available.map(m => (
                  <option key={m.id} value={m.id}>
                    {m.userName} — {m.userEmail}
                  </option>
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

// ─── Main Tab ─────────────────────────────────────────────────────────────────
export default function ProjectMembersTab({ project }: Props) {
  const { role } = useAuth();
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const canManage = canManageMembers(role);

  const [showAssign, setShowAssign] = useState(false);
  const [removeTarget, setRemoveTarget] = useState<ProjectMember | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['project-members', project.id],
    queryFn: () => projectService.getProjectMembers(project.id).then(r => r.data),
    // Always refetch on mount so list stays fresh after assignment
    refetchOnMount: true,
  });

  const members: ProjectMember[] = data?.data || [];

  const removeMutation = useMutation({
    mutationFn: (projectMemberId: number) => projectService.removeMember(projectMemberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-members', project.id] });
      queryClient.invalidateQueries({ queryKey: ['project-stats', project.id] });
      toast({ title: 'Member removed from project' });
      setRemoveTarget(null);
    },
    onError: () =>
      toast({ title: 'Error', description: 'Failed to remove member.', variant: 'destructive' }),
  });

  return (
    <div>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="text-base font-semibold text-white">Project Members</h3>
          <p className="text-sm text-muted-foreground mt-0.5">
            {members.length} {members.length === 1 ? 'member' : 'members'} assigned
          </p>
        </div>
        {canManage && (
          <button
            onClick={() => setShowAssign(true)}
            className="flex items-center gap-2 bg-primary text-primary-foreground text-sm font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors"
          >
            <Plus className="w-4 h-4" /> Assign Member
          </button>
        )}
      </div>

      {isLoading ? (
        <div className="flex justify-center py-10">
          <Loader2 className="w-5 h-5 animate-spin text-primary" />
        </div>
      ) : members.length === 0 ? (
        <div className="text-center py-12 border border-dashed border-border rounded-xl">
          <Users className="w-10 h-10 text-muted-foreground mx-auto mb-3" />
          <p className="text-sm font-medium text-white mb-1">No members assigned</p>
          <p className="text-xs text-muted-foreground">
            {canManage ? 'Click "Assign Member" to add team members to this project.' : 'No team members have been assigned yet.'}
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-border">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-background/50">
                <th className="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Member
                </th>
                <th className="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Role
                </th>
                <th className="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Team
                </th>
                <th className="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Assigned Date
                </th>
                {canManage && (
                  <th className="px-5 py-3 text-right text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Actions
                  </th>
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {members.map(member => (
                <tr key={member.id} className="hover:bg-white/[0.02] transition-colors">
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-primary text-sm font-bold flex-shrink-0">
                        <UserCircle2 className="w-4 h-4" />
                      </div>
                      <div>
                        <span className="font-medium text-white">{member.memberName}</span>
                        {member.memberEmail && (
                          <p className="text-xs text-muted-foreground">{member.memberEmail}</p>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className="flex items-center gap-1 text-xs px-2.5 py-1 rounded-full border font-medium bg-blue-500/10 text-blue-400 border-blue-500/20 w-fit">
                      <Shield className="w-3 h-3" />
                      {member.role}
                    </span>
                  </td>
                  <td className="px-5 py-3.5">
                    {member.teamName ? (
                      <span className="flex items-center gap-1 text-xs text-muted-foreground">
                        <Users2 className="w-3.5 h-3.5" />
                        {member.teamName}
                      </span>
                    ) : (
                      <span className="text-xs text-muted-foreground">—</span>
                    )}
                  </td>
                  <td className="px-5 py-3.5">
                    <span className="flex items-center gap-1 text-xs text-muted-foreground">
                      <Calendar className="w-3.5 h-3.5" />
                      {formatDate(member.assignedAt)}
                    </span>
                  </td>
                  {canManage && (
                    <td className="px-5 py-3.5 text-right">
                      <button
                        onClick={() => setRemoveTarget(member)}
                        className="p-1.5 rounded hover:bg-red-500/10 text-muted-foreground hover:text-red-400 transition-colors"
                        title="Remove from project"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {canManage && showAssign && (
        <AssignMemberModal
          project={project}
          existingTeamMemberIds={members.map(m => m.teamMemberId)}
          onClose={() => setShowAssign(false)}
        />
      )}

      {canManage && (
        <ConfirmDialog
          open={!!removeTarget}
          title="Remove Member"
          message={`Remove "${removeTarget?.memberName}" from this project?`}
          confirmLabel="Remove"
          onConfirm={() => removeTarget && removeMutation.mutate(removeTarget.id)}
          onCancel={() => setRemoveTarget(null)}
          isLoading={removeMutation.isPending}
        />
      )}
    </div>
  );
}
