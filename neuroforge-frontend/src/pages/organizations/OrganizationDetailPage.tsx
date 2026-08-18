import React, { useState } from 'react';
import { useRoute, useLocation, Link } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { Building2, LayoutDashboard, Users2, Users, Mail, Settings, ChevronLeft } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { organizationService } from '@/services/organizationService';
import { useAuth } from '@/context/AuthContext';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';
import OrganizationOverviewTab from './tabs/OrganizationOverviewTab';
import OrganizationTeamsTab from './tabs/OrganizationTeamsTab';
import OrganizationMembersTab from './tabs/OrganizationMembersTab';
import OrganizationInvitationsTab from './tabs/OrganizationInvitationsTab';
import OrganizationSettingsTab from './tabs/OrganizationSettingsTab';

type Tab = 'overview' | 'teams' | 'members' | 'invitations' | 'settings';

const TABS: { id: Tab; label: string; icon: React.ElementType }[] = [
  { id: 'overview',    label: 'Overview',    icon: LayoutDashboard },
  { id: 'teams',       label: 'Teams',       icon: Users2 },
  { id: 'members',     label: 'Members',     icon: Users },
  { id: 'invitations', label: 'Invitations', icon: Mail },
  { id: 'settings',    label: 'Settings',    icon: Settings },
];

export default function OrganizationDetailPage() {
  const [, params] = useRoute('/:role/organizations/:id');
  const { role } = useAuth();
  const [, setLocation] = useLocation();
  const [activeTab, setActiveTab] = useState<Tab>('overview');

  const orgId = parseInt(params?.id || '0', 10);
  const backPath = role ? `/${role}/organizations` : '/org-admin/organizations';

  const { data, isLoading } = useQuery({
    queryKey: ['org-detail', orgId],
    queryFn: () => organizationService.getById(orgId).then(r => r.data),
    enabled: !!orgId,
  });

  const org = data?.data;

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title={org ? org.name : 'Organization'} />
        <main className="flex-1 p-8 overflow-y-auto">
          {/* Back button */}
          <div className="mb-5">
            <Link
              href={backPath}
              className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-white transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
              Back to Organizations
            </Link>
          </div>

          {isLoading ? (
            <LoadingSkeleton rows={3} />
          ) : !org ? (
            <div className="p-6 text-center">
              <p className="text-muted-foreground">Organization not found.</p>
            </div>
          ) : (
            <>
              {/* Header */}
              <div className="flex items-start gap-4 mb-6">
                <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0">
                  <Building2 className="w-6 h-6 text-primary" />
                </div>
                <div>
                  <h2 className="text-xl font-bold text-white">{org.name}</h2>
                  <p className="text-sm text-muted-foreground">/{org.slug} · {org.plan} Plan</p>
                  {org.description && <p className="text-sm text-muted-foreground mt-1">{org.description}</p>}
                </div>
              </div>

              {/* Tabs */}
              <div className="flex gap-1 p-1 bg-background/50 rounded-lg border border-border w-fit mb-6 overflow-x-auto">
                {TABS.map(tab => (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`flex items-center gap-2 px-4 py-1.5 text-sm font-medium rounded-md transition-colors whitespace-nowrap ${
                      activeTab === tab.id ? 'bg-card text-white' : 'text-muted-foreground hover:text-white'
                    }`}
                  >
                    <tab.icon className="w-3.5 h-3.5" />
                    {tab.label}
                  </button>
                ))}
              </div>

              {/* Tab content */}
              {activeTab === 'overview'    && <OrganizationOverviewTab    orgId={orgId} />}
              {activeTab === 'teams'       && <OrganizationTeamsTab       orgId={orgId} />}
              {activeTab === 'members'     && <OrganizationMembersTab     orgId={orgId} />}
              {activeTab === 'invitations' && <OrganizationInvitationsTab orgId={orgId} />}
              {activeTab === 'settings'    && (
                <OrganizationSettingsTab
                  orgId={orgId}
                  onDeleted={() => setLocation(backPath)}
                />
              )}
            </>
          )}
        </main>
      </div>
    </div>
  );
}
