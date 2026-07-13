import React from 'react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { Briefcase, CheckCircle2, CalendarCheck, MessageSquare, Lock, BarChart2, FileText, Bell } from 'lucide-react';

const recentUpdates = [
  { id: 1, author: 'Project Manager', message: 'Your account has been created and your role is set to Client / Stakeholder.', time: '1 hour ago' },
  { id: 2, author: 'System', message: 'Welcome to NeuroForge! You now have read-only access to your assigned projects.', time: '2 hours ago' },
  { id: 3, author: 'Org Admin', message: 'You have been added to "Customer Portal Redesign" as a stakeholder.', time: '1 day ago' },
];

const notifications = [
  { id: 1, message: 'Your account is now active and ready', time: '1 hour ago', read: false },
  { id: 2, message: 'You were added to project Phoenix v2.0', time: '3 hours ago', read: false },
  { id: 3, message: 'Invitation accepted — welcome to NeuroForge', time: '1 day ago', read: true },
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

export default function ClientDashboard() {
  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Client Portal" />

        <main className="flex-1 p-8 overflow-y-auto">

          {/* Welcome Banner */}
          <div className="bg-gradient-to-r from-primary/20 via-[#0F172A] to-transparent border border-primary/20 rounded-2xl p-8 mb-8 relative overflow-hidden">
            <div className="absolute top-[-50%] right-[-10%] w-64 h-64 bg-primary/20 rounded-full blur-[80px] pointer-events-none" />
            <p className="text-xs font-semibold text-emerald-400 mb-2">✓ Module 1 — Authentication & RBAC</p>
            <h1 className="text-2xl font-bold text-white mb-2">Welcome back, Client.</h1>
            <p className="text-muted-foreground max-w-2xl">
              You are logged in as a Client / Stakeholder. Your account is active and you have been granted read-only visibility into your assigned projects.
            </p>
          </div>

          {/* Quick Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-blue-500/10 flex items-center justify-center text-blue-500">
                  <Briefcase className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="text-muted-foreground text-sm font-medium">Assigned Projects</h3>
                  <p className="text-2xl font-bold text-white">3</p>
                </div>
              </div>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-emerald-500/10 flex items-center justify-center text-emerald-500">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="text-muted-foreground text-sm font-medium">Account Status</h3>
                  <p className="text-xl font-bold text-white mt-1">Active</p>
                </div>
              </div>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-amber-500/10 flex items-center justify-center text-amber-500">
                  <Bell className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="text-muted-foreground text-sm font-medium">Notifications</h3>
                  <p className="text-2xl font-bold text-white">3</p>
                </div>
              </div>
            </div>
          </div>

          {/* Coming Soon Row */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <ComingSoonCard
              icon={BarChart2}
              title="Project Progress Tracking"
              description="Real-time progress bars, sprint completion, and delivery status."
              module="Coming Soon — Module 3"
            />
            <ComingSoonCard
              icon={FileText}
              title="Deliverables & Documents"
              description="Download reports, architecture diagrams, and project documentation."
              module="Coming Soon — Module 3"
            />
            <ComingSoonCard
              icon={CalendarCheck}
              title="Project Timeline"
              description="Interactive project roadmap with sprint and release tracking."
              module="Coming Soon — Module 3"
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Latest Updates */}
            <div className="lg:col-span-2 bg-card border border-border rounded-xl shadow-sm flex flex-col">
              <div className="p-6 border-b border-border flex items-center gap-2">
                <MessageSquare className="w-5 h-5 text-primary" />
                <h2 className="text-lg font-semibold text-white">Latest Updates</h2>
              </div>
              <div className="p-6 space-y-5 flex-1">
                {recentUpdates.map((update) => (
                  <div key={update.id} className="relative pb-5 border-b border-border/30 last:border-0 last:pb-0">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-white">{update.author}</span>
                      <span className="text-[10px] text-muted-foreground">{update.time}</span>
                    </div>
                    <p className="text-sm text-muted-foreground leading-relaxed">{update.message}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Side Panel */}
            <div className="flex flex-col gap-6">
              {/* Quick Actions */}
              <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
                <h2 className="text-lg font-semibold text-white mb-4">Quick Actions</h2>
                <div className="space-y-3">
                  <button className="w-full flex items-center justify-center gap-2 p-3 rounded-lg bg-primary text-white hover:bg-blue-600 transition-colors shadow-lg shadow-blue-500/20 font-medium text-sm">
                    <MessageSquare className="w-4 h-4" />
                    Message Team
                  </button>
                  <button className="w-full flex items-center justify-center gap-2 p-3 rounded-lg bg-background border border-border hover:border-primary/50 text-white transition-colors font-medium text-sm">
                    <CalendarCheck className="w-4 h-4 text-muted-foreground" />
                    Schedule Review Call
                  </button>
                </div>
              </div>

              {/* Notifications */}
              <div className="bg-card border border-border rounded-xl shadow-sm flex flex-col flex-1">
                <div className="p-5 border-b border-border flex items-center justify-between">
                  <h2 className="text-base font-semibold text-white">Notifications</h2>
                  <button className="text-xs text-primary hover:text-blue-400">Mark all read</button>
                </div>
                <div className="p-3 space-y-2">
                  {notifications.map((notif) => (
                    <div key={notif.id} className={`flex items-start gap-3 p-3 rounded-lg ${!notif.read ? 'bg-primary/5' : ''}`}>
                      <div className="mt-1 relative">
                        <div className="w-2 h-2 rounded-full bg-blue-500" />
                        {!notif.read && <div className="absolute -top-1 -right-1 w-1.5 h-1.5 bg-primary rounded-full" />}
                      </div>
                      <div>
                        <p className={`text-sm ${!notif.read ? 'text-white font-medium' : 'text-muted-foreground'}`}>{notif.message}</p>
                        <p className="text-xs text-muted-foreground mt-1">{notif.time}</p>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Coming Soon footer */}
                <div className="p-4 border-t border-border mt-auto">
                  <div className="flex items-center gap-2 p-3 bg-background rounded-lg border border-dashed border-border">
                    <Lock className="w-4 h-4 text-slate-500 flex-shrink-0" />
                    <div>
                      <p className="text-xs font-medium text-slate-400">Release Notes & Changelog</p>
                      <p className="text-[10px] text-muted-foreground">Coming Soon — Module 3</p>
                    </div>
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
