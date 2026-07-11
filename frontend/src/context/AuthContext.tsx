import React, { createContext, useContext, useState, ReactNode } from 'react';
import { authService, AuthUser } from '@/services/authService';

// Frontend UI roles (for routing — separate from the backend "ROLE_USER")
export type UserRole =
  | 'super-admin'
  | 'org-admin'
  | 'project-manager'
  | 'developer'
  | 'tester'
  | 'client'
  | null;

interface AuthContextType {
  user: AuthUser | null;
  role: UserRole;            // UI role (chosen at login, not from backend)
  setRole: (role: UserRole) => void;
  setUser: (user: AuthUser) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  // Rehydrate from localStorage on first load
  const [user, setUserState] = useState<AuthUser | null>(() => authService.getCurrentUser());
  const [role, setRoleState] = useState<UserRole>(() => {
    const saved = localStorage.getItem('userRole');
    return (saved as UserRole) || null;
});

  const setUser = (u: AuthUser) => setUserState(u);

  const setRole = (newRole: UserRole) => {
    setRoleState(newRole);
    if (newRole) {
      localStorage.setItem('userRole', newRole);
  } else {
    localStorage.removeItem('userRole');
  }
};
  

  const logout = () => {
  authService.logout();
  setUserState(null);
  setRoleState(null);
  localStorage.removeItem('userRole');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        role,
        setRole,
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
