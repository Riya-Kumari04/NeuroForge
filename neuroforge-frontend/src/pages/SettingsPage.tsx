import React from 'react';
import { Link, useLocation } from 'wouter';
import {
  ArrowLeft, User, Lock, Bell, Building2, Shield,
  Sliders, Info, ChevronRight, Users, Globe, Activity,
} from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { useAuth } from '@/context/AuthContext';
import { getRoleDisplayName } from '@/lib/roleUtils';

interface CardDef {
  icon: React.ElementType;
  title: string;
  description: string;
  link?: string;
  linkLabel?: string;
  badge?: string;
}

function SettingCard({ card }: { card: CardDef }) {
  return (
    <div className="bg-card border border-border rounded-xl p-5 flex items-start gap-4 hover:border-primary/30 transition-colors group">
      <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center text-primary flex-shrink-0 group-hover:bg-primary/15 transition-colors">
        <card.icon className="w-5 h-5" />
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-0.5">
          <p className="text-sm font-semibold text-white">{card.title}</p>
          {card.badge && (
            <span className="text-[10px] font-semibold px-2 py-0.5 bg-primary/15 text-primary rounded-full uppercase tracking-wider">
              {card.badge}
            </span>
          )}
        </div>
        <p className="text-xs text-muted-foreground leading-relaxed">{card.description}</p>
        {card.link && card.linkLabel && (
          <Link
            href={card.link}
            className="inline-flex items-center gap-1 mt-3 text-xs text-primary hover:text-blue-400 transition-colors font-medium"
          >
            {card.linkLabel}
            <ChevronRight className="w-3 h-3" />
          </Link>
        )}
      </div>
    </div>
  );
}

function Section({ title, description, cards }: { title: string; description?: string; cards: CardDef[] }) {
  return (
    <div className="mb-8">
      <div className="mb-4">
        <h3 className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">{title}</h3>
        {description && <p className="text-xs text-muted-foreground/70 mt-0.5">{description}</p>}
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {cards.map(card => <SettingCard key={card.title} card={card} />)}
      </div>
    </div>
  );
}

export default function SettingsPage() {
  const { user, role } = useAuth();
  const [, setLocation] = useLocation();
  const base = `/${role}`;

  const personalCards: CardDef[] = [
    { icon: User,    title: 'Profile',               description: 'Update your name, username, phone number, and profile information.',              link: `${base}/profile`,      linkLabel: 'Edit Profile' },
    { icon: Lock,    title: 'Password & Security',   description: 'Change your password and manage account security settings.',                      link: `${base}/profile`,      linkLabel: 'Change Password' },
    { icon: Bell,    title: 'Notifications',          description: 'Configure how and when you receive alerts for tasks, invitations, and events.',    link: `${base}/preferences`,  linkLabel: 'Manage Notifications' },
  ];

  const orgCards: CardDef[] = [
    { icon: Building2, title: 'Organization Settings', description: 'Manage organization details, branding, and plan configuration.',  link: `${base}/organizations`, linkLabel: 'Manage Organizations', badge: 'Admin' },
    { icon: Users,     title: 'Team & Members',         description: 'Add, remove, or update roles for organization members.',          link: `${base}/organizations`, linkLabel: 'Manage Members',       badge: 'Admin' },
  ];

  const pmCards: CardDef[] = [
    { icon: Sliders, title: 'Project Preferences',   description: 'Configure default sprint lengths, task priorities, and project defaults.' },
    { icon: Bell,    title: 'Project Notifications', description: 'Set up alerts for sprint deadlines, task assignments, and project updates.', link: `${base}/preferences`, linkLabel: 'Configure Notifications' },
  ];

  const platformCards: CardDef[] = [
    { icon: Shield,    title: 'Platform Security',      description: 'Review security policies, audit logs, and access controls.',                              badge: 'Super Admin' },
    { icon: Sliders,   title: 'Platform Configuration', description: 'Manage global platform settings and feature flags.',                                      badge: 'Super Admin' },
    { icon: Building2, title: 'All Organizations',      description: 'View and manage all registered organizations.', link: `${base}/organizations`, linkLabel: 'View Organizations', badge: 'Super Admin' },
    { icon: Activity,  title: 'System Activity',        description: 'Monitor platform-wide activity, sessions, and usage metrics.',                            badge: 'Super Admin' },
  ];

  const systemCards: CardDef[] = [
    { icon: Info,  title: 'System Information', description: 'Platform version, environment, and build details.' },
    { icon: Globe, title: 'About NeuroForge',   description: 'License, terms of service, and platform documentation.' },
  ];

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Settings" />
        <main className="flex-1 p-8 overflow-y-auto">

          {/* Back */}
          <button
            onClick={() => setLocation(base)}
            className="flex items-center gap-2 text-sm text-muted-foreground hover:text-white transition-colors mb-6"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Dashboard
          </button>

          {/* User summary */}
          <div className="bg-card border border-border rounded-xl p-5 flex items-center gap-4 mb-8">
            <div className="w-12 h-12 rounded-xl bg-primary/20 flex items-center justify-center text-primary text-lg font-bold flex-shrink-0">
              {user?.name?.charAt(0)?.toUpperCase() ?? 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-white">{user?.name ?? 'User'}</p>
              <p className="text-xs text-muted-foreground">{user?.email}</p>
              <p className="text-xs text-muted-foreground mt-0.5">{getRoleDisplayName(role)}</p>
            </div>
            <Link
              href={`${base}/profile`}
              className="text-xs text-primary hover:text-blue-400 transition-colors font-medium flex-shrink-0"
            >
              Edit Profile →
            </Link>
          </div>

          <Section title="Personal Settings" description="Manage your personal account preferences." cards={personalCards} />

          {role === 'org-admin' && (
            <Section title="Organization Settings" description="Manage your organization and team." cards={orgCards} />
          )}

          {role === 'project-manager' && (
            <Section title="Project Settings" description="Configure project management defaults." cards={pmCards} />
          )}

          {role === 'super-admin' && (
            <Section title="Platform Administration" description="Global platform configuration and monitoring." cards={platformCards} />
          )}

          <Section title="System" cards={systemCards} />

        </main>
      </div>
    </div>
  );
}
