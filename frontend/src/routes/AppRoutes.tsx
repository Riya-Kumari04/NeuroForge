import React from 'react';
import { Route, Switch } from 'wouter';
import NotFound from '@/pages/not-found';

import LandingPage from '@/pages/LandingPage';
import LoginPage from '@/pages/LoginPage';
import SignupPage from '@/pages/SignupPage';
import ForgotPasswordPage from '@/pages/ForgotPasswordPage';
import ResetPasswordPage from '@/pages/ResetPasswordPage';

import SuperAdminDashboard from '@/pages/dashboards/SuperAdminDashboard';
import OrgAdminDashboard from '@/pages/dashboards/OrgAdminDashboard';
import ProjectManagerDashboard from '@/pages/dashboards/ProjectManagerDashboard';
import DeveloperDashboard from '@/pages/dashboards/DeveloperDashboard';
import QADashboard from '@/pages/dashboards/QADashboard';
import ClientDashboard from '@/pages/dashboards/ClientDashboard';

export default function AppRoutes() {
  return (
    <Switch>
      <Route path="/" component={LandingPage} />
      <Route path="/login" component={LoginPage} />
      <Route path="/signup" component={SignupPage} />
      <Route path="/forgot-password" component={ForgotPasswordPage} />
      <Route path="/reset-password" component={ResetPasswordPage} />
      
      <Route path="/super-admin" component={SuperAdminDashboard} />
      <Route path="/super-admin/:rest*" component={SuperAdminDashboard} />
      <Route path="/org-admin" component={OrgAdminDashboard} />
      <Route path="/org-admin/:rest*" component={OrgAdminDashboard} />
      <Route path="/project-manager" component={ProjectManagerDashboard} />
      <Route path="/project-manager/:rest*" component={ProjectManagerDashboard} />
      <Route path="/developer" component={DeveloperDashboard} />
      <Route path="/developer/:rest*" component={DeveloperDashboard} />
      <Route path="/tester" component={QADashboard} />
      <Route path="/tester/:rest*" component={QADashboard} />
      <Route path="/client" component={ClientDashboard} />
      <Route path="/client/:rest*" component={ClientDashboard} />

      <Route component={NotFound} />
    </Switch>
  );
}