import React from 'react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { Users, Building, FolderKanban, Activity, ShieldAlert, Lock, BarChart2, Cpu } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const userGrowthData = [
  { name: 'Jan', users: 4000 },
  { name: 'Feb', users: 5500 },
  { name: 'Mar', users: 7200 },
  { name: 'Apr', users: 8500 },
  { name: 'May', users: 9800 },
  { name: 'Jun', users: 11200 },
  { name: 'Jul', users: 13500 },
];

const orgData = [
  { id: 1, name: 'Acme Corp', users: 124, projects: 12, status: 'Active', plan: 'Enterprise' },
  { id: 2, name: 'Globex Inc', users: 86, projects: 8, status: 'Active', plan: 'Pro' },
  { id: 3, name: 'Soylent Ltd', users: 215, projects: 24, status: 'Warning', plan: 'Enterprise' },
  { id: 4, name: 'Initech', users: 42, projects: 5, status: 'Active', plan: 'Pro' },
  { id: 5, name: 'Umbrella Corp', users: 310, projects: 42, status: 'Active', plan: 'Enterprise' },
];

const logs = [
  { id: 1, action: 'User Login', status: 'Success', time: '5 mins ago', user: 'admin@acme.com' },
  { id: 2, action: 'Failed Login Attempt', status: 'Warning', time: '15 mins ago', user: 'Unknown' },
  { id: 3, action: 'New Organisation Created', status: 'Success', time: '1 hour ago', user: 'Admin' },
  { id: 4, action: 'Role Assigned: Developer', status: 'Success', time: '2 hours ago', user: 'Admin' },
  { id: 5, action: 'Password Reset Requested', status: 'Warning', time: '3 hours ago', user: 'john@globex.com' },
];

function ComingSoonCard({ title, description, module }: { title: string; description: string; module: string }) {
  return (
    <div className="bg-card border border-dashed border-border rounded-xl p-6 flex flex-col items-center justify-center text-center min-h-[200px]">
      <div className="w-10 h-10 rounded-lg bg-slate-500/10 flex items-center justify-center text-slate-500 mb-3">
        <Lock className="w-5 h-5" />
      </div>
      <h3 className="text-sm font-semibold text-white mb-1">{title}</h3>
      <p className="text-xs text-muted-foreground mb-3">{description}</p>
      <span className="text-[10px] px-2.5 py-1 rounded-full bg-slate-500/10 text-slate-400 border border-slate-500/20 font-medium">
        {module}
      </span>
    </div>
  );
}

export default function SuperAdminDashboard() {
  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Super Admin Control Center" />

        <main className="flex-1 p-8 overflow-y-auto">

          {/* Module 1 Banner */}
          <div className="mb-8 bg-emerald-500/5 border border-emerald-500/20 rounded-xl px-6 py-4 flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-emerald-400">✓ Module 1 — Authentication & RBAC</p>
              <p className="text-xs text-muted-foreground mt-0.5">You are logged in as Super Admin. Full platform access is active.</p>
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
                  <Users className="w-5 h-5" />
                </div>
                <span className="text-xs font-medium text-emerald-500 bg-emerald-500/10 px-2 py-1 rounded-full">+12%</span>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Total Users</h3>
              <p className="text-3xl font-bold text-white">13,542</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-500">
                  <Building className="w-5 h-5" />
                </div>
                <span className="text-xs font-medium text-emerald-500 bg-emerald-500/10 px-2 py-1 rounded-full">+5%</span>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Organisations</h3>
              <p className="text-3xl font-bold text-white">524</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-500">
                  <FolderKanban className="w-5 h-5" />
                </div>
                <span className="text-xs font-medium text-emerald-500 bg-emerald-500/10 px-2 py-1 rounded-full">+18%</span>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">Active Projects</h3>
              <p className="text-3xl font-bold text-white">2,104</p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="w-10 h-10 rounded-lg bg-rose-500/10 flex items-center justify-center text-rose-500">
                  <Activity className="w-5 h-5" />
                </div>
                <span className="text-xs font-medium text-emerald-500 bg-emerald-500/10 px-2 py-1 rounded-full">99.9%</span>
              </div>
              <h3 className="text-muted-foreground text-sm font-medium mb-1">System Health</h3>
              <p className="text-3xl font-bold text-white">Stable</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
            {/* User Registration Chart */}
            <div className="bg-card border border-border rounded-xl p-6 lg:col-span-2 shadow-sm">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-semibold text-white">User Registration Trend</h2>
                <span className="text-xs text-muted-foreground bg-background border border-border px-3 py-1.5 rounded-lg">Last 6 Months</span>
              </div>
              <div className="h-72 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={userGrowthData} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                    <XAxis dataKey="name" stroke="#64748b" tick={{ fill: '#64748b', fontSize: 12 }} tickLine={false} axisLine={false} />
                    <YAxis stroke="#64748b" tick={{ fill: '#64748b', fontSize: 12 }} tickLine={false} axisLine={false} tickFormatter={(val) => `${val / 1000}k`} />
                    <Tooltip contentStyle={{ backgroundColor: '#111827', borderColor: '#1e293b', borderRadius: '8px', color: '#fff' }} itemStyle={{ color: '#3b82f6' }} />
                    <Line type="monotone" dataKey="users" stroke="#3b82f6" strokeWidth={3} dot={{ r: 4, fill: '#111827', strokeWidth: 2 }} activeDot={{ r: 6, fill: '#3b82f6' }} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Quick Actions + Coming Soon */}
            <div className="space-y-6">
              <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
                <h2 className="text-lg font-semibold text-white mb-4">Quick Actions</h2>
                <div className="space-y-2">
                  <button className="w-full flex items-center justify-between p-3 rounded-lg bg-background border border-border hover:border-primary/50 transition-colors group">
                    <span className="text-sm font-medium text-white">Add Organisation</span>
                    <span className="w-6 h-6 rounded bg-primary/10 text-primary flex items-center justify-center group-hover:bg-primary group-hover:text-white transition-colors">+</span>
                  </button>
                  <button className="w-full flex items-center justify-between p-3 rounded-lg bg-background border border-border hover:border-primary/50 transition-colors group">
                    <span className="text-sm font-medium text-white">Global Announcement</span>
                    <span className="w-6 h-6 rounded bg-primary/10 text-primary flex items-center justify-center group-hover:bg-primary group-hover:text-white transition-colors">↗</span>
                  </button>
                  <button className="w-full flex items-center justify-between p-3 rounded-lg bg-background border border-border hover:border-primary/50 transition-colors group">
                    <span className="text-sm font-medium text-white">System Settings</span>
                    <span className="w-6 h-6 rounded bg-primary/10 text-primary flex items-center justify-center group-hover:bg-primary group-hover:text-white transition-colors">⚙</span>
                  </button>
                </div>
              </div>

              {/* Coming Soon */}
              <div className="bg-card border border-dashed border-border rounded-xl p-5 flex flex-col items-center justify-center text-center">
                <div className="w-9 h-9 rounded-lg bg-slate-500/10 flex items-center justify-center text-slate-500 mb-3">
                  <Cpu className="w-4 h-4" />
                </div>
                <h3 className="text-sm font-semibold text-white mb-1">AI Analytics Suite</h3>
                <p className="text-xs text-muted-foreground mb-3">Platform-wide AI insights and recommendations.</p>
                <span className="text-[10px] px-2.5 py-1 rounded-full bg-slate-500/10 text-slate-400 border border-slate-500/20 font-medium">
                  Coming Soon — Module 10
                </span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Organisations Table */}
            <div className="bg-card border border-border rounded-xl shadow-sm overflow-hidden lg:col-span-2">
              <div className="p-6 border-b border-border flex items-center justify-between">
                <h2 className="text-lg font-semibold text-white">Top Organisations</h2>
                <button className="text-sm text-primary hover:text-blue-400">View All</button>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-border text-xs uppercase tracking-wider text-muted-foreground bg-background/50">
                      <th className="px-6 py-3 font-medium">Organisation</th>
                      <th className="px-6 py-3 font-medium">Users</th>
                      <th className="px-6 py-3 font-medium">Projects</th>
                      <th className="px-6 py-3 font-medium">Plan</th>
                      <th className="px-6 py-3 font-medium">Status</th>
                    </tr>
                  </thead>
                  <tbody className="text-sm">
                    {orgData.map((org) => (
                      <tr key={org.id} className="border-b border-border/50 hover:bg-white/5 transition-colors">
                        <td className="px-6 py-4 font-medium text-white">{org.name}</td>
                        <td className="px-6 py-4 text-muted-foreground">{org.users}</td>
                        <td className="px-6 py-4 text-muted-foreground">{org.projects}</td>
                        <td className="px-6 py-4">
                          <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${org.plan === 'Enterprise' ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20' : 'bg-blue-500/10 text-blue-400 border border-blue-500/20'}`}>
                            {org.plan}
                          </span>
                        </td>
                        <td className="px-6 py-4">
                          <span className={`flex items-center gap-1.5 ${org.status === 'Active' ? 'text-emerald-500' : 'text-amber-500'}`}>
                            <span className={`w-1.5 h-1.5 rounded-full ${org.status === 'Active' ? 'bg-emerald-500' : 'bg-amber-500'}`}></span>
                            {org.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Auth / System Logs */}
            <div className="bg-card border border-border rounded-xl shadow-sm flex flex-col">
              <div className="p-6 border-b border-border flex items-center justify-between">
                <h2 className="text-lg font-semibold text-white">Auth Logs</h2>
                <ShieldAlert className="w-4 h-4 text-muted-foreground" />
              </div>
              <div className="p-4 flex-1 flex flex-col gap-1 overflow-y-auto">
                {logs.map((log) => (
                  <div key={log.id} className="flex items-start gap-3 p-3 rounded-lg hover:bg-white/5 transition-colors">
                    <div className={`mt-0.5 w-2 h-2 rounded-full flex-shrink-0 ${log.status === 'Success' ? 'bg-emerald-500' : 'bg-amber-500'}`} />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-white truncate">{log.action}</p>
                      <div className="flex items-center gap-2 text-xs text-muted-foreground mt-1">
                        <span className="truncate">{log.user}</span>
                        <span>•</span>
                        <span className="flex-shrink-0">{log.time}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* Coming Soon footer */}
              <div className="p-4 border-t border-border">
                <div className="flex items-center gap-2 p-3 bg-background rounded-lg border border-dashed border-border">
                  <BarChart2 className="w-4 h-4 text-slate-500 flex-shrink-0" />
                  <div>
                    <p className="text-xs font-medium text-slate-400">Advanced Log Analytics</p>
                    <p className="text-[10px] text-muted-foreground">Coming Soon — Module 7</p>
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
