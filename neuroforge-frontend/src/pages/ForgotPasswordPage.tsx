import React, { useState, useRef } from 'react';
import { Link, useLocation } from 'wouter';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowLeft, Mail, Loader2, ShieldCheck, AlertCircle } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { authService } from '@/services/authService';

const emailSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
});
type EmailFormValues = z.infer<typeof emailSchema>;

export default function ForgotPasswordPage() {
  const [, navigate] = useLocation();
  const [step, setStep]                     = useState<'email' | 'code'>('email');
  const [submittedEmail, setSubmittedEmail] = useState('');
  const [isLoading, setIsLoading]           = useState(false);
  const [isVerifying, setIsVerifying]       = useState(false);
  const [codeError, setCodeError]           = useState('');
  const [apiError, setApiError]             = useState('');
  const [digits, setDigits]                 = useState(['', '', '', '', '', '']);
  const inputRefs                           = useRef<(HTMLInputElement | null)[]>([]);

  const { register, handleSubmit, formState: { errors } } = useForm<EmailFormValues>({
    resolver: zodResolver(emailSchema),
    defaultValues: { email: '' },
  });

  // ── Step 1: PATCH /auth/forgot-password/send-otp?email= ─────────────────────
  const onSubmitEmail = async (data: EmailFormValues) => {
    setIsLoading(true);
    setApiError('');
    try {
      await authService.sendForgotPasswordOtp(data.email);
      setSubmittedEmail(data.email);
      setStep('code');
    } catch (err: any) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'No account found with that email address.';
      setApiError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDigitChange = (i: number, val: string) => {
    const cleaned = val.replace(/\D/g, '').slice(-1);
    const next = [...digits];
    next[i] = cleaned;
    setDigits(next);
    setCodeError('');
    if (cleaned && i < 5) inputRefs.current[i + 1]?.focus();
  };

  const handleDigitKeyDown = (i: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !digits[i] && i > 0) inputRefs.current[i - 1]?.focus();
  };

  const handleDigitPaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (!pasted) return;
    const next = [...digits];
    pasted.split('').forEach((c, i) => { next[i] = c; });
    setDigits(next);
    inputRefs.current[Math.min(pasted.length, 5)]?.focus();
  };

  // ── Step 2: Navigate to /reset-password carrying email + OTP ─────────────────
  // The backend requires email + otp + password all in one POST /auth/reset-password call.
  // We pass email and otp to ResetPasswordPage via history state.
  const handleVerifyCode = async () => {
    const otp = digits.join('');
    if (otp.length < 6) {
      setCodeError('Please enter all 6 digits.');
      return;
    }
    setIsVerifying(true);
    try {
      // Navigate to reset page — pass email + otp as state
      navigate('/reset-password', { state: { email: submittedEmail, otp } } as any);
    } finally {
      setIsVerifying(false);
    }
  };

  const handleResend = async () => {
    setDigits(['', '', '', '', '', '']);
    setCodeError('');
    setApiError('');
    try {
      await authService.sendForgotPasswordOtp(submittedEmail);
      inputRefs.current[0]?.focus();
    } catch {
      setApiError('Failed to resend. Please try again.');
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
          <Link href="/login" className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-white transition-colors mb-6">
            <ArrowLeft className="w-4 h-4" />
            Back to login
          </Link>

          {/* API error banner */}
          {apiError && (
            <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-3 mb-5 text-sm text-red-400">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              {apiError}
            </div>
          )}

          <AnimatePresence mode="wait">
            {/* ── Step 1: Enter email ────────────────────────────────────────── */}
            {step === 'email' ? (
              <motion.div
                key="email-step"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.3 }}
              >
                <h1 className="text-2xl font-semibold text-white mb-2">Reset Password</h1>
                <p className="text-muted-foreground text-sm mb-8">
                  Enter your email address and we'll send you a 6-digit verification code.
                </p>

                <form onSubmit={handleSubmit(onSubmitEmail)} className="space-y-5">
                  <div className="space-y-1.5">
                    <label className="text-sm font-medium text-white" htmlFor="email">
                      Email Address
                    </label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                      <input
                        id="email"
                        type="email"
                        placeholder="name@company.com"
                        className="w-full bg-background border border-border rounded-lg pl-10 pr-4 py-2.5 text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
                        {...register('email')}
                      />
                    </div>
                    {errors.email && (
                      <p className="text-xs text-red-400 mt-1">{errors.email.message}</p>
                    )}
                  </div>

                  <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-primary text-primary-foreground font-medium rounded-lg py-2.5 shadow-[0_0_15px_rgba(37,99,235,0.3)] hover:shadow-[0_0_25px_rgba(37,99,235,0.5)] transition-all flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
                  >
                    {isLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Send Code'}
                  </button>

                  <p className="text-center text-sm text-muted-foreground pt-1">
                    Remembered it?{' '}
                    <Link href="/login" className="text-primary hover:underline font-medium">
                      Back to sign in
                    </Link>
                  </p>
                </form>
              </motion.div>

            ) : (
              /* ── Step 2: Enter OTP ─────────────────────────────────────────── */
              <motion.div
                key="code-step"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.3 }}
              >
                <div className="w-12 h-12 bg-primary/10 border border-primary/30 rounded-xl flex items-center justify-center mb-5">
                  <ShieldCheck className="w-6 h-6 text-primary" />
                </div>

                <h1 className="text-2xl font-semibold text-white mb-2">Enter verification code</h1>
                <p className="text-muted-foreground text-sm mb-1">We sent a 6-digit code to</p>
                <p className="text-white text-sm font-medium mb-8 truncate">{submittedEmail}</p>

                <div className="flex gap-3 justify-between mb-3" onPaste={handleDigitPaste}>
                  {digits.map((digit, i) => (
                    <input
                      key={i}
                      ref={el => { inputRefs.current[i] = el; }}
                      type="text"
                      inputMode="numeric"
                      maxLength={1}
                      value={digit}
                      onChange={e => handleDigitChange(i, e.target.value)}
                      onKeyDown={e => handleDigitKeyDown(i, e)}
                      className={`w-12 h-14 text-center text-xl font-bold bg-background border rounded-xl text-white focus:outline-none transition-all
                        ${codeError ? 'border-red-500' : 'border-border focus:border-primary focus:ring-1 focus:ring-primary'}
                        ${digit ? 'border-primary/60 shadow-[0_0_8px_rgba(37,99,235,0.2)]' : ''}
                      `}
                    />
                  ))}
                </div>

                {codeError && <p className="text-xs text-red-400 mb-4">{codeError}</p>}

                <button
                  onClick={handleVerifyCode}
                  disabled={isVerifying || digits.join('').length < 6}
                  className="w-full bg-primary text-primary-foreground font-medium rounded-lg py-2.5 mt-2 shadow-[0_0_15px_rgba(37,99,235,0.3)] hover:shadow-[0_0_25px_rgba(37,99,235,0.5)] transition-all flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
                >
                  {isVerifying ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Continue'}
                </button>

                <p className="text-center text-sm text-muted-foreground mt-5">
                  Didn't receive a code?{' '}
                  <button onClick={handleResend} className="text-primary hover:underline font-medium">
                    Resend
                  </button>
                </p>

                <button
                  onClick={() => { setStep('email'); setApiError(''); }}
                  className="mt-3 w-full text-center text-sm text-muted-foreground hover:text-white transition-colors"
                >
                  Use a different email
                </button>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </motion.div>
    </div>
  );
}
