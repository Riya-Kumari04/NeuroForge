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
