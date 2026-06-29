import axios from 'axios';
import { getStoredToken, removeStoredToken } from '@/shared/utils/auth';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = getStoredToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle global errors (e.g., 401 Unauthorized)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear token and redirect to login if unauthorized
      removeStoredToken();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    
    // Extract custom error format from backend if available
    if (error.response?.data?.error) {
       return Promise.reject(error.response.data.error);
    }
    return Promise.reject(error);
  }
);
