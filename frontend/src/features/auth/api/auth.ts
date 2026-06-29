import { apiClient } from '@/shared/api/axios';
import type { AuthResponse, LoginCredentials, RegisterCredentials, User } from '../types';
import { setStoredToken, removeStoredToken } from '@/shared/utils/auth';

export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
  const response = await apiClient.post<AuthResponse>('/auth/login', credentials);
  if (response.data.token) {
    setStoredToken(response.data.token);
  }
  return response.data;
};

export const register = async (credentials: RegisterCredentials): Promise<User> => {
  const response = await apiClient.post<User>('/auth/register', credentials);
  return response.data;
};

export const logout = async (): Promise<void> => {
  removeStoredToken();
  // Optional: call backend logout endpoint if it exists to blacklist token
  // await apiClient.post('/auth/logout');
};

export const fetchCurrentUser = async (): Promise<User> => {
  const response = await apiClient.get<User>('/users/me');
  return response.data;
};
