import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { queryClient } from './queryClient';
import { AppLayout } from '@/components/layout/AppLayout';

// ── Pages (lazy-loaded per route for optimal bundle splitting) ────────────────
// TODO Phase 2+: Replace placeholders with real page components
const PlaceholderPage = ({ title }: { title: string }) => (
  <div className="flex min-h-screen items-center justify-center">
    <div className="glass rounded-xl p-8 text-center">
      <h1 className="text-gradient mb-2 text-3xl font-bold">{title}</h1>
      <p className="text-muted text-sm">Coming in a future sprint</p>
    </div>
  </div>
);

/**
 * Root Router — defines the application's page hierarchy.
 *
 * Route layout:
 *   /              → Dashboard (Phase 2)
 *   /login         → Login (Phase 2)
 *   /register      → Register (Phase 2)
 *   /projects      → Project Explorer (Phase 2)
 *   /workspace/:id → Workspace IDE (Phase 2-3)
 *   /settings      → Settings (Phase 2)
 */
export function AppRouter() {
  return (
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <Routes>
          {/* Auth routes — outside AppLayout (no sidebar/navbar) */}
          <Route path="/login" element={<PlaceholderPage title="Login" />} />
          <Route path="/register" element={<PlaceholderPage title="Register" />} />

          {/* Protected application routes — wrapped in AppLayout */}
          <Route element={<AppLayout />}>
            <Route index element={<PlaceholderPage title="Dashboard" />} />
            <Route path="projects" element={<PlaceholderPage title="Projects" />} />
            <Route path="workspace/:id" element={<PlaceholderPage title="Workspace" />} />
            <Route path="settings" element={<PlaceholderPage title="Settings" />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>

        {/* React Query devtools — only visible in development */}
        <ReactQueryDevtools initialIsOpen={false} />
      </QueryClientProvider>
    </BrowserRouter>
  );
}
