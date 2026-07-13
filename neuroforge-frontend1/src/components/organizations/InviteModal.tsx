import React, { useState } from 'react';
import { X, Loader2, Mail, UserCheck } from 'lucide-react';
import { OrgRole } from '@/services/organizationService';

interface InviteModalProps {
  isOpen: boolean;
  onClose: () => void;
  onInvite: (email: string, role: OrgRole) => Promise<void>;
}

const ROLE_OPTIONS: { value: OrgRole; label: string }[] = [
  { value: 'ORG_ADMIN',       label: 'Organization Admin' },
  { value: 'PROJECT_MANAGER', label: 'Project Manager' },
  { value: 'DEVELOPER',       label: 'Developer' },
  { value: 'TESTER',          label: 'Tester' },
  { value: 'CLIENT',          label: 'Client' },
];

export default function InviteModal({ isOpen, onClose, onInvite }: InviteModalProps) {
  const [email, setEmail]   = useState('');
  const [role, setRole]     = useState<OrgRole>('DEVELOPER');
  const [loading, setLoading] = useState(false);
  const [error, setError]   = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) { setError('Email is required'); return; }
    setError('');
    setLoading(true);
    try {
      await onInvite(email.trim(), role);
      setEmail('');
      setRole('DEVELOPER');
      onClose();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to send invitation.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-card border border-border rounded-2xl p-6 w-full max-w-md shadow-2xl">
        <button onClick={onClose} className="absolute top-4 right-4 text-muted-foreground hover:text-white transition-colors">
          <X className="w-5 h-5" />
        </button>
        <div className="flex items-center gap-3 mb-5">
          <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
            <UserCheck className="w-5 h-5 text-primary" />
          </div>
          <h2 className="text-lg font-semibold text-white">Invite Member</h2>
        </div>

        {error && (
          <div className="bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-2 mb-4 text-sm text-red-400">{error}</div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <label className="text-sm font-medium text-white">Email Address</label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="colleague@company.com"
                className="w-full bg-background border border-border rounded-lg pl-10 pr-4 py-2.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-sm font-medium text-white">Role</label>
            <select
              value={role}
              onChange={e => setRole(e.target.value as OrgRole)}
              className="w-full bg-background border border-border rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-primary transition-all"
            >
              {ROLE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>

          <div className="flex gap-3 justify-end pt-2">
            <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-muted-foreground border border-border rounded-lg hover:text-white hover:bg-white/5 transition-colors">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="px-4 py-2 text-sm font-medium bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed">
              {loading && <Loader2 className="w-4 h-4 animate-spin" />}
              Send Invitation
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
