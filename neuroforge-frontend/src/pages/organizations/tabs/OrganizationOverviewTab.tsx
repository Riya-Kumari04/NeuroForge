import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Users, Users2, Mail, FolderKanban } from 'lucide-react';
import { organizationService } from '@/services/organizationService';
import OrgStatsCard from '@/components/organizations/OrgStatsCard';
import LoadingSkeleton from '@/components/organizations/LoadingSkeleton';

interface Props { orgId: number; }

export default function OrganizationOverviewTab({ orgId }: Props) {
  const { data, isLoading } = useQuery({
    queryKey: ['org-stats', orgId],
    queryFn: () => organizationService.getStats(orgId).then(r => r.data),
  });

  const stats = data?.data;

  if (isLoading) return <LoadingSkeleton rows={4} />;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <OrgStatsCard icon={Users2}      label="Teams"            value={stats?.teamsCount ?? 0}          color="blue"   />
        <OrgStatsCard icon={Users}       label="Members"          value={stats?.membersCount ?? 0}         color="green"  />
        <OrgStatsCard icon={Mail}        label="Pending Invites"  value={stats?.pendingInvitesCount ?? 0}  color="orange" />
        <OrgStatsCard icon={FolderKanban} label="Projects"        value={stats?.projectsCount ?? 0}        color="purple" />
      </div>
    </div>
  );
}
