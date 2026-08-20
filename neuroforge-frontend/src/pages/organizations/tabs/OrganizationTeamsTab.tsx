import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Plus, Users2, Loader2, X, UserCircle2, Calendar, Users, Shield,
} from 'lucide-react';
import {
  organizationService, Team, TeamMember, CreateTeamRequest,
} from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import TeamCard from '@/components/organizations/TeamCard';
import EmptyState from '@/components/organizations/EmptyState';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';
import ConfirmDeleteModal from '@/components/organizations/ConfirmDeleteModal';

interface Props { orgId: number; }

// ─── Team Details Modal ────────────────────────────────────────────────────────
function TeamDetailsModal({
  team,
  orgId,
  onClose,
  canEdit,
}: { team: Team; orgId: number; onClose: () => void; canEdit: boolean }) {
  const { data, isLoading } = useQuery({
    queryKey: ['team-members', orgId, team.id],
    queryFn: () => organizationService.getTeamMembers(orgId, team.id).then(r => r.data),
  });
  const members: TeamMember[] = data?.data || [];

  function formatDate(d?: string) {
    if (!d) return '—';
    try { return new Date(d).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' }); }
    catch { return '—'; }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-[#111827] border border-border rounded-2xl p-6 w-full max-w-lg mx-4 shadow-2xl max-h-[80vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-base font-semibold text-white">Team Details</h3>
          <button onClick={onClose}><X className="w-4 h-4 text-muted-foreground" /></button>
        </div>

        {/* Team info */}
        <div className="bg-background/50 border border-border rounded-xl p-4 mb-4 space-y-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-purple-500/10 flex items-center justify-center">
              <Users2 className="w-5 h-5 text-purple-400" />
            </div>
            <div>
              <p className="text-sm font-semibold text-white">{team.name}</p>
              {team.description && <p className="text-xs text-muted-foreground">{team.description}</p>}
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3 text-xs">
            <div className="flex items-center gap-1.5 text-muted-foreground">
              <UserCircle2 className="w-3.5 h-3.5" />
              <span>Lead: <span className="text-white">{team.leadName || 'None'}</span></span>
            </div>
            <div className="flex items-center gap-1.5 text-muted-foreground">
              <Users className="w-3.5 h-3.5" />
              <span>Members: <span className="text-white">{team.membersCount}</span></span>
            </div>
            <div className="flex items-center gap-1.5 text-muted-foreground col-span-2">
              <Calendar className="w-3.5 h-3.5" />
              <span>Created: <span className="text-white">{formatDate(team.createdAt)}</span></span>
            </div>
          </div>
        </div>

        {/* Members list */}
        <h4 className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-3">Members List</h4>
        {isLoading ? (
          <div className="flex justify-center py-4"><Loader2 className="w-4 h-4 animate-spin text-primary" /></div>
        ) : members.length === 0 ? (
          <p className="text-xs text-muted-foreground text-center py-4">No members assigned to this team yet.</p>
        ) : (
          <div className="space-y-2">
            {members.map(m => (
              <div key={m.id} className="flex items-center justify-between bg-background/50 border border-border rounded-lg px-3 py-2">
                <div>
                  <p className="text-sm font-medium text-white">{m.userName}</p>
                  <p className="text-xs text-muted-foreground">{m.userEmail}</p>
                </div>
                <div className="flex items-center gap-1.5">
                  <Shield className="w-3 h-3 text-muted-foreground" />
                  <span className="text-xs text-muted-foreground capitalize">{m.role?.toLowerCase().replace(/_/g, ' ')}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// ─── Assign / Remove Members Modal ──────────────────────────────────────────
function ManageMembersModal({
  team,
  orgId,
  mode,
  onClose,
}: { team: Team; orgId: number; mode: 'assign' | 'remove'; onClose: () => void }) {
  const qc = useQueryClient();
  const { toast } = useToast();

  const { data: allMembersData, isLoading: loadingAll } = useQuery({
    queryKey: ['org-members', orgId],
    queryFn: () => organizationService.getMembers(orgId).then(r => r.data),
  });
  const { data: teamMembersData, isLoading: loadingTeam } = useQuery({
    queryKey: ['team-members', orgId, team.id],
    queryFn: () => organizationService.getTeamMembers(orgId, team.id).then(r => r.data),
  });

  const allMembers: TeamMember[] = allMembersData?.data || [];
  const teamMembers: TeamMember[] = teamMembersData?.data || [];
  const teamMemberIds = new Set(teamMembers.map(m => m.id));

  const available = mode === 'assign'
    ? allMembers.filter(m => !teamMemberIds.has(m.id))
    : teamMembers;

  const assignMut = useMutation({
    mutationFn: (memberId: number) => organizationService.addTeamMember(orgId, team.id, memberId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['team-members', orgId, team.id] });
      qc.invalidateQueries({ queryKey: ['org-teams', orgId] });
      toast({ title: 'Member assigned to team' });
    },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const removeMut = useMutation({
    mutationFn: (memberId: number) => organizationService.removeTeamMember(orgId, team.id, memberId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['team-members', orgId, team.id] });
      qc.invalidateQueries({ queryKey: ['org-teams', orgId] });
      toast({ title: 'Member removed from team' });
    },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const isLoading = loadingAll || loadingTeam;
  const isPending = assignMut.isPending || removeMut.isPending;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-[#111827] border border-border rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl max-h-[70vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-base font-semibold text-white">
            {mode === 'assign' ? 'Assign Members to Team' : 'Remove Members from Team'}
          </h3>
          <button onClick={onClose}><X className="w-4 h-4 text-muted-foreground" /></button>
        </div>
        <p className="text-xs text-muted-foreground mb-4">
          Team: <span className="text-white font-medium">{team.name}</span>
        </p>

        {isLoading ? (
          <div className="flex justify-center py-6"><Loader2 className="w-5 h-5 animate-spin text-primary" /></div>
        ) : available.length === 0 ? (
          <p className="text-xs text-muted-foreground text-center py-6">
            {mode === 'assign' ? 'All org members are already in this team.' : 'This team has no members to remove.'}
          </p>
        ) : (
          <div className="space-y-2">
            {available.map(m => (
              <div key={m.id} className="flex items-center justify-between bg-background/50 border border-border rounded-lg px-3 py-2">
                <div>
                  <p className="text-sm font-medium text-white">{m.userName}</p>
                  <p className="text-xs text-muted-foreground">{m.userEmail}</p>
                </div>
                <button
                  onClick={() => mode === 'assign' ? assignMut.mutate(m.id) : removeMut.mutate(m.id)}
                  disabled={isPending}
                  className={`text-xs font-medium px-3 py-1.5 rounded-lg transition-colors disabled:opacity-60 ${
                    mode === 'assign'
                      ? 'bg-primary/20 text-primary hover:bg-primary/30'
                      : 'bg-red-500/10 text-red-400 hover:bg-red-500/20'
                  }`}
                >
                  {isPending ? <Loader2 className="w-3 h-3 animate-spin" /> : mode === 'assign' ? 'Add' : 'Remove'}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// ─── Main Tab ─────────────────────────────────────────────────────────────────
export default function OrganizationTeamsTab({ orgId }: Props) {
  const { role } = useAuth();
  const { toast } = useToast();
  const qc = useQueryClient();
  const canEdit = role === 'super-admin' || role === 'org-admin';

  const [showForm, setShowForm] = useState(false);
  const [editTeam, setEditTeam] = useState<Team | null>(null);
  const [deleteTeam, setDeleteTeam] = useState<Team | null>(null);
  const [viewTeam, setViewTeam] = useState<Team | null>(null);
  const [manageTeam, setManageTeam] = useState<{ team: Team; mode: 'assign' | 'remove' } | null>(null);

  const emptyForm: CreateTeamRequest = { name: '', description: '', leadId: undefined, initialMemberIds: [] };
  const [formData, setFormData] = useState<CreateTeamRequest>(emptyForm);
  const [selectedInitialMembers, setSelectedInitialMembers] = useState<number[]>([]);

  const { data, isLoading } = useQuery({
    queryKey: ['org-teams', orgId],
    queryFn: () => organizationService.getTeams(orgId).then(r => r.data),
  });
  const { data: membersData } = useQuery({
    queryKey: ['org-members', orgId],
    queryFn: () => organizationService.getMembers(orgId).then(r => r.data),
  });

  const teams: Team[] = data?.data || [];
  const orgMembers: TeamMember[] = membersData?.data || [];

  const resetForm = () => {
    setFormData(emptyForm);
    setSelectedInitialMembers([]);
    setShowForm(false);
    setEditTeam(null);
  };

  const createMut = useMutation({
    mutationFn: (d: CreateTeamRequest) => organizationService.createTeam(orgId, d),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['org-teams', orgId] });
      resetForm();
      toast({ title: 'Team created' });
    },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const updateMut = useMutation({
    mutationFn: ({ teamId, data }: { teamId: number; data: Partial<CreateTeamRequest> }) =>
      organizationService.updateTeam(orgId, teamId, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['org-teams', orgId] });
      resetForm();
      toast({ title: 'Team updated' });
    },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const deleteMut = useMutation({
    mutationFn: (teamId: number) => organizationService.deleteTeam(orgId, teamId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['org-teams', orgId] });
      setDeleteTeam(null);
      toast({ title: 'Team deleted' });
    },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const toggleInitialMember = (id: number) => {
    setSelectedInitialMembers(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const handleSubmit = () => {
    const payload: CreateTeamRequest = {
      ...formData,
      initialMemberIds: editTeam ? undefined : selectedInitialMembers,
    };
    if (editTeam) {
      updateMut.mutate({ teamId: editTeam.id, data: payload });
    } else {
      createMut.mutate(payload);
    }
  };

  const inputClass =
    'w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all';

  if (isLoading) return <LoadingSkeleton rows={3} />;

  return (
    <div>
      {canEdit && (
        <div className="mb-5 flex justify-end">
          <button
            onClick={() => { setShowForm(true); setEditTeam(null); setFormData(emptyForm); setSelectedInitialMembers([]); }}
            className="flex items-center gap-2 bg-primary text-primary-foreground text-sm font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors"
          >
            <Plus className="w-4 h-4" /> New Team
          </button>
        </div>
      )}

      {(showForm || editTeam) && canEdit && (
        <div className="bg-card border border-primary/30 rounded-xl p-5 mb-5">
          <h3 className="text-sm font-semibold text-white mb-4">
            {editTeam ? 'Edit Team' : 'Create New Team'}
          </h3>
          <div className="space-y-3">
            {/* Team Name */}
            <div>
              <label className="text-xs font-medium text-white block mb-1.5">Team Name *</label>
              <input
                type="text"
                value={formData.name}
                onChange={e => setFormData(p => ({ ...p, name: e.target.value }))}
                placeholder="e.g. Frontend Team"
                className={inputClass}
              />
            </div>

            {/* Description */}
            <div>
              <label className="text-xs font-medium text-white block mb-1.5">Description</label>
              <textarea
                value={formData.description || ''}
                onChange={e => setFormData(p => ({ ...p, description: e.target.value }))}
                placeholder="Describe the team's purpose..."
                rows={2}
                className={`${inputClass} resize-none`}
              />
            </div>

            {/* Team Lead */}
            <div>
              <label className="text-xs font-medium text-white block mb-1.5">Team Lead</label>
              <select
                value={formData.leadId ?? ''}
                onChange={e => setFormData(p => ({ ...p, leadId: e.target.value ? Number(e.target.value) : undefined }))}
                className={inputClass}
              >
                <option value="">No lead assigned</option>
                {orgMembers.map(m => (
                  <option key={m.id} value={m.userId}>{m.userName} ({m.userEmail})</option>
                ))}
              </select>
            </div>

            {/* Initial Members (create only) */}
            {!editTeam && orgMembers.length > 0 && (
              <div>
                <label className="text-xs font-medium text-white block mb-1.5">
                  Initial Members (optional)
                </label>
                <div className="max-h-36 overflow-y-auto space-y-1 border border-border rounded-lg p-2 bg-background">
                  {orgMembers.map(m => (
                    <label key={m.id} className="flex items-center gap-2 px-2 py-1 rounded hover:bg-white/5 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={selectedInitialMembers.includes(m.id)}
                        onChange={() => toggleInitialMember(m.id)}
                        className="accent-primary"
                      />
                      <span className="text-sm text-white">{m.userName}</span>
                      <span className="text-xs text-muted-foreground ml-auto">{m.userEmail}</span>
                    </label>
                  ))}
                </div>
                {selectedInitialMembers.length > 0 && (
                  <p className="text-xs text-primary mt-1">{selectedInitialMembers.length} member(s) selected</p>
                )}
              </div>
            )}

            <div className="flex gap-2 justify-end">
              <button
                onClick={resetForm}
                className="px-3 py-1.5 text-sm text-muted-foreground border border-border rounded-lg hover:text-white hover:bg-white/5 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmit}
                disabled={!formData.name.trim() || createMut.isPending || updateMut.isPending}
                className="flex items-center gap-2 px-4 py-1.5 text-sm font-medium bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {(createMut.isPending || updateMut.isPending) && <Loader2 className="w-3 h-3 animate-spin" />}
                {editTeam ? 'Update Team' : 'Create Team'}
              </button>
            </div>
          </div>
        </div>
      )}

      {teams.length === 0 ? (
        <EmptyState
          icon={Users2}
          title="No Teams Yet"
          message="Create your first team to get started."
          action={canEdit ? { label: 'New Team', onClick: () => setShowForm(true) } : undefined}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {teams.map(team => (
            <TeamCard
              key={team.id}
              team={team}
              canEdit={canEdit}
              onView={t => setViewTeam(t)}
              onEdit={t => {
                setEditTeam(t);
                setShowForm(false);
                setFormData({ name: t.name, description: t.description, leadId: t.leadId });
                setSelectedInitialMembers([]);
              }}
              onDelete={t => setDeleteTeam(t)}
              onAssignMembers={canEdit ? t => setManageTeam({ team: t, mode: 'assign' }) : undefined}
              onRemoveMembers={canEdit ? t => setManageTeam({ team: t, mode: 'remove' }) : undefined}
            />
          ))}
        </div>
      )}

      {/* Modals */}
      {viewTeam && (
        <TeamDetailsModal
          team={viewTeam}
          orgId={orgId}
          onClose={() => setViewTeam(null)}
          canEdit={canEdit}
        />
      )}

      {manageTeam && (
        <ManageMembersModal
          team={manageTeam.team}
          orgId={orgId}
          mode={manageTeam.mode}
          onClose={() => setManageTeam(null)}
        />
      )}

      <ConfirmDeleteModal
        isOpen={!!deleteTeam}
        onClose={() => setDeleteTeam(null)}
        onConfirm={() => deleteTeam && deleteMut.mutate(deleteTeam.id)}
        title="Delete Team"
        message={`Are you sure you want to delete "${deleteTeam?.name}"? This action cannot be undone.`}
        isLoading={deleteMut.isPending}
      />
    </div>
  );
}
