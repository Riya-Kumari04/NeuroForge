import React, { useState } from 'react';
import { useRoute, useLocation } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { Building2, LayoutDashboard, Users2, Users, Mail, Settings } from 'lucide-react';
import { organizationService } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import PageHeader from '@/components/organizations/PageHeader';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';
import OrganizationOverviewTab from './tabs/OrganizationOverviewTab';
import OrganizationTeamsTab from './tabs/OrganizationTeamsTab';
import OrganizationMembersTab from './tabs/OrganizationMembersTab';
import OrganizationInvitationsTab from './tabs/OrganizationInvitationsTab';
import OrganizationSettingsTab from './tabs/OrganizationSettingsTab';

type Tab = 'overview' | 'teams' | 'members' | 'invitations' | 'settings';

const TABS: { id: Tab; label: string; icon: React.ElementType }[] = [
  { id: 'overview',     label: 'Overview',     icon: LayoutDashboard },
  { id: 'teams',        label: 'Teams',        icon: Users2 },
  { id: 'members',      label: 'Members',      icon: Users },
  { id: 'invitations',  label: 'Invitations',  icon: Mail },
  { id: 'settings',     label: 'Settings',     icon: Settings },
];

export default function OrganizationDetailPage() {
  const [, params] = useRoute('/:role/organizations/:id');
  const [, setLocation] = useLocation();
  const { role } = useAuth();
  const [activeTab, setActiveTab] = useState<Tab>('overview');

  const orgId = parseInt(params?.id || '0', 10);
  const basePath = role ? `/${role}/organizations` : '/org-admin/organizations';

  const { data, isLoading } = useQuery({
    queryKey: ['org-detail', orgId],
    queryFn: () => organizationService.getById(orgId).then(r => r.data),
    enabled: !!orgId,
  });

  const org = data?.data;

  if (isLoading) return <div className="p-6"><LoadingSkeleton rows={3} /></div>;
  if (!org) return (
    <div className="p-6 text-center">
      <p className="text-muted-foreground">Organization not found.</p>
    </div>
  );

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <PageHeader
        title={org.name}
        description={`/${org.slug} · ${org.plan}`}
        breadcrumbs={[{ label: 'Organizations', href: basePath }, { label: org.name }]}
        action={
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-primary/10 flex items-center justify-center">
              <Building2 className="w-4 h-4 text-primary" />
            </div>
          </div>
        }
      />

      {/* Tab Bar */}
      <div className="flex gap-1 p-1 bg-background/50 border border-border rounded-xl w-fit mb-6 overflow-x-auto">
        {TABS.map(tab => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${
                activeTab === tab.id
                  ? 'bg-card text-white shadow-sm'
                  : 'text-muted-foreground hover:text-white'
              }`}
            >
              <Icon className="w-4 h-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* Tab Content */}
      <div>
        {activeTab === 'overview'    && <OrganizationOverviewTab    orgId={orgId} />}
        {activeTab === 'teams'       && <OrganizationTeamsTab       orgId={orgId} />}
        {activeTab === 'members'     && <OrganizationMembersTab     orgId={orgId} />}
        {activeTab === 'invitations' && <OrganizationInvitationsTab orgId={orgId} />}
        {activeTab === 'settings'    && <OrganizationSettingsTab    orgId={orgId} onDeleted={() => setLocation(basePath)} />}
      </div>
    </div>
  );
}
