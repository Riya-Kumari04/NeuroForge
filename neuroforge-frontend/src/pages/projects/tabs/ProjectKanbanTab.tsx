import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2, Plus, GripVertical, FileText } from 'lucide-react';
import { projectService, Task, Project } from '@/services/projectService';
import { useAuth } from '@/context/AuthContext';
import { canWriteTasks, canUpdateTasks, canViewModule5, canMoveTaskToDone } from '@/lib/roleUtils';
import Modal from '@/components/common/Modal';
import { useToast } from '@/hooks/use-toast';
import { websocketService } from '@/services/websocketService';

interface Props { project: Project }

const COLUMNS = [
  { id: 'TODO', label: 'To Do', color: 'bg-slate-500/10 border-slate-500/20' },
  { id: 'IN_PROGRESS', label: 'In Progress', color: 'bg-blue-500/10 border-blue-500/20' },
  { id: 'CODE_REVIEW', label: 'Code Review', color: 'bg-purple-500/10 border-purple-500/20' },
  { id: 'TESTING', label: 'Testing', color: 'bg-orange-500/10 border-orange-500/20' },
  { id: 'DONE', label: 'Done', color: 'bg-emerald-500/10 border-emerald-500/20' },
];

const priorityColor: Record<string, string> = {
  LOW: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
  MEDIUM: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
  HIGH: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
  CRITICAL: 'bg-red-500/10 text-red-400 border-red-500/20',
};

interface TaskFormData {
  title: string;
  description?: string;
  priority?: string;
  status?: string;
  storyPoints?: number;
  labels?: string;
  assignedToId?: number;
  sprintId?: number;
  specificationId?: string;
  specificationVersionId?: string;
}

function TaskModal({ task, projectId, onClose }: {
  task?: Task;
  projectId: number;
  onClose: () => void;
}) {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const isEdit = !!task;

  const [form, setForm] = useState<TaskFormData>({
    title: task?.title || '',
    description: task?.description || '',
    priority: task?.priority || 'MEDIUM',
    status: task?.status || 'TODO',
    storyPoints: task?.storyPoints,
    labels: task?.labels,
    assignedToId: task?.assignedToId,
    sprintId: task?.sprintId,
    specificationId: task?.specificationId,
    specificationVersionId: task?.specificationVersionId,
  });

  const mutation = useMutation({
    mutationFn: () => isEdit
      ? projectService.updateTask(task!.id, form)
      : projectService.createTask({ ...form, projectId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-tasks', projectId] });
      toast({ title: isEdit ? 'Task updated' : 'Task created' });
      onClose();
    },
    onError: () => toast({ title: 'Error', description: 'Operation failed.', variant: 'destructive' }),
  });

  const inputClass = 'w-full bg-background border border-border rounded-lg px-3 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all';

  return (
    <div className="space-y-4">
      <div>
        <label className="text-xs font-medium text-white block mb-1.5">Title</label>
        <input
          className={inputClass}
          value={form.title}
          onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
          placeholder="e.g. Implement login API"
        />
      </div>

      <div>
        <label className="text-xs font-medium text-white block mb-1.5">Description</label>
        <textarea
          className={inputClass}
          rows={3}
          value={form.description}
          onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
          placeholder="Task description..."
        />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="text-xs font-medium text-white block mb-1.5">Priority</label>
          <select
            className={inputClass}
            value={form.priority}
            onChange={e => setForm(f => ({ ...f, priority: e.target.value }))}
          >
            {['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map(p => (
              <option key={p} value={p}>{p}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="text-xs font-medium text-white block mb-1.5">Status</label>
          <select
            className={inputClass}
            value={form.status}
            onChange={e => setForm(f => ({ ...f, status: e.target.value }))}
          >
            {COLUMNS.map(c => (
              <option key={c.id} value={c.id}>{c.label}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="text-xs font-medium text-white block mb-1.5">Story Points</label>
          <input
            type="number"
            min="0"
            max="21"
            className={inputClass}
            value={form.storyPoints || ''}
            onChange={e => setForm(f => ({ ...f, storyPoints: e.target.value ? Number(e.target.value) : undefined }))}
            placeholder="0-21"
          />
        </div>

        <div>
          <label className="text-xs font-medium text-white block mb-1.5">Labels</label>
          <input
            className={inputClass}
            value={form.labels || ''}
            onChange={e => setForm(f => ({ ...f, labels: e.target.value }))}
            placeholder="e.g. bug, feature, urgent"
          />
        </div>
      </div>

      <div className="flex gap-3 pt-2">
        <button
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending || !form.title}
          className="flex-1 bg-primary text-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {mutation.isPending ? 'Saving...' : (isEdit ? 'Update Task' : 'Create Task')}
        </button>
        <button
          onClick={onClose}
          className="px-4 py-2 text-sm rounded-lg border border-border text-muted-foreground hover:text-white hover:border-primary/50 transition-colors"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}

function KanbanCard({ task, onDragStart, canUpdate }: {
  task: Task;
  onDragStart: (e: React.DragEvent, task: Task) => void;
  canUpdate: boolean;
}) {
  return (
    <div
      draggable={canUpdate}
      onDragStart={(e) => onDragStart(e, task)}
      className={`bg-card border border-border rounded-lg p-3 mb-2 cursor-grab hover:border-border/80 transition-all ${canUpdate ? 'hover:shadow-md' : ''}`}
    >
      <div className="flex items-start gap-2">
        {canUpdate && <GripVertical className="w-4 h-4 text-muted-foreground mt-0.5 flex-shrink-0" />}
        <div className="flex-1 min-w-0">
          <div className="text-sm font-medium text-white truncate">{task.title}</div>
          {task.specificationTitle && (
            <div className="mt-1.5 mb-2">
              <span className="inline-flex items-center px-2 py-0.5 rounded text-xs bg-purple-500/20 text-purple-400 border border-purple-500/30">
                <FileText className="w-3 h-3 mr-1" />
                {task.specificationTitle}
                {task.specificationVersionNumber && <span className="ml-1 text-purple-300">v{task.specificationVersionNumber}</span>}
              </span>
            </div>
          )}
          <div className="flex items-center gap-2 mt-2 flex-wrap">
            <span className={`px-2 py-0.5 rounded text-xs font-medium border ${priorityColor[task.priority]}`}>
              {task.priority}
            </span>
            {task.storyPoints !== undefined && task.storyPoints !== null && (
              <span className="px-2 py-0.5 rounded text-xs bg-primary/20 text-primary border border-primary/30">
                {task.storyPoints} pts
              </span>
          )}
          </div>
          {task.labels && (
            <div className="flex flex-wrap gap-1 mt-2">
              {task.labels.split(',').map((label, idx) => (
                <span key={idx} className="px-2 py-0.5 rounded text-xs bg-slate-500/20 text-slate-400 border border-slate-500/30">
                  {label.trim()}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default function ProjectKanbanTab({ project }: Props) {
  const { role } = useAuth();
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const [modalTask, setModalTask] = useState<Task | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [draggedTask, setDraggedTask] = useState<Task | null>(null);

  const canWrite = canWriteTasks(role);
  const canUpdate = canUpdateTasks(role) && role !== 'client';
  const canView = canViewModule5(role);
  const canMoveToDone = canMoveTaskToDone(role);

  // Module 5: WebSocket connection for real-time board updates
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token && canView) {
      websocketService.connect(token)
        .then(() => {
          websocketService.subscribeToProjectBoard(project.id, queryClient);
        })
        .catch((error) => {
          console.error('WebSocket connection failed:', error);
        });
    }

    return () => {
      websocketService.unsubscribeFromProjectBoard(project.id);
    };
  }, [project.id, queryClient, canView]);

  const { data: tasksData, isLoading } = useQuery({
    queryKey: ['project-tasks', project.id],
    queryFn: () => projectService.getTasksByProject(project.id).then(r => r.data),
  });

  const tasks: Task[] = tasksData?.data || [];

  const updateStatusMutation = useMutation({
    mutationFn: ({ taskId, status }: { taskId: number; status: string }) =>
      projectService.updateTask(taskId, { status }),
    onMutate: async ({ taskId, status }) => {
      await queryClient.cancelQueries({ queryKey: ['project-tasks', project.id] });
      const previousTasks = queryClient.getQueryData(['project-tasks', project.id]);
      queryClient.setQueryData(['project-tasks', project.id], (old: any) => {
        if (!old?.data) return old;
        return {
          ...old,
          data: old.data.map((task: Task) =>
            task.id === taskId ? { ...task, status } : task
          ),
        };
      });
      return { previousTasks };
    },
    onError: (err, variables, context) => {
      queryClient.setQueryData(['project-tasks', project.id], context?.previousTasks);
      toast({ title: 'Error', description: 'Failed to update task status.', variant: 'destructive' });
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['project-tasks', project.id] });
    },
  });

  const handleDragStart = (e: React.DragEvent, task: Task) => {
    setDraggedTask(task);
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  };

  const handleDrop = (e: React.DragEvent, status: string) => {
    e.preventDefault();
    if (draggedTask && draggedTask.status !== status) {
      // Module 5: QA-only restriction for DONE status
      if (status === 'DONE' && !canMoveToDone) {
        toast({ 
          title: 'Permission Denied', 
          description: 'Only QA users can move tasks to DONE status.', 
          variant: 'destructive' 
        });
        setDraggedTask(null);
        return;
      }
      updateStatusMutation.mutate({ taskId: draggedTask.id, status });
    }
    setDraggedTask(null);
  };

  const getTasksByStatus = (status: string) => {
    return tasks.filter(t => t.status === status);
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-base font-semibold text-white">Kanban Board</h3>
          <p className="text-xs text-muted-foreground mt-0.5">
            {role === 'client' ? 'View task status for this project' : 'Drag and drop tasks to update their status'}
          </p>
        </div>
        {canWrite && (
          <button
            onClick={() => {
              setModalTask(null);
              setIsModalOpen(true);
            }}
            className="flex items-center gap-2 bg-primary text-white text-xs font-medium px-3 py-1.5 rounded-lg hover:bg-primary/90 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            New Task
          </button>
        )}
      </div>

      {isLoading && (
        <div className="flex justify-center py-12">
          <Loader2 className="w-5 h-5 animate-spin text-primary" />
        </div>
      )}

      {!isLoading && (
        <div className="flex gap-4 overflow-x-auto pb-4">
          {COLUMNS.map(column => {
            const columnTasks = getTasksByStatus(column.id);
            return (
              <div
                key={column.id}
                onDragOver={handleDragOver}
                onDrop={(e) => handleDrop(e, column.id)}
                className={`flex-shrink-0 w-80 ${column.color} border rounded-xl p-4 ${column.id === 'DONE' && !canMoveToDone ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                <div className="flex items-center justify-between mb-4">
                  <h4 className="text-sm font-semibold text-white">{column.label}</h4>
                  <span className="text-xs text-muted-foreground bg-background/50 px-2 py-0.5 rounded-full">
                    {columnTasks.length}
                  </span>
                </div>

                <div className="min-h-[200px]">
                  {columnTasks.length === 0 ? (
                    <div className="text-center text-xs text-muted-foreground py-8">
                      No tasks
                    </div>
                  ) : (
                    columnTasks.map(task => (
                      <KanbanCard
                        key={task.id}
                        task={task}
                        onDragStart={handleDragStart}
                        canUpdate={canUpdate}
                      />
                    ))
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Task Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setModalTask(null);
        }}
        title={modalTask ? 'Edit Task' : 'New Task'}
        size="md"
      >
        <TaskModal
          task={modalTask || undefined}
          projectId={project.id}
          onClose={() => {
            setIsModalOpen(false);
            setModalTask(null);
          }}
        />
      </Modal>
    </div>
  );
}
