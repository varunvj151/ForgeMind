import { apiClient } from '@/shared/api/axios';
import type { Page, Project, ProjectFilters, ProjectRequest } from '../types';

// ─── Base URL ──────────────────────────────────────────────────────────────────
// Note: apiClient baseURL is /api/v1, but the project controller is at /api/projects.
// We override the base by providing the full path explicitly via Vite proxy.
const BASE = '/api/projects';

// ─── List (paginated + filtered) ──────────────────────────────────────────────
export async function getProjects(filters: ProjectFilters = {}): Promise<Page<Project>> {
  const {
    search,
    status,
    sort = 'createdAt',
    direction = 'desc',
    page = 0,
    size = 12,
  } = filters;

  const params: Record<string, string | number> = {
    page,
    size,
    sort: `${sort},${direction}`,
  };
  if (search) params['name'] = search;
  if (status) params['status'] = status;

  const { data } = await apiClient.get<Page<Project>>(BASE, { params });
  return data;
}

// ─── Get single project ────────────────────────────────────────────────────────
export async function getProjectById(id: string): Promise<Project> {
  const { data } = await apiClient.get<Project>(`${BASE}/${id}`);
  return data;
}

// ─── Create ────────────────────────────────────────────────────────────────────
export async function createProject(request: ProjectRequest): Promise<Project> {
  const { data } = await apiClient.post<Project>(BASE, request);
  return data;
}

// ─── Update ────────────────────────────────────────────────────────────────────
export async function updateProject(id: string, request: ProjectRequest): Promise<Project> {
  const { data } = await apiClient.put<Project>(`${BASE}/${id}`, request);
  return data;
}

// ─── Delete ────────────────────────────────────────────────────────────────────
export async function deleteProject(id: string): Promise<void> {
  await apiClient.delete(`${BASE}/${id}`);
}
