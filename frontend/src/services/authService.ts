import { authApi, decodeJwt } from './api';

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  role: string;      // "ROLE_USER" from backend JWT
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
function saveTokens(tokens: TokenPair) {
  localStorage.setItem('accessToken', tokens.accessToken);
  localStorage.setItem('refreshToken', tokens.refreshToken);
}

function clearTokens() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
}

// ─── Service ──────────────────────────────────────────────────────────────────
export const authService = {

  // ── Registration: Step 1 — send OTP ────────────────────────────────────────
  // POST /auth/send-otp?email=xxx
  sendRegistrationOtp: async (email: string) => {
    const { data } = await authApi.post(`/send-otp?email=${encodeURIComponent(email)}`);
    return data; // { success, message }
  },

  // ── Registration: Step 2 — register with OTP ────────────────────────────────
  // POST /auth/register  { name, otp, email, password }
  register: async (payload: { name: string; email: string; otp: string; password: string }) => {
    const { data } = await authApi.post('/register', payload);
    return data; // { success, message }
  },

  // ── Login ──────────────────────────────────────────────────────────────────
  // POST /auth/login  { email, password }
  // Returns tokens in data.data; user info decoded from JWT
  login: async (email: string, password: string): Promise<AuthUser> => {
    const { data } = await authApi.post('/login', { email, password });
    const tokens: TokenPair = data.data;
    saveTokens(tokens);

    // Decode JWT to get user info (id, name, role, sub=email)
    const claims = decodeJwt(tokens.accessToken);
    if (!claims) throw new Error('Invalid token received');

    const user: AuthUser = {
      id:    claims.id,
      name:  claims.name,
      email: claims.sub,
      role:  claims.role,
    };
    localStorage.setItem('user', JSON.stringify(user));
    return user;
  },

  // ── Forgot password: Step 1 — send OTP ─────────────────────────────────────
  // PATCH /auth/forgot-password/send-otp?email=xxx
  sendForgotPasswordOtp: async (email: string) => {
    const { data } = await authApi.patch(
      `/forgot-password/send-otp?email=${encodeURIComponent(email)}`
    );
    return data;
  },

  // ── Forgot password: Step 2 — reset with OTP + new password ─────────────────
  // POST /auth/reset-password  { email, otp, password }
  resetPassword: async (email: string, otp: string, password: string) => {
    const { data } = await authApi.post('/reset-password', { email, otp, password });
    return data;
  },

  // ── Refresh token ───────────────────────────────────────────────────────────
  // POST /auth/refresh-token  { refreshToken }
  refreshToken: async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) throw new Error('No refresh token');
    const { data } = await authApi.post('/refresh-token', { refreshToken });
    const tokens: TokenPair = data.data;
    saveTokens(tokens);
    return tokens;
  },

  // ── Logout (client-side only — no backend endpoint) ─────────────────────────
  logout: () => {
    clearTokens();
    localStorage.removeItem('user');
  },

  // ── Helpers ─────────────────────────────────────────────────────────────────
  getAccessToken: () => localStorage.getItem('accessToken'),

  getCurrentUser: (): AuthUser | null => {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  },

  isAuthenticated: () => !!localStorage.getItem('accessToken'),
};
