import axios from 'axios';

// ─── JWT helper ─────────────────────────────────────────────────────────────
export function decodeJwt(token: string): Record<string, any> | null {
  try {
    const payload = token.split('.')[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

// ─── Auth-service axios instance  (/auth/* → localhost:8081) ────────────────
export const authApi = axios.create({
  baseURL: '/auth',
  headers: { 'Content-Type': 'application/json' },
});

// ─── Main backend axios instance  (/api/* → localhost:8080) ─────────────────
const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Attach access token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto-refresh on 401
api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await authApi.post('/refresh-token', { refreshToken });
          const newAccess = data.data.accessToken;
          localStorage.setItem('accessToken', newAccess);
          original.headers.Authorization = `Bearer ${newAccess}`;
          return axios(original);
        } catch {
          // refresh failed — force logout
        }
      }
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
