import { useState, useCallback, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Plus,
  Search,
  Kanban,
  Loader2,
  AlertTriangle,
} from 'lucide-react';
import { cn } from '@/shared/utils/cn';
import { useProjectTasksQuery, useTaskWebSocket, KANBAN_STATUSES } from '../hooks/useTasks';
import { KanbanBoard } from '../components/KanbanBoard';
import { CreateTaskDialog } from '../components/CreateTaskDialog';
import { TaskDetailDrawer } from '../components/TaskDetailDrawer';
import type { Task, TaskStatus, TaskPriority } from '../types';

const PRIORITY_FILTERS: { value: TaskPriority | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'All priorities' },
  { value: 'CRITICAL', label: 'Critical' },
  { value: 'HIGH', label: 'High' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'LOW', label: 'Low' },
];

export function ProjectBoardPage() {
  const { id: projectId = '' } = useParams<{ id: string }>();

  const { data, isLoading, isError, error } = useProjectTasksQuery(projectId);

  // WebSocket real-time updates
  useTaskWebSocket(projectId);

  // UI State
  const [selectedTaskId, setSelectedTaskId] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [createDefaultStatus, setCreateDefaultStatus] = useState<TaskStatus>('TODO');
  const [search, setSearch] = useState('');
  const [priorityFilter, setPriorityFilter] = useState<TaskPriority | 'ALL'>('ALL');

  const handleTaskClick = useCallback((task: Task) => {
    setSelectedTaskId(task.id);
  }, []);

  const handleAddTask = useCallback((status: TaskStatus) => {
    setCreateDefaultStatus(status);
    setCreateOpen(true);
  }, []);

  // Filter tasks before passing to board
  const filteredTasks = useMemo(() => {
    const tasks = data?.content ?? [];
    return tasks.filter((t) => {
      const matchesSearch =
        !search ||
        t.title.toLowerCase().includes(search.toLowerCase()) ||
        t.description?.toLowerCase().includes(search.toLowerCase());
      const matchesPriority = priorityFilter === 'ALL' || t.priority === priorityFilter;
      return matchesSearch && matchesPriority;
    });
  }, [data, search, priorityFilter]);

  const totalCount = data?.totalElements ?? 0;
  const columnCounts = useMemo(() => {
    const tasks = data?.content ?? [];
    return Object.fromEntries(
      KANBAN_STATUSES.map((s) => [s, tasks.filter((t) => t.status === s).length])
    );
  }, [data]);

  if (isError) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-3 p-8 text-center">
        <AlertTriangle className="h-10 w-10 text-red-400" />
        <p className="text-foreground font-semibold">Failed to load tasks</p>
        <p className="text-sm text-muted-foreground">
          {(error as Error)?.message ?? 'Unknown error'}
        </p>
      </div>
    );
  }

  return (
    <div className="flex h-full flex-col">
      {/* ── Toolbar ─────────────────────────────────────────────── */}
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border px-4 py-3">
        <div className="flex items-center gap-3">
          <Link
            to={`/projects/${projectId}`}
            className="flex items-center gap-1.5 text-sm text-muted-foreground transition hover:text-foreground"
          >
            <ArrowLeft className="h-4 w-4" />
            Project
          </Link>
          <div className="flex items-center gap-2">
            <Kanban className="h-4 w-4 text-violet-500" />
            <h1 className="text-sm font-semibold text-foreground">Kanban Board</h1>
            <span className="rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground">
              {totalCount} tasks
            </span>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {/* Search */}
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search tasks…"
              className={cn(
                'h-8 w-44 rounded-lg border border-border bg-background pl-8 pr-3 text-sm',
                'text-foreground placeholder:text-muted-foreground',
                'focus:border-violet-500 focus:outline-none focus:ring-1 focus:ring-violet-500/30'
              )}
            />
          </div>

          {/* Priority filter */}
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value as TaskPriority | 'ALL')}
            className={cn(
              'h-8 rounded-lg border border-border bg-background px-2 text-xs text-foreground',
              'focus:border-violet-500 focus:outline-none'
            )}
          >
            {PRIORITY_FILTERS.map((f) => (
              <option key={f.value} value={f.value}>{f.label}</option>
            ))}
          </select>

          {/* New Task button */}
          <button
            id="board-new-task-btn"
            onClick={() => { setCreateDefaultStatus('TODO'); setCreateOpen(true); }}
            className="flex h-8 items-center gap-1.5 rounded-lg bg-violet-600 px-3 text-sm font-medium text-white transition hover:bg-violet-700 active:scale-95"
          >
            <Plus className="h-4 w-4" />
            New Task
          </button>
        </div>
      </div>

      {/* ── Column summary pills ────────────────────────────────── */}
      <div className="flex items-center gap-2 border-b border-border px-4 py-2 overflow-x-auto">
        {KANBAN_STATUSES.map((s) => (
          <span key={s} className="text-xs text-muted-foreground whitespace-nowrap">
            <span className="font-medium text-foreground">{columnCounts[s]}</span>{' '}
            {s.replace('_', ' ').toLowerCase()}
          </span>
        ))}
      </div>

      {/* ── Board area ─────────────────────────────────────────── */}
      <div className="flex-1 overflow-hidden p-4">
        {isLoading ? (
          <div className="flex h-full items-center justify-center gap-3 text-muted-foreground">
            <Loader2 className="h-6 w-6 animate-spin" />
            <span className="text-sm">Loading board…</span>
          </div>
        ) : (
          <KanbanBoard
            projectId={projectId}
            tasks={filteredTasks}
            onTaskClick={handleTaskClick}
            onAddTask={handleAddTask}
          />
        )}
      </div>

      {/* ── Dialogs & Drawers ───────────────────────────────────── */}
      <CreateTaskDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        projectId={projectId}
        defaultStatus={createDefaultStatus}
      />

      <TaskDetailDrawer
        taskId={selectedTaskId}
        projectId={projectId}
        onClose={() => setSelectedTaskId(null)}
      />
    </div>
  );
}
