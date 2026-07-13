import api from './api';

// ─── Types ────────────────────────────────────────────────────────────────────

export interface Project {
  id: number;
  projectName: string;
  description?: string;
  status: string;
  startDate?: string;
  endDate?: string;
  organizationId?: number;
  organizationName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Sprint {
  id: number;
  sprintName: string;
  goal?: string;
  status: string;
  startDate?: string;
  endDate?: string;
  projectId?: number;
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  priority: string;
  status: string;
  assignedToId?: number;
  projectId?: number;
  sprintId?: number;
  createdAt?: string;
  updatedAt?: string;
}

// A member of an organization's team who has been assigned onto this
// project (Module 2 TeamMember <-> Module 3 Project, with a project role).
export interface ProjectMember {
  id: number;
  projectId: number;
  teamMemberId: number;
  memberName: string;
  role: string;
}

export interface ProjectStats {
  projectId: number;
  projectName: string;
  status: string;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  totalSprints: number;
  totalMembers: number;
  healthScore: number;
  healthStatus: string;
}

export interface CreateProjectRequest {
  projectName: string;
  description?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  organizationId: number;
}

export interface UpdateProjectRequest {
  projectName?: string;
  description?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
}

export interface CreateSprintRequest {
  sprintName: string;
  goal?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  projectId: number;
}

export interface UpdateSprintRequest {
  sprintName?: string;
  goal?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority?: string;
  status?: string;
  assignedToId?: number;
  projectId: number;
  sprintId?: number;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  priority?: string;
  status?: string;
  assignedToId?: number;
  sprintId?: number;
}

export interface AssignProjectMemberRequest {
  projectId: number;
  teamMemberId: number;
}

// ─── Date helper — backend needs ISO-8601 datetime, forms produce YYYY-MM-DD ──
const toIso = (d?: string) => (d && d.length === 10 ? `${d}T00:00:00` : d);

const toIsoProject = (data: CreateProjectRequest | UpdateProjectRequest): any => ({
  ...data,
  startDate: toIso((data as any).startDate),
  endDate:   toIso((data as any).endDate),
});

const toIsoSprint = (data: CreateSprintRequest | UpdateSprintRequest): any => ({
  ...data,
  startDate: toIso((data as any).startDate),
  endDate:   toIso((data as any).endDate),
});

// ─── Service ──────────────────────────────────────────────────────────────────

export const projectService = {
  // ── Projects ────────────────────────────────────────────────────────────────
  getAll: () => api.get<any>('/projects'),
  getByOrganization: (orgId: number) => api.get<any>(`/projects/organization/${orgId}`),
  getById: (id: number) => api.get<any>(`/projects/${id}`),
  create: (data: CreateProjectRequest) => api.post<any>('/projects', toIsoProject(data)),
  update: (id: number, data: UpdateProjectRequest) => api.put<any>(`/projects/${id}`, toIsoProject(data)),
  delete: (id: number) => api.delete(`/projects/${id}`),
  getStats: (id: number) => api.get<any>(`/projects/${id}/stats`),

  // ── Sprints ─────────────────────────────────────────────────────────────────
  createSprint: (data: CreateSprintRequest) => api.post<any>('/sprints', toIsoSprint(data)),
  getSprintsByProject: (projectId: number) => api.get<any>(`/sprints/project/${projectId}`),
  getSprintById: (id: number) => api.get<any>(`/sprints/${id}`),
  updateSprint: (id: number, data: UpdateSprintRequest) => api.put<any>(`/sprints/${id}`, toIsoSprint(data)),
  deleteSprint: (id: number) => api.delete(`/sprints/${id}`),

  // ── Tasks ───────────────────────────────────────────────────────────────────
  createTask: (data: CreateTaskRequest) => api.post<any>('/tasks', data),
  getTasksByProject: (projectId: number) => api.get<any>(`/tasks/project/${projectId}`),
  getTasksBySprint: (sprintId: number) => api.get<any>(`/tasks/sprint/${sprintId}`),
  getTaskById: (id: number) => api.get<any>(`/tasks/${id}`),
  updateTask: (id: number, data: UpdateTaskRequest) => api.put<any>(`/tasks/${id}`, data),
  deleteTask: (id: number) => api.delete(`/tasks/${id}`),

  // ── Project Members (assign a Module-2 TeamMember onto this project) ────────
  assignMember: (data: AssignProjectMemberRequest) => api.post<any>('/project-members', data),
  getProjectMembers: (projectId: number) => api.get<any>(`/project-members/${projectId}`),
  removeMember: (projectMemberId: number) => api.delete(`/project-members/${projectMemberId}`),
};
