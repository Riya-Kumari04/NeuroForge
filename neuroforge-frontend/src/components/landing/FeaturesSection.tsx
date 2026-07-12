import React from 'react';
import { motion } from 'framer-motion';
import { Shield, Fingerprint, KanbanSquare, Users, LineChart, Cpu, Lock } from 'lucide-react';
import { FaBrain } from 'react-icons/fa';

const features = [
  {
    title: 'AI Authentication',
    description: 'Biometric and AI-powered identity verification ensuring secure access.',
    icon: Fingerprint,
    colSpan: 'md:col-span-1',
  },
  {
    title: 'Role-Based Access',
    description: 'Granular permissions for every team member from dev to client.',
    icon: Shield,
    colSpan: 'md:col-span-1',
  },
  {
    title: 'Real-time Collaboration',
    description: 'Live editing, inline comments, and presence awareness across all tools.',
    icon: Users,
    colSpan: 'md:col-span-1',
  },
  {
    title: 'AI-Assisted Planning',
    description: 'Predictive sprint planning, automated task breakdown, and intelligent resource allocation.',
    icon: FaBrain,
    colSpan: 'md:col-span-3',
    highlight: true,
  },
  {
    title: 'Predictive Analytics',
    description: 'Deep insights powered by machine learning to catch bottlenecks before they happen.',
    icon: LineChart,
    colSpan: 'md:col-span-1',
  },
  {
    title: 'Enterprise Security',
    description: 'Military-grade encryption, SOC2 compliance, and automated vulnerability scanning.',
    icon: Lock,
    colSpan: 'md:col-span-1',
  },
  {
    title: 'Intelligent Co-pilot',
    description: 'Your AI assistant that learns your codebase, suggests fixes, and automates mundane tasks.',
    icon: Cpu,
    colSpan: 'md:col-span-1',
  },
];

export default function FeaturesSection() {
  return (
    <section id="features" className="py-24 relative">
      <div className="container mx-auto px-6">
        <div className="text-center mb-16 max-w-3xl mx-auto">
          <h2 className="text-3xl md:text-5xl font-bold text-white mb-6">Built for the future of software</h2>
          <p className="text-muted-foreground text-lg">
            NeuroForge integrates advanced AI at every layer of the stack, providing tools that don't just track work, but actively help you complete it.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-5xl mx-auto">
          {features.map((feature, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: i * 0.1 }}
              className={`
                group relative p-8 rounded-2xl bg-card/40 backdrop-blur-sm border border-border 
                hover:bg-card/80 transition-all duration-300
                ${feature.colSpan}
                ${feature.highlight ? 'shadow-[0_0_30px_rgba(37,99,235,0.1)] border-primary/30' : ''}
              `}
            >
              {/* Hover Glow */}
              <div className="absolute inset-0 rounded-2xl bg-gradient-to-br from-primary/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none" />
              
              <div className={`
                w-12 h-12 rounded-xl flex items-center justify-center mb-6
                ${feature.highlight ? 'bg-primary text-white shadow-[0_0_15px_rgba(37,99,235,0.5)]' : 'bg-primary/10 text-primary group-hover:bg-primary/20'}
                transition-colors
              `}>
                <feature.icon className="w-6 h-6" />
              </div>
              
              <h3 className="text-xl font-semibold text-white mb-3">{feature.title}</h3>
              <p className="text-muted-foreground leading-relaxed">{feature.description}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}