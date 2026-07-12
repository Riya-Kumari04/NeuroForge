import React, { useState, useMemo } from 'react';
import { useLocation } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { Building2, Plus } from 'lucide-react';
import { organizationService, Organization } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import OrganizationCard from '@/components/organizations/OrganizationCard';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';
import EmptyState from '@/components/organizations/EmptyState';
import SearchFilterBar from '@/components/organizations/SearchFilterBar';
import PageHeader from '@/components/organizations/PageHeader';
import PaginationControls from '@/components/organizations/PaginationControls';

const PAGE_SIZE = 9;

export default function OrganizationListPage() {
  const [, setLocation] = useLocation();
  const { role } = useAuth();
  const [search, setSearch] = useState('');
  const [industry, setIndustry] = useState('');
  const [plan, setPlan] = useState('');
  const [page, setPage] = useState(1);

  const basePath = role ? `/${role}/organizations` : '/org-admin/organizations';
  const canCreate = role === 'super-admin' || role === 'org-admin';

  const { data, isLoading, isError } = useQuery({
    queryKey: ['organizations'],
    queryFn: () => organizationService.getAll().then(r => r.data),
  });

  const orgs: Organization[] = data?.data || [];

  const filtered = useMemo(() => {
    return orgs.filter(o => {
      const matchSearch = !search || o.name.toLowerCase().includes(search.toLowerCase()) || o.slug.toLowerCase().includes(search.toLowerCase());
      const matchIndustry = !industry || o.industry === industry;
      const matchPlan = !plan || o.plan === plan;
      return matchSearch && matchIndustry && matchPlan;
    });
  }, [orgs, search, industry, plan]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const industries = [...new Set(orgs.map(o => o.industry).filter(Boolean))] as string[];
  const plans = [...new Set(orgs.map(o => o.plan))];

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <PageHeader
        title="Organizations"
        description="Manage your organizations and workspaces"
        breadcrumbs={[{ label: 'Dashboard' }, { label: 'Organizations' }]}
        action={canCreate ? (
          <button
            onClick={() => setLocation(`${basePath}/new`)}
            className="flex items-center gap-2 bg-primary text-primary-foreground text-sm font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors shadow-[0_0_15px_rgba(37,99,235,0.3)]"
          >
            <Plus className="w-4 h-4" />
            New Organization
          </button>
        ) : undefined}
      />

      <div className="mb-5">
        <SearchFilterBar
          search={search}
          onSearchChange={v => { setSearch(v); setPage(1); }}
          placeholder="Search organizations..."
          filters={[
            { label: 'All Industries', value: industry, options: industries.map(i => ({ value: i, label: i })), onChange: v => { setIndustry(v); setPage(1); } },
            { label: 'All Plans', value: plan, options: plans.map(p => ({ value: p, label: p })), onChange: v => { setPlan(v); setPage(1); } },
          ]}
        />
      </div>

      {isLoading && <LoadingSkeleton rows={6} />}

      {isError && (
        <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-5 text-red-400 text-sm">
          Failed to load organizations. Please try again.
        </div>
      )}

      {!isLoading && !isError && paginated.length === 0 && (
        <EmptyState
          icon={Building2}
          title="No Organizations Found"
          message={search || industry || plan ? 'Try adjusting your filters.' : 'Get started by creating your first organization.'}
          action={canCreate ? { label: 'New Organization', onClick: () => setLocation(`${basePath}/new`) } : undefined}
        />
      )}

      {!isLoading && !isError && paginated.length > 0 && (
        <>
          <p className="text-xs text-muted-foreground mb-3">{filtered.length} organization{filtered.length !== 1 ? 's' : ''} found</p>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {paginated.map(org => (
              <OrganizationCard key={org.id} org={org} basePath={basePath} />
            ))}
          </div>
          <PaginationControls page={page} totalPages={totalPages} onPrev={() => setPage(p => p - 1)} onNext={() => setPage(p => p + 1)} />
        </>
      )}
    </div>
  );
}
