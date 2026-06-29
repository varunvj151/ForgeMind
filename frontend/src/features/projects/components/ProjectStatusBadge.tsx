import { cn } from '@/shared/utils/cn';
import type { ProjectStatus } from '../types';

const statusConfig: Record<
  ProjectStatus,
  { label: string; className: string; dot: string }
> = {
  ACTIVE: {
    label: 'Active',
    className: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 ring-1 ring-emerald-500/20',
    dot: 'bg-emerald-500',
  },
  ON_HOLD: {
    label: 'On Hold',
    className: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 ring-1 ring-amber-500/20',
    dot: 'bg-amber-500',
  },
  COMPLETED: {
    label: 'Completed',
    className: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 ring-1 ring-blue-500/20',
    dot: 'bg-blue-500',
  },
  ARCHIVED: {
    label: 'Archived',
    className: 'bg-muted text-muted-foreground ring-1 ring-border',
    dot: 'bg-muted-foreground',
  },
};

interface ProjectStatusBadgeProps {
  status: ProjectStatus;
  className?: string;
}

export function ProjectStatusBadge({ status, className }: ProjectStatusBadgeProps) {
  const config = statusConfig[status];
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium',
        config.className,
        className,
      )}
    >
      <span className={cn('h-1.5 w-1.5 rounded-full', config.dot)} />
      {config.label}
    </span>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export { statusConfig };
