import { QueryClient } from '@tanstack/react-query';

/**
 * Shared React Query client instance.
 *
 * Configuration:
 * - staleTime: 1 minute — data is considered fresh for 60s before a background refetch
 * - gcTime: 5 minutes — unused cache entries are garbage-collected after 5 minutes
 * - retry: 2 — failed queries are retried up to 2 times before showing an error
 * - refetchOnWindowFocus: true — stale data is refetched when the user returns to the tab
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60 * 1000,
      gcTime: 5 * 60 * 1000,
      retry: 2,
      refetchOnWindowFocus: true,
    },
    mutations: {
      retry: 0,
    },
  },
});
