import api from './api';

export const projectService = {
  getProjects: async () => {
    return api.get('/projects');
  },

  getProject: async (id: string) => {
    return api.get(`/projects/${id}`);
  },

  createProject: async (data: {
    name: string;
    description?: string;
    deadline?: string;
    status?: string;
  }) => {
    return api.post('/projects', data);
  },

  updateProject: async (id: string, data: Partial<{
    name: string;
    description: string;
    deadline: string;
    status: string;
  }>) => {
    return api.put(`/projects/${id}`, data);
  },

  deleteProject: async (id: string) => {
    return api.delete(`/projects/${id}`);
  },

  getProjectMembers: async (id: string) => {
    return api.get(`/projects/${id}/members`);
  },

  addProjectMember: async (projectId: string, userId: string, role: string) => {
    return api.post(`/projects/${projectId}/members`, { userId, role });
  },

  removeProjectMember: async (projectId: string, userId: string) => {
    return api.delete(`/projects/${projectId}/members/${userId}`);
  },
};
