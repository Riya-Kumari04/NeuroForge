export type UiRoleSlug =
  | 'super-admin'
  | 'org-admin'
  | 'project-manager'
  | 'developer'
  | 'tester'
  | 'client';

export const mapBackendRoleToUiRole = (role?: string): UiRoleSlug => {
  const map: Record<string, UiRoleSlug> = {
    'ROLE_SUPER_ADMIN':     'super-admin',
    'SUPER_ADMIN':          'super-admin',
    'ROLE_ORG_ADMIN':       'org-admin',
    'ORG_ADMIN':            'org-admin',
    'ROLE_PROJECT_MANAGER': 'project-manager',
    'PROJECT_MANAGER':      'project-manager',
    'ROLE_DEVELOPER':       'developer',
    'DEVELOPER':            'developer',
    'ROLE_TESTER':          'tester',
    'TESTER':               'tester',
    'ROLE_CLIENT':          'client',
    'CLIENT':               'client',
    // legacy / fallback
    'ROLE_ADMIN':           'super-admin',
    'ADMIN':                'super-admin',
    'ROLE_USER':            'developer',
    'USER':                 'developer',
  };
  return map[role || ''] || 'developer';
};

export const roleRouteMap: Record<UiRoleSlug, string> = {
  'super-admin':     '/super-admin',
  'org-admin':       '/org-admin',
  'project-manager': '/project-manager',
  'developer':       '/developer',
  'tester':          '/tester',
  'client':          '/client',
};

export const getRoleDisplayName = (role: string): string => {
  const map: Record<string, string> = {
    'super-admin':         'Super Admin',
    'org-admin':           'Organization Admin',
    'project-manager':     'Project Manager',
    'developer':           'Developer',
    'tester':              'Tester',
    'client':              'Client',
  };
  return map[role] || getRoleDisplayName(mapBackendRoleToUiRole(role));
};

// ── Role-based permission helpers ────────────────────────────────────────────

/** Returns the role-appropriate base path for project routes */
export const getProjectBasePath = (role: UiRoleSlug): string =>
  ({
    'super-admin':     '/super-admin/projects',
    'org-admin':       '/org-admin/projects',
    'project-manager': '/project-manager/projects',
    'developer':       '/developer/projects',
    'tester':          '/tester/projects',
    'client':          '/client/projects',
  } as Record<UiRoleSlug, string>)[role] || '/developer/projects';

/**
 * Whether this role can create / edit / delete projects, sprints,
 * and manage project members.  PROJECT_MANAGER, ORG_ADMIN, SUPER_ADMIN.
 */
export const canManageProjects = (role: UiRoleSlug): boolean =>
  ['super-admin', 'org-admin', 'project-manager'].includes(role);

/** Alias — sprint management uses the same set of privileged roles */
export const canManageSprints = canManageProjects;

/** Alias — member assignment uses the same set of privileged roles */
export const canManageMembers = canManageProjects;

/**
 * Whether this role can CREATE or DELETE tasks.
 * Project Manager and above only.
 */
export const canWriteTasks = canManageProjects;

/**
 * Whether this role can EDIT an existing task (e.g. update status).
 * Developers and testers can update task status but not create/delete.
 */
export const canUpdateTasks = (role: UiRoleSlug): boolean =>
  ['super-admin', 'org-admin', 'project-manager', 'developer', 'tester'].includes(role);
