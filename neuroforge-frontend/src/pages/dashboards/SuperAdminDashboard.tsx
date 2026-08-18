import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'wouter';
import { Users, Building, FolderKanban, CheckSquare, Loader2 } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { organizationService, Organization } from '@/services/organizationService';
import api from '@/services/api';

interface DashboardStats {
  totalProjects: number; activeProjects: number; completedProjects: number;
  totalSprints: number; totalTasks: number; completedTasks: number;
  pendingTasks: number; overallProgress: number;
}
interface UserDto { id: number; name: string; email: string; role: string; }

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

export default function SuperAdminDashboard() {
  const { data: orgsData, isLoading: orgsLoading } = useQuery({
    queryKey: ['organizations'],
    queryFn: () => organizationService.getAll().then(r => r.data),
  });
  const { data: usersData, isLoading: usersLoading } = useQuery({
    queryKey: ['all-users'],
    queryFn: () => api.get<any>('/users').then(r => r.data),
  });
  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => api.get<any>('/dashboard').then(r => r.data),
  });

  const orgs: Organization[] = orgsData?.data || [];
  const users: UserDto[] = usersData?.data || [];
  const stats: DashboardStats | undefined = statsData?.data;
  const isLoading = orgsLoading || usersLoading || statsLoading;

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Super Admin Dashboard" />
        <main className="flex-1 p-8 overflow-y-auto">

          <div className="mb-8">
            <h2 className="text-2xl font-bold text-white">Super Admin Dashboard</h2>
            <p className="text-muted-foreground text-sm mt-1">Manage all organizations, users, and platform settings.</p>
          </div>

          {isLoading ? (
            <div className="flex items-center justify-center py-24">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                <StatCard label="Organizations" value={orgs.length}               icon={Building}     color="text-blue-400"    bg="bg-blue-500/10" />
                <StatCard label="Total Users"   value={users.length}              icon={Users}        color="text-indigo-400"  bg="bg-indigo-500/10" />
                <StatCard label="Projects"      value={stats?.totalProjects ?? 0} icon={FolderKanban} color="text-violet-400"  bg="bg-violet-500/10" />
                <StatCard label="Total Tasks"   value={stats?.totalTasks ?? 0}    icon={CheckSquare}  color="text-emerald-400" bg="bg-emerald-500/10" />
              </div>

              <div className="bg-card border border-border rounded-xl shadow-sm">
                <div className="p-6 border-b border-border flex items-center justify-between">
                  <h2 className="text-lg font-semibold text-white">Users ({users.length})</h2>
                  <Link href="/super-admin/organizations" className="text-xs text-primary hover:text-blue-400 transition-colors">
                    View Organizations →
                  </Link>
                </div>
                {users.length === 0 ? (
                  <div className="p-10 text-center text-sm text-muted-foreground">No users found.</div>
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
                        {users.slice(0, 10).map((u) => (
                          <tr key={u.id} className="hover:bg-white/5 transition-colors">
                            <td className="px-6 py-4">
                              <div className="flex items-center gap-3">
                                <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-xs">
                                  {u.name?.charAt(0)?.toUpperCase() || 'U'}
                                </div>
                                <span className="font-medium text-white">{u.name}</span>
                              </div>
                            </td>
                            <td className="px-6 py-4 text-muted-foreground">{u.email}</td>
                            <td className="px-6 py-4">
                              <span className="text-xs px-2 py-0.5 rounded border bg-slate-500/10 text-slate-400 border-slate-500/20">
                                {u.role?.replace('ROLE_', '') || 'USER'}
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
