import React from 'react';
import { useLocation } from 'wouter';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Building2, Loader2, ArrowLeft } from 'lucide-react';
import { organizationService, CreateOrganizationRequest } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/hooks/use-toast';
import PageHeader from '@/components/organizations/PageHeader';

const schema = z.object({
  name:        z.string().min(2, 'Name must be at least 2 characters').max(100),
  slug:        z.string().min(2, 'Slug must be at least 2 characters').max(60).regex(/^[a-z0-9-]+$/, 'Slug can only contain lowercase letters, numbers, and hyphens'),
  industry:    z.string().optional(),
  size:        z.string().optional(),
  plan:        z.string().optional(),
  description: z.string().max(500).optional(),
});

type FormValues = z.infer<typeof schema>;

function toSlug(name: string) {
  return name.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '').slice(0, 60);
}

const INDUSTRIES = ['Technology', 'Finance', 'Healthcare', 'Education', 'Retail', 'Manufacturing', 'Media', 'Other'];
const SIZES      = ['1-10', '11-50', '51-200', '201-500', '500+'];
const PLANS      = ['FREE', 'STARTER', 'GROWTH', 'ENTERPRISE'];

export default function CreateOrganizationPage() {
  const [, setLocation] = useLocation();
  const { role } = useAuth();
  const { toast } = useToast();
  const basePath = role ? `/${role}/organizations` : '/org-admin/organizations';

  const { register, handleSubmit, setValue, watch, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', slug: '', industry: '', size: '', plan: 'FREE', description: '' },
  });

  const nameVal = watch('name');
  React.useEffect(() => {
    if (nameVal) setValue('slug', toSlug(nameVal), { shouldValidate: false });
  }, [nameVal, setValue]);

  const onSubmit = async (data: FormValues) => {
    try {
      const payload: CreateOrganizationRequest = {
        name:        data.name,
        slug:        data.slug,
        industry:    data.industry || undefined,
        size:        data.size || undefined,
        plan:        data.plan || 'FREE',
        description: data.description || undefined,
      };
      await organizationService.create(payload);
      toast({ title: 'Organization created', description: `"${data.name}" has been created successfully.` });
      setLocation(basePath);
    } catch (err: any) {
      toast({ title: 'Error', description: err?.response?.data?.message || 'Failed to create organization.', variant: 'destructive' });
    }
  };

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <PageHeader
        title="New Organization"
        description="Create a new organization workspace"
        breadcrumbs={[{ label: 'Organizations', href: basePath }, { label: 'New' }]}
      />

      <div className="bg-card border border-border rounded-2xl p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
            <Building2 className="w-5 h-5 text-primary" />
          </div>
          <div>
            <h2 className="text-sm font-semibold text-white">Organization Details</h2>
            <p className="text-xs text-muted-foreground">Fill in the details for your new organization</p>
          </div>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-white">Organization Name <span className="text-red-400">*</span></label>
              <input {...register('name')} placeholder="Acme Corp" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all" />
              {errors.name && <p className="text-xs text-red-400">{errors.name.message}</p>}
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-white">Slug <span className="text-red-400">*</span></label>
              <input {...register('slug')} placeholder="acme-corp" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all font-mono" />
              {errors.slug && <p className="text-xs text-red-400">{errors.slug.message}</p>}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-white">Industry</label>
              <select {...register('industry')} className="w-full bg-background border border-border rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-primary transition-all">
                <option value="">Select industry</option>
                {INDUSTRIES.map(i => <option key={i} value={i}>{i}</option>)}
              </select>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-white">Company Size</label>
              <select {...register('size')} className="w-full bg-background border border-border rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-primary transition-all">
                <option value="">Select size</option>
                {SIZES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-white">Plan</label>
              <select {...register('plan')} className="w-full bg-background border border-border rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-primary transition-all">
                {PLANS.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-sm font-medium text-white">Description</label>
            <textarea {...register('description')} rows={3} placeholder="A brief description of your organization..." className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all resize-none" />
            {errors.description && <p className="text-xs text-red-400">{errors.description.message}</p>}
          </div>

          <div className="flex gap-3 justify-end pt-2">
            <button type="button" onClick={() => setLocation(basePath)} className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-muted-foreground border border-border rounded-lg hover:text-white hover:bg-white/5 transition-colors">
              <ArrowLeft className="w-4 h-4" /> Cancel
            </button>
            <button type="submit" disabled={isSubmitting} className="flex items-center gap-2 px-6 py-2 text-sm font-medium bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors shadow-[0_0_15px_rgba(37,99,235,0.3)] disabled:opacity-70 disabled:cursor-not-allowed">
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              Create Organization
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
