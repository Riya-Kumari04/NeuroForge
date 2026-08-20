import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'wouter';
import { FolderKanban, CheckSquare, ShieldCheck, Loader2, BarChart3 } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { projectService, Project } from '@/services/projectService';
import HealthBadge from '@/components/projects/HealthBadge';
import { useAuth } from '@/context/AuthContext';

export default function DeveloperDashboard() {
  const { user } = useAuth();
  const { data: projectsData, isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectService.getAll().then(r => r.data),
  });

  const projects: Project[] = projectsData?.data || [];
  const activeProjects = projects.filter(p => p.status === 'ACTIVE');

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Developer Dashboard" />
        <main className="flex-1 p-8 overflow-y-auto">

          <div className="mb-8 flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-white">Developer Dashboard</h2>
              <p className="text-muted-foreground text-sm mt-1">
                Welcome back, {user?.name || 'Developer'}. View your assigned projects and tasks below.
              </p>
            </div>
            <Link href="/developer/analytics" className="flex items-center gap-2 bg-primary hover:bg-primary/90 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors">
              <BarChart3 className="w-4 h-4" />
              View Analytics
            </Link>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-8">
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-400 mb-4">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Role</h3>
              <p className="text-xl font-bold text-white">Developer</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400 mb-4">
                <FolderKanban className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Assigned Projects</h3>
              <p className="text-3xl font-bold text-white">{isLoading ? '—' : projects.length}</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-400 mb-4">
                <CheckSquare className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Active Projects</h3>
              <p className="text-3xl font-bold text-white">{isLoading ? '—' : activeProjects.length}</p>
            </div>
          </div>

          <div className="bg-card border border-border rounded-xl shadow-sm">
            <div className="p-6 border-b border-border flex items-center justify-between">
              <h2 className="text-lg font-semibold text-white">Assigned Projects</h2>
              <Link href="/developer/projects" className="text-xs text-primary hover:text-blue-400 transition-colors">
                View All →
              </Link>
            </div>
            {isLoading ? (
              <div className="flex justify-center py-12"><Loader2 className="w-5 h-5 animate-spin text-primary" /></div>
            ) : projects.length === 0 ? (
              <div className="p-10 text-center text-sm text-muted-foreground">No projects assigned yet.</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left">
                  <thead>
                    <tr className="bg-background/50 border-b border-border text-xs uppercase tracking-wider text-muted-foreground">
                      <th className="px-5 py-3 font-medium">Project</th>
                      <th className="px-5 py-3 font-medium">Status</th>
                      <th className="px-5 py-3 font-medium">Organization</th>
                      <th className="px-5 py-3 font-medium"></th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border/50 text-sm">
                    {projects.slice(0, 8).map(p => (
                      <tr key={p.id} className="hover:bg-white/5 transition-colors">
                        <td className="px-5 py-3.5 font-medium text-white">{p.projectName}</td>
                        <td className="px-5 py-3.5"><HealthBadge status={p.status} size="sm" /></td>
                        <td className="px-5 py-3.5 text-muted-foreground">{p.organizationName || '—'}</td>
                        <td className="px-5 py-3.5">
                          <Link href={`/developer/projects/${p.id}`} className="text-xs text-primary hover:text-blue-400 transition-colors">
                            Open →
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}
