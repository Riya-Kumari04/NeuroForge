import React, { useEffect } from 'react';
import { useLocation } from 'wouter';
import { useAuth, UserRole } from '@/context/AuthContext';
import { roleRouteMap } from '@/lib/roleUtils';

interface Props {
  component: React.ComponentType;
  allowedRole: UserRole;
}

export default function ProtectedRoute({ component: Component, allowedRole }: Props) {
  const { isAuthenticated, role } = useAuth();
  const [, setLocation] = useLocation();

  useEffect(() => {
    if (!isAuthenticated) {
      setLocation('/login');
      return;
    }
    if (allowedRole && role !== allowedRole) {
      const ownRoute = role ? (roleRouteMap[role] ?? '/login') : '/login';
      setLocation(ownRoute);
    }
  }, [isAuthenticated, role, allowedRole, setLocation]);

  // Don't render anything while redirecting
  if (!isAuthenticated) return null;
  if (allowedRole && role !== allowedRole) return null;

  return <Component />;
}
