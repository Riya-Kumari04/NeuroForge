import api from './api';

// ─── Types ────────────────────────────────────────────────────────────────────

export enum TaskStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  CODE_REVIEW = 'CODE_REVIEW',
  TESTING = 'TESTING',
  DONE = 'DONE'
}

export enum TaskPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface Task {
  id: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  storyPoints?: number;
  labels?: string;
  // Module 4: Specification Traceability
  specificationId?: string;
  specificationVersionId?: string;
  specificationTitle?: string;
  specificationVersionNumber?: number;
  sprintId?: string;
  assigneeId?: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  storyPoints?: number;
  labels?: string;
  // Module 4: Specification Traceability (optional)
  specificationId?: string;
  specificationVersionId?: string;
  sprintId?: string;
  assigneeId?: string;
}

export interface UpdateTaskStatusRequest {
  status: TaskStatus;
}

export interface TaskStatusHistory {
  id: string;
  taskId: string;
  oldStatus?: TaskStatus;
  newStatus: TaskStatus;
  changedAt: string;
  changedBy?: string;
}

// ─── Service ──────────────────────────────────────────────────────────────────

export const taskService = {
  // ── Task CRUD ────────────────────────────────────────────────────────────
  createTask: (data: CreateTaskRequest) => api.post<Task>('/tasks', data),
  getAllTasks: () => api.get<Task[]>('/tasks'),
  getBacklogTasks: () => api.get<Task[]>('/tasks/backlog'),
  searchTasks: (keyword?: string) => api.get<Task[]>(`/tasks/search${keyword ? `?keyword=${keyword}` : ''}`),
  getTasksByStatus: (status: TaskStatus) => api.get<Task[]>(`/tasks/status/${status}`),
  getTasksByPriority: (priority: TaskPriority) => api.get<Task[]>(`/tasks/priority/${priority}`),
  getTasksBySprint: (sprintId: string) => api.get<Task[]>(`/tasks/sprint/${sprintId}`),
  getTasksByAssignee: (userId: string) => api.get<Task[]>(`/tasks/assignee/${userId}`),
  getTaskById: (id: string) => api.get<Task>(`/tasks/${id}`),
  updateTask: (id: string, data: CreateTaskRequest) => api.put<Task>(`/tasks/${id}`, data),
  deleteTask: (id: string) => api.delete(`/tasks/${id}`),

  // ── Task Assignment ────────────────────────────────────────────────────────
  assignSprint: (taskId: string, sprintId: string) => api.patch<Task>(`/tasks/${taskId}/assign-sprint/${sprintId}`),
  removeSprint: (taskId: string) => api.patch<Task>(`/tasks/${taskId}/remove-sprint`),
  assignUser: (taskId: string, userId: string) => api.patch<Task>(`/tasks/${taskId}/assign-user/${userId}`),
  removeUser: (taskId: string) => api.patch<Task>(`/tasks/${taskId}/remove-user`),

  // ── Task Status ───────────────────────────────────────────────────────────
  updateTaskStatus: (taskId: string, data: UpdateTaskStatusRequest) => api.patch<Task>(`/tasks/${taskId}/status`, data),

  // ── Task History ───────────────────────────────────────────────────────────
  getTaskStatusHistory: (taskId: string) => api.get<TaskStatusHistory[]>(`/tasks/${taskId}/history`),
};
