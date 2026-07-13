import React from 'react';
import { Route, Switch } from 'wouter';

import LandingPage            from '@/pages/LandingPage';
import LoginPage              from '@/pages/LoginPage';
import SignupPage             from '@/pages/SignupPage';
import ForgotPasswordPage     from '@/pages/ForgotPasswordPage';
import ResetPasswordPage      from '@/pages/ResetPasswordPage';
import NotFound               from '@/pages/not-found';
import ProtectedRoute         from '@/components/common/ProtectedRoute';

// Role Dashboards
import SuperAdminDashboard      from '@/pages/dashboards/SuperAdminDashboard';
import OrgAdminDashboard        from '@/pages/dashboards/OrgAdminDashboard';
import ProjectManagerDashboard  from '@/pages/dashboards/ProjectManagerDashboard';
import DeveloperDashboard       from '@/pages/dashboards/DeveloperDashboard';
import QADashboard              from '@/pages/dashboards/QADashboard';
import ClientDashboard          from '@/pages/dashboards/ClientDashboard';

// Module 2 — Organization pages
import OrganizationListPage     from '@/pages/organizations/OrganizationListPage';
import OrganizationDetailPage   from '@/pages/organizations/OrganizationDetailPage';
import CreateOrganizationPage   from '@/pages/organizations/CreateOrganizationPage';

// Module 3 — Project pages
import ProjectListPage          from '@/pages/projects/ProjectListPage';
import ProjectDetailPage        from '@/pages/projects/ProjectDetailPage';
import CreateProjectPage        from '@/pages/projects/CreateProjectPage';
import EditProjectPage          from '@/pages/projects/EditProjectPage';
import PortfolioDashboard       from '@/pages/projects/PortfolioDashboard';
import { UiRoleSlug } from '@/lib/roleUtils';

// ─── Route guards ─────────────────────────────────────────────────────────────
const guard = (Component: React.ComponentType<any>, role: UiRoleSlug) =>
  () => <ProtectedRoute component={Component} allowedRole={role} />;

export default function AppRoutes() {
  return (
    <Switch>
      {/* ── Public ──────────────────────────────────────────────────────────── */}
      <Route path="/"                component={LandingPage} />
      <Route path="/login"           component={LoginPage} />
      <Route path="/signup"          component={SignupPage} />
      <Route path="/forgot-password" component={ForgotPasswordPage} />
      <Route path="/reset-password"  component={ResetPasswordPage} />

      {/* ── Super Admin ─────────────────────────────────────────────────────── */}
      <Route path="/super-admin">
        {guard(SuperAdminDashboard, 'super-admin')}
      </Route>
      <Route path="/super-admin/orgs">
        {guard(OrganizationListPage, 'super-admin')}
      </Route>
      <Route path="/super-admin/orgs/new">
        {guard(CreateOrganizationPage, 'super-admin')}
      </Route>
      <Route path="/super-admin/orgs/:id">
        {guard(OrganizationDetailPage, 'super-admin')}
      </Route>
      <Route path="/super-admin/:rest*">
        {guard(SuperAdminDashboard, 'super-admin')}
      </Route>

      {/* ── Org Admin ───────────────────────────────────────────────────────── */}
      <Route path="/org-admin">
        {guard(OrgAdminDashboard, 'org-admin')}
      </Route>
      <Route path="/org-admin/organizations">
        {guard(OrganizationListPage, 'org-admin')}
      </Route>
      <Route path="/org-admin/organizations/new">
        {guard(CreateOrganizationPage, 'org-admin')}
      </Route>
      <Route path="/org-admin/organizations/:id">
        {guard(OrganizationDetailPage, 'org-admin')}
      </Route>
      <Route path="/org-admin/portfolio">
        {guard(PortfolioDashboard, 'org-admin')}
      </Route>
      <Route path="/org-admin/projects/new">
        {guard(CreateProjectPage, 'org-admin')}
      </Route>
      <Route path="/org-admin/projects/:id/edit">
        {guard(EditProjectPage, 'org-admin')}
      </Route>
      <Route path="/org-admin/projects/:id">
        {guard(ProjectDetailPage, 'org-admin')}
      </Route>
      <Route path="/org-admin/projects">
        {guard(ProjectListPage, 'org-admin')}
      </Route>
      <Route path="/org-admin/:rest*">
        {guard(OrgAdminDashboard, 'org-admin')}
      </Route>

      {/* ── Project Manager ─────────────────────────────────────────────────── */}
      <Route path="/project-manager">
        {guard(ProjectManagerDashboard, 'project-manager')}
      </Route>
      <Route path="/project-manager/portfolio">
        {guard(PortfolioDashboard, 'project-manager')}
      </Route>
      <Route path="/project-manager/projects/new">
        {guard(CreateProjectPage, 'project-manager')}
      </Route>
      <Route path="/project-manager/projects/:id/edit">
        {guard(EditProjectPage, 'project-manager')}
      </Route>
      <Route path="/project-manager/projects/:id">
        {guard(ProjectDetailPage, 'project-manager')}
      </Route>
      <Route path="/project-manager/projects">
        {guard(ProjectListPage, 'project-manager')}
      </Route>
      <Route path="/project-manager/:rest*">
        {guard(ProjectManagerDashboard, 'project-manager')}
      </Route>

      {/* ── Developer ───────────────────────────────────────────────────────── */}
      <Route path="/developer">
        {guard(DeveloperDashboard, 'developer')}
      </Route>
      <Route path="/developer/:rest*">
        {guard(DeveloperDashboard, 'developer')}
      </Route>

      {/* ── Tester ──────────────────────────────────────────────────────────── */}
      <Route path="/tester">
        {guard(QADashboard, 'tester')}
      </Route>
      <Route path="/tester/:rest*">
        {guard(QADashboard, 'tester')}
      </Route>

      {/* ── Client ──────────────────────────────────────────────────────────── */}
      <Route path="/client">
        {guard(ClientDashboard, 'client')}
      </Route>
      <Route path="/client/:rest*">
        {guard(ClientDashboard, 'client')}
      </Route>

      <Route component={NotFound} />
    </Switch>
  );
}
