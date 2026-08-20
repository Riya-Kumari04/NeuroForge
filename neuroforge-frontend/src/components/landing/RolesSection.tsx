import React from 'react';
import { motion } from 'framer-motion';
import { Shield, Building, Kanban, Code2, Bug, UserSquare2 } from 'lucide-react';
import { Link } from 'wouter';

const roles = [
  {
    id: 'super-admin',
    title: 'Super Admin',
    description: 'Global system configuration, cross-org management, and deep system analytics.',
    icon: Shield,
    color: 'from-purple-500 to-indigo-600',
    link: '/login?role=super-admin'
  },
  {
    id: 'org-admin',
    title: 'Organization Admin',
    description: 'Manage users, billing, and high-level project portfolios for your company.',
    icon: Building,
    color: 'from-blue-500 to-cyan-600',
    link: '/login?role=org-admin'
  },
  {
    id: 'project-manager',
    title: 'Project Manager',
    description: 'AI-assisted sprint planning, resource allocation, and velocity tracking.',
    icon: Kanban,
    color: 'from-emerald-500 to-teal-600',
    link: '/login?role=project-manager'
  },
  {
    id: 'developer',
    title: 'Developer',
    description: 'Code reviews, task management, and deep integration with your IDE.',
    icon: Code2,
    color: 'from-orange-500 to-red-600',
    link: '/login?role=developer'
  },
  {
    id: 'qa',
    title: 'QA',
    description: 'Automated test runs, bug tracking, and coverage reporting.',
    icon: Bug,
    color: 'from-pink-500 to-rose-600',
    link: '/login?role=qa'
  },
  {
    id: 'client',
    title: 'Client / Stakeholder',
    description: 'Real-time project updates, transparent billing, and deliverable reviews.',
    icon: UserSquare2,
    color: 'from-slate-400 to-slate-600',
    link: '/login?role=client'
  }
];

export default function RolesSection() {
  return (
    <section id="roles" className="py-24 relative overflow-hidden">
      <div className="container mx-auto px-6">
        <div className="text-center mb-16 max-w-3xl mx-auto">
          <h2 className="text-3xl md:text-5xl font-bold text-white mb-6">A workspace for every role</h2>
          <p className="text-muted-foreground text-lg">
            NeuroForge provides custom-tailored dashboards and tools for every stakeholder in the software development lifecycle.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
          {roles.map((role, i) => (
            <Link key={role.id} href={role.link}>
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.4, delay: i * 0.1 }}
                className="group relative p-6 rounded-2xl bg-card border border-border hover:border-primary/50 transition-all duration-300 h-full flex flex-col cursor-pointer overflow-hidden"
              >
                {/* Background Gradient Hover */}
                <div className={`absolute -inset-px bg-gradient-to-br ${role.color} opacity-0 group-hover:opacity-10 transition-opacity duration-500`} />
                
                <div className="relative z-10">
                  <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-5 bg-gradient-to-br ${role.color} text-white shadow-lg`}>
                    <role.icon className="w-6 h-6" />
                  </div>
                  
                  <h3 className="text-xl font-semibold text-white mb-2 group-hover:text-primary transition-colors">
                    {role.title}
                  </h3>
                  <p className="text-muted-foreground text-sm leading-relaxed flex-1">
                    {role.description}
                  </p>
                </div>
              </motion.div>
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
}