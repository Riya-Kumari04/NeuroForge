import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, GitBranch, Calendar, Trash2, Edit2, Loader2, X, BarChart3, Play, CheckCircle, LayoutGrid } from 'lucide-react';
import { projectService, Sprint, Project, Task } from '@/services/projectService';
import { sprintService } from '@/services/sprintService';
import { useAuth } from '@/context/AuthContext';
import { canManageSprints, getProjectBasePath } from '@/lib/roleUtils';
import HealthBadge from '@/components/projects/HealthBadge';
import ConfirmDialog from '@/components/projects/ConfirmDialog';
import { useToast } from '@/hooks/use-toast';
import { Link } from 'wouter';

interface Props { project: Project }

type SprintFormData = {
  sprintName: string;
  goal: string;
  status: string;
  startDate: string;
  endDate: string;
};

const SPRINT_STATUSES = ['PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED'];

function SprintModal({ sprint, projectId, onClose }: { sprint?: Sprint; projectId: number; onClose: () => void }) {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const isEdit = !!sprint;

  const [form, setForm] = useState<SprintFormData>({
    sprintName: sprint?.sprintName || '',
    goal:       sprint?.goal       || '',
    status:     sprint?.status     || 'PLANNED',
    startDate:  sprint?.startDate  ? new Date(sprint.startDate).toISOString().split('T')[0] : '',
    endDate:    sprint?.endDate    ? new Date(sprint.endDate).toISOString().split('T')[0]   : '',
  });

  const mutation = useMutation({
    mutationFn: () => isEdit
      ? projectService.updateSprint(sprint!.id, { sprintName: form.sprintName, goal: form.goal, status: form.status, startDate: form.startDate || undefined, endDate: form.endDate || undefined })
      : projectService.createSprint({ sprintName: form.sprintName, goal: form.goal, status: form.status, startDate: form.startDate || undefined, endDate: form.endDate || undefined, projectId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sprints', projectId] });
      toast({ title: isEdit ? 'Sprint updated' : 'Sprint created' });
      onClose();
    },
    onError: () => toast({ title: 'Error', description: 'Operation failed', variant: 'destructive' }),
  });

  const inputClass = 'w-full bg-background border border-border rounded-lg px-3 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-[#111827] border border-border rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl">
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-base font-semibold text-white">{isEdit ? 'Edit Sprint' : 'New Sprint'}</h3>
          <button onClick={onClose}><X className="w-4 h-4 text-muted-foreground" /></button>
        </div>
        <div className="space-y-4">
          <div>
            <label className="text-xs font-medium text-white block mb-1.5">Sprint Name *</label>
            <input className={inputClass} value={form.sprintName} onChange={e => setForm(f => ({ ...f, sprintName: e.target.value }))} placeholder="e.g. Sprint 1" />
          </div>
          <div>
            <label className="text-xs font-medium text-white block mb-1.5">Goal</label>
            <textarea rows={2} className={`${inputClass} resize-none`} value={form.goal} onChange={e => setForm(f => ({ ...f, goal: e.target.value }))} placeholder="Sprint goal..." />
          </div>
          <div>
            <label className="text-xs font-medium text-white block mb-1.5">Status</label>
            <select className={inputClass} value={form.status} onChange={e => setForm(f => ({ ...f, status: e.target.value }))}>
              {SPRINT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs font-medium text-white block mb-1.5">Start Date</label>
              <input type="date" className={inputClass} value={form.startDate} onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs font-medium text-white block mb-1.5">End Date</label>
              <input type="date" className={inputClass} value={form.endDate} onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))} />
            </div>
          </div>
          <button
            onClick={() => mutation.mutate()}
            disabled={!form.sprintName || mutation.isPending}
            className="w-full bg-primary text-white text-sm font-medium py-2.5 rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-60 flex items-center justify-center gap-2"
          >
            {mutation.isPending && <Loader2 className="w-4 h-4 animate-spin" />}
            {isEdit ? 'Save Changes' : 'Create Sprint'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function ProjectTimelineTab({ project }: Props) {
  const { role } = useAuth();
  const basePath = getProjectBasePath(role);
  const canManage = canManageSprints(role);   // PM, Org Admin, Super Admin only (not client)

  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [sprintModal, setSprintModal] = useState<Sprint | null | 'new'>(null);
  const [deleteTarget, setDeleteTarget] = useState<Sprint | null>(null);
  const [startTarget, setStartTarget] = useState<Sprint | null>(null);
  const [completeTarget, setCompleteTarget] = useState<Sprint | null>(null);
  const [viewMode, setViewMode] = useState<'timeline' | 'gantt'>('timeline');

  const { data, isLoading } = useQuery({
    queryKey: ['sprints', project.id],
    queryFn: () => projectService.getSprintsByProject(project.id).then(r => r.data),
  });
  const sprints: Sprint[] = data?.data || [];

  const { data: tasksData } = useQuery({
    queryKey: ['project-tasks', project.id],
    queryFn: () => projectService.getTasksByProject(project.id).then(r => r.data),
  });
  const tasks: Task[] = tasksData?.data || [];

  const deleteMutation = useMutation({
    mutationFn: (id: number) => projectService.deleteSprint(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sprints', project.id] });
      toast({ title: 'Sprint deleted' });
      setDeleteTarget(null);
    },
  });

  const startSprintMutation = useMutation({
    mutationFn: (sprintId: number) => sprintService.startSprint(sprintId.toString()),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sprints', project.id] });
      toast({ title: 'Sprint started' });
      setStartTarget(null);
    },
    onError: () => toast({ title: 'Error', description: 'Failed to start sprint.', variant: 'destructive' }),
  });

  const completeSprintMutation = useMutation({
    mutationFn: (sprintId: number) => sprintService.completeSprint(sprintId.toString()),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sprints', project.id] });
      toast({ title: 'Sprint completed' });
      setCompleteTarget(null);
    },
    onError: () => toast({ title: 'Error', description: 'Failed to complete sprint.', variant: 'destructive' }),
  });

  const formatDate = (dt?: string) =>
    dt ? new Date(dt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '—';

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h3 className="text-base font-semibold text-white">Sprints ({sprints.length})</h3>
        <div className="flex items-center gap-3">
          {/* View Toggle */}
          <div className="flex items-center bg-background border border-border rounded-lg p-1">
            <button
              onClick={() => setViewMode('timeline')}
              className={`px-3 py-1 text-xs font-medium rounded transition-colors ${
                viewMode === 'timeline' ? 'bg-primary text-white' : 'text-muted-foreground hover:text-white'
              }`}
            >
              Timeline
            </button>
            <button
              onClick={() => setViewMode('gantt')}
              className={`px-3 py-1 text-xs font-medium rounded transition-colors ${
                viewMode === 'gantt' ? 'bg-primary text-white' : 'text-muted-foreground hover:text-white'
              }`}
            >
              <LayoutGrid className="w-3.5 h-3.5 inline mr-1" />
              Gantt
            </button>
          </div>
          {/* New Sprint — project managers and above only */}
          {canManage && (
            <button
              onClick={() => setSprintModal('new')}
              className="flex items-center gap-1.5 bg-primary text-white text-xs font-medium px-3 py-1.5 rounded-lg hover:bg-primary/90 transition-colors"
            >
              <Plus className="w-3.5 h-3.5" /> New Sprint
            </button>
          )}
        </div>
      </div>

      {isLoading && <div className="flex justify-center py-10"><Loader2 className="w-5 h-5 animate-spin text-primary" /></div>}

      {!isLoading && sprints.length === 0 && (
        <div className="bg-card border border-dashed border-border rounded-xl p-10 text-center">
          <GitBranch className="w-8 h-8 text-muted-foreground mx-auto mb-3" />
          <p className="text-sm font-medium text-white mb-1">No sprints yet</p>
          <p className="text-xs text-muted-foreground">
            {canManage ? 'Create your first sprint to start planning.' : (role === 'client' ? 'No sprints have been created for this project.' : 'No sprints have been created for this project.')}
          </p>
        </div>
      )}

      {/* Timeline View */}
      {viewMode === 'timeline' && sprints.length > 0 && (
        <div className="relative">
          <div className="absolute left-5 top-5 bottom-5 w-px bg-border/50" />
          <div className="space-y-4">
            {sprints.map((sprint) => (
              <div key={sprint.id} className="relative flex gap-4 pl-12">
                <div className="absolute left-3.5 w-3 h-3 rounded-full bg-primary border-2 border-background mt-4" />
                <div className="flex-1 bg-card border border-border rounded-xl p-4 hover:border-primary/30 transition-all">
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <h4 className="text-sm font-semibold text-white">{sprint.sprintName}</h4>
                      {sprint.goal && <p className="text-xs text-muted-foreground mt-0.5">{sprint.goal}</p>}
                    </div>
                    <div className="flex items-center gap-2">
                      <HealthBadge status={sprint.status} size="sm" />
                      {/* View Dashboard */}
                      <Link
                        href={`${basePath}/${project.id}/sprints/${sprint.id}/dashboard`}
                        className="p-1.5 rounded hover:bg-white/10 text-muted-foreground hover:text-white transition-colors"
                        title="View sprint dashboard"
                      >
                        <BarChart3 className="w-3.5 h-3.5" />
                      </Link>
                      {/* Start Sprint — only for PLANNED sprints */}
                      {canManage && sprint.status === 'PLANNED' && (
                        <button
                          onClick={() => setStartTarget(sprint)}
                          className="p-1.5 rounded hover:bg-green-500/10 text-muted-foreground hover:text-green-400 transition-colors"
                          title="Start sprint"
                        >
                          <Play className="w-3.5 h-3.5" />
                        </button>
                      )}
                      {/* Complete Sprint — only for ACTIVE sprints */}
                      {canManage && sprint.status === 'ACTIVE' && (
                        <button
                          onClick={() => setCompleteTarget(sprint)}
                          className="p-1.5 rounded hover:bg-blue-500/10 text-muted-foreground hover:text-blue-400 transition-colors"
                          title="Complete sprint"
                        >
                          <CheckCircle className="w-3.5 h-3.5" />
                        </button>
                      )}
                      {/* Edit / Delete — project managers and above only */}
                      {canManage && (
                        <>
                          <button
                            onClick={() => setSprintModal(sprint)}
                            className="p-1.5 rounded hover:bg-white/10 text-muted-foreground hover:text-white transition-colors"
                            title="Edit sprint"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => setDeleteTarget(sprint)}
                            className="p-1.5 rounded hover:bg-red-500/10 text-muted-foreground hover:text-red-400 transition-colors"
                            title="Delete sprint"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-4 text-xs text-muted-foreground">
                    <div className="flex items-center gap-1">
                      <Calendar className="w-3 h-3" />
                      {formatDate(sprint.startDate)} → {formatDate(sprint.endDate)}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Gantt View */}
      {viewMode === 'gantt' && sprints.length > 0 && (
        <div className="bg-card border border-border rounded-xl p-6 overflow-x-auto">
          <div className="min-w-[800px]">
            <div className="grid grid-cols-[200px_1fr] gap-4 mb-4">
              <div className="text-xs font-semibold text-muted-foreground">Sprint / Task</div>
              <div className="text-xs font-semibold text-muted-foreground">Timeline</div>
            </div>
            {sprints.map((sprint) => {
              const sprintTasks = tasks.filter(t => t.sprintId === sprint.id);
              const sprintStart = sprint.startDate ? new Date(sprint.startDate).getTime() : Date.now();
              const sprintEnd = sprint.endDate ? new Date(sprint.endDate).getTime() : sprintStart + 14 * 24 * 60 * 60 * 1000;
              const sprintDuration = sprintEnd - sprintStart;
              const projectStart = Math.min(...sprints.map(s => s.startDate ? new Date(s.startDate).getTime() : Date.now()));
              const projectEnd = Math.max(...sprints.map(s => s.endDate ? new Date(s.endDate).getTime() : Date.now() + 30 * 24 * 60 * 60 * 1000));
              const totalDuration = projectEnd - projectStart;

              const sprintLeft = ((sprintStart - projectStart) / totalDuration) * 100;
              const sprintWidth = (sprintDuration / totalDuration) * 100;

              return (
                <div key={sprint.id} className="mb-6">
                  <div className="grid grid-cols-[200px_1fr] gap-4 mb-2">
                    <div className="flex items-center gap-2">
                      <div className={`w-2 h-2 rounded-full ${
                        sprint.status === 'COMPLETED' ? 'bg-emerald-500' :
                        sprint.status === 'ACTIVE' ? 'bg-blue-500' :
                        sprint.status === 'PLANNED' ? 'bg-slate-500' : 'bg-red-500'
                      }`} />
                      <span className="text-sm font-medium text-white">{sprint.sprintName}</span>
                    </div>
                    <div className="relative h-8 bg-background/50 rounded">
                      <div
                        className={`absolute h-full rounded ${
                          sprint.status === 'COMPLETED' ? 'bg-emerald-500/30 border border-emerald-500' :
                          sprint.status === 'ACTIVE' ? 'bg-blue-500/30 border border-blue-500' :
                          sprint.status === 'PLANNED' ? 'bg-slate-500/30 border border-slate-500' : 'bg-red-500/30 border border-red-500'
                        }`}
                        style={{ left: `${Math.max(0, sprintLeft)}%`, width: `${Math.min(100, sprintWidth)}%` }}
                      >
                        <div className="px-2 py-1 text-xs text-white truncate">{formatDate(sprint.startDate)} - {formatDate(sprint.endDate)}</div>
                      </div>
                    </div>
                  </div>
                  {sprintTasks.map(task => (
                    <div key={task.id} className="grid grid-cols-[200px_1fr] gap-4 mb-1 ml-4">
                      <div className="text-xs text-muted-foreground truncate">{task.title}</div>
                      <div className="relative h-6">
                        <div
                          className="absolute h-full bg-primary/20 border border-primary/50 rounded"
                          style={{ left: `${Math.max(0, sprintLeft + 2)}%`, width: `${Math.min(100, sprintWidth - 4)}%` }}
                        >
                          <div className="px-2 py-0.5 text-xs text-primary truncate">{task.status}</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {canManage && sprintModal !== null && (
        <SprintModal
          sprint={sprintModal === 'new' ? undefined : sprintModal}
          projectId={project.id}
          onClose={() => setSprintModal(null)}
        />
      )}
      {canManage && (
        <ConfirmDialog
          open={!!deleteTarget}
          title="Delete Sprint"
          message={`Delete "${deleteTarget?.sprintName}"? Tasks in this sprint will remain attached to the project.`}
          confirmLabel="Delete"
          onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
          onCancel={() => setDeleteTarget(null)}
          isLoading={deleteMutation.isPending}
        />
      )}
      {canManage && (
        <ConfirmDialog
          open={!!startTarget}
          title="Start Sprint"
          message={`Start "${startTarget?.sprintName}"? This will mark the sprint as active.`}
          confirmLabel="Start"
          onConfirm={() => startTarget && startSprintMutation.mutate(startTarget.id)}
          onCancel={() => setStartTarget(null)}
          isLoading={startSprintMutation.isPending}
        />
      )}
      {canManage && (
        <ConfirmDialog
          open={!!completeTarget}
          title="Complete Sprint"
          message={`Complete "${completeTarget?.sprintName}"? This will mark the sprint as completed.`}
          confirmLabel="Complete"
          onConfirm={() => completeTarget && completeSprintMutation.mutate(completeTarget.id)}
          onCancel={() => setCompleteTarget(null)}
          isLoading={completeSprintMutation.isPending}
        />
      )}
    </div>
  );
}
