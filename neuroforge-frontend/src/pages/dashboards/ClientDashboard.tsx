import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'wouter';
import { FolderKanban, Eye, TrendingUp, Loader2, BarChart3 } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { projectService, Project } from '@/services/projectService';
import HealthBadge from '@/components/projects/HealthBadge';
import ProgressBar from '@/components/projects/ProgressBar';
import { useAuth } from '@/context/AuthContext';

export default function ClientDashboard() {
  const { user } = useAuth();
  const { data: projectsData, isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectService.getAll().then(r => r.data),
  });

  const projects: Project[] = projectsData?.data || [];
  const active    = projects.filter(p => p.status === 'ACTIVE').length;
  const completed = projects.filter(p => p.status === 'COMPLETED').length;

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Client Dashboard" />
        <main className="flex-1 p-8 overflow-y-auto">

          <div className="mb-8 flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-white">Client Dashboard</h2>
              <p className="text-muted-foreground text-sm mt-1">
                Welcome, {user?.name || 'Client'}. Monitor project progress and milestones.
              </p>
            </div>
            <Link href="/client/analytics" className="flex items-center gap-2 bg-primary hover:bg-primary/90 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors">
              <BarChart3 className="w-4 h-4" />
              View Analytics
            </Link>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-8">
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-400 mb-4">
                <FolderKanban className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Total Projects</h3>
              <p className="text-3xl font-bold text-white">{isLoading ? '—' : projects.length}</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-400 mb-4">
                <TrendingUp className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Active</h3>
              <p className="text-3xl font-bold text-white">{isLoading ? '—' : active}</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-violet-500/10 flex items-center justify-center text-violet-400 mb-4">
                <Eye className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Completed</h3>
              <p className="text-3xl font-bold text-white">{isLoading ? '—' : completed}</p>
            </div>
          </div>

          <div className="bg-card border border-border rounded-xl shadow-sm">
            <div className="p-6 border-b border-border flex items-center justify-between">
              <h2 className="text-lg font-semibold text-white">Project Overview</h2>
              <Link href="/client/projects" className="text-xs text-primary hover:text-blue-400 transition-colors">
                View All →
              </Link>
            </div>
            {isLoading ? (
              <div className="flex justify-center py-12"><Loader2 className="w-5 h-5 animate-spin text-primary" /></div>
            ) : projects.length === 0 ? (
              <div className="p-10 text-center text-sm text-muted-foreground">No projects available.</div>
            ) : (
              <div className="divide-y divide-border/50">
                {projects.slice(0, 8).map(p => (
                  <div key={p.id} className="px-6 py-4 hover:bg-white/5 transition-colors">
                    <div className="flex items-center justify-between mb-2">
                      <div>
                        <p className="text-sm font-medium text-white">{p.projectName}</p>
                        {p.organizationName && (
                          <p className="text-xs text-muted-foreground">{p.organizationName}</p>
                        )}
                      </div>
                      <div className="flex items-center gap-3">
                        <HealthBadge status={p.status} size="sm" />
                        <Link href={`/client/projects/${p.id}`} className="text-xs text-primary hover:text-blue-400 transition-colors">
                          View →
                        </Link>
                      </div>
                    </div>
                    {p.description && (
                      <p className="text-xs text-muted-foreground truncate">{p.description}</p>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}
