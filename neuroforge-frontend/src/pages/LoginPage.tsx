import React, { useState } from 'react';
import { Link, useLocation } from 'wouter';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { FaBrain } from 'react-icons/fa';
import { Shield, Building, Kanban, Code2, Bug, UserSquare2, Eye, EyeOff, Loader2, AlertCircle } from 'lucide-react';
import { motion } from 'framer-motion';
import { useAuth } from '@/context/AuthContext';
import { authService } from '@/services/authService';
import { mapBackendRoleToUiRole, roleRouteMap } from '@/lib/roleUtils';

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  rememberMe: z.boolean().optional(),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const roles = [
  { id: 'super-admin',      title: 'Super Admin',      icon: Shield       },
  { id: 'org-admin',        title: 'Org Admin',        icon: Building     },
  { id: 'project-manager',  title: 'Project Manager',  icon: Kanban       },
  { id: 'developer',        title: 'Developer',        icon: Code2        },
  { id: 'qa',              title: 'QA',              icon: Bug          },
  { id: 'client',           title: 'Client',           icon: UserSquare2  },
];

export default function LoginPage() {
  const [, setLocation] = useLocation();
  const { setUser, isAuthenticated, role } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading]       = useState(false);
  const [apiError, setApiError]         = useState('');

  // Redirect already-authenticated users to their dashboard (role is
  // derived from their real backend role, never chosen manually)
  React.useEffect(() => {
    if (isAuthenticated && role) {
      setLocation(roleRouteMap[role] ?? '/');
    }
  }, [isAuthenticated, role, setLocation]);

  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '', rememberMe: false },
  });

  const onSubmit = async (data: LoginFormValues) => {
    setIsLoading(true);
    setApiError('');
    try {
      // Calls POST /auth/login, saves tokens, returns AuthUser (with the
      // account's real backend role, e.g. "ROLE_DEVELOPER")
      const user = await authService.login(data.email, data.password);

      // Store real user in context — role is derived from user.role
      setUser(user);

      const destination = roleRouteMap[mapBackendRoleToUiRole(user.role) ?? ''] ?? '/';
      setLocation(destination);
    } catch (err: any) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Invalid email or password.';
      setApiError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-background">
      {/* Left Panel - Branding */}
      <div className="hidden md:flex md:w-[60%] bg-[#020617] border-r border-border p-12 flex-col justify-between relative overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
          <div className="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-primary/20 rounded-full blur-[120px]" />
          <div className="absolute bottom-[-20%] right-[-10%] w-[50%] h-[50%] bg-blue-600/10 rounded-full blur-[120px]" />
        </div>

        <div className="relative z-10">
          <Link href="/" className="flex items-center gap-3 mb-16 inline-flex">
            <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-card border border-border shadow-[0_0_15px_rgba(37,99,235,0.2)]">
              <FaBrain className="text-primary text-xl" />
            </div>
            <span className="text-2xl font-bold text-white">NeuroForge</span>
          </Link>

          <h1 className="text-5xl font-bold text-white mb-6 leading-tight">
            Welcome back,<br />
            <span className="text-primary">pioneer.</span>
          </h1>
          <p className="text-muted-foreground text-lg max-w-md leading-relaxed">
            Sign in to your intelligent workspace. The Enterprise SDLC and DevOps
            platform is ready for your next deployment.
          </p>

          <div className="mt-10 flex flex-col gap-3">
            <div className="flex items-center gap-3 text-sm text-muted-foreground">
              <div className="w-5 h-5 rounded-full bg-primary/20 flex items-center justify-center">
                <Shield className="w-3 h-3 text-primary" />
              </div>
              Enterprise-grade secure access
            </div>
            <div className="flex items-center gap-3 text-sm text-muted-foreground">
              <div className="w-5 h-5 rounded-full bg-primary/20 flex items-center justify-center">
                <FaBrain className="text-primary text-xs" />
              </div>
              AI context preserved across sessions
            </div>
          </div>
        </div>

        {/* Available Workspaces */}
        <div className="relative z-10">
          <p className="text-xs text-muted-foreground mb-3 uppercase tracking-wider">Available Workspaces</p>
          <div className="grid grid-cols-3 gap-2">
            {roles.map((r) => (
              <div
                key={r.id}
                className="flex items-center gap-2 px-3 py-2 bg-card/50 border border-border rounded-lg text-sm text-muted-foreground"
              >
                <r.icon className="w-3.5 h-3.5" />
                {r.title}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right Panel - Login Form */}
      <div className="flex-1 flex items-center justify-center p-6 md:p-12 bg-background relative">
        <div className="absolute inset-0 pointer-events-none md:hidden">
          <div className="absolute top-[-20%] right-[-20%] w-[60%] h-[60%] bg-primary/10 rounded-full blur-[100px]" />
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="w-full max-w-md relative z-10"
        >
          {/* Mobile logo */}
          <div className="md:hidden flex items-center gap-3 mb-8">
            <div className="flex items-center justify-center w-9 h-9 rounded-xl bg-card border border-border">
              <FaBrain className="text-primary" />
            </div>
            <span className="text-xl font-bold text-white">NeuroForge</span>
          </div>

          <div className="bg-card border border-border rounded-2xl p-8 shadow-2xl backdrop-blur-xl">
            <h2 className="text-2xl font-semibold text-white mb-1">Sign in</h2>
            <p className="text-muted-foreground text-sm mb-7">
              Enter your credentials to access your dashboard.
            </p>

            {/* API Error */}
            {apiError && (
              <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-3 mb-5 text-sm text-red-400">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                {apiError}
              </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
              {/* Email */}
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-white" htmlFor="email">
                  Email Address
                </label>
                <input
                  id="email"
                  type="email"
                  placeholder="name@company.com"
                  className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
                  {...register('email')}
                />
                {errors.email && (
                  <p className="text-xs text-red-400 mt-1">{errors.email.message}</p>
                )}
              </div>

              {/* Password */}
              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <label className="text-sm font-medium text-white" htmlFor="password">
                    Password
                  </label>
                  <Link
                    href="/forgot-password"
                    className="text-xs text-primary hover:text-blue-400 transition-colors"
                  >
                    Forgot password?
                  </Link>
                </div>
                <div className="relative">
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
                    {...register('password')}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-white"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
                {errors.password && (
                  <p className="text-xs text-red-400 mt-1">{errors.password.message}</p>
                )}
              </div>

              {/* Remember me */}
              <div className="flex items-center gap-2">
                <input
                  id="rememberMe"
                  type="checkbox"
                  className="w-4 h-4 accent-primary rounded"
                  {...register('rememberMe')}
                />
                <label htmlFor="rememberMe" className="text-sm text-muted-foreground">
                  Remember me for 30 days
                </label>
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full mt-2 bg-primary text-primary-foreground font-medium rounded-lg py-3 shadow-[0_0_15px_rgba(37,99,235,0.3)] hover:shadow-[0_0_25px_rgba(37,99,235,0.5)] transition-all flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Sign in'}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-muted-foreground">
              Don't have an account?{' '}
              <Link href="/signup" className="text-primary font-medium hover:text-blue-400 transition-colors">
                Register
              </Link>
            </p>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
