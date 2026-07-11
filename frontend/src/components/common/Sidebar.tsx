import React from 'react';
import { Link, useLocation } from 'wouter';
import { FaBrain } from 'react-icons/fa';
import { useAuth } from '@/context/AuthContext';
import { 
  LayoutDashboard, 
  Users, 
  Briefcase, 
  Settings, 
  LogOut,
  FolderKanban,
  CheckSquare,
  Activity,
  FileCode2,
  Bug,
  PieChart,
  MessageSquare
} from 'lucide-react';

export default function Sidebar() {
  const [location] = useLocation();
  const { user, role , logout } = useAuth();
  

  const getNavItems = () => {
    switch (role) {
      case 'super-admin':
        return [
          { label: 'Dashboard', icon: LayoutDashboard, href: '/super-admin' },
          { label: 'Organizations', icon: Briefcase, href: '/super-admin/orgs' },
          { label: 'Users', icon: Users, href: '/super-admin/users' },
          { label: 'System Logs', icon: Activity, href: '/super-admin/logs' },
          { label: 'Settings', icon: Settings, href: '/super-admin/settings' },
        ];
      case 'org-admin':
        return [
          { label: 'Overview', icon: LayoutDashboard, href: '/org-admin' },
          { label: 'Projects', icon: FolderKanban, href: '/org-admin/projects' },
          { label: 'Team', icon: Users, href: '/org-admin/team' },
          { label: 'Reports', icon: PieChart, href: '/org-admin/reports' },
          { label: 'Settings', icon: Settings, href: '/org-admin/settings' },
        ];
      case 'project-manager':
        return [
          { label: 'Board', icon: FolderKanban, href: '/project-manager' },
          { label: 'Sprints', icon: Activity, href: '/project-manager/sprints' },
          { label: 'Team', icon: Users, href: '/project-manager/team' },
          { label: 'Reports', icon: PieChart, href: '/project-manager/reports' },
        ];
      case 'developer':
        return [
          { label: 'My Tasks', icon: CheckSquare, href: '/developer' },
          { label: 'Code', icon: FileCode2, href: '/developer/code' },
          { label: 'Pull Requests', icon: Activity, href: '/developer/prs' },
          { label: 'Issues', icon: Bug, href: '/developer/issues' },
        ];
      case 'tester':
        return [
          { label: 'Test Runs', icon: Activity, href: '/tester' },
          { label: 'Bug Tracker', icon: Bug, href: '/tester/bugs' },
          { label: 'Test Cases', icon: CheckSquare, href: '/tester/cases' },
          { label: 'Reports', icon: PieChart, href: '/tester/reports' },
        ];
      case 'client':
        return [
          { label: 'Dashboard', icon: LayoutDashboard, href: '/client' },
          { label: 'Projects', icon: FolderKanban, href: '/client/projects' },
          { label: 'Updates', icon: MessageSquare, href: '/client/updates' },
          { label: 'Invoices', icon: FileCode2, href: '/client/invoices' },
        ];
      default:
        return [];
    }
  };

  const navItems = getNavItems();

  return (
    <aside className="w-64 fixed top-0 left-0 h-screen bg-[#0F172A] border-r border-border flex flex-col z-40">
      {/* Brand */}
      <div className="h-16 flex items-center px-6 border-b border-border">
        <Link href="/" className="flex items-center gap-3">
          <FaBrain className="text-primary text-xl" />
          <span className="text-lg font-bold text-white">NeuroForge</span>
        </Link>
      </div>

      {/* Nav Links */}
      <div className="flex-1 overflow-y-auto py-6 px-3 flex flex-col gap-1">
        {navItems.map((item) => {
          const isActive = location === item.href || location.startsWith(`${item.href}/`);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-primary/10 text-primary'
                  : 'text-muted-foreground hover:text-white hover:bg-white/5'
              }`}
            >
              <item.icon className={`w-5 h-5 ${isActive ? 'text-primary' : 'text-muted-foreground'}`} />
              {item.label}
            </Link>
          );
        })}
      </div>

      {/* User Profile */}
      <div className="p-4 border-t border-border">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold">
            {user?.name?.charAt(0) || 'U'}
          </div>
          <div className="flex-1 overflow-hidden">
            <p className="text-sm font-medium text-white truncate">{user?.name || 'User Name'}</p>
            <p className="text-xs text-muted-foreground capitalize">{role.replace('-', ' ')}</p>
          </div>
        </div>
        <Link
          href="/login"
          onClick={() => logout()}
          className="flex items-center gap-2 text-sm text-muted-foreground hover:text-red-400 transition-colors w-full px-2 py-1"
        >
          <LogOut className="w-4 h-4" />
          Sign Out
        </Link>
      </div>
    </aside>
  );
}