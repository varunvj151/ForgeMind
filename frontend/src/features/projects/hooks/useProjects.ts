import {
  useQuery,
  useMutation,
  useQueryClient,
  keepPreviousData,
} from '@tanstack/react-query';
import {
  getProjects,
  getProjectById,
  createProject,
  updateProject,
  deleteProject,
} from '../api/project';
import type { ProjectFilters, ProjectRequest } from '../types';

// ─── Query Keys ────────────────────────────────────────────────────────────────

export const projectKeys = {
  all: ['projects'] as const,
  lists: () => [...projectKeys.all, 'list'] as const,
  list: (filters: ProjectFilters) => [...projectKeys.lists(), filters] as const,
  details: () => [...projectKeys.all, 'detail'] as const,
  detail: (id: string) => [...projectKeys.details(), id] as const,
};

// ─── List ──────────────────────────────────────────────────────────────────────

export function useProjectsQuery(filters: ProjectFilters = {}) {
  return useQuery({
    queryKey: projectKeys.list(filters),
    queryFn: () => getProjects(filters),
    placeholderData: keepPreviousData,
    staleTime: 60_000,
  });
}

// ─── Single ────────────────────────────────────────────────────────────────────

export function useProjectQuery(id: string | undefined) {
  return useQuery({
    queryKey: projectKeys.detail(id ?? ''),
    queryFn: () => getProjectById(id!),
    enabled: !!id,
    staleTime: 60_000,
  });
}

// ─── Create ────────────────────────────────────────────────────────────────────

export function useCreateProjectMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: ProjectRequest) => createProject(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: projectKeys.lists() });
    },
  });
}

// ─── Update ────────────────────────────────────────────────────────────────────

export function useUpdateProjectMutation(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: ProjectRequest) => updateProject(id, request),
    onSuccess: (updated) => {
      queryClient.setQueryData(projectKeys.detail(id), updated);
      queryClient.invalidateQueries({ queryKey: projectKeys.lists() });
    },
  });
}

// ─── Delete ────────────────────────────────────────────────────────────────────

export function useDeleteProjectMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteProject(id),
    onSuccess: (_data, id) => {
      queryClient.removeQueries({ queryKey: projectKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: projectKeys.lists() });
    },
  });
}
