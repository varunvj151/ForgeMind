import { Menu, Sun, Moon, Monitor, Bell, ChevronDown } from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useTheme } from '@/app/providers/ThemeProvider';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { cn } from '@/shared/utils/cn';

interface TopNavProps {
  onMenuClick: () => void;
}

const themeOptions = [
  { label: 'Light', value: 'light' as const, icon: Sun },
  { label: 'Dark', value: 'dark' as const, icon: Moon },
  { label: 'System', value: 'system' as const, icon: Monitor },
];

export const TopNav = ({ onMenuClick }: TopNavProps) => {
  const { user, logout } = useAuth();
  const { theme, setTheme, resolvedTheme } = useTheme();
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [themeMenuOpen, setThemeMenuOpen] = useState(false);

  const initials = user
    ? `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase()
    : '?';

  const ThemeIcon = resolvedTheme === 'dark' ? Moon : Sun;

  return (
    <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-border bg-background/80 px-4 backdrop-blur-md">
      {/* Left: Hamburger */}
      <button
        onClick={onMenuClick}
        className="rounded-md p-2 text-muted-foreground hover:text-foreground lg:hidden"
        aria-label="Toggle sidebar"
      >
        <Menu className="h-5 w-5" />
      </button>

      {/* Right: Actions */}
      <div className="ml-auto flex items-center gap-2">
        {/* Theme toggle */}
        <div className="relative">
          <button
            onClick={() => setThemeMenuOpen(!themeMenuOpen)}
            className="flex h-9 w-9 items-center justify-center rounded-lg text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
            aria-label="Change theme"
          >
            <ThemeIcon className="h-4 w-4" />
          </button>
          {themeMenuOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setThemeMenuOpen(false)} />
              <div className="absolute right-0 top-11 z-20 min-w-[140px] rounded-xl border border-border bg-popover p-1.5 shadow-xl">
                {themeOptions.map(({ label, value, icon: Icon }) => (
                  <button
                    key={value}
                    onClick={() => { setTheme(value); setThemeMenuOpen(false); }}
                    className={cn(
                      'flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm transition-colors',
                      theme === value ? 'bg-accent text-foreground font-medium' : 'text-muted-foreground hover:bg-accent hover:text-foreground'
                    )}
                  >
                    <Icon className="h-3.5 w-3.5" />
                    {label}
                  </button>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Notifications (placeholder) */}
        <button
          className="relative flex h-9 w-9 items-center justify-center rounded-lg text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
          aria-label="Notifications"
        >
          <Bell className="h-4 w-4" />
          <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-violet-500" />
        </button>

        {/* User menu */}
        <div className="relative">
          <button
            onClick={() => setUserMenuOpen(!userMenuOpen)}
            className="flex items-center gap-2.5 rounded-lg px-2 py-1.5 hover:bg-accent transition-colors"
            aria-label="User menu"
          >
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-600 text-xs font-bold text-white shadow-sm">
              {initials}
            </div>
            <div className="hidden text-left sm:block">
              <p className="text-sm font-medium text-foreground leading-none">
                {user ? `${user.firstName} ${user.lastName}` : 'Loading...'}
              </p>
              <p className="mt-0.5 text-xs text-muted-foreground">{user?.email}</p>
            </div>
            <ChevronDown className="h-3.5 w-3.5 text-muted-foreground" />
          </button>

          {userMenuOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setUserMenuOpen(false)} />
              <div className="absolute right-0 top-12 z-20 min-w-[200px] rounded-xl border border-border bg-popover p-1.5 shadow-xl">
                <Link
                  to="/profile"
                  onClick={() => setUserMenuOpen(false)}
                  className="flex w-full items-center rounded-lg px-3 py-2 text-sm text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
                >
                  Profile Settings
                </Link>
                <hr className="my-1 border-border" />
                <button
                  onClick={() => { logout(); setUserMenuOpen(false); }}
                  className="flex w-full items-center rounded-lg px-3 py-2 text-sm text-red-500 hover:bg-red-500/10 transition-colors"
                >
                  Sign out
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  );
};
