import React from 'react';
import { Link, useLocation } from 'wouter';
import { FaBrain } from 'react-icons/fa';
import { useAuth } from '@/context/AuthContext';
import {
  LayoutDashboard,
  FolderKanban,
  Building2,
  PieChart,
  Settings,
  User,
  SlidersHorizontal,
  LogOut,
} from 'lucide-react';

interface NavItem {
  label: string;
  icon: React.ElementType;
  href: string;
}

export default function Sidebar() {
  const [location] = useLocation();
  const { user, role: uiRole, logout } = useAuth();
  const role = uiRole || 'developer';

  const getNavItems = (): NavItem[] => {
    switch (role) {
      case 'super-admin':
        return [
          { label: 'Dashboard',     icon: LayoutDashboard,   href: '/super-admin' },
          { label: 'Organizations', icon: Building2,          href: '/super-admin/organizations' },
          { label: 'Settings',      icon: Settings,           href: '/super-admin/settings' },
          { label: 'Profile',       icon: User,               href: '/super-admin/profile' },
          { label: 'Preferences',   icon: SlidersHorizontal,  href: '/super-admin/preferences' },
        ];

      case 'org-admin':
        return [
          { label: 'Overview',      icon: LayoutDashboard,   href: '/org-admin' },
          { label: 'Organizations', icon: Building2,          href: '/org-admin/organizations' },
          { label: 'Projects',      icon: FolderKanban,       href: '/org-admin/projects' },
          { label: 'Portfolio',     icon: PieChart,           href: '/org-admin/portfolio' },
          { label: 'Settings',      icon: Settings,           href: '/org-admin/settings' },
          { label: 'Profile',       icon: User,               href: '/org-admin/profile' },
          { label: 'Preferences',   icon: SlidersHorizontal,  href: '/org-admin/preferences' },
        ];

      case 'project-manager':
        return [
          { label: 'Dashboard',   icon: LayoutDashboard,   href: '/project-manager' },
          { label: 'Projects',    icon: FolderKanban,       href: '/project-manager/projects' },
          { label: 'Portfolio',   icon: PieChart,           href: '/project-manager/portfolio' },
          { label: 'Settings',    icon: Settings,           href: '/project-manager/settings' },
          { label: 'Profile',     icon: User,               href: '/project-manager/profile' },
          { label: 'Preferences', icon: SlidersHorizontal,  href: '/project-manager/preferences' },
        ];

      case 'developer':
        return [
          { label: 'Dashboard',   icon: LayoutDashboard,   href: '/developer' },
          { label: 'My Projects', icon: FolderKanban,       href: '/developer/projects' },
          { label: 'Settings',    icon: Settings,           href: '/developer/settings' },
          { label: 'Profile',     icon: User,               href: '/developer/profile' },
          { label: 'Preferences', icon: SlidersHorizontal,  href: '/developer/preferences' },
        ];

      case 'tester':
        return [
          { label: 'Dashboard',   icon: LayoutDashboard,   href: '/tester' },
          { label: 'Projects',    icon: FolderKanban,       href: '/tester/projects' },
          { label: 'Settings',    icon: Settings,           href: '/tester/settings' },
          { label: 'Profile',     icon: User,               href: '/tester/profile' },
          { label: 'Preferences', icon: SlidersHorizontal,  href: '/tester/preferences' },
        ];

      case 'client':
        return [
          { label: 'Dashboard',   icon: LayoutDashboard,   href: '/client' },
          { label: 'Projects',    icon: FolderKanban,       href: '/client/projects' },
          { label: 'Settings',    icon: Settings,           href: '/client/settings' },
          { label: 'Profile',     icon: User,               href: '/client/profile' },
          { label: 'Preferences', icon: SlidersHorizontal,  href: '/client/preferences' },
        ];

      default:
        return [];
    }
  };

  const navItems = getNavItems();

  const isActive = (href: string) =>
    location === href || (href !== `/${role}` && location.startsWith(`${href}/`));

  return (
    <aside className="w-64 fixed top-0 left-0 h-screen bg-[#0F172A] border-r border-border flex flex-col z-40">
      {/* Brand */}
      <div className="h-16 flex items-center px-6 border-b border-border">
        <Link href="/" className="flex items-center gap-3">
          <FaBrain className="text-primary text-xl" />
          <span className="text-lg font-bold text-white">NeuroForge</span>
        </Link>
      </div>

      {/* Nav */}
      <div className="flex-1 overflow-y-auto py-6 px-3 flex flex-col gap-1">
        {navItems.map((item) => {
          const active = isActive(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                active
                  ? 'bg-primary/10 text-primary'
                  : 'text-muted-foreground hover:text-white hover:bg-white/5'
              }`}
            >
              <item.icon className={`w-5 h-5 ${active ? 'text-primary' : 'text-muted-foreground'}`} />
              {item.label}
            </Link>
          );
        })}
      </div>

      {/* User footer */}
      <div className="p-4 border-t border-border">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold">
            {user?.name?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <div className="flex-1 overflow-hidden">
            <p className="text-sm font-medium text-white truncate">{user?.name || 'User'}</p>
            <p className="text-xs text-muted-foreground capitalize">{role.replace(/-/g, ' ')}</p>
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
