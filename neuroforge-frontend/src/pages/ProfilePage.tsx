import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, User, Key, Mail, Phone, Shield, Building2, Save, X, Loader2 } from 'lucide-react';
import { useLocation } from 'wouter';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import { getRoleDisplayName } from '@/lib/roleUtils';
import { userService } from '@/services/userService';
import api from '@/services/api';

export default function ProfilePage() {
  const { user, role } = useAuth();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [, setLocation] = useLocation();

  const [tab, setTab] = useState<'profile' | 'security'>('profile');
  const [form, setForm] = useState({ name: user?.name ?? '', username: '', phone: '' });
  const [pwForm, setPwForm] = useState({ current: '', next: '', confirm: '' });

  // Fetch full profile from backend
  const { data: profileRes, isLoading } = useQuery({
    queryKey: ['my-profile'],
    queryFn: () => api.get<any>('/users/me').then(r => r.data),
    retry: false,
  });

  // Populate form once data arrives
  useEffect(() => {
    const d = profileRes?.data;
    if (d) {
      setForm({
        name: d.name ?? user?.name ?? '',
        username: d.username ?? '',
        phone: d.phone ?? '',
      });
    }
  }, [profileRes]);

  const profileData = profileRes?.data ?? {};
  const displayEmail = profileData.email ?? user?.email ?? '';
  const displayRole  = profileData.role  ?? user?.role  ?? role;
  const displayOrgId = profileData.organizationId;
  const initials = (profileData.name ?? user?.name ?? 'U').charAt(0).toUpperCase();

  const updateMutation = useMutation({
    mutationFn: (data: object) => api.put('/users/me', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-profile'] });
      toast({ title: 'Profile updated' });
    },
    onError: (e: any) => toast({ title: 'Update failed', description: e?.response?.data?.message ?? 'Error', variant: 'destructive' }),
  });

  const pwMutation = useMutation({
    mutationFn: () => userService.changePassword(pwForm.current, pwForm.next),
    onSuccess: () => {
      toast({ title: 'Password changed' });
      setPwForm({ current: '', next: '', confirm: '' });
    },
    onError: (e: any) => toast({ title: 'Password change failed', description: e?.response?.data?.message ?? 'Error', variant: 'destructive' }),
  });

  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate({ name: form.name, username: form.username, phone: form.phone });
  };

  const handlePwSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (pwForm.next !== pwForm.confirm) {
      toast({ title: 'Passwords do not match', variant: 'destructive' });
      return;
    }
    if (pwForm.next.length < 8) {
      toast({ title: 'Password too short (min 8 characters)', variant: 'destructive' });
      return;
    }
    pwMutation.mutate();
  };

  const inputCls = 'w-full bg-background border border-border rounded-lg px-3 py-2.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-colors';

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Profile Settings" />
        <main className="flex-1 p-8 overflow-y-auto">

          {/* Back */}
          <button
            onClick={() => setLocation(`/${role}`)}
            className="flex items-center gap-2 text-sm text-muted-foreground hover:text-white transition-colors mb-6"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Dashboard
          </button>

          {/* Header */}
          <div className="flex items-center gap-4 mb-8">
            <div className="w-16 h-16 rounded-2xl bg-primary/20 flex items-center justify-center text-primary text-2xl font-bold ring-2 ring-primary/30">
              {initials}
            </div>
            <div>
              <h1 className="text-xl font-bold text-white">{profileData.name ?? user?.name ?? 'Your Profile'}</h1>
              <p className="text-sm text-muted-foreground mt-0.5">
                {getRoleDisplayName(displayRole || role)}
                {displayOrgId ? ` · Org #${displayOrgId}` : ''}
              </p>
            </div>
          </div>

          {/* Tabs */}
          <div className="flex gap-1 bg-card border border-border rounded-xl p-1 w-fit mb-6">
            {([
              { id: 'profile' as const, label: 'Profile', Icon: User },
              { id: 'security' as const, label: 'Security', Icon: Key },
            ]).map(({ id, label, Icon }) => (
              <button
                key={id}
                onClick={() => setTab(id)}
                className={`flex items-center gap-2 px-5 py-2 rounded-lg text-sm font-medium transition-colors ${
                  tab === id ? 'bg-primary/15 text-primary' : 'text-muted-foreground hover:text-white'
                }`}
              >
                <Icon className="w-4 h-4" />
                {label}
              </button>
            ))}
          </div>

          <div className="max-w-2xl">
            {/* ── Profile Tab ─────────────────────────── */}
            {tab === 'profile' && (
              <form onSubmit={handleProfileSubmit} className="bg-card border border-border rounded-xl p-6 space-y-5">
                <h2 className="text-base font-semibold text-white">Personal Information</h2>

                {isLoading && (
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Loader2 className="w-4 h-4 animate-spin" /> Loading…
                  </div>
                )}

                {/* Read-only */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pb-4 border-b border-border/50">
                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Email</label>
                    <div className="flex items-center gap-3 bg-background/50 border border-border/50 rounded-lg px-3 py-2.5">
                      <Mail className="w-4 h-4 text-muted-foreground" />
                      <span className="text-sm text-muted-foreground">{displayEmail || '—'}</span>
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Role</label>
                    <div className="flex items-center gap-3 bg-background/50 border border-border/50 rounded-lg px-3 py-2.5">
                      <Shield className="w-4 h-4 text-muted-foreground" />
                      <span className="text-sm text-muted-foreground">{getRoleDisplayName(displayRole || role)}</span>
                    </div>
                  </div>
                  {displayOrgId && (
                    <div>
                      <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Organization</label>
                      <div className="flex items-center gap-3 bg-background/50 border border-border/50 rounded-lg px-3 py-2.5">
                        <Building2 className="w-4 h-4 text-muted-foreground" />
                        <span className="text-sm text-muted-foreground">Org #{displayOrgId}</span>
                      </div>
                    </div>
                  )}
                </div>

                {/* Editable */}
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Full Name</label>
                  <input type="text" value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} placeholder="Your full name" required className={inputCls} />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Username</label>
                  <input type="text" value={form.username} onChange={e => setForm(p => ({ ...p, username: e.target.value }))} placeholder="e.g. john_doe" className={inputCls} />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Phone</label>
                  <div className="flex items-center gap-2">
                    <Phone className="w-4 h-4 text-muted-foreground flex-shrink-0" />
                    <input type="tel" value={form.phone} onChange={e => setForm(p => ({ ...p, phone: e.target.value }))} placeholder="+1 555 000 0000" className={inputCls} />
                  </div>
                </div>

                <div className="flex justify-end gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setForm({ name: profileData.name ?? user?.name ?? '', username: profileData.username ?? '', phone: profileData.phone ?? '' })}
                    className="flex items-center gap-2 px-5 py-2 text-sm font-medium text-muted-foreground border border-border rounded-lg hover:text-white transition-colors"
                  >
                    <X className="w-4 h-4" /> Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={updateMutation.isPending}
                    className="flex items-center gap-2 bg-primary text-white text-sm font-medium px-5 py-2 rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-50"
                  >
                    {updateMutation.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                    Save Changes
                  </button>
                </div>
              </form>
            )}

            {/* ── Security Tab ─────────────────────────── */}
            {tab === 'security' && (
              <form onSubmit={handlePwSubmit} className="bg-card border border-border rounded-xl p-6 space-y-5">
                <h2 className="text-base font-semibold text-white">Change Password</h2>

                {([
                  { label: 'Current Password', key: 'current' as const, placeholder: '••••••••' },
                  { label: 'New Password', key: 'next' as const, placeholder: 'Min. 8 characters' },
                  { label: 'Confirm New Password', key: 'confirm' as const, placeholder: '••••••••' },
                ]).map(f => (
                  <div key={f.key}>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">{f.label}</label>
                    <input
                      type="password"
                      value={pwForm[f.key]}
                      onChange={e => setPwForm(p => ({ ...p, [f.key]: e.target.value }))}
                      placeholder={f.placeholder}
                      required
                      className={inputCls}
                    />
                  </div>
                ))}

                <p className="text-xs text-yellow-400 bg-yellow-500/10 border border-yellow-500/20 rounded-lg px-4 py-3">
                  Use at least 8 characters with a mix of letters, numbers, and symbols.
                </p>

                <div className="flex justify-end pt-2">
                  <button
                    type="submit"
                    disabled={pwMutation.isPending}
                    className="flex items-center gap-2 bg-primary text-white text-sm font-medium px-5 py-2 rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-50"
                  >
                    {pwMutation.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : <Key className="w-4 h-4" />}
                    Change Password
                  </button>
                </div>
              </form>
            )}
          </div>

        </main>
      </div>
    </div>
  );
}
