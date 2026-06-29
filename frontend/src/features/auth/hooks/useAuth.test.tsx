import { renderHook, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { useAuth } from './useAuth';
import { server } from '../../../test/mocks/server';
import { http, HttpResponse } from 'msw';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
    },
  },
});

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={queryClient}>
    <MemoryRouter>
      {children}
    </MemoryRouter>
  </QueryClientProvider>
);

describe('useAuth hook', () => {
  beforeEach(() => {
    queryClient.clear();
    localStorage.clear();
  });

  it('fetches and returns the current user when token exists', async () => {
    // Set a dummy token so `enabled: !!getStoredToken()` passes
    localStorage.setItem('forgemind_token', 'dummy-token');

    const { result } = renderHook(() => useAuth(), { wrapper });

    // Wait for the query to resolve
    await waitFor(() => {
      expect(result.current.isUserLoading).toBe(false);
    });

    expect(result.current.user).toBeDefined();
    expect(result.current.user?.username).toBe('testuser');
    expect(result.current.isAuthenticated).toBe(true);
  });

  it('handles unauthorized errors correctly', async () => {
    localStorage.setItem('forgemind_token', 'dummy-token');

    // Override the mock to return 401
    server.use(
      http.get('http://localhost:8080/api/v1/users/me', () => {
        return HttpResponse.json({ code: 'UNAUTHORIZED' }, { status: 401 });
      })
    );

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => {
      expect(result.current.isUserLoading).toBe(false);
    });

    expect(result.current.user).toBeUndefined();
    expect(result.current.isAuthenticated).toBe(false);
  });
});

