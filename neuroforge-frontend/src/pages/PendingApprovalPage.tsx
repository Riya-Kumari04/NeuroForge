import React from 'react';
import { Link } from 'wouter';
import { Clock, LogOut, RefreshCw, Mail } from 'lucide-react';
import { FaBrain } from 'react-icons/fa';
import { useAuth } from '@/context/AuthContext';

export default function PendingApprovalPage() {
  const { logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  const handleRefresh = () => {
    window.location.reload();
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 py-12 bg-[#020617] relative overflow-hidden">
      <div className="absolute inset-0 z-0 pointer-events-none">
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-indigo-500/10 rounded-full blur-[120px]" />
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#ffffff02_1px,transparent_1px),linear-gradient(to_bottom,#ffffff02_1px,transparent_1px)] bg-[size:4rem_4rem]" />
      </div>

      <div className="w-full max-w-md z-10">
        <div className="text-center mb-8">
          <Link href="/" className="inline-flex items-center gap-3 mb-6">
            <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-card border border-border shadow-[0_0_15px_rgba(37,99,235,0.2)]">
              <FaBrain className="text-primary text-xl" />
            </div>
            <span className="text-2xl font-bold text-white">NeuroForge</span>
          </Link>
        </div>

        <div className="bg-card/80 backdrop-blur-xl border border-border rounded-2xl p-8 shadow-2xl">
          <div className="flex justify-center mb-6">
            <div className="w-16 h-16 rounded-full bg-amber-500/10 border border-amber-500/20 flex items-center justify-center">
              <Clock className="w-8 h-8 text-amber-400" />
            </div>
          </div>

          <h1 className="text-2xl font-bold text-white text-center mb-2">Account Pending Approval</h1>
          <p className="text-muted-foreground text-center text-sm mb-6">
            Your account has been created successfully, but an organization administrator must approve your account before you can access NeuroForge.
          </p>

          <div className="bg-background/50 border border-border rounded-xl p-4 mb-6">
            <div className="flex items-center gap-3 mb-3">
              <Mail className="w-4 h-4 text-primary" />
              <span className="text-sm font-medium text-white">Status Information</span>
            </div>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Current Status:</span>
                <span className="text-amber-400 font-medium">PENDING</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Required Action:</span>
                <span className="text-white">Organization Admin Approval</span>
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <button
              onClick={handleRefresh}
              className="w-full bg-primary text-primary-foreground font-medium rounded-lg py-3 shadow-[0_0_15px_rgba(37,99,235,0.3)] hover:shadow-[0_0_25px_rgba(37,99,235,0.5)] transition-all flex items-center justify-center gap-2"
            >
              <RefreshCw className="w-4 h-4" />
              Check Approval Status
            </button>

            <button
              onClick={handleLogout}
              className="w-full bg-secondary text-white font-medium rounded-lg py-3 hover:bg-secondary/80 transition-colors flex items-center justify-center gap-2"
            >
              <LogOut className="w-4 h-4" />
              Logout
            </button>
          </div>

          <div className="mt-6 text-center">
            <p className="text-xs text-muted-foreground">
              Need help? Contact your organization administrator for assistance.
            </p>
          </div>
        </div>

        <div className="mt-6 text-center">
          <Link href="/login" className="text-sm text-primary hover:text-blue-400 transition-colors">
            Return to Login
          </Link>
        </div>
      </div>
    </div>
  );
}
