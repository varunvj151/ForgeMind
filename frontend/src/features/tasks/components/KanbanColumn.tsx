import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Plus } from 'lucide-react';
import { cn } from '@/shared/utils/cn';
import type { Task, TaskStatus } from '../types';
import { TaskCard } from './TaskCard';

const COLUMN_META: Record<
  TaskStatus,
  { label: string; color: string; headerBg: string; dotColor: string }
> = {
  TODO: {
    label: 'To Do',
    color: 'text-slate-400',
    headerBg: 'bg-slate-400/10',
    dotColor: 'bg-slate-400',
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: 'text-blue-400',
    headerBg: 'bg-blue-400/10',
    dotColor: 'bg-blue-400',
  },
  IN_REVIEW: {
    label: 'In Review',
    color: 'text-violet-400',
    headerBg: 'bg-violet-400/10',
    dotColor: 'bg-violet-400',
  },
  DONE: {
    label: 'Done',
    color: 'text-emerald-400',
    headerBg: 'bg-emerald-400/10',
    dotColor: 'bg-emerald-400',
  },
  BLOCKED: {
    label: 'Blocked',
    color: 'text-red-400',
    headerBg: 'bg-red-400/10',
    dotColor: 'bg-red-400',
  },
};

interface KanbanColumnProps {
  status: TaskStatus;
  tasks: Task[];
  onTaskClick: (task: Task) => void;
  onAddTask: (status: TaskStatus) => void;
  isOver?: boolean;
}

export function KanbanColumn({
  status,
  tasks,
  onTaskClick,
  onAddTask,
  isOver,
}: KanbanColumnProps) {
  const { setNodeRef, isOver: droppableIsOver } = useDroppable({ id: status });
  const meta = COLUMN_META[status];
  const hovering = isOver || droppableIsOver;

  return (
    <div
      className={cn(
        'flex h-full min-w-[280px] max-w-[320px] flex-col rounded-xl border border-border bg-card/60 transition-all duration-200',
        hovering && 'border-violet-500/40 bg-violet-500/5 shadow-lg shadow-violet-500/10'
      )}
    >
      {/* Column header */}
      <div className="flex items-center justify-between p-3.5">
        <div className="flex items-center gap-2">
          <span className={cn('h-2 w-2 rounded-full', meta.dotColor)} />
          <span className={cn('text-sm font-semibold', meta.color)}>{meta.label}</span>
          <span
            className={cn(
              'rounded-full px-1.5 py-0.5 text-xs font-medium',
              meta.headerBg,
              meta.color
            )}
          >
            {tasks.length}
          </span>
        </div>

        <button
          onClick={() => onAddTask(status)}
          className="rounded-md p-1 text-muted-foreground transition hover:bg-muted hover:text-foreground"
          aria-label={`Add task to ${meta.label}`}
        >
          <Plus className="h-4 w-4" />
        </button>
      </div>

      {/* Drop zone + scrollable task list */}
      <div
        ref={setNodeRef}
        className={cn(
          'flex-1 overflow-y-auto p-3.5 pt-0 space-y-2.5',
          'scrollbar-thin scrollbar-thumb-border scrollbar-track-transparent'
        )}
      >
        <SortableContext
          items={tasks.map((t) => t.id)}
          strategy={verticalListSortingStrategy}
        >
          {tasks.map((task) => (
            <TaskCard key={task.id} task={task} onClick={onTaskClick} />
          ))}
        </SortableContext>

        {/* Empty state */}
        {tasks.length === 0 && (
          <div
            className={cn(
              'flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-border/50 p-6 text-center transition-colors',
              hovering && 'border-violet-500/40 bg-violet-500/5'
            )}
          >
            <p className="text-xs text-muted-foreground">
              {hovering ? 'Drop here' : 'No tasks'}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
