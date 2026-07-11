import api from './api';

export const userService = {
  getUsers: async () => {
    return api.get('/users');
  },

  getUser: async (id: string) => {
    return api.get(`/users/${id}`);
  },

  updateUser: async (id: string, data: Partial<{
    name: string;
    email: string;
    role: string;
    phone: string;
    avatar: string;
  }>) => {
    return api.put(`/users/${id}`, data);
  },

  deleteUser: async (id: string) => {
    return api.delete(`/users/${id}`);
  },

  getUsersByRole: async (role: string) => {
    return api.get(`/users?role=${role}`);
  },

  updateProfile: async (data: Partial<{
    name: string;
    phone: string;
    avatar: string;
  }>) => {
    return api.put('/users/me', data);
  },

  changePassword: async (currentPassword: string, newPassword: string) => {
    return api.put('/users/me/password', { currentPassword, newPassword });
  },
};
