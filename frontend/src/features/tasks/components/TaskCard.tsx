import { memo } from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { formatDistanceToNow, isPast, isToday } from 'date-fns';
import { Calendar, MessageSquare, GripVertical } from 'lucide-react';
import { cn } from '@/shared/utils/cn';
import type { Task } from '../types';
import { TaskPriorityBadge } from './TaskPriorityBadge';

interface TaskCardProps {
  task: Task;
  onClick: (task: Task) => void;
  isDragging?: boolean;
}

function getInitials(firstName: string, lastName: string) {
  return `${firstName[0] ?? ''}${lastName[0] ?? ''}`.toUpperCase();
}

function AvatarCircle({ firstName, lastName }: { firstName: string; lastName: string }) {
  const colors = [
    'bg-violet-500',
    'bg-blue-500',
    'bg-emerald-500',
    'bg-amber-500',
    'bg-pink-500',
    'bg-cyan-500',
  ];
  const color = colors[(firstName.charCodeAt(0) ?? 0) % colors.length];
  return (
    <span
      className={cn(
        'inline-flex h-6 w-6 items-center justify-center rounded-full text-[10px] font-semibold text-white',
        color
      )}
      title={`${firstName} ${lastName}`}
    >
      {getInitials(firstName, lastName)}
    </span>
  );
}

export const TaskCard = memo(function TaskCard({ task, onClick, isDragging }: TaskCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isSorting,
  } = useSortable({ id: task.id, data: { task } });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  const hasDueDate = !!task.dueDate;
  const dueDate = hasDueDate ? new Date(task.dueDate!) : null;
  const isOverdue = dueDate ? isPast(dueDate) && task.status !== 'DONE' : false;
  const isDueToday = dueDate ? isToday(dueDate) : false;

  return (
    <div
      ref={setNodeRef}
      style={style}
      onClick={() => onClick(task)}
      className={cn(
        'group relative rounded-xl border border-border bg-card p-3.5 shadow-sm',
        'cursor-pointer select-none transition-all duration-150',
        'hover:border-violet-500/30 hover:shadow-md hover:shadow-violet-500/5',
        isDragging && 'opacity-40',
        isSorting && 'cursor-grabbing',
        task.status === 'DONE' && 'opacity-70'
      )}
    >
      {/* Drag handle */}
      <button
        {...attributes}
        {...listeners}
        onClick={(e) => e.stopPropagation()}
        className={cn(
          'absolute left-1 top-1/2 -translate-y-1/2 p-1 rounded',
          'text-muted-foreground/30 opacity-0 transition',
          'group-hover:opacity-100 hover:text-muted-foreground cursor-grab active:cursor-grabbing'
        )}
        aria-label="Drag task"
      >
        <GripVertical className="h-3.5 w-3.5" />
      </button>

      <div className="ml-3">
        {/* Priority badge */}
        <div className="mb-2">
          <TaskPriorityBadge priority={task.priority} />
        </div>

        {/* Title */}
        <p
          className={cn(
            'text-sm font-medium leading-snug text-foreground',
            task.status === 'DONE' && 'line-through text-muted-foreground'
          )}
        >
          {task.title}
        </p>

        {/* Description preview */}
        {task.description && (
          <p className="mt-1 line-clamp-2 text-xs text-muted-foreground">
            {task.description}
          </p>
        )}

        {/* Footer */}
        <div className="mt-3 flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            {/* Due date */}
            {dueDate && (
              <span
                className={cn(
                  'inline-flex items-center gap-1 rounded-md px-1.5 py-0.5 text-[10px] font-medium',
                  isOverdue
                    ? 'bg-red-500/10 text-red-400'
                    : isDueToday
                    ? 'bg-amber-500/10 text-amber-400'
                    : 'bg-muted text-muted-foreground'
                )}
              >
                <Calendar className="h-2.5 w-2.5" />
                {isOverdue
                  ? 'Overdue'
                  : isDueToday
                  ? 'Today'
                  : formatDistanceToNow(dueDate, { addSuffix: true })}
              </span>
            )}

            {/* Activity indicator */}
            <span className="inline-flex items-center gap-1 text-[10px] text-muted-foreground/60">
              <MessageSquare className="h-2.5 w-2.5" />
            </span>
          </div>

          {/* Assignee avatar */}
          {task.assignee && (
            <AvatarCircle
              firstName={task.assignee.firstName}
              lastName={task.assignee.lastName}
            />
          )}
        </div>
      </div>
    </div>
  );
});
