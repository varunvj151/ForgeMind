import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { login, logout, fetchCurrentUser, register } from '../api/auth';
import type { LoginCredentials, RegisterCredentials } from '../types';
import { getStoredToken } from '@/shared/utils/auth';
import { useNavigate } from 'react-router-dom';

const USER_QUERY_KEY = ['currentUser'];

export const useAuth = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  // Query to fetch current user (only enabled if we have a token)
  const {
    data: user,
    isLoading: isUserLoading,
  } = useQuery({
    queryKey: USER_QUERY_KEY,
    queryFn: fetchCurrentUser,
    enabled: !!getStoredToken(),
    retry: false, // Don't retry if it fails (likely 401)
  });

  const loginMutation = useMutation({
    mutationFn: (credentials: LoginCredentials) => login(credentials),
    onSuccess: (data) => {
      // Set the user data in cache directly since login returns the user
      queryClient.setQueryData(USER_QUERY_KEY, data.user);
      navigate('/dashboard');
    },
  });

  const registerMutation = useMutation({
    mutationFn: (credentials: RegisterCredentials) => register(credentials),
    onSuccess: () => {
      // Registration successful, navigate to login
      navigate('/login');
    },
  });

  const logoutMutation = useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.setQueryData(USER_QUERY_KEY, null);
      queryClient.clear(); // Clear all cached queries
      navigate('/login');
    },
  });

  return {
    user,
    isUserLoading,
    isAuthenticated: !!user,
    login: loginMutation.mutate,
    loginAsync: loginMutation.mutateAsync,
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error,
    
    register: registerMutation.mutate,
    registerAsync: registerMutation.mutateAsync,
    isRegistering: registerMutation.isPending,
    registerError: registerMutation.error,
    
    logout: logoutMutation.mutate,
  };
};
