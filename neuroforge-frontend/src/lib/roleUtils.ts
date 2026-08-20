export type UiRoleSlug =
  | 'super-admin'
  | 'org-admin'
  | 'project-manager'
  | 'developer'
  | 'qa'
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
    'ROLE_QA':              'qa',
    'QA':                   'qa',
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
  'qa':              '/qa',
  'client':          '/client',
};

export const getRoleDisplayName = (role: string): string => {
  const map: Record<string, string> = {
    'super-admin':         'Super Admin',
    'org-admin':           'Organization Admin',
    'project-manager':     'Project Manager',
    'developer':           'Developer',
    'qa':                  'QA',
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
    'qa':              '/qa/projects',
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

/** Alias — sprint write operations (start/complete) use the same set of privileged roles */
export const canWriteSprints = canManageProjects;

/** Alias — member assignment uses the same set of privileged roles */
export const canManageMembers = canManageProjects;

/**
 * Whether this role can CREATE or DELETE tasks.
 * Project Manager and above only.
 */
export const canWriteTasks = canManageProjects;

/**
 * Whether this role can EDIT an existing task (e.g. update status).
 * Developers and QA can update task status but not create/delete.
 * Client is read-only.
 */
export const canUpdateTasks = (role: UiRoleSlug): boolean =>
  ['super-admin', 'org-admin', 'project-manager', 'developer', 'qa'].includes(role);

/**
 * Whether this role can view Module 5 features (Backlog, Kanban, Sprint Dashboard).
 * Client can view but not modify.
 */
export const canViewModule5 = (role: UiRoleSlug): boolean =>
  ['super-admin', 'org-admin', 'project-manager', 'developer', 'qa', 'client'].includes(role);

/**
 * Whether this role can move tasks to DONE status.
 * Only QA users can move tasks to DONE.
 */
export const canMoveTaskToDone = (role: UiRoleSlug): boolean =>
  role === 'qa';
