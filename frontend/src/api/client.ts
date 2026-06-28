import axios from 'axios';

/**
 * Configured Axios instance for all ForgeMind API calls.
 *
 * - Base URL: /api (proxied to the backend by Vite dev server in development,
 *   served via Nginx reverse proxy in production)
 * - Automatically attaches the JWT Authorization header on every request
 *   (Phase 2: token is read from localStorage/Zustand auth store)
 * - On 401 responses, clears the token and redirects to login (Phase 2)
 */
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

// ── Request Interceptor ────────────────────────────────────────────────────────
// TODO Phase 2: Uncomment and wire to Zustand auth store when JWT is implemented
apiClient.interceptors.request.use(
  (config) => {
    // const token = useAuthStore.getState().accessToken;
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response Interceptor ───────────────────────────────────────────────────────
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // TODO Phase 2: Clear auth state and redirect to /login
      // useAuthStore.getState().logout();
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
