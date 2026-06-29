import { Navigate, Outlet } from 'react-router-dom';
import { getStoredToken } from '@/shared/utils/auth';

/**
 * PublicRoute ensures that unauthenticated users can access the route.
 * If the user is already authenticated (has a token), they are redirected to the dashboard.
 */
export const PublicRoute = () => {
  const token = getStoredToken();
  
  if (token) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
};
