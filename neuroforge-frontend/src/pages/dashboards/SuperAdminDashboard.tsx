import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'wouter';
import { Users, Building, FolderKanban, CheckSquare, Loader2, Plus, X, Clock, BarChart3, Trash2 } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { organizationService, Organization } from '@/services/organizationService';
import api from '@/services/api';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/context/AuthContext';

interface DashboardStats {
  totalProjects: number; activeProjects: number; completedProjects: number;
  totalSprints: number; totalTasks: number; completedTasks: number;
  pendingTasks: number; overallProgress: number;
}
interface UserDto { id: number; name: string; email: string; role: string; enabled?: boolean; }

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
  const [showCreateUserModal, setShowCreateUserModal] = useState(false);
  const [showPendingUsers, setShowPendingUsers] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<UserDto | null>(null);
  const [newUser, setNewUser] = useState({ name: '', username: '', email: '', password: '', role: 'ROLE_DEVELOPER', organizationId: '' as any, enabled: true });
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const { user: currentUser } = useAuth();

  const { data: orgsData, isLoading: orgsLoading } = useQuery({
    queryKey: ['organizations'],
    queryFn: () => organizationService.getAll().then(r => r.data),
  });
  const { data: usersData, isLoading: usersLoading } = useQuery({
    queryKey: ['all-users'],
    queryFn: () => api.get<any>('/users').then(r => r.data),
  });
  const { data: pendingUsersData, isLoading: pendingUsersLoading } = useQuery({
    queryKey: ['pending-users'],
    queryFn: () => api.get<any>('/users/pending').then(r => r.data),
    enabled: showPendingUsers,
  });
  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => api.get<any>('/dashboard').then(r => r.data),
  });

  const orgs: Organization[] = orgsData?.data || [];
  const users: UserDto[] = usersData?.data || [];
  const stats: DashboardStats | undefined = statsData?.data;
  const isLoading = orgsLoading || usersLoading || statsLoading;

  const createUserMutation = useMutation({
    mutationFn: (userData: any) => api.post<any>('/users', userData).then(r => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['all-users'] });
      setShowCreateUserModal(false);
      setNewUser({ name: '', username: '', email: '', password: '', role: 'ROLE_DEVELOPER', organizationId: '', enabled: true });
      toast({ title: 'User Created', description: 'New user has been created successfully.' });
    },
    onError: (err: any) => {
      toast({ title: 'Error', description: err?.response?.data?.message || 'Failed to create user', variant: 'destructive' });
    },
  });

  const approveUserMutation = useMutation({
    mutationFn: ({ userId, action }: { userId: number; action: string }) =>
      api.put<any>(`/users/${userId}/approve`, { action }).then(r => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['all-users'] });
      queryClient.invalidateQueries({ queryKey: ['pending-users'] });
      toast({ title: 'User Updated', description: 'User approval status has been updated.' });
    },
    onError: (err: any) => {
      toast({ title: 'Error', description: err?.response?.data?.message || 'Failed to update user', variant: 'destructive' });
    },
  });

  const deleteUserMutation = useMutation({
    mutationFn: (userId: number) => api.delete<any>(`/users/${userId}`).then(r => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['all-users'] });
      queryClient.invalidateQueries({ queryKey: ['pending-users'] });
      setDeleteTarget(null);
      toast({ title: 'User Deleted', description: 'User has been removed from the system.' });
    },
    onError: (err: any) => {
      toast({ title: 'Error', description: err?.response?.data?.message || 'Failed to delete user', variant: 'destructive' });
    },
  });

  const handleCreateUser = () => {
    if (!newUser.name || !newUser.username || !newUser.email || !newUser.password || !newUser.role) {
      toast({ title: 'Error', description: 'Please fill all required fields', variant: 'destructive' });
      return;
    }
    createUserMutation.mutate(newUser);
  };

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Super Admin Dashboard" />
        <main className="flex-1 p-8 overflow-y-auto">

          <div className="mb-8 flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-white">Super Admin Dashboard</h2>
              <p className="text-muted-foreground text-sm mt-1">Manage all organizations, users, and platform settings.</p>
            </div>
            <Link href="/super-admin/analytics" className="flex items-center gap-2 bg-primary hover:bg-primary/90 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors">
              <BarChart3 className="w-4 h-4" />
              View Analytics
            </Link>
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
                  <div className="flex gap-2">
                    <button
                      onClick={() => setShowCreateUserModal(true)}
                      className="flex items-center gap-2 bg-primary hover:bg-primary/90 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
                    >
                      <Plus className="w-4 h-4" />
                      Add User
                    </button>
                    <button
                      onClick={() => setShowPendingUsers(true)}
                      className="flex items-center gap-2 bg-amber-500/20 hover:bg-amber-500/30 text-amber-400 text-sm font-medium px-4 py-2 rounded-lg transition-colors"
                    >
                      <Clock className="w-4 h-4" />
                      Pending Approvals
                    </button>
                  </div>
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
                          <th className="px-6 py-3 font-medium">Status</th>
                          <th className="px-6 py-3 font-medium">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="text-sm divide-y divide-border/50">
                        {users.map((u) => (
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
                            <td className="px-6 py-4">
                              <span className={`text-xs px-2 py-0.5 rounded ${u.enabled ? 'bg-green-500/10 text-green-400 border-green-500/20' : 'bg-red-500/10 text-red-400 border-red-500/20'}`}>
                                {u.enabled ? 'Active' : 'Inactive'}
                              </span>
                            </td>
                            <td className="px-6 py-4">
                              {u.id !== currentUser?.id && (
                                <button
                                  onClick={() => setDeleteTarget(u)}
                                  className="text-red-400 hover:text-red-300 transition-colors"
                                  title="Delete user"
                                >
                                  <Trash2 className="w-4 h-4" />
                                </button>
                              )}
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

      {showCreateUserModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-card border border-border rounded-xl p-6 w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-white">Create New User</h3>
              <button onClick={() => setShowCreateUserModal(false)} className="text-muted-foreground hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-white mb-2">Name *</label>
                <input
                  type="text"
                  className="w-full bg-background border border-border rounded-lg p-3 text-white focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="Full name"
                  value={newUser.name}
                  onChange={(e) => setNewUser({ ...newUser, name: e.target.value })}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-white mb-2">Username *</label>
                <input
                  type="text"
                  className="w-full bg-background border border-border rounded-lg p-3 text-white focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="Username"
                  value={newUser.username}
                  onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-white mb-2">Email *</label>
                <input
                  type="email"
                  className="w-full bg-background border border-border rounded-lg p-3 text-white focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="email@example.com"
                  value={newUser.email}
                  onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-white mb-2">Password *</label>
                <input
                  type="password"
                  className="w-full bg-background border border-border rounded-lg p-3 text-white focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="Min 8 characters"
                  value={newUser.password}
                  onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-white mb-2">Role *</label>
                <select
                  className="w-full bg-background border border-border rounded-lg p-3 text-white focus:outline-none focus:ring-2 focus:ring-primary"
                  value={newUser.role}
                  onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
                >
                  <option value="ROLE_SUPER_ADMIN">Super Admin</option>
                  <option value="ROLE_ORG_ADMIN">Organization Admin</option>
                  <option value="ROLE_PROJECT_MANAGER">Project Manager</option>
                  <option value="ROLE_DEVELOPER">Developer</option>
                  <option value="ROLE_QA">QA</option>
                  <option value="ROLE_CLIENT">Client</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-white mb-2">Organization</label>
                <select
                  className="w-full bg-background border border-border rounded-lg p-3 text-white focus:outline-none focus:ring-2 focus:ring-primary"
                  value={newUser.organizationId}
                  onChange={(e) => setNewUser({ ...newUser, organizationId: e.target.value })}
                >
                  <option value="">None</option>
                  {orgs.map((org) => (
                    <option key={org.id} value={org.id}>{org.name}</option>
                  ))}
                </select>
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="enabled"
                  checked={newUser.enabled}
                  onChange={(e) => setNewUser({ ...newUser, enabled: e.target.checked })}
                  className="w-4 h-4 rounded border-border"
                />
                <label htmlFor="enabled" className="text-sm text-white">Enable user immediately</label>
              </div>
              <div className="flex gap-3 pt-4">
                <button
                  onClick={handleCreateUser}
                  disabled={createUserMutation.isPending}
                  className="flex-1 bg-primary hover:bg-primary/90 text-white font-medium py-3 px-4 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {createUserMutation.isPending ? 'Creating...' : 'Create User'}
                </button>
                <button
                  onClick={() => setShowCreateUserModal(false)}
                  className="flex-1 bg-secondary hover:bg-secondary/80 text-white font-medium py-3 px-4 rounded-lg transition-colors"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Pending Users Modal */}
      {showPendingUsers && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-card border border-border rounded-xl p-6 w-full max-w-4xl shadow-xl max-h-[80vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-white">Pending User Approvals</h3>
              <button onClick={() => setShowPendingUsers(false)} className="text-muted-foreground hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>
            {pendingUsersLoading ? (
              <div className="flex justify-center py-12">
                <Loader2 className="w-6 h-6 animate-spin text-primary" />
              </div>
            ) : (
              <>
                {pendingUsersData?.data?.length === 0 ? (
                  <div className="text-center text-sm text-muted-foreground py-8">No pending users awaiting approval.</div>
                ) : (
                  <div className="space-y-4">
                    {pendingUsersData?.data?.map((user: UserDto) => (
                      <div key={user.id} className="bg-background/50 border border-border rounded-lg p-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-4">
                            <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-sm">
                              {user.name?.charAt(0)?.toUpperCase() || 'U'}
                            </div>
                            <div>
                              <p className="font-medium text-white">{user.name}</p>
                              <p className="text-sm text-muted-foreground">{user.email}</p>
                              <div className="flex items-center gap-2 mt-1">
                                <span className="text-xs px-2 py-0.5 rounded bg-slate-500/10 text-slate-400 border border-slate-500/20">
                                  {user.role?.replace('ROLE_', '') || 'USER'}
                                </span>
                                <span className="text-xs px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20">
                                  PENDING
                                </span>
                              </div>
                            </div>
                          </div>
                          <div className="flex gap-2">
                            <button
                              onClick={() => approveUserMutation.mutate({ userId: user.id, action: 'APPROVE' })}
                              disabled={approveUserMutation.isPending}
                              className="px-3 py-1.5 text-sm bg-emerald-500/20 text-emerald-400 rounded hover:bg-emerald-500/30 transition-colors disabled:opacity-50"
                            >
                              Approve
                            </button>
                            <button
                              onClick={() => approveUserMutation.mutate({ userId: user.id, action: 'REJECT' })}
                              disabled={approveUserMutation.isPending}
                              className="px-3 py-1.5 text-sm bg-red-500/20 text-red-400 rounded hover:bg-red-500/30 transition-colors disabled:opacity-50"
                            >
                              Reject
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      )}

      {/* Delete User Confirmation Dialog */}
      {deleteTarget && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-card border border-border rounded-xl p-6 w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-white">Delete User</h3>
              <button onClick={() => setDeleteTarget(null)} className="text-muted-foreground hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-4">
              <p className="text-sm text-white">
                Are you sure you want to delete <span className="font-semibold">{deleteTarget.name}</span> ({deleteTarget.email})?
              </p>
              <p className="text-xs text-red-400">
                This action will permanently remove the user from the system, including all their assignments and access.
              </p>
              <div className="flex gap-3 pt-4">
                <button
                  onClick={() => deleteUserMutation.mutate(deleteTarget.id)}
                  disabled={deleteUserMutation.isPending}
                  className="flex-1 bg-red-500 hover:bg-red-600 text-white font-medium py-3 px-4 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {deleteUserMutation.isPending ? 'Deleting...' : 'Delete User'}
                </button>
                <button
                  onClick={() => setDeleteTarget(null)}
                  className="flex-1 bg-secondary hover:bg-secondary/80 text-white font-medium py-3 px-4 rounded-lg transition-colors"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
