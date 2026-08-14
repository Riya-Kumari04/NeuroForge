import React, { useEffect } from 'react';
import { useLocation } from 'wouter';
import { useAuth } from '@/context/AuthContext';
import { mapBackendRoleToUiRole, roleRouteMap } from '@/lib/roleUtils';

export default function OAuthCallbackPage() {
  const [, setLocation] = useLocation();
  const { setUser } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const refresh = params.get('refresh');
    const userId = params.get('userId');
    const name = params.get('name');
    const email = params.get('email');
    const role = params.get('role');
    const error = params.get('error');

    if (error) {
      // Handle OAuth errors
      console.error('OAuth error:', error);
      setLocation('/login?error=' + error);
      return;
    }

    if (token && refresh && userId && name && email && role) {
      // Store tokens
      localStorage.setItem('access_token', token);
      localStorage.setItem('refresh_token', refresh);

      // Set user in context
      setUser({
        id: userId,
        name: decodeURIComponent(name),
        email: decodeURIComponent(email),
        role: role,
      });

      // Redirect to appropriate dashboard based on role
      const uiRole = mapBackendRoleToUiRole(role);
      const destination = roleRouteMap[uiRole ?? ''] ?? '/';
      setLocation(destination);
    } else {
      // Missing required parameters
      console.error('OAuth callback missing required parameters');
      setLocation('/login?error=oauth_failed');
    }
  }, [setUser, setLocation]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="text-center">
        <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p className="text-muted-foreground">Completing authentication...</p>
      </div>
    </div>
  );
}
