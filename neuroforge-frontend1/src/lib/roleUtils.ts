// Frontend UI role slugs used for routing (kebab-case), independent of the
// raw backend authority strings (e.g. "ROLE_DEVELOPER").
export type UiRoleSlug =
  | 'super-admin'
  | 'org-admin'
  | 'project-manager'
  | 'developer'
  | 'tester'
  | 'client'
  | null;

// Single source of truth for backend role -> UI route slug.
// The UI role is ALWAYS derived from the authenticated user's real backend
// role — never chosen independently by the user — so the sidebar, the top
// profile menu, and route guarding can never disagree with each other.
const BACKEND_ROLE_TO_UI_SLUG: Record<string, UiRoleSlug> = {
  ROLE_SUPER_ADMIN: 'super-admin',
  ROLE_ORG_ADMIN: 'org-admin',
  ROLE_PROJECT_MANAGER: 'project-manager',
  ROLE_DEVELOPER: 'developer',
  ROLE_TESTER: 'tester',
  ROLE_CLIENT: 'client',
  ROLE_USER: 'developer',
  ROLE_ADMIN: 'super-admin',
};

export const mapBackendRoleToUiRole = (backendRole?: string | null): UiRoleSlug => {
  if (!backendRole) return null;
  return BACKEND_ROLE_TO_UI_SLUG[backendRole] ?? null;
};

export const roleRouteMap: Record<string, string> = {
  'super-admin':     '/super-admin',
  'org-admin':       '/org-admin',
  'project-manager': '/project-manager',
  'developer':       '/developer',
  'tester':          '/tester',
  'client':          '/client',
};

export const getRoleDisplayName = (role: string): string => {
  const map: Record<string, string> = {
    'super-admin': 'Super Admin',
    'org-admin': 'Organization Admin',
    'project-manager': 'Project Manager',
    'developer': 'Developer',
    'tester': 'Tester',
    'client': 'Client',
    'ROLE_USER': 'Developer',
    'USER': 'Developer',
    'ROLE_ADMIN': 'Super Admin',
    'ADMIN': 'Super Admin',
    'ROLE_SUPER_ADMIN': 'Super Admin',
    'ROLE_ORG_ADMIN': 'Organization Admin',
    'ROLE_PROJECT_MANAGER': 'Project Manager',
    'ROLE_DEVELOPER': 'Developer',
    'ROLE_TESTER': 'Tester',
    'ROLE_CLIENT': 'Client',
  };
  return map[role] || role;
};
