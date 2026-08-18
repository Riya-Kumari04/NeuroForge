import React, { useState, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { ArrowLeft, Palette, Bell, Globe, Save, Loader2, Check } from 'lucide-react';
import { useLocation } from 'wouter';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import api from '@/services/api';

const LANGUAGES = ['English', 'French', 'German', 'Spanish', 'Portuguese', 'Arabic', 'Chinese', 'Japanese', 'Hindi', 'Korean'];
const TIMEZONES = ['UTC', 'America/New_York', 'America/Chicago', 'America/Denver', 'America/Los_Angeles', 'Europe/London', 'Europe/Paris', 'Europe/Berlin', 'Asia/Kolkata', 'Asia/Tokyo', 'Asia/Shanghai', 'Australia/Sydney'];

interface Prefs {
  theme: 'dark' | 'light' | 'system';
  notifications: boolean;
  language: string;
  timezone: string;
}

const DEFAULTS: Prefs = { theme: 'dark', notifications: true, language: 'English', timezone: 'UTC' };

export default function PreferencesPage() {
  const { role } = useAuth();
  const { toast } = useToast();
  const [, setLocation] = useLocation();
  const [prefs, setPrefs] = useState<Prefs>(DEFAULTS);
  const [dirty, setDirty] = useState(false);

  const { data: prefRes, isLoading } = useQuery({
    queryKey: ['my-preferences'],
    queryFn: () => api.get<any>('/users/me/preferences').then(r => r.data),
    retry: false,
  });

  useEffect(() => {
    const d = prefRes?.data;
    if (d) setPrefs(prev => ({ ...prev, ...d }));
  }, [prefRes]);

  const patch = (updates: Partial<Prefs>) => {
    setPrefs(p => ({ ...p, ...updates }));
    setDirty(true);
  };

  const saveMutation = useMutation({
    mutationFn: () => api.put('/users/me/preferences', prefs),
    onSuccess: () => { setDirty(false); toast({ title: 'Preferences saved' }); },
    onError: (e: any) => toast({ title: 'Save failed', description: e?.response?.data?.message ?? 'Error', variant: 'destructive' }),
  });

  const selectCls = 'w-full bg-background border border-border rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:ring-1 focus:ring-primary transition-colors';

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Preferences" />
        <main className="flex-1 p-8 overflow-y-auto">

          {/* Back */}
          <button
            onClick={() => setLocation(`/${role}`)}
            className="flex items-center gap-2 text-sm text-muted-foreground hover:text-white transition-colors mb-6"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Dashboard
          </button>

          <div className="max-w-2xl space-y-6">

            {/* ── Appearance ─────────────────────────────────── */}
            <div className="bg-card border border-border rounded-xl p-6">
              <div className="flex items-center gap-2 mb-5">
                <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center">
                  <Palette className="w-4 h-4 text-primary" />
                </div>
                <h3 className="text-sm font-semibold text-white">Appearance</h3>
              </div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Theme</label>
              <div className="flex gap-3">
                {(['dark', 'light', 'system'] as const).map(t => (
                  <button
                    key={t}
                    type="button"
                    onClick={() => patch({ theme: t })}
                    className={`flex items-center gap-2 px-4 py-2 rounded-lg border text-sm font-medium transition-colors capitalize ${
                      prefs.theme === t
                        ? 'border-primary bg-primary/10 text-primary'
                        : 'border-border text-muted-foreground hover:text-white hover:border-border/80'
                    }`}
                  >
                    {prefs.theme === t && <Check className="w-3.5 h-3.5" />}
                    {t}
                  </button>
                ))}
              </div>
            </div>

            {/* ── Notifications ───────────────────────────────── */}
            <div className="bg-card border border-border rounded-xl p-6">
              <div className="flex items-center gap-2 mb-5">
                <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center">
                  <Bell className="w-4 h-4 text-primary" />
                </div>
                <h3 className="text-sm font-semibold text-white">Notifications</h3>
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-white font-medium">Enable notifications</p>
                  <p className="text-xs text-muted-foreground mt-0.5">Receive alerts for tasks, invitations, and activity.</p>
                </div>
                <button
                  type="button"
                  onClick={() => patch({ notifications: !prefs.notifications })}
                  className={`relative w-11 h-6 rounded-full transition-colors ${prefs.notifications ? 'bg-primary' : 'bg-border'}`}
                >
                  <span className={`absolute top-0.5 w-5 h-5 rounded-full bg-white shadow transition-transform ${prefs.notifications ? 'translate-x-5' : 'translate-x-0.5'}`} />
                </button>
              </div>
            </div>

            {/* ── Language & Timezone ─────────────────────────── */}
            <div className="bg-card border border-border rounded-xl p-6">
              <div className="flex items-center gap-2 mb-5">
                <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center">
                  <Globe className="w-4 h-4 text-primary" />
                </div>
                <h3 className="text-sm font-semibold text-white">Language &amp; Region</h3>
              </div>
              <div className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Language</label>
                  <select value={prefs.language} onChange={e => patch({ language: e.target.value })} className={selectCls}>
                    {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Timezone</label>
                  <select value={prefs.timezone} onChange={e => patch({ timezone: e.target.value })} className={selectCls}>
                    {TIMEZONES.map(tz => <option key={tz} value={tz}>{tz}</option>)}
                  </select>
                </div>
              </div>
            </div>

            {/* ── Save bar ────────────────────────────────────── */}
            <div className="flex items-center justify-between pt-2">
              {dirty && <p className="text-xs text-yellow-400">You have unsaved changes.</p>}
              <div className="ml-auto">
                <button
                  type="button"
                  onClick={() => saveMutation.mutate()}
                  disabled={saveMutation.isPending || isLoading}
                  className="flex items-center gap-2 bg-primary text-white text-sm font-medium px-5 py-2.5 rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-50"
                >
                  {saveMutation.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                  {isLoading ? 'Loading…' : 'Save Preferences'}
                </button>
              </div>
            </div>

          </div>
        </main>
      </div>
    </div>
  );
}
