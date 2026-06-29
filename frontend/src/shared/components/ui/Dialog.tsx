import type { ReactNode, ButtonHTMLAttributes } from 'react';
import { cn } from '@/shared/utils/cn';

interface DialogProps {
  open: boolean;
  onClose: () => void;
  children: ReactNode;
}

export function Dialog({ open, onClose, children }: DialogProps) {
  if (!open) return null;
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      role="dialog"
      aria-modal="true"
    >
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden="true"
      />
      {/* Panel */}
      <div className="relative z-10 w-full max-w-lg mx-4 animate-in fade-in slide-in-from-bottom-4 duration-200">
        {children}
      </div>
    </div>
  );
}

interface DialogPanelProps { children: ReactNode; className?: string }
export function DialogPanel({ children, className }: DialogPanelProps) {
  return (
    <div className={cn('rounded-2xl border border-border bg-card shadow-2xl', className)}>
      {children}
    </div>
  );
}

interface DialogHeaderProps { children: ReactNode }
export function DialogHeader({ children }: DialogHeaderProps) {
  return <div className="flex items-start justify-between gap-4 p-6 pb-0">{children}</div>;
}

interface DialogTitleProps { children: ReactNode }
export function DialogTitle({ children }: DialogTitleProps) {
  return <h2 className="text-lg font-semibold text-foreground">{children}</h2>;
}

interface DialogBodyProps { children: ReactNode; className?: string }
export function DialogBody({ children, className }: DialogBodyProps) {
  return <div className={cn('p-6', className)}>{children}</div>;
}

interface DialogFooterProps { children: ReactNode }
export function DialogFooter({ children }: DialogFooterProps) {
  return (
    <div className="flex items-center justify-end gap-3 border-t border-border px-6 py-4">
      {children}
    </div>
  );
}

// ─── Close Button ──────────────────────────────────────────────────────────────
type CloseButtonProps = ButtonHTMLAttributes<HTMLButtonElement>;
export function DialogCloseButton(props: CloseButtonProps) {
  return (
    <button
      {...props}
      className={cn(
        'rounded-lg p-1.5 text-muted-foreground transition hover:bg-muted hover:text-foreground',
        props.className,
      )}
      aria-label="Close dialog"
    >
      <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
      </svg>
    </button>
  );
}
