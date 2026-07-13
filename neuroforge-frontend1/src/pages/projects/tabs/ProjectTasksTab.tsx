import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, CheckSquare, Loader2, X, Trash2, Edit2 } from 'lucide-react';
import { projectService, Task, Sprint, Project, ProjectMember } from '@/services/projectService';
import ConfirmDialog from '@/components/projects/ConfirmDialog';
import { useToast } from '@/hooks/use-toast';

interface Props { project: Project }

type TaskFormData = { title: string; description: string; priority: string; status: string; assignedToId: string; sprintId: string };
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const TASK_STATUSES = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];

const priorityColor: Record<string, string> = {
  LOW:      'bg-slate-500/10  text-slate-400  border-slate-500/20',
  MEDIUM:   'bg-blue-500/10   text-blue-400   border-blue-500/20',
  HIGH:     'bg-amber-500/10  text-amber-400  border-amber-500/20',
  CRITICAL: 'bg-red-500/10    text-red-400    border-red-500/20',
};

function TaskModal({ task, projectId, sprintOptions, memberOptions, onClose }: { task?: Task; projectId: number; sprintOptions: Sprint[]; memberOptions: ProjectMember[]; onClose: () => void }) {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const isEdit = !!task;

  const [form, setForm] = useState<TaskFormData>({
    title: task?.title || '',
    description: task?.description || '',
    priority: task?.priority || 'MEDIUM',
    status: task?.status || 'TODO',
    assignedToId: task?.assignedToId?.toString() || '',
    sprintId: task?.sprintId?.toString() || '',
  });

  const mutation = useMutation({
    mutationFn: () => isEdit
      ? projectService.updateTask(task!.id, { title: form.title, description: form.description, priority: form.priority, status: form.status, assignedToId: form.assignedToId ? Number(form.assignedToId) : undefined, sprintId: form.sprintId ? Number(form.sprintId) : undefined })
      : projectService.createTask({ title: form.title, description: form.description, priority: form.priority, status: form.status, assignedToId: form.assignedToId ? Number(form.assignedToId) : undefined, projectId, sprintId: form.sprintId ? Number(form.sprintId) : undefined }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-stats', projectId] });
      toast({ title: isEdit ? 'Task updated' : 'Task created' });
      onClose();
    },
    onError: () => toast({ title: 'Error', description: 'Operation failed.', variant: 'destructive' }),
  });

  const inputClass = 'w-full bg-background border border-border rounded-lg px-3 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-[#111827] border border-border rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl">
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-base font-semibold text-white">{isEdit ? 'Edit Task' : 'New Task'}</h3>
          <button onClick={onClose}><X className="w-4 h-4 text-muted-foreground" /></button>
        </div>
        <div className="space-y-3">
          <div>
            <label className="text-xs font-medium text-white block mb-1.5">Title *</label>
            <input className={inputClass} value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} placeholder="e.g. Implement login API" />
          </div>
          <div>
            <label className="text-xs font-medium text-white block mb-1.5">Description</label>
            <textarea rows={2} className={`${inputClass} resize-none`} value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} placeholder="Task details..." />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs font-medium text-white block mb-1.5">Priority</label>
              <select className={inputClass} value={form.priority} onChange={e => setForm(f => ({ ...f, priority: e.target.value }))}>
                {PRIORITIES.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
            </div>
            <div>
              <label className="text-xs font-medium text-white block mb-1.5">Status</label>
              <select className={inputClass} value={form.status} onChange={e => setForm(f => ({ ...f, status: e.target.value }))}>
                {TASK_STATUSES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
              </select>
            </div>
          </div>
          <div>
            <label className="text-xs font-medium text-white block mb-1.5">Assigned To</label>
            {memberOptions.length > 0 ? (
              <select className={inputClass} value={form.assignedToId} onChange={e => setForm(f => ({ ...f, assignedToId: e.target.value }))}>
                <option value="">Unassigned</option>
                {memberOptions.map(m => <option key={m.id} value={m.id}>{m.memberName}</option>)}
              </select>
            ) : (
              <p className="text-xs text-muted-foreground">No members assigned to this project yet — assign one from the Members tab first.</p>
            )}
          </div>
          {sprintOptions.length > 0 && (
            <div>
              <label className="text-xs font-medium text-white block mb-1.5">Sprint</label>
              <select className={inputClass} value={form.sprintId} onChange={e => setForm(f => ({ ...f, sprintId: e.target.value }))}>
                <option value="">No sprint</option>
                {sprintOptions.map(s => <option key={s.id} value={s.id}>{s.sprintName}</option>)}
              </select>
            </div>
          )}
          <button
            onClick={() => mutation.mutate()}
            disabled={!form.title || mutation.isPending}
            className="w-full bg-primary text-white text-sm font-medium py-2.5 rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-60 flex items-center justify-center gap-2"
          >
            {mutation.isPending && <Loader2 className="w-4 h-4 animate-spin" />}
            {isEdit ? 'Save Changes' : 'Create Task'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function ProjectTasksTab({ project }: Props) {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [modal, setModal] = useState<Task | null | 'new'>(null);
  const [deleteTarget, setDeleteTarget] = useState<Task | null>(null);
  const [filter, setFilter] = useState('ALL');

  const { data: tasksData, isLoading } = useQuery({
    queryKey: ['tasks', project.id],
    queryFn: () => projectService.getTasksByProject(project.id).then(r => r.data),
  });
  const { data: sprintsData } = useQuery({
    queryKey: ['sprints', project.id],
    queryFn: () => projectService.getSprintsByProject(project.id).then(r => r.data),
  });
  const { data: membersData } = useQuery({
    queryKey: ['project-members', project.id],
    queryFn: () => projectService.getProjectMembers(project.id).then(r => r.data),
  });

  const allTasks: Task[] = tasksData?.data || [];
  const sprints: Sprint[] = sprintsData?.data || [];
  const members: ProjectMember[] = membersData?.data || [];
  const tasks = filter === 'ALL' ? allTasks : allTasks.filter(t => t.status === filter);

  const deleteMutation = useMutation({
    mutationFn: (id: number) => projectService.deleteTask(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', project.id] });
      queryClient.invalidateQueries({ queryKey: ['project-stats', project.id] });
      toast({ title: 'Task deleted' });
      setDeleteTarget(null);
    },
  });

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h3 className="text-base font-semibold text-white">Tasks ({allTasks.length})</h3>
        <button
          onClick={() => setModal('new')}
          className="flex items-center gap-1.5 bg-primary text-white text-xs font-medium px-3 py-1.5 rounded-lg hover:bg-primary/90 transition-colors"
        >
          <Plus className="w-3.5 h-3.5" /> New Task
        </button>
      </div>

      {/* Status filter */}
      <div className="flex items-center gap-2 flex-wrap">
        {['ALL', ...TASK_STATUSES].map(s => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            className={`text-xs px-3 py-1.5 rounded-full border transition-colors ${filter === s ? 'bg-primary border-primary text-white' : 'border-border text-muted-foreground hover:text-white hover:border-primary/50'}`}
          >
            {s.replace('_', ' ')}
          </button>
        ))}
      </div>

      {isLoading && <div className="flex justify-center py-10"><Loader2 className="w-5 h-5 animate-spin text-primary" /></div>}

      {!isLoading && tasks.length === 0 && (
        <div className="bg-card border border-dashed border-border rounded-xl p-10 text-center">
          <CheckSquare className="w-8 h-8 text-muted-foreground mx-auto mb-3" />
          <p className="text-sm font-medium text-white mb-1">No tasks found</p>
          <p className="text-xs text-muted-foreground">{filter !== 'ALL' ? 'No tasks with this status.' : 'Create your first task.'}</p>
        </div>
      )}

      {tasks.length > 0 && (
        <div className="bg-card border border-border rounded-xl overflow-hidden">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-background/50 border-b border-border text-xs uppercase tracking-wider text-muted-foreground">
                <th className="px-5 py-3 font-medium">Task</th>
                <th className="px-5 py-3 font-medium">Priority</th>
                <th className="px-5 py-3 font-medium">Status</th>
                <th className="px-5 py-3 font-medium">Assigned To</th>
                <th className="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/50 text-sm">
              {tasks.map(task => (
                <tr key={task.id} className="hover:bg-white/5 transition-colors">
                  <td className="px-5 py-3.5">
                    <p className="font-medium text-white">{task.title}</p>
                    {task.description && <p className="text-xs text-muted-foreground truncate max-w-xs">{task.description}</p>}
                  </td>
                  <td className="px-5 py-3.5">
                    <span className={`text-xs px-2 py-0.5 rounded border font-medium ${priorityColor[task.priority] || ''}`}>{task.priority}</span>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className={`text-xs px-2 py-0.5 rounded border font-medium ${task.status === 'DONE' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : task.status === 'IN_PROGRESS' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' : 'bg-slate-500/10 text-slate-400 border-slate-500/20'}`}>
                      {task.status.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-muted-foreground text-xs">
                    {task.assignedToId ? (members.find(m => m.id === task.assignedToId)?.memberName || `Member #${task.assignedToId}`) : '—'}
                  </td>
                  <td className="px-5 py-3.5">
                    <div className="flex items-center justify-end gap-2">
                      <button onClick={() => setModal(task)} className="p-1.5 rounded hover:bg-white/10 text-muted-foreground hover:text-white transition-colors"><Edit2 className="w-3.5 h-3.5" /></button>
                      <button onClick={() => setDeleteTarget(task)} className="p-1.5 rounded hover:bg-red-500/10 text-muted-foreground hover:text-red-400 transition-colors"><Trash2 className="w-3.5 h-3.5" /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modal !== null && (
        <TaskModal
          task={modal === 'new' ? undefined : modal}
          projectId={project.id}
          sprintOptions={sprints}
          memberOptions={members}
          onClose={() => setModal(null)}
        />
      )}
      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Task"
        message={`Delete task "${deleteTarget?.title}"?`}
        confirmLabel="Delete"
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
        onCancel={() => setDeleteTarget(null)}
        isLoading={deleteMutation.isPending}
      />
    </div>
  );
}
