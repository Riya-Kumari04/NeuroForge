import React, { useState } from 'react';
import { useParams, useLocation } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Edit2, Loader2, LayoutDashboard, GitBranch, UserPlus, Users, CheckSquare, Shield, Settings } from 'lucide-react';
import { Link } from 'wouter';
import { projectService, Project } from '@/services/projectService';
import { useAuth } from '@/context/AuthContext';
import { canManageProjects, getProjectBasePath } from '@/lib/roleUtils';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import HealthBadge from '@/components/projects/HealthBadge';
import ProjectOverviewTab from './tabs/ProjectOverviewTab';
import ProjectTimelineTab from './tabs/ProjectTimelineTab';
import ProjectMembersTab from './tabs/ProjectMembersTab';
import ProjectTeamTab from './tabs/ProjectTeamTab';
import ProjectTasksTab from './tabs/ProjectTasksTab';
import ProjectHealthTab from './tabs/ProjectHealthTab';
import ProjectSettingsTab from './tabs/ProjectSettingsTab';

type TabId = 'overview' | 'timeline' | 'members' | 'tasks' | 'team' | 'health' | 'settings';

const ALL_TABS: Array<{ id: TabId; label: string; icon: React.ElementType; managerOnly?: boolean; clientHidden?: boolean }> = [
  { id: 'overview',  label: 'Overview',  icon: LayoutDashboard },
  { id: 'tasks',     label: 'Tasks',     icon: CheckSquare,   clientHidden: true },
  { id: 'timeline',  label: 'Timeline',  icon: GitBranch,     clientHidden: true },
  { id: 'team',      label: 'Org Team',  icon: Users,         clientHidden: true },
  { id: 'health',    label: 'Health',    icon: Shield },
  { id: 'members',   label: 'Members',   icon: UserPlus,      managerOnly: true },
  { id: 'settings',  label: 'Settings',  icon: Settings,      managerOnly: true },
];

export default function ProjectDetailPage() {
  const params = useParams<{ id: string }>();
  const id = Number(params.id);
  const { role } = useAuth();
  const [activeTab, setActiveTab] = useState<TabId>('overview');

  const basePath    = getProjectBasePath(role);
  const canManage   = canManageProjects(role);   // PM, Org Admin, Super Admin
  const isClient    = role === 'client';

  // Filter tabs based on role
  const tabs = ALL_TABS.filter(tab => {
    if (tab.managerOnly && !canManage) return false;
    if (tab.clientHidden && isClient)  return false;
    return true;
  });

  const { data, isLoading, isError } = useQuery({
    queryKey: ['project', id],
    queryFn: () => projectService.getById(id).then(r => r.data),
    enabled: !!id,
  });
  const project: Project | undefined = data?.data;

  // If current tab was hidden by role, reset to overview
  const safeTab = tabs.some(t => t.id === activeTab) ? activeTab : 'overview';

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title={project?.projectName || 'Project Details'} />
        <main className="flex-1 p-8 overflow-y-auto">

          {/* Breadcrumb */}
          <div className="flex items-center gap-4 mb-6">
            <Link
              href={basePath}
              className="flex items-center gap-2 text-sm text-muted-foreground hover:text-white transition-colors"
            >
              <ArrowLeft className="w-4 h-4" /> Projects
            </Link>
            <span className="text-muted-foreground">/</span>
            <span className="text-sm text-white">{project?.projectName || '...'}</span>
          </div>

          {isLoading && (
            <div className="flex justify-center py-24">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
            </div>
          )}

          {isError && (
            <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-5 text-red-400 text-sm">
              Failed to load project. Please try again.
            </div>
          )}

          {project && (
            <>
              {/* Project Header Card */}
              <div className="bg-card border border-border rounded-xl p-6 mb-6">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-3 mb-1">
                      <h2 className="text-xl font-bold text-white">{project.projectName}</h2>
                      <HealthBadge status={project.status} />
                    </div>
                    {project.organizationName && (
                      <p className="text-sm text-muted-foreground">{project.organizationName}</p>
                    )}
                    {project.description && (
                      <p className="text-sm text-muted-foreground mt-2 max-w-2xl line-clamp-2">
                        {project.description}
                      </p>
                    )}
                  </div>
                  {/* Edit button — project managers and above only */}
                  {canManage && (
                    <Link
                      href={`${basePath}/${id}/edit`}
                      className="flex items-center gap-2 bg-background border border-border text-white text-sm font-medium px-3 py-2 rounded-lg hover:border-primary/50 transition-colors flex-shrink-0"
                    >
                      <Edit2 className="w-4 h-4" />
                      Edit
                    </Link>
                  )}
                </div>
              </div>

              {/* Tabs */}
              <div className="border-b border-border mb-6">
                <div className="flex items-center gap-1 overflow-x-auto scrollbar-hide pb-px">
                  {tabs.map(tab => (
                    <button
                      key={tab.id}
                      onClick={() => setActiveTab(tab.id)}
                      className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                        safeTab === tab.id
                          ? 'border-primary text-primary'
                          : 'border-transparent text-muted-foreground hover:text-white'
                      }`}
                    >
                      <tab.icon className="w-4 h-4" />
                      {tab.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Tab Content */}
              <div>
                {safeTab === 'overview'  && <ProjectOverviewTab  project={project} />}
                {safeTab === 'tasks'     && <ProjectTasksTab     project={project} />}
                {safeTab === 'timeline'  && <ProjectTimelineTab  project={project} />}
                {safeTab === 'members'   && <ProjectMembersTab   project={project} />}
                {safeTab === 'team'      && <ProjectTeamTab      project={project} />}
                {safeTab === 'health'    && <ProjectHealthTab    project={project} />}
                {safeTab === 'settings'  && <ProjectSettingsTab  project={project} />}
              </div>
            </>
          )}
        </main>
      </div>
    </div>
  );
}
