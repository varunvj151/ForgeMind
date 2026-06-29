import { cn } from '@/shared/utils/cn';
import type { TaskPriority } from '../types';

const config: Record<
  TaskPriority,
  { label: string; classes: string; dotColor: string }
> = {
  LOW: {
    label: 'Low',
    classes: 'text-slate-400 bg-slate-400/10',
    dotColor: 'bg-slate-400',
  },
  MEDIUM: {
    label: 'Medium',
    classes: 'text-blue-400 bg-blue-400/10',
    dotColor: 'bg-blue-400',
  },
  HIGH: {
    label: 'High',
    classes: 'text-amber-400 bg-amber-400/10',
    dotColor: 'bg-amber-400',
  },
  CRITICAL: {
    label: 'Critical',
    classes: 'text-red-400 bg-red-400/10',
    dotColor: 'bg-red-400',
  },
};

interface TaskPriorityBadgeProps {
  priority: TaskPriority;
  showLabel?: boolean;
  className?: string;
}

export function TaskPriorityBadge({
  priority,
  showLabel = true,
  className,
}: TaskPriorityBadgeProps) {
  const { label, classes, dotColor } = config[priority];

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full px-2 py-0.5 text-xs font-medium',
        classes,
        className
      )}
    >
      <span className={cn('h-1.5 w-1.5 rounded-full', dotColor)} />
      {showLabel && label}
    </span>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export { config as priorityConfig };
