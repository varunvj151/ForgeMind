import apiClient from './client';

export interface HealthResponse {
  status: string;
  service: string;
  version: string;
  timestamp: string;
}

/**
 * Fetches the backend health status.
 * Used by the application shell to verify backend connectivity on startup.
 */
export const getHealth = async (): Promise<HealthResponse> => {
  const response = await apiClient.get<HealthResponse>('/v1/health');
  return response.data;
};
