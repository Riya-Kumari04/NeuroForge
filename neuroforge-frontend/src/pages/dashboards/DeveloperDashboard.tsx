import React from 'react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { ShieldCheck, Users, Briefcase, Bell, Lock, GitBranch, Cpu, TerminalSquare, CheckSquare } from 'lucide-react';

const notifications = [
  { id: 1, type: 'role', message: 'Your role has been set to Developer by Org Admin', time: '1 hour ago', read: false },
  { id: 2, type: 'invite', message: 'You were added to project "Phoenix v2.0"', time: '3 hours ago', read: false },
  { id: 3, type: 'system', message: 'Welcome to NeuroForge! Your account is now active.', time: '1 day ago', read: true },
  { id: 4, type: 'system', message: 'Password set successfully via reset flow', time: '1 day ago', read: true },
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

export default function DeveloperDashboard() {
  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Developer Workspace" />

        <main className="flex-1 p-8 overflow-y-auto">

          {/* Module 1 Banner */}
          <div className="mb-8 bg-emerald-500/5 border border-emerald-500/20 rounded-xl px-6 py-4 flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-emerald-400">✓ Module 1 — Authentication & RBAC</p>
              <p className="text-xs text-muted-foreground mt-0.5">You are logged in as Developer. Your account and role are active.</p>
            </div>
            <span className="text-[10px] px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-medium">
              Active Module
            </span>
          </div>

          {/* Stats Row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-500">
                  <ShieldCheck className="w-5 h-5" />
                </div>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">My Role</h3>
              <p className="text-xl font-bold text-white">Developer</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-500">
                  <Briefcase className="w-5 h-5" />
                </div>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Assigned Projects</h3>
              <p className="text-3xl font-bold text-white">2</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-500">
                  <Users className="w-5 h-5" />
                </div>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Team Members</h3>
              <p className="text-3xl font-bold text-white">8</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-500">
                  <Bell className="w-5 h-5" />
                </div>
                <span className="text-xs font-medium text-blue-400 bg-blue-500/10 px-2 py-1 rounded-full">2 New</span>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Notifications</h3>
              <p className="text-3xl font-bold text-white">4</p>
            </div>
          </div>

          {/* Coming Soon Row */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <ComingSoonCard
              icon={CheckSquare}
              title="Task Management"
              description="View and manage your assigned tasks, bugs, and feature tickets."
              module="Coming Soon — Module 2"
            />
            <ComingSoonCard
              icon={GitBranch}
              title="Version Control Integration"
              description="Link commits, branches, and pull requests to project tasks."
              module="Coming Soon — Module 6"
            />
            <ComingSoonCard
              icon={Cpu}
              title="Code Co-pilot"
              description="AI-powered code review, security scanning, and optimisation hints."
              module="Coming Soon — Module 10"
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Notifications */}
            <div className="bg-card border border-border rounded-xl shadow-sm flex flex-col">
              <div className="p-6 border-b border-border flex items-center justify-between">
                <h2 className="text-lg font-semibold text-white">Notifications</h2>
                <button className="text-xs text-primary hover:text-blue-400 transition-colors">Mark all read</button>
              </div>
              <div className="p-3 space-y-2 flex-1">
                {notifications.map((notif) => (
                  <div key={notif.id} className={`flex items-start gap-3 p-3 rounded-lg border border-transparent hover:border-border transition-colors ${!notif.read ? 'bg-primary/5' : ''}`}>
                    <div className="mt-1 relative">
                      <div className={`w-2.5 h-2.5 rounded-full ${notif.type === 'role' ? 'bg-purple-500' : notif.type === 'invite' ? 'bg-blue-500' : 'bg-slate-500'}`} />
                      {!notif.read && <div className="absolute -top-1 -right-1 w-1.5 h-1.5 bg-primary rounded-full" />}
                    </div>
                    <div className="flex-1">
                      <p className={`text-sm ${!notif.read ? 'text-white font-medium' : 'text-muted-foreground'}`}>{notif.message}</p>
                      <p className="text-xs text-muted-foreground mt-1">{notif.time}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Account Activity Timeline */}
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm flex flex-col">
              <h2 className="text-lg font-semibold text-white mb-6">Account Activity</h2>
              <div className="relative border-l border-border/50 ml-3 flex-1 space-y-6">
                {[
                  { action: 'Logged in successfully', time: '10 mins ago', type: 'login' },
                  { action: 'Role assigned: Developer', time: '1 hour ago', type: 'role' },
                  { action: 'Added to project Phoenix v2.0', time: '3 hours ago', type: 'project' },
                  { action: 'Account created & verified', time: '1 day ago', type: 'account' },
                  { action: 'Invitation email accepted', time: '1 day ago', type: 'invite' },
                ].map((item, i) => (
                  <div key={i} className="relative pl-6">
                    <div className={`absolute -left-[5px] top-1.5 w-2 h-2 rounded-full
                      ${item.type === 'login' ? 'bg-emerald-500' :
                        item.type === 'role' ? 'bg-purple-500' :
                        item.type === 'project' ? 'bg-blue-500' :
                        item.type === 'account' ? 'bg-primary' : 'bg-amber-500'}`}
                    />
                    <p className="text-sm font-medium text-white">{item.action}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">{item.time}</p>
                  </div>
                ))}
              </div>

              {/* Coming Soon */}
              <div className="mt-6 pt-4 border-t border-border">
                <div className="flex items-center gap-2 p-3 bg-background rounded-lg border border-dashed border-border">
                  <TerminalSquare className="w-4 h-4 text-slate-500 flex-shrink-0" />
                  <div>
                    <p className="text-xs font-medium text-slate-400">IDE Integration & Code Activity</p>
                    <p className="text-[10px] text-muted-foreground">Coming Soon — Module 6</p>
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
