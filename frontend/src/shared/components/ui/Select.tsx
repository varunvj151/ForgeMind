import type { SelectHTMLAttributes } from 'react';
import { cn } from '@/shared/utils/cn';

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  error?: string;
}

export function Select({ className, error, children, ...props }: SelectProps) {
  return (
    <div className="w-full">
      <select
        className={cn(
          'w-full appearance-none rounded-lg border px-3 py-2 text-sm text-foreground',
          'bg-background transition-colors cursor-pointer',
          'focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500',
          error
            ? 'border-red-500 focus:ring-red-500/50 focus:border-red-500'
            : 'border-border',
          className,
        )}
        {...props}
      >
        {children}
      </select>
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  );
}
