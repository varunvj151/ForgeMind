import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { AppProvider } from '@/app/providers/AppProvider';
import { AppRouter } from '@/app/router/AppRouter';
import { ErrorBoundary } from '@/shared/components/ErrorBoundary';
import '@/styles/globals.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ErrorBoundary>
      <AppProvider>
        <AppRouter />
      </AppProvider>
    </ErrorBoundary>
  </StrictMode>
);
