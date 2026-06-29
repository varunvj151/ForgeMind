import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { formatDistanceToNow } from 'date-fns';
import { CheckSquare, ArrowRight, Loader2 } from 'lucide-react';
import { apiClient } from '@/shared/api/axios';
import { TaskPriorityBadge } from '../components/TaskPriorityBadge';
import type { Task, Page } from '../types';

function useMyTasksQuery() {
  return useQuery({
    queryKey: ['tasks', 'my'],
    queryFn: async () => {
      const { data } = await apiClient.get<Page<Task>>('/api/v1/tasks/my', {
        params: { size: 50, sort: 'dueDate,asc' },
      });
      return data;
    },
    staleTime: 60_000,
  });
}

const STATUS_LABEL: Record<string, string> = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  IN_REVIEW: 'In Review',
  DONE: 'Done',
  BLOCKED: 'Blocked',
};

const STATUS_COLOR: Record<string, string> = {
  TODO: 'bg-slate-400/10 text-slate-400',
  IN_PROGRESS: 'bg-blue-400/10 text-blue-400',
  IN_REVIEW: 'bg-violet-400/10 text-violet-400',
  DONE: 'bg-emerald-400/10 text-emerald-400',
  BLOCKED: 'bg-red-400/10 text-red-400',
};

export function TasksPage() {
  const { data, isLoading, isError } = useMyTasksQuery();

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">My Tasks</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Tasks assigned to you across all projects.
          </p>
        </div>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-20 text-muted-foreground gap-2">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span className="text-sm">Loading your tasks…</span>
        </div>
      ) : isError ? (
        <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <p className="text-sm">Could not load tasks. Please try again.</p>
        </div>
      ) : !data || data.empty ? (
        <div className="flex flex-col items-center justify-center py-20 gap-4 text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-violet-500/10">
            <CheckSquare className="h-8 w-8 text-violet-500" />
          </div>
          <div>
            <p className="font-medium text-foreground">No tasks assigned to you</p>
            <p className="mt-1 text-sm text-muted-foreground">Head over to a project board and get started.</p>
          </div>
          <Link
            to="/projects"
            className="flex items-center gap-1.5 rounded-lg bg-violet-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-violet-700"
          >
            View Projects <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
      ) : (
        <div className="rounded-xl border border-border overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-muted/30">
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground">Task</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground">Project</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground">Priority</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground">Status</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground">Updated</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {data.content.map((task: Task) => (
                <tr key={task.id} className="group transition hover:bg-muted/30">
                  <td className="px-4 py-3 font-medium text-foreground max-w-xs">
                    <p className="truncate">{task.title}</p>
                    {task.description && (
                      <p className="mt-0.5 truncate text-xs text-muted-foreground">{task.description}</p>
                    )}
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">
                    <Link
                      to={`/projects/${task.projectId}`}
                      className="text-xs hover:text-violet-400 transition"
                    >
                      {task.projectName}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <TaskPriorityBadge priority={task.priority} />
                  </td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLOR[task.status]}`}>
                      {STATUS_LABEL[task.status]}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-muted-foreground">
                    {formatDistanceToNow(new Date(task.updatedAt), { addSuffix: true })}
                  </td>
                  <td className="px-4 py-3">
                    <Link
                      to={`/projects/${task.projectId}/board`}
                      className="flex items-center gap-1 text-xs text-violet-500 opacity-0 transition group-hover:opacity-100 hover:text-violet-400"
                    >
                      Board <ArrowRight className="h-3 w-3" />
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
