import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, afterAll } from 'vitest';
import { server } from './mocks/server';

// Set environment variable for API client
process.env.VITE_API_URL = 'http://localhost:8080/api/v1';

// Cleanup after each test — RTL mounts components into a DOM node;
// this removes it between tests so no state leaks.
afterEach(() => {
  cleanup();
});

// Start the MSW server before all tests, reset handlers between tests,
// and stop the server after all tests complete.
beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

