import type { ReactNode } from 'react';
import { QueryProvider } from './QueryProvider';
import { ThemeProvider } from './ThemeProvider';
import { WebSocketProvider } from './WebSocketProvider';

/**
 * Composes all global providers for the app.
 * Order matters: QueryProvider must wrap WebSocketProvider since WS may use queries.
 */
export const AppProvider = ({ children }: { children: ReactNode }) => (
  <ThemeProvider>
    <QueryProvider>
      <WebSocketProvider>
        {children}
      </WebSocketProvider>
    </QueryProvider>
  </ThemeProvider>
);
