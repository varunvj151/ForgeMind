import { Outlet } from 'react-router-dom';

/**
 * Application shell layout wrapping all authenticated pages.
 *
 * Phase 1: Minimal container — renders the routed page content via <Outlet />.
 * Phase 2: Will include Navbar, Sidebar, and global notification toast.
 */
export function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-[rgb(var(--color-bg-primary))]">
      {/* TODO Phase 2: <Navbar /> */}
      <div className="flex flex-1">
        {/* TODO Phase 2: <Sidebar /> */}
        <main className="flex-1 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
