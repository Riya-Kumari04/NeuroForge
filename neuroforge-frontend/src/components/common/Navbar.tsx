import React, { useState, useEffect } from 'react';
import { Link } from 'wouter';
import { FaBrain } from 'react-icons/fa';
import { motion, AnimatePresence } from 'framer-motion';

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <motion.nav
      initial={{ y: -100, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled
          ? 'bg-background/80 backdrop-blur-md border-b border-border py-4'
          : 'bg-transparent py-6'
      }`}
    >
      <div className="container mx-auto px-6 flex items-center justify-between">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-3 group">
          <div className="relative flex items-center justify-center w-10 h-10 rounded-xl bg-card border border-border shadow-[0_0_15px_rgba(37,99,235,0.2)] group-hover:shadow-[0_0_20px_rgba(37,99,235,0.4)] transition-shadow">
            <FaBrain className="text-primary text-xl" />
          </div>
          <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
            NeuroForge
          </span>
        </Link>

        {/* Desktop Links */}
        <div className="hidden md:flex items-center gap-8">
          <a
            href="#home"
            className="text-sm font-medium text-muted-foreground hover:text-white transition-colors"
          >
            Home
          </a>
          <a
            href="#features"
            className="text-sm font-medium text-muted-foreground hover:text-white transition-colors"
          >
            Features
          </a>
          <a
            href="#stats"
            className="text-sm font-medium text-muted-foreground hover:text-white transition-colors"
          >
            Stats
          </a>
          <a
            href="#roles"
            className="text-sm font-medium text-muted-foreground hover:text-white transition-colors"
          >
            Roles
          </a>
        </div>

        {/* Actions */}
        <div className="hidden md:flex items-center gap-4">
          <Link
            href="/login"
            className="text-sm font-medium text-muted-foreground hover:text-white transition-colors px-4 py-2"
          >
            Login
          </Link>
          <Link
            href="/signup"
            className="text-sm font-medium bg-primary text-primary-foreground px-5 py-2.5 rounded-lg shadow-[0_0_15px_rgba(37,99,235,0.4)] hover:shadow-[0_0_25px_rgba(37,99,235,0.6)] transition-all"
          >
            Get Started
          </Link>
        </div>

        {/* Mobile Toggle */}
        <button
          className="md:hidden text-white p-2"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            {mobileMenuOpen ? (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>
      </div>

      {/* Mobile Menu */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="md:hidden bg-card/95 backdrop-blur-xl border-b border-border overflow-hidden"
          >
            <div className="px-6 py-4 flex flex-col gap-4">
              <a href="#home" onClick={() => setMobileMenuOpen(false)} className="text-white font-medium py-2">Home</a>
              <a href="#features" onClick={() => setMobileMenuOpen(false)} className="text-white font-medium py-2">Features</a>
              <a href="#stats" onClick={() => setMobileMenuOpen(false)} className="text-white font-medium py-2">Stats</a>
              <a href="#roles" onClick={() => setMobileMenuOpen(false)} className="text-white font-medium py-2">Roles</a>
              <div className="h-px bg-border my-2" />
              <Link href="/login" onClick={() => setMobileMenuOpen(false)} className="text-white font-medium py-2">Login</Link>
              <Link href="/signup" onClick={() => setMobileMenuOpen(false)} className="bg-primary text-center text-primary-foreground font-medium py-3 rounded-lg shadow-lg">Get Started</Link>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.nav>
  );
}