import { Link } from 'react-router-dom';
import { Brain, ArrowLeft } from 'lucide-react';

const NotFoundPage = () => (
  <div className="flex min-h-screen flex-col items-center justify-center bg-background p-4 text-center">
    <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-600/20 to-indigo-600/20">
      <Brain className="h-8 w-8 text-violet-500" />
    </div>
    <h1 className="text-7xl font-black text-transparent bg-clip-text bg-gradient-to-r from-violet-500 to-indigo-500">404</h1>
    <h2 className="mt-4 text-xl font-semibold text-foreground">Page not found</h2>
    <p className="mt-2 max-w-sm text-sm text-muted-foreground">
      The page you&apos;re looking for doesn&apos;t exist or has been moved.
    </p>
    <Link
      to="/dashboard"
      className="mt-8 flex items-center gap-2 rounded-lg bg-violet-600 px-6 py-2.5 text-sm font-medium text-white shadow-sm transition-all hover:bg-violet-500"
    >
      <ArrowLeft className="h-4 w-4" />
      Back to Dashboard
    </Link>
  </div>
);

export default NotFoundPage;
