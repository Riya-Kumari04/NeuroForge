import React from 'react';
import { Link } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import {
  Users, FolderKanban, ShieldCheck, Briefcase, BarChart2, Calendar,
  ArrowRight, GitBranch, Flag, CheckSquare, Plus, TrendingUp,
} from 'lucide-react';
import { projectService, Project } from '@/services/projectService';
import HealthBadge from '@/components/projects/HealthBadge';

const recentActivity = [
  { id: 1, text: 'Alex Chen joined the project team', time: '1 hour ago' },
  { id: 2, text: 'Emma Watson role assigned: QA Tester', time: '3 hours ago' },
  { id: 3, text: 'David Miller added to Engineering dept', time: '5 hours ago' },
  { id: 4, text: 'Project "Phoenix v2.0" created', time: '1 day ago' },
];

export default function ProjectManagerDashboard() {
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
        <DashboardNavbar title="Project Manager Dashboard" />

        <main className="flex-1 p-8 overflow-y-auto">

          {/* Module Banners */}
          <div className="flex flex-col gap-3 mb-8">
            <div className="bg-emerald-500/5 border border-emerald-500/20 rounded-xl px-6 py-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-emerald-400">✓ Module 1 — Authentication &amp; RBAC</p>
                <p className="text-xs text-muted-foreground mt-0.5">You are logged in as Project Manager. View your team and assigned projects below.</p>
              </div>
              <span className="text-[10px] px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-medium">Active</span>
            </div>
            <div className="bg-purple-500/5 border border-purple-500/20 rounded-xl px-6 py-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-purple-400">✓ Module 3 — Project Management</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {projects.length} projects • {activeProjects} active • {completedProjects} completed
                </p>
              </div>
              <div className="flex items-center gap-3">
                <Link href="/project-manager/portfolio" className="text-xs text-purple-400 hover:text-purple-300 flex items-center gap-1 transition-colors">
                  Portfolio <ArrowRight className="w-3 h-3" />
                </Link>
                <Link href="/project-manager/projects" className="text-xs text-purple-400 hover:text-purple-300 flex items-center gap-1 transition-colors">
                  All Projects <ArrowRight className="w-3 h-3" />
                </Link>
              </div>
            </div>
          </div>

          {/* Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-500 mb-4">
                <Briefcase className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Total Projects</h3>
              <p className="text-3xl font-bold text-white">{projects.length}</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-500 mb-4">
                <TrendingUp className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Active Projects</h3>
              <p className="text-3xl font-bold text-white">{activeProjects}</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-500 mb-4">
                <FolderKanban className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Active Departments</h3>
              <p className="text-3xl font-bold text-white">3</p>
            </div>
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="w-10 h-10 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-500 mb-4">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">My Role</h3>
              <p className="text-lg font-bold text-white mt-1">Project Manager</p>
            </div>
          </div>

          {/* Module 3 Feature Links */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <Link href="/project-manager/projects" className="group bg-card border border-border hover:border-primary/40 rounded-xl p-6 flex flex-col transition-all">
              <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary mb-3">
                <FolderKanban className="w-5 h-5" />
              </div>
              <h3 className="text-sm font-semibold text-white mb-1">All Projects</h3>
              <p className="text-xs text-muted-foreground mb-4 flex-1">Browse, filter, and manage all your projects. Create new projects and track status.</p>
              <span className="flex items-center gap-1 text-xs text-primary group-hover:gap-2 transition-all font-medium">
                Open Projects <ArrowRight className="w-3.5 h-3.5" />
              </span>
            </Link>
            <Link href="/project-manager/portfolio" className="group bg-card border border-border hover:border-primary/40 rounded-xl p-6 flex flex-col transition-all">
              <div className="w-10 h-10 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-400 mb-3">
                <BarChart2 className="w-5 h-5" />
              </div>
              <h3 className="text-sm font-semibold text-white mb-1">Portfolio Dashboard</h3>
              <p className="text-xs text-muted-foreground mb-4 flex-1">High-level view of all projects. Status breakdown, completion rates, and health scores.</p>
              <span className="flex items-center gap-1 text-xs text-primary group-hover:gap-2 transition-all font-medium">
                View Portfolio <ArrowRight className="w-3.5 h-3.5" />
              </span>
            </Link>
            <Link href="/project-manager/projects/new" className="group bg-card border border-border hover:border-primary/40 rounded-xl p-6 flex flex-col transition-all">
              <div className="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-400 mb-3">
                <Calendar className="w-5 h-5" />
              </div>
              <h3 className="text-sm font-semibold text-white mb-1">Create Project</h3>
              <p className="text-xs text-muted-foreground mb-4 flex-1">Start a new project with sprints, tasks, and team member assignments.</p>
              <span className="flex items-center gap-1 text-xs text-primary group-hover:gap-2 transition-all font-medium">
                New Project <ArrowRight className="w-3.5 h-3.5" />
              </span>
            </Link>
          </div>

          {/* Recent Projects + Activity */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Projects Table */}
            <div className="bg-card border border-border rounded-xl shadow-sm overflow-hidden lg:col-span-2">
              <div className="p-5 border-b border-border flex items-center justify-between">
                <h2 className="text-base font-semibold text-white">Recent Projects</h2>
                <div className="flex items-center gap-2">
                  <Link href="/project-manager/projects/new" className="bg-primary text-white text-xs px-3 py-1.5 rounded-lg hover:bg-blue-600 transition-colors flex items-center gap-1">
                    <Plus className="w-3 h-3" /> New
                  </Link>
                  <Link href="/project-manager/projects" className="text-xs text-primary hover:text-blue-400 transition-colors">
                    View All →
                  </Link>
                </div>
              </div>
              {recentProjects.length === 0 ? (
                <div className="p-10 text-center">
                  <FolderKanban className="w-8 h-8 text-muted-foreground mx-auto mb-3" />
                  <p className="text-sm text-white mb-1">No projects yet</p>
                  <Link href="/project-manager/projects/new" className="text-xs text-primary hover:text-blue-400">
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
                      <Link href={`/project-manager/projects/${p.id}`} className="text-xs text-primary hover:text-blue-400 transition-colors flex-shrink-0">
                        View →
                      </Link>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Recent Activity */}
            <div className="bg-card border border-border rounded-xl shadow-sm flex flex-col">
              <div className="p-6 border-b border-border">
                <h2 className="text-base font-semibold text-white">Recent Activity</h2>
              </div>
              <div className="p-6 flex-1">
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
          </div>
        </main>
      </div>
    </div>
  );
}
