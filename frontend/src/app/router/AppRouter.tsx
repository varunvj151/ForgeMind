import { lazy, Suspense } from 'react';
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { PublicRoute } from './PublicRoute';
import { AppLayout } from '@/app/layouts/AppLayout';
import { LoadingScreen } from '@/shared/components/ui/LoadingScreen';

// Lazy-loaded pages
const LoginPage = lazy(() => import('@/features/auth/pages/LoginPage'));
const RegisterPage = lazy(() => import('@/features/auth/pages/RegisterPage'));
const DashboardPage = lazy(() => import('@/features/dashboard/pages/DashboardPage'));
const ProjectsPage = lazy(() =>
  import('@/features/projects/pages/ProjectsPage').then((m) => ({ default: m.ProjectsPage }))
);
const ProjectDetailPage = lazy(() =>
  import('@/features/projects/pages/ProjectDetailPage').then((m) => ({ default: m.ProjectDetailPage }))
);
const ProjectBoardPage = lazy(() =>
  import('@/features/tasks/pages/ProjectBoardPage').then((m) => ({ default: m.ProjectBoardPage }))
);
const TasksPage = lazy(() =>
  import('@/features/tasks/pages/TasksPage').then((m) => ({ default: m.TasksPage }))
);
const TeamsPage = lazy(() => import('@/features/teams/pages/TeamsPage'));
const ProfilePage = lazy(() => import('@/features/users/pages/ProfilePage'));
const NotFoundPage = lazy(() => import('@/shared/components/NotFoundPage'));

const router = createBrowserRouter([
  // Root redirect
  {
    path: '/',
    element: <Navigate to="/dashboard" replace />,
  },

  // Public routes (accessible only when NOT authenticated)
  {
    element: <PublicRoute />,
    children: [
      {
        path: '/login',
        element: (
          <Suspense fallback={<LoadingScreen />}>
            <LoginPage />
          </Suspense>
        ),
      },
      {
        path: '/register',
        element: (
          <Suspense fallback={<LoadingScreen />}>
            <RegisterPage />
          </Suspense>
        ),
      },
    ],
  },

  // Protected routes (authenticated users only)
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          {
            path: '/dashboard',
            element: (
              <Suspense fallback={<LoadingScreen />}>
                <DashboardPage />
              </Suspense>
            ),
          },
          {
            path: '/projects',
            element: (
              <Suspense fallback={<LoadingScreen />}>
                <ProjectsPage />
              </Suspense>
            ),
          },
          {
            path: '/projects/:id',
            element: (
              <Suspense fallback={<LoadingScreen />}>
                <ProjectDetailPage />
              </Suspense>
            ),
          },
          {
            path: '/projects/:id/board',
            element: (
              <Suspense fallback={<LoadingScreen />}>
                <ProjectBoardPage />
              </Suspense>
            ),
          },
          {
            path: '/tasks',
            element: (
              <Suspense fallback={<LoadingScreen />}>
                <TasksPage />
              </Suspense>
            ),
          },
          {
            path: '/teams',
            element: (
              <Suspense fallback={<LoadingScreen />}>
                <TeamsPage />
              </Suspense>
            ),
          },
          {
            path: '/profile',
            element: (
              <Suspense fallback={<LoadingScreen />}>
                <ProfilePage />
              </Suspense>
            ),
          },
        ],
      },
    ],
  },

  // 404
  {
    path: '*',
    element: (
      <Suspense fallback={<LoadingScreen />}>
        <NotFoundPage />
      </Suspense>
    ),
  },
]);

export const AppRouter = () => <RouterProvider router={router} />;
