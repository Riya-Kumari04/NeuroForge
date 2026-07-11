import React from 'react';
import { motion } from 'framer-motion';
import { Users, Building2, Server, Zap } from 'lucide-react';

const stats = [
  { value: '10,000+', label: 'Active Users', icon: Users },
  { value: '500+', label: 'Enterprise Clients', icon: Building2 },
  { value: '99.99%', label: 'Uptime SLA', icon: Server },
  { value: '3x', label: 'Faster Delivery', icon: Zap, highlight: true },
];

export default function StatsSection() {
  return (
    <section id="stats" className="py-20 border-y border-border/50 bg-[#0F172A]/30">
      <div className="container mx-auto px-6">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          {stats.map((stat, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, scale: 0.9 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: i * 0.1 }}
              className="text-center"
            >
              <div className={`
                inline-flex items-center justify-center w-12 h-12 rounded-full mb-4
                ${stat.highlight ? 'bg-primary/20 text-primary' : 'bg-card border border-border text-muted-foreground'}
              `}>
                <stat.icon className="w-5 h-5" />
              </div>
              <div className={`text-4xl md:text-5xl font-bold mb-2 ${stat.highlight ? 'text-transparent bg-clip-text bg-gradient-to-r from-primary to-blue-400' : 'text-white'}`}>
                {stat.value}
              </div>
              <div className="text-sm font-medium text-muted-foreground uppercase tracking-wider">
                {stat.label}
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}