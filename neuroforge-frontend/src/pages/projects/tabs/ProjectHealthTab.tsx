import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { ShieldCheck, AlertTriangle, XCircle, Loader2, TrendingUp } from 'lucide-react';
import { projectService, Project, ProjectStats } from '@/services/projectService';
import ProgressBar from '@/components/projects/ProgressBar';
import HealthBadge from '@/components/projects/HealthBadge';

interface Props { project: Project }

export default function ProjectHealthTab({ project }: Props) {
  const { data, isLoading } = useQuery({
    queryKey: ['project-stats', project.id],
    queryFn: () => projectService.getStats(project.id).then(r => r.data),
  });
  const stats: ProjectStats | undefined = data?.data;

  if (isLoading) return (
    <div className="flex justify-center py-20">
      <Loader2 className="w-6 h-6 animate-spin text-primary" />
    </div>
  );

  if (!stats) return <p className="text-sm text-red-400">Failed to load health data.</p>;

  const completionRate = stats.totalTasks > 0
    ? Math.round((stats.completedTasks / stats.totalTasks) * 100)
    : 0;

  const HealthIcon = stats.healthStatus === 'HEALTHY' ? ShieldCheck
    : stats.healthStatus === 'AT_RISK' ? AlertTriangle
    : XCircle;

  const healthColor = stats.healthStatus === 'HEALTHY' ? 'text-emerald-400'
    : stats.healthStatus === 'AT_RISK' ? 'text-amber-400'
    : 'text-red-400';

  const healthBg = stats.healthStatus === 'HEALTHY' ? 'bg-emerald-500/10'
    : stats.healthStatus === 'AT_RISK' ? 'bg-amber-500/10'
    : 'bg-red-500/10';

  const metrics = [
    { label: 'Task Completion', value: completionRate, sublabel: `${stats.completedTasks} of ${stats.totalTasks} tasks done` },
    { label: 'Overall Health', value: stats.healthScore, sublabel: stats.healthStatus },
  ];

  const breakdown = [
    { label: 'To Do', value: stats.todoTasks, color: 'bg-slate-500', pct: stats.totalTasks > 0 ? Math.round((stats.todoTasks / stats.totalTasks) * 100) : 0 },
    { label: 'In Progress', value: stats.inProgressTasks, color: 'bg-amber-500', pct: stats.totalTasks > 0 ? Math.round((stats.inProgressTasks / stats.totalTasks) * 100) : 0 },
    { label: 'Completed', value: stats.completedTasks, color: 'bg-emerald-500', pct: completionRate },
  ];

  return (
    <div className="space-y-6">
      {/* Health Score Hero */}
      <div className="bg-card border border-border rounded-xl p-6">
        <div className="flex items-center gap-5">
          <div className={`w-16 h-16 rounded-2xl ${healthBg} flex items-center justify-center flex-shrink-0`}>
            <HealthIcon className={`w-8 h-8 ${healthColor}`} />
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-3 mb-1">
              <span className="text-4xl font-bold text-white">{stats.healthScore}%</span>
              <HealthBadge status={stats.healthStatus} size="lg" />
            </div>
            <p className="text-sm text-muted-foreground">
              {stats.healthStatus === 'HEALTHY' && 'Project is on track and progressing well.'}
              {stats.healthStatus === 'AT_RISK' && 'Project needs attention — some tasks are falling behind.'}
              {stats.healthStatus === 'CRITICAL' && 'Project is in critical state. Immediate action required.'}
            </p>
          </div>
        </div>
      </div>

      {/* Metric bars */}
      <div className="bg-card border border-border rounded-xl p-5 space-y-5">
        <h3 className="text-sm font-semibold text-white flex items-center gap-2">
          <TrendingUp className="w-4 h-4 text-primary" /> Health Metrics
        </h3>
        {metrics.map(m => (
          <div key={m.label}>
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-xs font-medium text-white">{m.label}</span>
              <span className="text-xs text-muted-foreground">{m.sublabel}</span>
            </div>
            <ProgressBar value={m.value} showPercent={false} size="md" />
            <div className="flex justify-between mt-1">
              <span className="text-xs text-muted-foreground" />
              <span className="text-xs font-medium text-white">{m.value}%</span>
            </div>
          </div>
        ))}
      </div>

      {/* Task breakdown */}
      <div className="bg-card border border-border rounded-xl p-5">
        <h3 className="text-sm font-semibold text-white mb-4">Task Breakdown</h3>

        {/* Stacked bar */}
        {stats.totalTasks > 0 ? (
          <div className="flex h-4 rounded-full overflow-hidden mb-4 gap-0.5">
            {breakdown.filter(b => b.pct > 0).map(b => (
              <div
                key={b.label}
                className={`${b.color} transition-all`}
                style={{ width: `${b.pct}%` }}
                title={`${b.label}: ${b.pct}%`}
              />
            ))}
          </div>
        ) : (
          <div className="h-4 rounded-full bg-white/5 mb-4" />
        )}

        <div className="flex items-center gap-6 flex-wrap">
          {breakdown.map(b => (
            <div key={b.label} className="flex items-center gap-2">
              <span className={`w-3 h-3 rounded-sm ${b.color}`} />
              <span className="text-xs text-muted-foreground">{b.label}</span>
              <span className="text-xs font-medium text-white">{b.value}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Sprint + Team summary */}
      <div className="grid grid-cols-2 gap-4">
        {[
          { label: 'Total Sprints', value: stats.totalSprints },
          { label: 'Team Members', value: stats.totalMembers },
          { label: 'Pending Tasks', value: stats.todoTasks + stats.inProgressTasks },
          { label: 'In Progress', value: stats.inProgressTasks },
        ].map(card => (
          <div key={card.label} className="bg-card border border-border rounded-xl p-4 text-center">
            <p className="text-2xl font-bold text-white">{card.value}</p>
            <p className="text-xs text-muted-foreground mt-1">{card.label}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
