import type { InputHTMLAttributes } from 'react';
import { cn } from '@/shared/utils/cn';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string;
}

export function Input({ className, error, ...props }: InputProps) {
  return (
    <div className="w-full">
      <input
        className={cn(
          'w-full rounded-lg border px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground',
          'bg-background transition-colors',
          'focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500',
          error
            ? 'border-red-500 focus:ring-red-500/50 focus:border-red-500'
            : 'border-border',
          className,
        )}
        {...props}
      />
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  );
}
