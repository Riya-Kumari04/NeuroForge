import React from 'react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { Users, FolderKanban, ShieldCheck, Briefcase, Lock, BarChart2, Cpu, Calendar } from 'lucide-react';

const teamMembers = [
  { name: 'Alex Chen', role: 'Frontend Developer', dept: 'Engineering', status: 'Active' },
  { name: 'Sarah Connor', role: 'Backend Developer', dept: 'Engineering', status: 'Active' },
  { name: 'David Miller', role: 'Fullstack Developer', dept: 'Engineering', status: 'Active' },
  { name: 'Emma Watson', role: 'QA Tester', dept: 'QA', status: 'Active' },
];

const recentActivity = [
  { id: 1, text: 'Alex Chen joined the project team', time: '1 hour ago' },
  { id: 2, text: 'Emma Watson role assigned: QA Tester', time: '3 hours ago' },
  { id: 3, text: 'David Miller added to Engineering dept', time: '5 hours ago' },
  { id: 4, text: 'Project "Phoenix v2.0" created', time: '1 day ago' },
];

function ComingSoonCard({ icon: Icon, title, description, module }: { icon: React.ElementType; title: string; description: string; module: string }) {
  return (
    <div className="bg-card border border-dashed border-border rounded-xl p-6 flex flex-col items-center justify-center text-center min-h-[180px]">
      <div className="w-10 h-10 rounded-lg bg-slate-500/10 flex items-center justify-center text-slate-500 mb-3">
        <Icon className="w-5 h-5" />
      </div>
      <h3 className="text-sm font-semibold text-white mb-1">{title}</h3>
      <p className="text-xs text-muted-foreground mb-3">{description}</p>
      <span className="text-[10px] px-2.5 py-1 rounded-full bg-slate-500/10 text-slate-400 border border-slate-500/20 font-medium">
        {module}
      </span>
    </div>
  );
}

export default function ProjectManagerDashboard() {
  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Project Manager Dashboard" />

        <main className="flex-1 p-8 overflow-y-auto">

          {/* Module 1 Banner */}
          <div className="mb-8 bg-emerald-500/5 border border-emerald-500/20 rounded-xl px-6 py-4 flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-emerald-400">✓ Module 1 — Authentication & RBAC</p>
              <p className="text-xs text-muted-foreground mt-0.5">You are logged in as Project Manager. View your team and assigned projects below.</p>
            </div>
            <span className="text-[10px] px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-medium">
              Active Module
            </span>
          </div>

          {/* Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-500">
                  <Briefcase className="w-5 h-5" />
                </div>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Assigned Projects</h3>
              <p className="text-3xl font-bold text-white">3</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-500">
                  <Users className="w-5 h-5" />
                </div>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Team Members</h3>
              <p className="text-3xl font-bold text-white">12</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-500">
                  <FolderKanban className="w-5 h-5" />
                </div>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Active Departments</h3>
              <p className="text-3xl font-bold text-white">3</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-500">
                  <ShieldCheck className="w-5 h-5" />
                </div>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">My Role</h3>
              <p className="text-lg font-bold text-white mt-1">Project Manager</p>
            </div>
          </div>

          {/* Coming Soon Row */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <ComingSoonCard
              icon={BarChart2}
              title="Sprint Board"
              description="Manage sprints, backlogs, and track team velocity in real-time."
              module="Coming Soon — Module 2"
            />
            <ComingSoonCard
              icon={Calendar}
              title="Timeline & Milestones"
              description="Visual project timeline with milestone tracking and deadline alerts."
              module="Coming Soon — Module 3"
            />
            <ComingSoonCard
              icon={Cpu}
              title="AI Sprint Prediction"
              description="AI-powered sprint forecasting and workload balancing."
              module="Coming Soon — Module 10"
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Assigned Team Table */}
            <div className="bg-card border border-border rounded-xl shadow-sm overflow-hidden lg:col-span-2">
              <div className="p-6 border-b border-border flex items-center justify-between">
                <h2 className="text-lg font-semibold text-white">Assigned Team</h2>
                <button className="bg-primary text-white text-xs px-3 py-1.5 rounded-lg hover:bg-blue-600 transition-colors">
                  + Add Member
                </button>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-border text-xs uppercase tracking-wider text-muted-foreground bg-background/50">
                      <th className="px-6 py-3 font-medium">Name</th>
                      <th className="px-6 py-3 font-medium">Role</th>
                      <th className="px-6 py-3 font-medium">Department</th>
                      <th className="px-6 py-3 font-medium">Status</th>
                    </tr>
                  </thead>
                  <tbody className="text-sm">
                    {teamMembers.map((member, i) => (
                      <tr key={i} className="border-b border-border/50 hover:bg-white/5 transition-colors">
                        <td className="px-6 py-4 font-medium text-white flex items-center gap-3">
                          <div className="w-8 h-8 rounded-full bg-primary/20 text-primary flex items-center justify-center font-bold text-xs">
                            {member.name.split(' ').map(n => n[0]).join('')}
                          </div>
                          {member.name}
                        </td>
                        <td className="px-6 py-4 text-muted-foreground">{member.role}</td>
                        <td className="px-6 py-4 text-muted-foreground">{member.dept}</td>
                        <td className="px-6 py-4">
                          <span className="px-2.5 py-1 rounded-full text-xs font-medium border bg-emerald-500/10 text-emerald-400 border-emerald-500/20">
                            {member.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="bg-card border border-border rounded-xl shadow-sm flex flex-col">
              <div className="p-6 border-b border-border">
                <h2 className="text-lg font-semibold text-white">Recent Activity</h2>
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

              {/* Coming Soon footer */}
              <div className="p-4 border-t border-border">
                <div className="flex items-center gap-2 p-3 bg-background rounded-lg border border-dashed border-border">
                  <Lock className="w-4 h-4 text-slate-500 flex-shrink-0" />
                  <div>
                    <p className="text-xs font-medium text-slate-400">Kanban Board</p>
                    <p className="text-[10px] text-muted-foreground">Coming Soon — Module 2</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
