import React from 'react';
import { FaBrain } from 'react-icons/fa';

export default function Footer() {
  return (
    <footer className="bg-[#020617] border-t border-border py-12">
      <div className="container mx-auto px-6">
        <div className="flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex items-center gap-3">
            <FaBrain className="text-primary text-2xl" />
            <span className="text-xl font-bold text-white">NeuroForge</span>
          </div>
          <p className="text-muted-foreground text-sm">
            &copy; {new Date().getFullYear()} NeuroForge Enterprise. All rights reserved.
          </p>
          <div className="flex gap-6">
            <a href="#" className="text-muted-foreground hover:text-white transition-colors text-sm">Privacy Policy</a>
            <a href="#" className="text-muted-foreground hover:text-white transition-colors text-sm">Terms of Service</a>
            <a href="#" className="text-muted-foreground hover:text-white transition-colors text-sm">Contact</a>
          </div>
        </div>
      </div>
    </footer>
  );
}