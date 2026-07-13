import React from 'react';
import { Link } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import {
  Users, FolderKanban, MailPlus, ShieldCheck, BarChart2, Cpu,
  ArrowRight, TrendingUp, CheckCircle2, Clock, Plus,
} from 'lucide-react';
import { projectService, Project } from '@/services/projectService';
import { organizationService } from '@/services/organizationService';
import HealthBadge from '@/components/projects/HealthBadge';

const recentActivity = [
  { id: 1, text: 'Sarah Connor assigned role: Project Manager', time: '1 hour ago' },
  { id: 2, text: 'John Smith invitation accepted', time: '3 hours ago' },
  { id: 3, text: '3 new developer invites sent', time: '5 hours ago' },
  { id: 4, text: 'Emily Chen role updated to QA Lead', time: '1 day ago' },
];

export default function OrgAdminDashboard() {
  const { data: projectsData } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectService.getAll().then(r => r.data),
  });
  const projects: Project[] = projectsData?.data || [];
  const activeProjects = projects.filter(p => p.status === 'ACTIVE').length;
  const completedProjects = projects.filter(p => p.status === 'COMPLETED').length;
  const recentProjects = [...projects]
    .sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
    .slice(0, 4);

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Organisation Overview" />

        <main className="flex-1 p-8 overflow-y-auto">

          {/* Module Banners */}
          <div className="flex flex-col gap-3 mb-8">
            <div className="bg-emerald-500/5 border border-emerald-500/20 rounded-xl px-6 py-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-emerald-400">✓ Module 1 — Authentication &amp; RBAC</p>
                <p className="text-xs text-muted-foreground mt-0.5">You are logged in as Org Admin. Manage your team members and their roles.</p>
              </div>
              <span className="text-[10px] px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-medium">Active</span>
            </div>
            <div className="bg-blue-500/5 border border-blue-500/20 rounded-xl px-6 py-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-blue-400">✓ Module 2 — Organization Management</p>
                <p className="text-xs text-muted-foreground mt-0.5">Manage organizations, teams, and member invitations.</p>
              </div>
              <Link href="/org-admin/organizations" className="text-xs text-blue-400 hover:text-blue-300 flex items-center gap-1 transition-colors">
                View Orgs <ArrowRight className="w-3 h-3" />
              </Link>
            </div>
            <div className="bg-purple-500/5 border border-purple-500/20 rounded-xl px-6 py-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-purple-400">✓ Module 3 — Project Management</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {projects.length} projects • {activeProjects} active • {completedProjects} completed
                </p>
              </div>
              <div className="flex items-center gap-3">
                <Link href="/org-admin/portfolio" className="text-xs text-purple-400 hover:text-purple-300 flex items-center gap-1 transition-colors">
                  Portfolio <ArrowRight className="w-3 h-3" />
                </Link>
                <Link href="/org-admin/projects" className="text-xs text-purple-400 hover:text-purple-300 flex items-center gap-1 transition-colors">
                  Projects <ArrowRight className="w-3 h-3" />
                </Link>
              </div>
            </div>
          </div>

          {/* Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-500 mb-4">
                <Users className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Total Members</h3>
              <p className="text-3xl font-bold text-white">38</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-500 mb-4">
                <FolderKanban className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Total Projects</h3>
              <p className="text-3xl font-bold text-white">{projects.length}</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-500 mb-4">
                <MailPlus className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Pending Invites</h3>
              <p className="text-3xl font-bold text-white">4</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-500 mb-4">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Active Projects</h3>
              <p className="text-3xl font-bold text-white">{activeProjects}</p>
            </div>
          </div>

          {/* Projects + Quick Actions Row */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            {/* Recent Projects */}
            <div className="md:col-span-2 bg-card border border-border rounded-xl overflow-hidden">
              <div className="p-5 border-b border-border flex items-center justify-between">
                <h2 className="text-base font-semibold text-white">Recent Projects</h2>
                <Link href="/org-admin/projects" className="text-xs text-primary hover:text-blue-400 transition-colors flex items-center gap-1">
                  View All <ArrowRight className="w-3 h-3" />
                </Link>
              </div>
              {recentProjects.length === 0 ? (
                <div className="p-8 text-center">
                  <FolderKanban className="w-8 h-8 text-muted-foreground mx-auto mb-3" />
                  <p className="text-sm text-white mb-1">No projects yet</p>
                  <Link href="/org-admin/projects/new" className="text-xs text-primary hover:text-blue-400 transition-colors">
                    Create your first project →
                  </Link>
                </div>
              ) : (
                <div className="divide-y divide-border/50">
                  {recentProjects.map(p => (
                    <div key={p.id} className="flex items-center gap-4 px-5 py-3.5 hover:bg-white/5 transition-colors">
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-white truncate">{p.projectName}</p>
                        <p className="text-xs text-muted-foreground">{p.organizationName || '—'}</p>
                      </div>
                      <HealthBadge status={p.status} size="sm" />
                      <Link href={`/org-admin/projects/${p.id}`} className="text-xs text-primary hover:text-blue-400 transition-colors">
                        View →
                      </Link>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Quick Actions */}
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm flex flex-col">
              <h2 className="text-base font-semibold text-white mb-4">Quick Actions</h2>
              <div className="space-y-2 flex-1 flex flex-col justify-center">
                <Link href="/org-admin/projects/new" className="w-full flex items-center justify-center gap-2 p-3 rounded-lg bg-primary text-white hover:bg-blue-600 transition-colors shadow-lg shadow-blue-500/20 font-medium text-sm">
                  <Plus className="w-4 h-4" /> Create Project
                </Link>
                <Link href="/org-admin/portfolio" className="w-full flex items-center justify-center gap-2 p-3 rounded-lg bg-background border border-border hover:border-primary/50 text-white transition-colors font-medium text-sm">
                  <TrendingUp className="w-4 h-4" /> View Portfolio
                </Link>
                <Link href="/org-admin/organizations" className="w-full flex items-center justify-center gap-2 p-3 rounded-lg bg-background border border-border hover:border-primary/50 text-white transition-colors font-medium text-sm">
                  <Users className="w-4 h-4" /> Manage Organizations
                </Link>
              </div>
            </div>
          </div>

          {/* Activity Feed */}
          <div className="bg-card border border-border rounded-xl shadow-sm">
            <div className="p-6 border-b border-border">
              <h2 className="text-lg font-semibold text-white">Recent Activity</h2>
            </div>
            <div className="p-6">
              <div className="relative border-l border-border/50 ml-3 space-y-6">
                {recentActivity.map((activity) => (
                  <div key={activity.id} className="relative pl-6">
                    <div className="absolute -left-[5px] top-1.5 w-2 h-2 rounded-full bg-primary shadow-[0_0_8px_rgba(37,99,235,0.8)]" />
                    <p className="text-sm text-white mb-1">{activity.text}</p>
                    <p className="text-xs text-muted-foreground">{activity.time}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
