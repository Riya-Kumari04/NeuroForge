import api from './api';

// ─── Types ────────────────────────────────────────────────────────────────────
// NOTE: Sprint type in sprintService uses string IDs and different field names (name vs sprintName)
// This is for analytics APIs which may have a different backend schema than projectService.
// Cannot consolidate without backend changes.

export enum SprintStatus {
  PLANNED = 'PLANNED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface SprintAnalytics {
  id: string;
  name: string;
  goal?: string;
  startDate?: string;
  endDate?: string;
  actualStartDate?: string;
  actualEndDate?: string;
  status: SprintStatus;
  teamId?: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface CreateSprintRequest {
  name: string;
  goal?: string;
  startDate?: string;
  endDate?: string;
  teamId?: string;
}

export interface SprintSummary {
  sprintId: string;
  sprintName: string;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  completionPercentage: number;
}

export interface SprintStatistics {
  sprintId: string;
  totalTasks: number;
  completedTasks: number;
  totalStoryPoints: number;
  completedStoryPoints: number;
  averageStoryPointsPerTask: number;
}

export interface SprintProgress {
  sprintId: string;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  codeReviewTasks: number;
  testingTasks: number;
  doneTasks: number;
  completionPercentage: number;
}

export interface BurndownPoint {
  date: string;
  remainingStoryPoints: number;
  remainingTasks: number;
}

export interface SprintVelocity {
  sprintId: string;
  plannedVelocity: number;
  actualVelocity: number;
  velocityDifference: number;
}

export interface TaskDistribution {
  todo: number;
  inProgress: number;
  codeReview: number;
  testing: number;
  done: number;
}

// ─── Service ──────────────────────────────────────────────────────────────────

export const sprintService = {
  // ── Sprint CRUD ───────────────────────────────────────────────────────────
  createSprint: (data: CreateSprintRequest) => api.post<SprintAnalytics>('/sprints', data),
  getAllSprints: () => api.get<SprintAnalytics[]>('/sprints'),
  getActiveSprint: () => api.get<SprintAnalytics>('/sprints/active'),
  getSprintById: (id: string) => api.get<SprintAnalytics>(`/sprints/${id}`),
  updateSprint: (id: string, data: CreateSprintRequest) => api.put<SprintAnalytics>(`/sprints/${id}`, data),
  deleteSprint: (id: string) => api.delete(`/sprints/${id}`),

  // ── Sprint Lifecycle ────────────────────────────────────────────────────────
  startSprint: (id: string) => api.post<SprintAnalytics>(`/sprints/${id}/start`),
  completeSprint: (id: string) => api.post<SprintAnalytics>(`/sprints/${id}/complete`),

  // ── Sprint Analytics ────────────────────────────────────────────────────────
  getSprintSummary: (id: string) => api.get<SprintSummary>(`/sprints/${id}/summary`),
  getSprintStatistics: (id: string) => api.get<SprintStatistics>(`/sprints/${id}/statistics`),
  getSprintProgress: (id: string) => api.get<SprintProgress>(`/sprints/${id}/progress`),
  getSprintBurndown: (id: string) => api.get<BurndownPoint[]>(`/sprints/${id}/burndown`),
  getSprintVelocity: (id: string) => api.get<SprintVelocity>(`/sprints/${id}/velocity`),
  getTaskDistribution: (id: string) => api.get<TaskDistribution>(`/sprints/${id}/distribution`),
};
