import { AppRouter } from './app/AppRouter';
import './styles/globals.css';

/**
 * Root application component.
 * Renders the router which injects the QueryClientProvider and all route definitions.
 */
function App() {
  return <AppRouter />;
}

export default App;
