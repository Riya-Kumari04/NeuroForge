import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Mail, RefreshCw, X, CheckCircle2, XCircle, Clock, AlertCircle } from 'lucide-react';
import { organizationService, Invite } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import EmptyState from '@/components/organizations/EmptyState';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';

interface Props { orgId: number; }

type TabFilter = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';

const STATUS_CONFIG: Record<string, { color: string; icon: React.ReactNode; label: string }> = {
  PENDING:   { color: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30', icon: <Clock className="w-3 h-3" />, label: 'Pending' },
  ACCEPTED:  { color: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30', icon: <CheckCircle2 className="w-3 h-3" />, label: 'Accepted' },
  REJECTED:  { color: 'bg-red-500/20 text-red-400 border-red-500/30', icon: <XCircle className="w-3 h-3" />, label: 'Rejected' },
  CANCELLED: { color: 'bg-slate-500/20 text-slate-400 border-slate-500/30', icon: <X className="w-3 h-3" />, label: 'Cancelled' },
  EXPIRED:   { color: 'bg-orange-500/20 text-orange-400 border-orange-500/30', icon: <AlertCircle className="w-3 h-3" />, label: 'Expired' },
};

const ROLE_LABELS: Record<string, string> = {
  ORG_ADMIN: 'Org Admin', PROJECT_MANAGER: 'Project Manager',
  DEVELOPER: 'Developer', QA: 'QA', CLIENT: 'Client',
};

const TABS: { key: TabFilter; label: string }[] = [
  { key: 'PENDING', label: 'Pending' },
  { key: 'ACCEPTED', label: 'Accepted' },
  { key: 'REJECTED', label: 'Rejected' },
  { key: 'CANCELLED', label: 'Cancelled' },
];

export default function OrganizationInvitationsTab({ orgId }: Props) {
  const { role } = useAuth();
  const { toast } = useToast();
  const qc = useQueryClient();
  const canManage = role === 'super-admin' || role === 'org-admin';
  const [activeTab, setActiveTab] = useState<TabFilter>('PENDING');

  const { data, isLoading, isError } = useQuery({
    queryKey: ['org-invitations', orgId],
    queryFn: () => organizationService.getInvitations(orgId).then(r => r.data),
  });

  const allInvites: Invite[] = data?.data || [];
  const visible = allInvites.filter(i => i.status === activeTab);

  const cancelMut = useMutation({
    mutationFn: (id: number) => organizationService.cancelInvitation(orgId, id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['org-invitations', orgId] }); toast({ title: 'Invitation cancelled' }); },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const resendMut = useMutation({
    mutationFn: (id: number) => organizationService.resendInvitation(orgId, id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['org-invitations', orgId] }); toast({ title: 'Invitation resent successfully' }); },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  if (isLoading) return <LoadingSkeleton rows={3} />;

  if (isError) return (
    <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-4 text-red-400 text-sm">
      Failed to load invitations. Please try again.
    </div>
  );

  return (
    <div>
      {/* Tab bar with counts */}
      <div className="flex gap-1 p-1 bg-background/50 rounded-lg border border-border w-fit mb-5 overflow-x-auto">
        {TABS.map(t => (
          <button key={t.key} onClick={() => setActiveTab(t.key)}
            className={`flex items-center gap-1.5 px-4 py-1.5 text-sm font-medium rounded-md transition-colors whitespace-nowrap ${activeTab === t.key ? 'bg-card text-white' : 'text-muted-foreground hover:text-white'}`}>
            {t.label}
            <span className={`text-xs px-1.5 py-0.5 rounded-full ${activeTab === t.key ? 'bg-primary/20 text-primary' : 'bg-border/50 text-muted-foreground'}`}>
              {allInvites.filter(i => i.status === t.key).length}
            </span>
          </button>
        ))}
      </div>

      {visible.length === 0 ? (
        <EmptyState icon={Mail}
          title={`No ${activeTab.charAt(0) + activeTab.slice(1).toLowerCase()} Invitations`}
          message={activeTab === 'PENDING' ? 'Invite people using the button on the Members tab.' : 'No invitations with this status yet.'} />
      ) : (
        <div className="space-y-2">
          {visible.map(invite => {
            const sc = STATUS_CONFIG[invite.status] || STATUS_CONFIG.PENDING;
            return (
              <div key={invite.id} className="flex items-center gap-4 px-4 py-3.5 bg-card border border-border rounded-xl hover:border-border/80 transition-colors">
                <div className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                  <Mail className="w-4 h-4 text-primary" />
                </div>

                <div className="flex-1 min-w-0 grid grid-cols-2 gap-x-4 gap-y-0.5">
                  <div>
                    <p className="text-sm font-medium text-white truncate">{invite.email}</p>
                    <p className="text-xs text-muted-foreground">
                      Invited {new Date(invite.createdAt).toLocaleDateString()}
                      {invite.expiresAt && ` · Expires ${new Date(invite.expiresAt).toLocaleDateString()}`}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">Role</p>
                    <p className="text-sm text-white">{ROLE_LABELS[(invite as any).role] || (invite as any).role || '—'}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2 flex-shrink-0">
                  <span className={`flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full border ${sc.color}`}>
                    {sc.icon} {sc.label}
                  </span>

                  {invite.status === 'PENDING' && canManage && (
                    <>
                      <button onClick={() => resendMut.mutate(invite.id)} disabled={resendMut.isPending}
                        className="p-1.5 text-muted-foreground hover:text-primary hover:bg-primary/10 rounded-lg transition-colors" title="Resend invitation">
                        <RefreshCw className="w-3.5 h-3.5" />
                      </button>
                      <button onClick={() => cancelMut.mutate(invite.id)} disabled={cancelMut.isPending}
                        className="p-1.5 text-muted-foreground hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors" title="Cancel invitation">
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
