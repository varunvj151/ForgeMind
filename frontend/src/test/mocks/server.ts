import { setupServer } from 'msw/node';
import { projectHandlers, taskHandlers, authHandlers } from './handlers';

// Combine all handlers into a single MSW server instance
// used by Vitest (Node.js runtime) via the msw/node adapter.
export const server = setupServer(...projectHandlers, ...taskHandlers, ...authHandlers);
