import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { UserPlus, Users } from 'lucide-react';
import { organizationService, TeamMember, OrgRole } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import MemberCard from '@/components/organizations/MemberCard';
import EmptyState from '@/components/organizations/EmptyState';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';
import InviteModal from '@/components/organizations/InviteModal';
import ConfirmDeleteModal from '@/components/organizations/ConfirmDeleteModal';

interface Props { orgId: number; }

export default function OrganizationMembersTab({ orgId }: Props) {
  const { role } = useAuth();
  const { toast } = useToast();
  const qc = useQueryClient();
  const canManage = role === 'super-admin' || role === 'org-admin';

  const [showInvite, setShowInvite]       = useState(false);
  const [removeMember, setRemoveMember]   = useState<TeamMember | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['org-members', orgId],
    queryFn: () => organizationService.getMembers(orgId).then(r => r.data),
  });

  const members: TeamMember[] = data?.data || [];

  const inviteMut = useMutation({
    mutationFn: ({ email, role: r }: { email: string; role: OrgRole }) =>
      organizationService.inviteMember(orgId, { email, role: r }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['org-invitations', orgId] });
      toast({ title: 'Invitation sent' });
    },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const removeMut = useMutation({
    mutationFn: (memberId: number) => organizationService.removeMember(orgId, memberId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['org-members', orgId] });
      setRemoveMember(null);
      toast({ title: 'Member removed' });
    },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  if (isLoading) return <LoadingSkeleton rows={4} />;

  return (
    <div>
      {canManage && (
        <div className="mb-5 flex justify-end">
          <button onClick={() => setShowInvite(true)} className="flex items-center gap-2 bg-primary text-primary-foreground text-sm font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors">
            <UserPlus className="w-4 h-4" /> Invite Member
          </button>
        </div>
      )}

      {members.length === 0 ? (
        <EmptyState
          icon={Users}
          title="No Members Yet"
          message="Invite people to join this organization."
          action={canManage ? { label: 'Invite Member', onClick: () => setShowInvite(true) } : undefined}
        />
      ) : (
        <div className="space-y-2">
          {members.map(member => (
            <MemberCard
              key={member.id}
              member={member}
              canRemove={canManage}
              onRemove={m => setRemoveMember(m)}
            />
          ))}
        </div>
      )}

      <InviteModal
        isOpen={showInvite}
        onClose={() => setShowInvite(false)}
        onInvite={async (email, r) => { await inviteMut.mutateAsync({ email, role: r }); }}
      />

      <ConfirmDeleteModal
        isOpen={!!removeMember}
        onClose={() => setRemoveMember(null)}
        onConfirm={() => removeMember && removeMut.mutate(removeMember.id)}
        title="Remove Member"
        message={`Remove "${removeMember?.userName}" from this organization?`}
        isLoading={removeMut.isPending}
      />
    </div>
  );
}
