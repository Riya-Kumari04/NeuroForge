import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Users2, Loader2 } from 'lucide-react';
import { organizationService, Team, CreateTeamRequest } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import TeamCard from '@/components/organizations/TeamCard';
import EmptyState from '@/components/organizations/EmptyState';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';
import ConfirmDeleteModal from '@/components/organizations/ConfirmDeleteModal';

interface Props { orgId: number; }

export default function OrganizationTeamsTab({ orgId }: Props) {
  const { role } = useAuth();
  const { toast } = useToast();
  const qc = useQueryClient();
  const canEdit = role === 'super-admin' || role === 'org-admin';

  const [showForm, setShowForm]   = useState(false);
  const [editTeam, setEditTeam]   = useState<Team | null>(null);
  const [deleteTeam, setDeleteTeam] = useState<Team | null>(null);
  const [formData, setFormData]   = useState<CreateTeamRequest>({ name: '', description: '' });

  const { data, isLoading } = useQuery({
    queryKey: ['org-teams', orgId],
    queryFn: () => organizationService.getTeams(orgId).then(r => r.data),
  });

  const teams: Team[] = data?.data || [];

  const createMut = useMutation({
    mutationFn: (d: CreateTeamRequest) => organizationService.createTeam(orgId, d),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['org-teams', orgId] }); setShowForm(false); setFormData({ name: '', description: '' }); toast({ title: 'Team created' }); },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const updateMut = useMutation({
    mutationFn: ({ teamId, data }: { teamId: number; data: Partial<CreateTeamRequest> }) => organizationService.updateTeam(orgId, teamId, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['org-teams', orgId] }); setEditTeam(null); toast({ title: 'Team updated' }); },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const deleteMut = useMutation({
    mutationFn: (teamId: number) => organizationService.deleteTeam(orgId, teamId),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['org-teams', orgId] }); setDeleteTeam(null); toast({ title: 'Team deleted' }); },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  if (isLoading) return <LoadingSkeleton rows={3} />;

  return (
    <div>
      {canEdit && (
        <div className="mb-5 flex justify-end">
          <button onClick={() => { setShowForm(true); setEditTeam(null); setFormData({ name: '', description: '' }); }} className="flex items-center gap-2 bg-primary text-primary-foreground text-sm font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors">
            <Plus className="w-4 h-4" /> New Team
          </button>
        </div>
      )}

      {(showForm || editTeam) && canEdit && (
        <div className="bg-card border border-primary/30 rounded-xl p-5 mb-5">
          <h3 className="text-sm font-semibold text-white mb-4">{editTeam ? 'Edit Team' : 'Create New Team'}</h3>
          <div className="space-y-3">
            <input
              type="text"
              value={formData.name}
              onChange={e => setFormData(p => ({ ...p, name: e.target.value }))}
              placeholder="Team name"
              className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all"
            />
            <textarea
              value={formData.description || ''}
              onChange={e => setFormData(p => ({ ...p, description: e.target.value }))}
              placeholder="Description (optional)"
              rows={2}
              className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all resize-none"
            />
            <div className="flex gap-2 justify-end">
              <button onClick={() => { setShowForm(false); setEditTeam(null); }} className="px-3 py-1.5 text-sm text-muted-foreground border border-border rounded-lg hover:text-white hover:bg-white/5 transition-colors">Cancel</button>
              <button
                onClick={() => editTeam ? updateMut.mutate({ teamId: editTeam.id, data: formData }) : createMut.mutate(formData)}
                disabled={!formData.name.trim() || createMut.isPending || updateMut.isPending}
                className="flex items-center gap-2 px-4 py-1.5 text-sm font-medium bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {(createMut.isPending || updateMut.isPending) && <Loader2 className="w-3 h-3 animate-spin" />}
                {editTeam ? 'Update' : 'Create'}
              </button>
            </div>
          </div>
        </div>
      )}

      {teams.length === 0 ? (
        <EmptyState icon={Users2} title="No Teams Yet" message="Create your first team to get started." action={canEdit ? { label: 'New Team', onClick: () => setShowForm(true) } : undefined} />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {teams.map(team => (
            <TeamCard
              key={team.id}
              team={team}
              canEdit={canEdit}
              onEdit={t => { setEditTeam(t); setShowForm(false); setFormData({ name: t.name, description: t.description }); }}
              onDelete={t => setDeleteTeam(t)}
            />
          ))}
        </div>
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
