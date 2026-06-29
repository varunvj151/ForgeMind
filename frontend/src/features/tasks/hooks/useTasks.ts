import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useCallback } from 'react';
import type { IMessage } from '@stomp/stompjs';
import { useStompSubscription } from '@/app/providers/WebSocketProvider';
import {
  getProjectTasks,
  getTask,
  createTask,
  updateTask,
  deleteTask,
  changeStatus,
  changePriority,
  assignUser,
} from '../api/task';
import type {
  Task,
  TaskStatus,
  CreateTaskRequest,
  UpdateTaskRequest,
  UpdateTaskStatusRequest,
  UpdateTaskPriorityRequest,
  UpdateTaskAssigneeRequest,
  Page,
} from '../types';

// ─── Query Keys ───────────────────────────────────────────────────────────────
export const taskKeys = {
  all: ['tasks'] as const,
  lists: () => [...taskKeys.all, 'list'] as const,
  list: (projectId: string) => [...taskKeys.lists(), projectId] as const,
  details: () => [...taskKeys.all, 'detail'] as const,
  detail: (taskId: string) => [...taskKeys.details(), taskId] as const,
};

// ─── List query ───────────────────────────────────────────────────────────────
export function useProjectTasksQuery(projectId: string) {
  return useQuery({
    queryKey: taskKeys.list(projectId),
    queryFn: () => getProjectTasks(projectId),
    enabled: !!projectId,
    staleTime: 30_000,
  });
}

// ─── Single task ──────────────────────────────────────────────────────────────
export function useTaskQuery(taskId: string | null) {
  return useQuery({
    queryKey: taskKeys.detail(taskId ?? ''),
    queryFn: () => getTask(taskId!),
    enabled: !!taskId,
    staleTime: 30_000,
  });
}

// ─── Create ───────────────────────────────────────────────────────────────────
export function useCreateTaskMutation(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: CreateTaskRequest) => createTask(projectId, req),
    onSuccess: (created) => {
      queryClient.setQueryData(taskKeys.list(projectId), (old: Page<Task> | undefined) => {
        if (!old) return old;
        return { ...old, content: [...old.content, created], totalElements: old.totalElements + 1 };
      });
    },
  });
}

// ─── Update ───────────────────────────────────────────────────────────────────
export function useUpdateTaskMutation(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, req }: { taskId: string; req: UpdateTaskRequest }) =>
      updateTask(taskId, req),
    onSuccess: (updated) => {
      _updateTaskInCache(queryClient, projectId, updated);
    },
  });
}

// ─── Delete ───────────────────────────────────────────────────────────────────
export function useDeleteTaskMutation(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (taskId: string) => deleteTask(taskId),
    onSuccess: (_, taskId) => {
      queryClient.setQueryData(taskKeys.list(projectId), (old: Page<Task> | undefined) => {
        if (!old) return old;
        return {
          ...old,
          content: old.content.filter((t: Task) => t.id !== taskId),
          totalElements: old.totalElements - 1,
        };
      });
      queryClient.removeQueries({ queryKey: taskKeys.detail(taskId) });
    },
  });
}

// ─── Change Status (Optimistic) ──────────────────────────────────────────────
export function useChangeStatusMutation(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, req }: { taskId: string; req: UpdateTaskStatusRequest }) =>
      changeStatus(taskId, req),
    // Optimistic update
    onMutate: async ({ taskId, req }) => {
      await queryClient.cancelQueries({ queryKey: taskKeys.list(projectId) });
      const snapshot = queryClient.getQueryData(taskKeys.list(projectId));
      queryClient.setQueryData(taskKeys.list(projectId), (old: Page<Task> | undefined) => {
        if (!old) return old;
        return {
          ...old,
          content: old.content.map((t: Task) =>
            t.id === taskId ? { ...t, status: req.status } : t
          ),
        };
      });
      return { snapshot };
    },
    onError: (_err, _vars, context) => {
      // Rollback
      if (context?.snapshot) {
        queryClient.setQueryData(taskKeys.list(projectId), context.snapshot);
      }
    },
    onSuccess: (updated) => {
      _updateTaskInCache(queryClient, projectId, updated);
    },
  });
}

// ─── Change Priority ─────────────────────────────────────────────────────────
export function useChangePriorityMutation(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, req }: { taskId: string; req: UpdateTaskPriorityRequest }) =>
      changePriority(taskId, req),
    onSuccess: (updated) => {
      _updateTaskInCache(queryClient, projectId, updated);
    },
  });
}

// ─── Assign User ─────────────────────────────────────────────────────────────
export function useAssignUserMutation(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, req }: { taskId: string; req: UpdateTaskAssigneeRequest }) =>
      assignUser(taskId, req),
    onSuccess: (updated) => {
      _updateTaskInCache(queryClient, projectId, updated);
    },
  });
}

// ─── WebSocket subscription ───────────────────────────────────────────────────
export function useTaskWebSocket(projectId: string) {
  const queryClient = useQueryClient();

  const handleMessage = useCallback(
    (msg: IMessage) => {
      try {
        const payload = JSON.parse(msg.body) as { type: string; task?: Task; taskId?: string };
        if (payload.type === 'TASK_DELETED' && payload.taskId) {
          queryClient.setQueryData(taskKeys.list(projectId), (old: Page<Task> | undefined) => {
            if (!old) return old;
            return {
              ...old,
              content: old.content.filter((t: Task) => t.id !== payload.taskId),
            };
          });
        } else if (payload.task) {
          const task = payload.task;
          queryClient.setQueryData(taskKeys.list(projectId), (old: Page<Task> | undefined) => {
            if (!old) return old;
            const exists = old.content.some((t: Task) => t.id === task.id);
            return {
              ...old,
              content: exists
                ? old.content.map((t: Task) => (t.id === task.id ? task : t))
                : [...old.content, task],
            };
          });
          queryClient.setQueryData(taskKeys.detail(task.id), task);
        }
      } catch {
        // Ignore malformed messages
      }
    },
    [queryClient, projectId]
  );

  useStompSubscription(`/topic/tasks/${projectId}`, handleMessage, !!projectId);
}

// ─── Helper ───────────────────────────────────────────────────────────────────
function _updateTaskInCache(queryClient: ReturnType<typeof useQueryClient>, projectId: string, updated: Task) {
  queryClient.setQueryData(taskKeys.list(projectId), (old: Page<Task> | undefined) => {
    if (!old) return old;
    return {
      ...old,
      content: old.content.map((t: Task) => (t.id === updated.id ? updated : t)),
    };
  });
  queryClient.setQueryData(taskKeys.detail(updated.id), updated);
}

// ─── Derive Kanban columns from flat list ─────────────────────────────────────
export const KANBAN_STATUSES: TaskStatus[] = [
  'TODO',
  'IN_PROGRESS',
  'IN_REVIEW',
  'DONE',
  'BLOCKED',
];

export function groupTasksByStatus(tasks: Task[]): Record<TaskStatus, Task[]> {
  const groups = Object.fromEntries(
    KANBAN_STATUSES.map((s) => [s, [] as Task[]])
  ) as Record<TaskStatus, Task[]>;

  for (const task of tasks) {
    if (groups[task.status]) {
      groups[task.status].push(task);
    }
  }
  return groups;
}
