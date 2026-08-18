import React from 'react';
import { AlertTriangle, X } from 'lucide-react';

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
  isLoading?: boolean;
  variant?: 'danger' | 'warning';
}

export default function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  onConfirm,
  onCancel,
  isLoading = false,
  variant = 'danger',
}: ConfirmDialogProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onCancel} />
      <div className="relative bg-[#111827] border border-border rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl">
        <button
          onClick={onCancel}
          className="absolute top-4 right-4 text-muted-foreground hover:text-white transition-colors"
        >
          <X className="w-4 h-4" />
        </button>

        <div className="flex items-start gap-4 mb-5">
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${
            variant === 'danger' ? 'bg-red-500/10' : 'bg-amber-500/10'
          }`}>
            <AlertTriangle className={`w-5 h-5 ${variant === 'danger' ? 'text-red-400' : 'text-amber-400'}`} />
          </div>
          <div>
            <h3 className="text-base font-semibold text-white mb-1">{title}</h3>
            <p className="text-sm text-muted-foreground leading-relaxed">{message}</p>
          </div>
        </div>

        <div className="flex items-center gap-3 justify-end">
          <button
            onClick={onCancel}
            disabled={isLoading}
            className="px-4 py-2 rounded-lg text-sm font-medium text-muted-foreground border border-border hover:border-white/30 hover:text-white transition-colors disabled:opacity-50"
          >
            {cancelLabel}
          </button>
          <button
            onClick={onConfirm}
            disabled={isLoading}
            className={`px-4 py-2 rounded-lg text-sm font-medium text-white transition-colors disabled:opacity-50 ${
              variant === 'danger'
                ? 'bg-red-500 hover:bg-red-600 shadow-lg shadow-red-500/20'
                : 'bg-amber-500 hover:bg-amber-600'
            }`}
          >
            {isLoading ? 'Processing...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
