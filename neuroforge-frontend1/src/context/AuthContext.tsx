import React, { createContext, useContext, useState, ReactNode } from 'react';
import { authService, AuthUser } from '@/services/authService';
import { mapBackendRoleToUiRole, UiRoleSlug } from '@/lib/roleUtils';

// Frontend UI role (for routing) — ALWAYS derived from the authenticated
// user's real backend role. There is no separate, independently-chosen UI
// role anymore: that dual source of truth is what used to let the sidebar
// and the top profile menu disagree about which role was "active".
export type UserRole = UiRoleSlug;

interface AuthContextType {
  user: AuthUser | null;
  role: UserRole;             // derived from user.role — read-only for consumers
  setUser: (user: AuthUser) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  // Rehydrate from localStorage on first load
  const [user, setUserState] = useState<AuthUser | null>(() => authService.getCurrentUser());

  const setUser = (u: AuthUser) => setUserState(u);

  const logout = () => {
    authService.logout();
    setUserState(null);
  };

  const role = mapBackendRoleToUiRole(user?.role);

  return (
    <AuthContext.Provider
      value={{
        user,
        role,
        setUser,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
