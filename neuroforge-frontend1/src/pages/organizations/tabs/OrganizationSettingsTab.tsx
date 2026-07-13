import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2, Trash2, Settings } from 'lucide-react';
import { organizationService, Organization } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import ConfirmDeleteModal from '@/components/organizations/ConfirmDeleteModal';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';

interface Props { orgId: number; onDeleted?: () => void; }

const INDUSTRIES = ['Technology', 'Finance', 'Healthcare', 'Education', 'Retail', 'Manufacturing', 'Media', 'Other'];
const SIZES      = ['1-10', '11-50', '51-200', '201-500', '500+'];

export default function OrganizationSettingsTab({ orgId, onDeleted }: Props) {
  const { role } = useAuth();
  const { toast } = useToast();
  const qc = useQueryClient();
  const canDelete = role === 'super-admin';
  const canEdit   = role === 'super-admin' || role === 'org-admin';

  const [showDelete, setShowDelete] = useState(false);
  const [form, setForm] = useState<{ name: string; industry: string; size: string; description: string } | null>(null);

  const { data, isLoading } = useQuery<any>({
    queryKey: ['org-detail', orgId],
    queryFn: () => organizationService.getById(orgId).then(r => r.data),
  });

  const org: Organization | null = data?.data || null;

  React.useEffect(() => {
    if (org && !form) {
      setForm({ name: org.name, industry: org.industry || '', size: org.size || '', description: org.description || '' });
    }
  }, [org]);

  const updateMut = useMutation({
    mutationFn: () => organizationService.update(orgId, { name: form!.name, industry: form!.industry || undefined, size: form!.size || undefined, description: form!.description || undefined }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['org-detail', orgId] }); toast({ title: 'Settings saved' }); },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  const deleteMut = useMutation({
    mutationFn: () => organizationService.delete(orgId),
    onSuccess: () => { toast({ title: 'Organization deleted' }); onDeleted?.(); },
    onError: (e: any) => toast({ title: 'Error', description: e?.response?.data?.message || 'Failed', variant: 'destructive' }),
  });

  if (isLoading || !form) return <LoadingSkeleton rows={3} />;

  return (
    <div className="space-y-6 max-w-2xl">
      {canEdit && (
        <div className="bg-card border border-border rounded-xl p-5">
          <div className="flex items-center gap-2 mb-5">
            <Settings className="w-4 h-4 text-muted-foreground" />
            <h3 className="text-sm font-semibold text-white">General Settings</h3>
          </div>
          <div className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-white">Organization Name</label>
              <input value={form.name} onChange={e => setForm(f => ({ ...f!, name: e.target.value }))} className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm text-white focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-white">Industry</label>
                <select value={form.industry} onChange={e => setForm(f => ({ ...f!, industry: e.target.value }))} className="w-full bg-background border border-border rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-primary transition-all">
                  <option value="">Select industry</option>
                  {INDUSTRIES.map(i => <option key={i} value={i}>{i}</option>)}
                </select>
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-white">Company Size</label>
                <select value={form.size} onChange={e => setForm(f => ({ ...f!, size: e.target.value }))} className="w-full bg-background border border-border rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-primary transition-all">
                  <option value="">Select size</option>
                  {SIZES.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
              </div>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-white">Description</label>
              <textarea value={form.description} onChange={e => setForm(f => ({ ...f!, description: e.target.value }))} rows={3} className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm text-white focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all resize-none" />
            </div>
            <div className="flex justify-end">
              <button onClick={() => updateMut.mutate()} disabled={updateMut.isPending} className="flex items-center gap-2 px-4 py-2 text-sm font-medium bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-70 disabled:cursor-not-allowed">
                {updateMut.isPending && <Loader2 className="w-4 h-4 animate-spin" />}
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}

      {canDelete && (
        <div className="bg-card border border-red-500/30 rounded-xl p-5">
          <h3 className="text-sm font-semibold text-red-400 mb-2">Danger Zone</h3>
          <p className="text-xs text-muted-foreground mb-4">Deleting this organization is permanent and cannot be undone.</p>
          <button onClick={() => setShowDelete(true)} className="flex items-center gap-2 px-4 py-2 text-sm font-medium bg-red-600/10 text-red-400 border border-red-500/30 rounded-lg hover:bg-red-600 hover:text-white transition-colors">
            <Trash2 className="w-4 h-4" /> Delete Organization
          </button>
        </div>
      )}

      <ConfirmDeleteModal
        isOpen={showDelete}
        onClose={() => setShowDelete(false)}
        onConfirm={() => deleteMut.mutate()}
        title="Delete Organization"
        message="Are you sure? All teams, members, and data will be permanently removed."
        isLoading={deleteMut.isPending}
      />
    </div>
  );
}
