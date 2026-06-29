import { useEffect, useRef } from 'react';
import { formatDistanceToNow, format, isPast, isToday } from 'date-fns';
import {
  X,
  Calendar,
  AlertCircle,
  User,
  Tag,
  Loader2,
  Trash2,
  Clock,
} from 'lucide-react';
import { cn } from '@/shared/utils/cn';
import { Select } from '@/shared/components/ui/Select';
import {
  useTaskQuery,
  useChangeStatusMutation,
  useChangePriorityMutation,
  useDeleteTaskMutation,
} from '../hooks/useTasks';
import { TaskPriorityBadge } from './TaskPriorityBadge';
import type { TaskStatus, TaskPriority } from '../types';

const STATUS_OPTIONS: { value: TaskStatus; label: string }[] = [
  { value: 'TODO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'IN_REVIEW', label: 'In Review' },
  { value: 'DONE', label: 'Done' },
  { value: 'BLOCKED', label: 'Blocked' },
];

const PRIORITY_OPTIONS: { value: TaskPriority; label: string }[] = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'CRITICAL', label: 'Critical' },
];

interface TaskDetailDrawerProps {
  taskId: string | null;
  projectId: string;
  onClose: () => void;
}

function MetaRow({ icon: Icon, label, children }: {
  icon: React.ElementType;
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex items-start gap-3 py-2.5 border-b border-border/50 last:border-0">
      <div className="flex items-center gap-2 w-28 shrink-0 text-muted-foreground">
        <Icon className="h-3.5 w-3.5" />
        <span className="text-xs font-medium">{label}</span>
      </div>
      <div className="flex-1 text-sm">{children}</div>
    </div>
  );
}

function initials(first: string, last: string) {
  return `${first[0] ?? ''}${last[0] ?? ''}`.toUpperCase();
}

export function TaskDetailDrawer({ taskId, projectId, onClose }: TaskDetailDrawerProps) {
  const { data: task, isLoading } = useTaskQuery(taskId);
  const drawerRef = useRef<HTMLDivElement>(null);

  const changeStatus = useChangeStatusMutation(projectId);
  const changePriority = useChangePriorityMutation(projectId);
  const deleteTask = useDeleteTaskMutation(projectId);

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [onClose]);

  // Focus trap
  useEffect(() => {
    if (taskId) drawerRef.current?.focus();
  }, [taskId]);

  const isOpen = !!taskId;

  const handleDelete = async () => {
    if (!task) return;
    if (!confirm(`Delete "${task.title}"? This cannot be undone.`)) return;
    await deleteTask.mutateAsync(task.id);
    onClose();
  };

  const dueDate = task?.dueDate ? new Date(task.dueDate) : null;
  const isOverdue = dueDate && isPast(dueDate) && task?.status !== 'DONE';
  const isDueToday = dueDate && isToday(dueDate);

  return (
    <>
      {/* Backdrop */}
      <div
        className={cn(
          'fixed inset-0 z-40 bg-black/40 backdrop-blur-sm transition-opacity duration-300',
          isOpen ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
        )}
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Drawer panel */}
      <div
        ref={drawerRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-label="Task Details"
        className={cn(
          'fixed right-0 top-0 z-50 flex h-full w-full max-w-md flex-col',
          'border-l border-border bg-card shadow-2xl shadow-black/30',
          'transform transition-transform duration-300 ease-in-out focus:outline-none',
          isOpen ? 'translate-x-0' : 'translate-x-full'
        )}
      >
        {/* Header */}
        <div className="flex items-start justify-between gap-3 border-b border-border p-5">
          <div className="flex-1 min-w-0">
            {isLoading ? (
              <div className="h-5 w-3/4 animate-pulse rounded bg-muted" />
            ) : (
              <h2 className="text-base font-semibold text-foreground leading-snug">
                {task?.title}
              </h2>
            )}
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <button
              onClick={handleDelete}
              disabled={deleteTask.isPending || !task}
              className="rounded-md p-1.5 text-muted-foreground transition hover:bg-red-500/10 hover:text-red-400"
              aria-label="Delete task"
            >
              {deleteTask.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Trash2 className="h-4 w-4" />
              )}
            </button>
            <button
              onClick={onClose}
              className="rounded-md p-1.5 text-muted-foreground transition hover:bg-muted hover:text-foreground"
              aria-label="Close"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-6">
          {isLoading ? (
            <div className="space-y-3">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="h-8 animate-pulse rounded-lg bg-muted" />
              ))}
            </div>
          ) : task ? (
            <>
              {/* Description */}
              {task.description ? (
                <div>
                  <p className="mb-1.5 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                    Description
                  </p>
                  <p className="text-sm leading-relaxed text-foreground/90 whitespace-pre-wrap">
                    {task.description}
                  </p>
                </div>
              ) : (
                <p className="text-xs text-muted-foreground italic">No description provided.</p>
              )}

              {/* Meta fields */}
              <div className="rounded-xl border border-border bg-background/50 p-4">
                {/* Status */}
                <MetaRow icon={AlertCircle} label="Status">
                  <Select
                    value={task.status}
                    onChange={(e) =>
                      changeStatus.mutate({ taskId: task.id, req: { status: e.target.value as TaskStatus } })
                    }
                    className="h-7 py-0 text-xs"
                  >
                    {STATUS_OPTIONS.map((o) => (
                      <option key={o.value} value={o.value}>{o.label}</option>
                    ))}
                  </Select>
                </MetaRow>

                {/* Priority */}
                <MetaRow icon={Tag} label="Priority">
                  <div className="flex items-center gap-2">
                    <TaskPriorityBadge priority={task.priority} />
                    <Select
                      value={task.priority}
                      onChange={(e) =>
                        changePriority.mutate({ taskId: task.id, req: { priority: e.target.value as TaskPriority } })
                      }
                      className="h-7 py-0 text-xs flex-1"
                    >
                      {PRIORITY_OPTIONS.map((o) => (
                        <option key={o.value} value={o.value}>{o.label}</option>
                      ))}
                    </Select>
                  </div>
                </MetaRow>

                {/* Assignee */}
                <MetaRow icon={User} label="Assignee">
                  {task.assignee ? (
                    <span className="flex items-center gap-2 text-sm">
                      <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-violet-500 text-[10px] font-bold text-white">
                        {initials(task.assignee.firstName, task.assignee.lastName)}
                      </span>
                      {task.assignee.firstName} {task.assignee.lastName}
                    </span>
                  ) : (
                    <span className="text-xs text-muted-foreground">Unassigned</span>
                  )}
                </MetaRow>

                {/* Created By */}
                <MetaRow icon={User} label="Created By">
                  <span className="flex items-center gap-2 text-sm">
                    <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-emerald-500 text-[10px] font-bold text-white">
                      {initials(task.createdBy.firstName, task.createdBy.lastName)}
                    </span>
                    {task.createdBy.firstName} {task.createdBy.lastName}
                  </span>
                </MetaRow>

                {/* Due Date */}
                <MetaRow icon={Calendar} label="Due Date">
                  {dueDate ? (
                    <span
                      className={cn(
                        'text-sm',
                        isOverdue ? 'text-red-400 font-medium' : isDueToday ? 'text-amber-400 font-medium' : 'text-foreground'
                      )}
                    >
                      {format(dueDate, 'MMM d, yyyy')}
                      {isOverdue && ' · Overdue'}
                      {isDueToday && ' · Today'}
                    </span>
                  ) : (
                    <span className="text-xs text-muted-foreground">No due date</span>
                  )}
                </MetaRow>

                {/* Timestamps */}
                <MetaRow icon={Clock} label="Updated">
                  <span className="text-xs text-muted-foreground">
                    {formatDistanceToNow(new Date(task.updatedAt), { addSuffix: true })}
                  </span>
                </MetaRow>
              </div>

              {/* Activity placeholder */}
              <div>
                <p className="mb-3 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Activity
                </p>
                <div className="rounded-xl border border-border bg-background/50 px-4 py-8 text-center">
                  <p className="text-xs text-muted-foreground">
                    Activity history coming soon.
                  </p>
                </div>
              </div>
            </>
          ) : null}
        </div>
      </div>
    </>
  );
}
