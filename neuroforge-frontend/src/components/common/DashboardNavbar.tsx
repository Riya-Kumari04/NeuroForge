import React, { useState, useRef, useEffect } from 'react';
import { Search, User, Settings, LogOut } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { useLocation } from 'wouter';
import { getRoleDisplayName } from '@/lib/roleUtils';
import NotificationBell from './NotificationBell';

export default function DashboardNavbar({ title }: { title: string }) {
  const { user, role, logout } = useAuth();
  const [, setLocation] = useLocation();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsProfileOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setLocation('/login');
  };

  const handleSearch = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && searchQuery.trim()) {
      setLocation(`/${role}/search?q=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }
  };

  const displayRole = role ? getRoleDisplayName(role) : 'User';

  const profileHref = `/${role}/profile`;
  const prefsHref   = `/${role}/preferences`;

  return (
    <header className="h-16 border-b border-border bg-[#0F172A]/80 backdrop-blur-md sticky top-0 z-30 flex items-center justify-between px-8">
      <h1 className="text-lg font-semibold text-white">{title}</h1>

      <div className="flex items-center gap-6">
        <div className="relative hidden md:block">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            onKeyDown={handleSearch}
            placeholder="Search… (press Enter)"
            className="w-64 bg-card border border-border rounded-full pl-9 pr-4 py-1.5 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all"
          />
        </div>

        <NotificationBell />

        <div className="relative" ref={menuRef}>
          <button
            onClick={() => setIsProfileOpen(!isProfileOpen)}
            className="w-9 h-9 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-sm hover:ring-2 hover:ring-primary/50 transition-all cursor-pointer"
          >
            {user?.name?.charAt(0)?.toUpperCase() || 'U'}
          </button>

          {isProfileOpen && (
            <div className="absolute top-full right-0 mt-2 w-56 bg-[#111827] border border-border rounded-xl shadow-2xl z-50 py-1.5 backdrop-blur-xl">
              <div className="px-4 py-2 border-b border-border/50 mb-1">
                <p className="text-sm font-medium text-white">{user?.name || 'User'}</p>
                <p className="text-xs text-muted-foreground">{displayRole}</p>
              </div>
              <div
                onClick={() => { setIsProfileOpen(false); setLocation(profileHref); }}
                className="flex items-center gap-3 px-4 py-2.5 text-sm text-muted-foreground hover:text-white hover:bg-white/5 transition-colors cursor-pointer"
              >
                <User className="w-4 h-4" />
                Profile Settings
              </div>
              <div
                onClick={() => { setIsProfileOpen(false); setLocation(prefsHref); }}
                className="flex items-center gap-3 px-4 py-2.5 text-sm text-muted-foreground hover:text-white hover:bg-white/5 transition-colors cursor-pointer"
              >
                <Settings className="w-4 h-4" />
                Preferences
              </div>
              <div className="h-px bg-border/50 my-1"></div>
              <div
                onClick={handleLogout}
                className="flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:text-red-300 hover:bg-white/5 transition-colors cursor-pointer"
              >
                <LogOut className="w-4 h-4" />
                Sign Out
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
