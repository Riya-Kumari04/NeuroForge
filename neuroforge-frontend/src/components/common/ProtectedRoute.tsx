import React, { useEffect } from 'react';
import { useLocation } from 'wouter';
import { useAuth, UserRole } from '@/context/AuthContext';
import { roleRouteMap } from '@/lib/roleUtils';

interface Props {
  component: React.ComponentType;
  allowedRole: UserRole;
}

export default function ProtectedRoute({ component: Component, allowedRole }: Props) {
  const { isAuthenticated, role, user } = useAuth();
  const [, setLocation] = useLocation();

  useEffect(() => {
    if (!isAuthenticated) {
      setLocation('/login');
      return;
    }
    
    // Check approval status
    if (user?.approvalStatus === 'PENDING') {
      setLocation('/pending-approval');
      return;
    }
    if (user?.approvalStatus === 'REJECTED') {
      setLocation('/login?error=rejected');
      return;
    }
    
    if (allowedRole && role !== allowedRole) {
      const ownRoute = role ? (roleRouteMap[role] ?? '/login') : '/login';
      setLocation(ownRoute);
    }
  }, [isAuthenticated, role, allowedRole, setLocation, user]);

  // Don't render anything while redirecting
  if (!isAuthenticated) return null;
  if (user?.approvalStatus === 'PENDING') return null;
  if (user?.approvalStatus === 'REJECTED') return null;
  if (allowedRole && role !== allowedRole) return null;

  return <Component />;
}
