import React from 'react';
import { Link } from 'wouter';
import { Building2, Users, Users2, Briefcase } from 'lucide-react';
import { Organization } from '@/services/organizationService';

interface OrganizationCardProps {
  org: Organization;
  basePath: string;
}

const planColors: Record<string, string> = {
  FREE:       'bg-slate-500/20 text-slate-400 border-slate-500/30',
  STARTER:    'bg-blue-500/20 text-blue-400 border-blue-500/30',
  GROWTH:     'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
  ENTERPRISE: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
};

export default function OrganizationCard({ org, basePath }: OrganizationCardProps) {
  const planClass = planColors[org.plan] || planColors.FREE;
  return (
    <Link href={`${basePath}/${org.id}`}>
      <div className="bg-card border border-border rounded-xl p-5 hover:border-primary/40 hover:shadow-[0_0_20px_rgba(37,99,235,0.08)] transition-all cursor-pointer group">
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
              <Building2 className="w-5 h-5 text-primary" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-white group-hover:text-primary transition-colors">{org.name}</h3>
              <p className="text-xs text-muted-foreground">/{org.slug}</p>
            </div>
          </div>
          <span className={`text-xs font-medium px-2 py-0.5 rounded-full border ${planClass}`}>{org.plan}</span>
        </div>

        {org.description && (
          <p className="text-xs text-muted-foreground mb-3 line-clamp-2">{org.description}</p>
        )}

        <div className="flex items-center gap-4 text-xs text-muted-foreground pt-3 border-t border-border/50">
          {org.industry && (
            <span className="flex items-center gap-1">
              <Briefcase className="w-3 h-3" /> {org.industry}
            </span>
          )}
          <span className="flex items-center gap-1">
            <Users2 className="w-3 h-3" /> {org.teamsCount} teams
          </span>
          <span className="flex items-center gap-1">
            <Users className="w-3 h-3" /> {org.membersCount} members
          </span>
        </div>
      </div>
    </Link>
  );
}
