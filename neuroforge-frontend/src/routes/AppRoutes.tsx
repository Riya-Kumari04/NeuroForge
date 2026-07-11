import React from 'react';
import { Route, Switch } from 'wouter';

import LandingPage          from '@/pages/LandingPage';
import LoginPage            from '@/pages/LoginPage';
import SignupPage           from '@/pages/SignupPage';
import ForgotPasswordPage   from '@/pages/ForgotPasswordPage';
import ResetPasswordPage    from '@/pages/ResetPasswordPage';
import NotFound             from '@/pages/not-found';
import ProtectedRoute       from '@/components/common/ProtectedRoute';

import SuperAdminDashboard      from '@/pages/dashboards/SuperAdminDashboard';
import OrgAdminDashboard        from '@/pages/dashboards/OrgAdminDashboard';
import ProjectManagerDashboard  from '@/pages/dashboards/ProjectManagerDashboard';
import DeveloperDashboard       from '@/pages/dashboards/DeveloperDashboard';
import QADashboard              from '@/pages/dashboards/QADashboard';
import ClientDashboard          from '@/pages/dashboards/ClientDashboard';

export default function AppRoutes() {
  return (
    <Switch>
      {/* Public routes */}
      <Route path="/"                component={LandingPage} />
      <Route path="/login"           component={LoginPage} />
      <Route path="/signup"          component={SignupPage} />
      <Route path="/forgot-password" component={ForgotPasswordPage} />
      <Route path="/reset-password"  component={ResetPasswordPage} />

      {/* Protected dashboard routes — each locked to its role */}
      <Route path="/super-admin">
        {() => <ProtectedRoute component={SuperAdminDashboard}     allowedRole="super-admin" />}
      </Route>
      <Route path="/super-admin/:rest*">
        {() => <ProtectedRoute component={SuperAdminDashboard}     allowedRole="super-admin" />}
      </Route>

      <Route path="/org-admin">
        {() => <ProtectedRoute component={OrgAdminDashboard}       allowedRole="org-admin" />}
      </Route>
      <Route path="/org-admin/:rest*">
        {() => <ProtectedRoute component={OrgAdminDashboard}       allowedRole="org-admin" />}
      </Route>

      <Route path="/project-manager">
        {() => <ProtectedRoute component={ProjectManagerDashboard} allowedRole="project-manager" />}
      </Route>
      <Route path="/project-manager/:rest*">
        {() => <ProtectedRoute component={ProjectManagerDashboard} allowedRole="project-manager" />}
      </Route>

      <Route path="/developer">
        {() => <ProtectedRoute component={DeveloperDashboard}      allowedRole="developer" />}
      </Route>
      <Route path="/developer/:rest*">
        {() => <ProtectedRoute component={DeveloperDashboard}      allowedRole="developer" />}
      </Route>

      <Route path="/tester">
        {() => <ProtectedRoute component={QADashboard}             allowedRole="tester" />}
      </Route>
      <Route path="/tester/:rest*">
        {() => <ProtectedRoute component={QADashboard}             allowedRole="tester" />}
      </Route>

      <Route path="/client">
        {() => <ProtectedRoute component={ClientDashboard}         allowedRole="client" />}
      </Route>
      <Route path="/client/:rest*">
        {() => <ProtectedRoute component={ClientDashboard}         allowedRole="client" />}
      </Route>

      <Route component={NotFound} />
    </Switch>
  );
}
