import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'wouter';
import { FolderKanban, Bug, ShieldCheck, Loader2 } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { projectService, Project } from '@/services/projectService';
import HealthBadge from '@/components/projects/HealthBadge';
import { useAuth } from '@/context/AuthContext';

export default function QADashboard() {
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
        <DashboardNavbar title="QA Dashboard" />
        <main className="flex-1 p-8 overflow-y-auto">

          <div className="mb-8">
            <h2 className="text-2xl font-bold text-white">QA Dashboard</h2>
            <p className="text-muted-foreground text-sm mt-1">
              Welcome back, {user?.name || 'QA'}. View your testing assignments and projects below.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-8">
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-400 mb-4">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Role</h3>
              <p className="text-xl font-bold text-white">QA</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400 mb-4">
                <FolderKanban className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Accessible Projects</h3>
              <p className="text-3xl font-bold text-white">{isLoading ? '—' : projects.length}</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-red-500/10 flex items-center justify-center text-red-400 mb-4">
                <Bug className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Active for Testing</h3>
              <p className="text-3xl font-bold text-white">{isLoading ? '—' : activeProjects.length}</p>
            </div>
          </div>

          <div className="bg-card border border-border rounded-xl shadow-sm">
            <div className="p-6 border-b border-border flex items-center justify-between">
              <h2 className="text-lg font-semibold text-white">Testing Projects</h2>
              <Link href="/qa/projects" className="text-xs text-primary hover:text-blue-400 transition-colors">
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
                    {projects.map(p => (
                      <tr key={p.id} className="hover:bg-white/5 transition-colors">
                        <td className="px-5 py-3.5 font-medium text-white">{p.projectName}</td>
                        <td className="px-5 py-3.5"><HealthBadge status={p.status} size="sm" /></td>
                        <td className="px-5 py-3.5 text-muted-foreground">{p.organizationName || '—'}</td>
                        <td className="px-5 py-3.5">
                          <Link href={`/qa/projects/${p.id}`} className="text-xs text-primary hover:text-blue-400 transition-colors">
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
