import { apiClient } from '@/shared/api/axios';
import type {
  Page,
  Task,
  CreateTaskRequest,
  UpdateTaskRequest,
  UpdateTaskStatusRequest,
  UpdateTaskPriorityRequest,
  UpdateTaskAssigneeRequest,
} from '../types';

const PROJECTS_BASE = '/api/v1/projects';
const TASKS_BASE = '/api/v1/tasks';

// ─── Project-scoped ───────────────────────────────────────────────────────────

export async function getProjectTasks(projectId: string): Promise<Page<Task>> {
  const { data } = await apiClient.get<Page<Task>>(
    `${PROJECTS_BASE}/${projectId}/tasks`,
    { params: { size: 200, sort: 'createdAt,asc' } }
  );
  return data;
}

export async function createTask(projectId: string, request: CreateTaskRequest): Promise<Task> {
  const { data } = await apiClient.post<Task>(
    `${PROJECTS_BASE}/${projectId}/tasks`,
    request
  );
  return data;
}

// ─── Task-scoped ──────────────────────────────────────────────────────────────

export async function getTask(taskId: string): Promise<Task> {
  const { data } = await apiClient.get<Task>(`${TASKS_BASE}/${taskId}`);
  return data;
}

export async function updateTask(taskId: string, request: UpdateTaskRequest): Promise<Task> {
  const { data } = await apiClient.put<Task>(`${TASKS_BASE}/${taskId}`, request);
  return data;
}

export async function deleteTask(taskId: string): Promise<void> {
  await apiClient.delete(`${TASKS_BASE}/${taskId}`);
}

export async function changeStatus(
  taskId: string,
  request: UpdateTaskStatusRequest
): Promise<Task> {
  const { data } = await apiClient.patch<Task>(`${TASKS_BASE}/${taskId}/status`, request);
  return data;
}

export async function changePriority(
  taskId: string,
  request: UpdateTaskPriorityRequest
): Promise<Task> {
  const { data } = await apiClient.patch<Task>(`${TASKS_BASE}/${taskId}/priority`, request);
  return data;
}

export async function assignUser(
  taskId: string,
  request: UpdateTaskAssigneeRequest
): Promise<Task> {
  const { data } = await apiClient.patch<Task>(`${TASKS_BASE}/${taskId}/assignee`, request);
  return data;
}
