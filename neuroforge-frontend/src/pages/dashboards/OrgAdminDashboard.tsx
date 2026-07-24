import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'wouter';
import { Users, FolderKanban, MailPlus, Users2, Loader2, Building2 } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { organizationService, Organization, TeamMember, OrgStatsDto } from '@/services/organizationService';

function StatCard({ label, value, icon: Icon, color, bg }: {
  label: string; value: number | string; icon: React.ElementType; color: string; bg: string;
}) {
  return (
    <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
      <div className={`w-10 h-10 rounded-lg ${bg} flex items-center justify-center ${color} mb-4`}>
        <Icon className="w-5 h-5" />
      </div>
      <h3 className="text-muted-foreground text-sm font-medium mb-1">{label}</h3>
      <p className="text-3xl font-bold text-white">{value}</p>
    </div>
  );
}

export default function OrgAdminDashboard() {
  const { data: orgsData, isLoading: orgsLoading } = useQuery({
    queryKey: ['organizations'],
    queryFn: () => organizationService.getAll().then(r => r.data),
  });
  const orgs: Organization[] = orgsData?.data || [];
  const firstOrg = orgs[0];

  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: ['org-stats', firstOrg?.id],
    queryFn: () => organizationService.getStats(firstOrg!.id).then(r => r.data),
    enabled: !!firstOrg?.id,
  });
  const { data: membersData, isLoading: membersLoading } = useQuery({
    queryKey: ['org-members', firstOrg?.id],
    queryFn: () => organizationService.getMembers(firstOrg!.id).then(r => r.data),
    enabled: !!firstOrg?.id,
  });

  const stats: OrgStatsDto | undefined = statsData?.data;
  const members: TeamMember[] = membersData?.data || [];
  const isLoading = orgsLoading || statsLoading || membersLoading;

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Organization Admin Dashboard" />
        <main className="flex-1 p-8 overflow-y-auto">

          <div className="mb-8">
            <h2 className="text-2xl font-bold text-white">Organization Admin Dashboard</h2>
            <p className="text-muted-foreground text-sm mt-1">Manage your team members, projects, and invitations.</p>
          </div>

          {isLoading ? (
            <div className="flex items-center justify-center py-24">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
            </div>
          ) : !firstOrg ? (
            <div className="bg-card border border-dashed border-border rounded-xl p-10 text-center">
              <Building2 className="w-8 h-8 text-muted-foreground mx-auto mb-3" />
              <p className="text-sm font-medium text-white mb-1">No organization found</p>
              <p className="text-xs text-muted-foreground mb-4">Create your first organization to get started.</p>
              <Link href="/org-admin/organizations/new" className="inline-flex items-center gap-2 bg-primary text-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors">
                Create Organization
              </Link>
            </div>
          ) : (
            <>
              <div className="bg-card border border-border rounded-xl p-5 mb-6 flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center">
                  <Building2 className="w-6 h-6 text-primary" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-white">{firstOrg.name}</h3>
                  <p className="text-xs text-muted-foreground">/{firstOrg.slug} · {firstOrg.plan} Plan</p>
                </div>
                <Link href={`/org-admin/organizations/${firstOrg.id}`} className="ml-auto text-xs text-primary hover:text-blue-400 transition-colors">
                  Manage →
                </Link>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                <StatCard label="Teams"            value={stats?.teamsCount ?? 0}          icon={Users2}     color="text-blue-400"    bg="bg-blue-500/10" />
                <StatCard label="Members"          value={stats?.membersCount ?? 0}         icon={Users}      color="text-indigo-400"  bg="bg-indigo-500/10" />
                <StatCard label="Pending Invites"  value={stats?.pendingInvitesCount ?? 0}  icon={MailPlus}   color="text-amber-400"   bg="bg-amber-500/10" />
                <StatCard label="Projects"         value={stats?.projectsCount ?? 0}        icon={FolderKanban} color="text-emerald-400" bg="bg-emerald-500/10" />
              </div>

              <div className="bg-card border border-border rounded-xl shadow-sm">
                <div className="p-6 border-b border-border flex items-center justify-between">
                  <h2 className="text-lg font-semibold text-white">Members ({members.length})</h2>
                  <Link href={`/org-admin/organizations/${firstOrg.id}`} className="text-xs text-primary hover:text-blue-400 transition-colors">
                    View All →
                  </Link>
                </div>
                {members.length === 0 ? (
                  <div className="p-10 text-center text-sm text-muted-foreground">No members yet.</div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left">
                      <thead>
                        <tr className="bg-background/50 border-b border-border text-xs uppercase tracking-wider text-muted-foreground">
                          <th className="px-6 py-3 font-medium">Name</th>
                          <th className="px-6 py-3 font-medium">Email</th>
                          <th className="px-6 py-3 font-medium">Role</th>
                        </tr>
                      </thead>
                      <tbody className="text-sm divide-y divide-border/50">
                        {members.slice(0, 8).map(m => (
                          <tr key={m.id} className="hover:bg-white/5 transition-colors">
                            <td className="px-6 py-4">
                              <div className="flex items-center gap-3">
                                <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-xs">
                                  {m.userName?.charAt(0)?.toUpperCase() || 'U'}
                                </div>
                                <span className="font-medium text-white">{m.userName}</span>
                              </div>
                            </td>
                            <td className="px-6 py-4 text-muted-foreground">{m.userEmail}</td>
                            <td className="px-6 py-4">
                              <span className="text-xs px-2 py-0.5 rounded border bg-slate-500/10 text-slate-400 border-slate-500/20">
                                {m.role}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </>
          )}
        </main>
      </div>
    </div>
  );
}
