import React from 'react';
import { Loader2 } from 'lucide-react';

export default function Loader({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const sizes = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  return (
    <div className="flex items-center justify-center w-full h-full min-h-[100px]">
      <Loader2 className={`${sizes[size]} text-primary animate-spin`} />
    </div>
  );
}
