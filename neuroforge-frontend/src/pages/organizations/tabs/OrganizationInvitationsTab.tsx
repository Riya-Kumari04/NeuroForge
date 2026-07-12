import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Mail, RefreshCw, X, CheckCircle2, XCircle, Clock } from 'lucide-react';
import { organizationService, Invite, InviteStatus } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import EmptyState from '@/components/organizations/EmptyState';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';

interface Props { orgId: number; }

type TabFilter = 'PENDING' | 'ACCEPTED' | 'REJECTED';

const statusConfig: Record<string, { color: string; icon: React.ReactNode }> = {
  PENDING:   { color: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30', icon: <Clock className="w-3 h-3" /> },
  ACCEPTED:  { color: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30', icon: <CheckCircle2 className="w-3 h-3" /> },
  REJECTED:  { color: 'bg-red-500/20 text-red-400 border-red-500/30', icon: <XCircle className="w-3 h-3" /> },
  CANCELLED: { color: 'bg-slate-500/20 text-slate-400 border-slate-500/30', icon: <X className="w-3 h-3" /> },
};

export default function OrganizationInvitationsTab({ orgId }: Props) {
  const { role } = useAuth();
  const { toast } = useToast();
  const qc = useQueryClient();
  const canManage = role === 'super-admin' || role === 'org-admin';
  const [activeTab, setActiveTab] = useState<TabFilter>('PENDING');

  const { data, isLoading } = useQuery({
    queryKey: ['org-invitations', orgId],
    queryFn: () => organizationService.getInvitations(orgId).then(r => r.data),
  });

  const allInvites: Invite[] = data?.data || [];
  const invites = allInvites.filter(i => i.status === activeTab);

  const cancelMut = useMutation({
    mutationFn: (id: number) => organizationService.cancelInvitation(orgId, id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['org-invitations', orgId] }); toast({ title: 'Invitation cancelled' }); },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const resendMut = useMutation({
    mutationFn: (id: number) => organizationService.resendInvitation(orgId, id),
    onSuccess: () => toast({ title: 'Invitation resent' }),
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  if (isLoading) return <LoadingSkeleton rows={3} />;

  const tabs: TabFilter[] = ['PENDING', 'ACCEPTED', 'REJECTED'];

  return (
    <div>
      <div className="flex gap-1 p-1 bg-background/50 rounded-lg border border-border w-fit mb-5">
        {tabs.map(t => (
          <button
            key={t}
            onClick={() => setActiveTab(t)}
            className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${activeTab === t ? 'bg-card text-white' : 'text-muted-foreground hover:text-white'}`}
          >
            {t.charAt(0) + t.slice(1).toLowerCase()}
            <span className="ml-1.5 text-xs opacity-70">
              {allInvites.filter(i => i.status === t).length}
            </span>
          </button>
        ))}
      </div>

      {invites.length === 0 ? (
        <EmptyState icon={Mail} title={`No ${activeTab.charAt(0) + activeTab.slice(1).toLowerCase()} Invitations`} message="No invitations with this status." />
      ) : (
        <div className="space-y-2">
          {invites.map(invite => {
            const sc = statusConfig[invite.status] || statusConfig.PENDING;
            return (
              <div key={invite.id} className="flex items-center gap-3 px-4 py-3 bg-card border border-border rounded-xl">
                <div className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center">
                  <Mail className="w-4 h-4 text-primary" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-white truncate">{invite.email}</p>
                  <p className="text-xs text-muted-foreground">
                    Invited {new Date(invite.createdAt).toLocaleDateString()} · Expires {new Date(invite.expiresAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="flex items-center gap-2 flex-shrink-0">
                  <span className={`flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full border ${sc.color}`}>
                    {sc.icon} {invite.status}
                  </span>
                  {invite.status === 'PENDING' && canManage && (
                    <>
                      <button
                        onClick={() => resendMut.mutate(invite.id)}
                        disabled={resendMut.isPending}
                        className="p-1.5 text-muted-foreground hover:text-primary hover:bg-primary/10 rounded-lg transition-colors"
                        title="Resend"
                      >
                        <RefreshCw className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => cancelMut.mutate(invite.id)}
                        disabled={cancelMut.isPending}
                        className="p-1.5 text-muted-foreground hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
                        title="Cancel"
                      >
                        <X className="w-3.5 h-3.5" />
                      </button>
                    </>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
