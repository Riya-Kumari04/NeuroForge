import React, { useState } from 'react';
import { Link, useLocation } from 'wouter';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Eye, EyeOff, Loader2, CheckCircle2, AlertCircle } from 'lucide-react';
import { motion } from 'framer-motion';
import { authService } from '@/services/authService';

const resetSchema = z
  .object({
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(20)
      .regex(/[A-Z]/, 'Must contain an uppercase letter')
      .regex(/[a-z]/, 'Must contain a lowercase letter')
      .regex(/[0-9]/, 'Must contain a number')
      .regex(/[@$!%*?&]/, 'Must contain a special character (@$!%*?&)'),
    confirmPassword: z.string(),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  });

type ResetFormValues = z.infer<typeof resetSchema>;

function getStrength(pw: string) {
  if (!pw) return 0;
  let s = 0;
  if (pw.length >= 8)           s += 25;
  if (pw.match(/[A-Z]/))        s += 25;
  if (pw.match(/[a-z]/))        s += 25;
  if (pw.match(/[0-9@$!%*?&]/)) s += 25;
  return s;
}

export default function ResetPasswordPage() {
  const [, setLocation] = useLocation();
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading]       = useState(false);
  const [isSuccess, setIsSuccess]       = useState(false);
  const [apiError, setApiError]         = useState('');

  // ── Read email + OTP passed from ForgotPasswordPage via navigate state ────────
  // wouter doesn't expose history state directly; we read from window.history
  const state = (window.history.state as { email?: string; otp?: string }) || {};
  const email = state.email || '';
  const otp   = state.otp   || '';

  const { register, handleSubmit, watch, formState: { errors } } = useForm<ResetFormValues>({
    resolver: zodResolver(resetSchema),
    defaultValues: { password: '', confirmPassword: '' },
  });

  const pwStrength = getStrength(watch('password'));

  const onSubmit = async (data: ResetFormValues) => {
    if (!email || !otp) {
      setApiError('Session expired. Please restart the password reset flow.');
      return;
    }
    setIsLoading(true);
    setApiError('');
    try {
      // POST /auth/reset-password  { email, otp, password }
      await authService.resetPassword(email, otp, data.password);
      setIsSuccess(true);
      setTimeout(() => setLocation('/login'), 3000);
    } catch (err: any) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Invalid or expired OTP. Please request a new code.';
      setApiError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-[#020617] relative overflow-hidden">
      <div className="absolute inset-0 z-0 pointer-events-none">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-primary/10 rounded-full blur-[150px]" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="w-full max-w-md z-10"
      >
        <div className="bg-card border border-border rounded-2xl p-8 shadow-2xl backdrop-blur-xl">
          {!isSuccess ? (
            <>
              <h1 className="text-2xl font-semibold text-white mb-2">Set new password</h1>
              <p className="text-muted-foreground text-sm mb-8">
                Your new password must be different from previously used passwords.
              </p>

              {/* API error */}
              {apiError && (
                <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-3 mb-5 text-sm text-red-400">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  {apiError}
                  {!email && (
                    <Link href="/forgot-password" className="ml-auto text-primary underline whitespace-nowrap">
                      Restart
                    </Link>
                  )}
                </div>
              )}

              <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                {/* New Password */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-white" htmlFor="password">
                    New Password
                  </label>
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
                  <div className="mt-2 flex gap-1 h-1.5">
                    <div className={`flex-1 rounded-full transition-colors ${pwStrength > 0 ? (pwStrength < 50 ? 'bg-red-500' : pwStrength < 75 ? 'bg-yellow-500' : 'bg-emerald-500') : 'bg-border'}`} />
                    <div className={`flex-1 rounded-full transition-colors ${pwStrength >= 50 ? (pwStrength < 75 ? 'bg-yellow-500' : 'bg-emerald-500') : 'bg-border'}`} />
                    <div className={`flex-1 rounded-full transition-colors ${pwStrength >= 75 ? 'bg-emerald-500' : 'bg-border'}`} />
                    <div className={`flex-1 rounded-full transition-colors ${pwStrength >= 100 ? 'bg-emerald-500' : 'bg-border'}`} />
                  </div>
                  {errors.password && (
                    <p className="text-xs text-red-400 mt-1">{errors.password.message}</p>
                  )}
                </div>

                {/* Confirm Password */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-white" htmlFor="confirmPassword">
                    Confirm New Password
                  </label>
                  <input
                    id="confirmPassword"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
                    {...register('confirmPassword')}
                  />
                  {errors.confirmPassword && (
                    <p className="text-xs text-red-400 mt-1">{errors.confirmPassword.message}</p>
                  )}
                </div>

                <button
                  type="submit"
                  disabled={isLoading}
                  className="w-full mt-2 bg-primary text-primary-foreground font-medium rounded-lg py-2.5 shadow-[0_0_15px_rgba(37,99,235,0.3)] hover:shadow-[0_0_25px_rgba(37,99,235,0.5)] transition-all flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
                >
                  {isLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Reset Password'}
                </button>
              </form>
            </>
          ) : (
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              className="text-center py-6"
            >
              <div className="w-16 h-16 bg-emerald-500/20 rounded-full flex items-center justify-center mx-auto mb-4 border border-emerald-500/30">
                <CheckCircle2 className="w-8 h-8 text-emerald-500" />
              </div>
              <h2 className="text-xl font-semibold text-white mb-2">Password reset successful</h2>
              <p className="text-muted-foreground text-sm mb-6">
                Your password has been updated. Redirecting to login…
              </p>
              <Link
                href="/login"
                className="w-full inline-flex items-center justify-center bg-primary text-primary-foreground font-medium rounded-lg py-2.5"
              >
                Go to login now
              </Link>
            </motion.div>
          )}
        </div>
      </motion.div>
    </div>
  );
}
