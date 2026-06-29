import { Navigate, Outlet } from 'react-router-dom';
import { getStoredToken } from '@/shared/utils/auth';

/**
 * ProtectedRoute ensures that only authenticated users can access the route.
 * If the user is not authenticated (no token), they are redirected to the login page.
 */
export const ProtectedRoute = () => {
  const token = getStoredToken();
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // Usually you would also wrap Outlet with an AppLayout here, or do that in AppRouter
  return <Outlet />;
};
