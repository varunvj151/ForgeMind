import {
  FolderKanban,
  CheckSquare,
  Users,
  Activity,
  TrendingUp,
  Plus,
  ArrowRight,
  Brain,
  Zap,
  Clock,
} from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import { formatDistanceToNow } from 'date-fns';
import { useProjectsQuery } from '@/features/projects/hooks/useProjects';
import { ProjectStatusBadge } from '@/features/projects/components/ProjectStatusBadge';
import { CreateProjectDialog } from '@/features/projects/components/CreateProjectDialog';
import { Skeleton } from '@/shared/components/ui/Skeleton';

// ─── Stat Card ─────────────────────────────────────────────────────────────────
const StatCard = ({
  label, value, icon: Icon, color,
}: {
  label: string;
  value: string | number;
  icon: React.ElementType;
  color: string;
}) => (
  <div className="group rounded-xl border border-border bg-card p-5 transition-all duration-200 hover:border-violet-500/30 hover:shadow-lg hover:shadow-violet-500/5">
    <div className="flex items-start justify-between">
      <div>
        <p className="text-sm text-muted-foreground">{label}</p>
        <p className="mt-1.5 text-3xl font-bold text-foreground">{value}</p>
      </div>
      <div className={`flex h-10 w-10 items-center justify-center rounded-lg ${color}`}>
        <Icon className="h-5 w-5" />
      </div>
    </div>
    <div className="mt-4 flex items-center gap-1 text-xs text-green-500">
      <TrendingUp className="h-3 w-3" />
      <span>Active workspace</span>
    </div>
  </div>
);

// ─── Dashboard Page ────────────────────────────────────────────────────────────
const DashboardPage = () => {
  const { user } = useAuth();
  const [createOpen, setCreateOpen] = useState(false);

  // Fetch 4 most recent projects for the dashboard widget
  const { data: recentProjects, isLoading: projectsLoading } = useProjectsQuery({
    sort: 'updatedAt',
    direction: 'desc',
    size: 4,
    page: 0,
  });

  const greeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">
            {greeting()}, {user?.firstName ?? 'there'} 👋
          </h1>
          <p className="text-sm text-muted-foreground">
            Here&apos;s what&apos;s happening in your workspace today.
          </p>
        </div>
        <div className="flex gap-2">
          <button
            id="dashboard-new-project-btn"
            onClick={() => setCreateOpen(true)}
            className="flex items-center gap-2 rounded-lg bg-violet-600 px-4 py-2 text-sm font-medium text-white shadow-sm shadow-violet-500/20 transition-all hover:bg-violet-500 active:scale-95"
          >
            <Plus className="h-4 w-4" />
            New Project
          </button>
        </div>
      </div>

      {/* Stats grid */}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="Active Projects"
          value={
            recentProjects
              ? recentProjects.content.filter((p) => p.status === 'ACTIVE').length
              : '—'
          }
          icon={FolderKanban}
          color="bg-violet-500/10 text-violet-500"
        />
        <StatCard label="Open Tasks" value="—" icon={CheckSquare} color="bg-blue-500/10 text-blue-500" />
        <StatCard label="Team Members" value="—" icon={Users} color="bg-emerald-500/10 text-emerald-500" />
        <StatCard label="Activities" value="—" icon={Activity} color="bg-amber-500/10 text-amber-500" />
      </div>

      {/* Content grid */}
      <div className="grid gap-4 lg:grid-cols-3">

        {/* Recent Projects – Real Data */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <FolderKanban className="h-4 w-4 text-violet-500" />
              <h3 className="font-semibold text-foreground">Recent Projects</h3>
            </div>
            <Link
              to="/projects"
              className="flex items-center gap-1 text-xs text-violet-500 hover:text-violet-400 transition-colors"
            >
              View all <ArrowRight className="h-3 w-3" />
            </Link>
          </div>

          {projectsLoading ? (
            <div className="space-y-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-center gap-3">
                  <Skeleton className="h-10 flex-1" />
                </div>
              ))}
            </div>
          ) : !recentProjects || recentProjects.empty ? (
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <FolderKanban className="mb-2 h-8 w-8 text-muted-foreground/40" />
              <p className="text-sm text-muted-foreground">No projects yet</p>
              <button
                onClick={() => setCreateOpen(true)}
                className="mt-2 text-xs text-violet-500 hover:text-violet-400"
              >
                Create your first →
              </button>
            </div>
          ) : (
            <div className="space-y-2">
              {recentProjects.content.map((project) => (
                <Link
                  key={project.id}
                  to={`/projects/${project.id}`}
                  className="flex items-center justify-between rounded-lg p-2.5 transition hover:bg-muted group"
                >
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-foreground group-hover:text-violet-600 dark:group-hover:text-violet-400">
                      {project.name}
                    </p>
                    <p className="flex items-center gap-1 text-xs text-muted-foreground">
                      <Clock className="h-2.5 w-2.5" />
                      {formatDistanceToNow(new Date(project.updatedAt), { addSuffix: true })}
                    </p>
                  </div>
                  <ProjectStatusBadge status={project.status} className="ml-2 shrink-0" />
                </Link>
              ))}
            </div>
          )}
        </div>

        {/* My Tasks placeholder */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <CheckSquare className="h-4 w-4 text-violet-500" />
              <h3 className="font-semibold text-foreground">My Tasks</h3>
            </div>
            <Link to="/tasks" className="flex items-center gap-1 text-xs text-violet-500 hover:text-violet-400 transition-colors">
              View all <ArrowRight className="h-3 w-3" />
            </Link>
          </div>
          <div className="flex flex-col items-center justify-center py-8 text-center">
            <CheckSquare className="mb-2 h-8 w-8 text-muted-foreground/40" />
            <p className="text-sm text-muted-foreground">Task UI coming soon</p>
          </div>
        </div>

        {/* Recent Activity placeholder */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="mb-4 flex items-center gap-2.5">
            <Activity className="h-4 w-4 text-violet-500" />
            <h3 className="font-semibold text-foreground">Recent Activity</h3>
          </div>
          <div className="flex flex-col items-center justify-center py-8 text-center">
            <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-violet-500/10">
              <Brain className="h-6 w-6 text-violet-500" />
            </div>
            <p className="text-sm font-medium text-foreground">No activity yet</p>
            <p className="mt-1 text-xs text-muted-foreground">Activity from your team will appear here</p>
          </div>
        </div>
      </div>

      {/* Quick start banner */}
      <div className="overflow-hidden rounded-xl border border-violet-500/20 bg-gradient-to-r from-violet-600/10 via-indigo-600/10 to-purple-600/10 p-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-start gap-4">
            <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg bg-violet-500/20">
              <Zap className="h-5 w-5 text-violet-400" />
            </div>
            <div>
              <h3 className="font-semibold text-foreground">Get started with ForgeMind</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Create your first project to unlock AI-powered engineering insights.
              </p>
            </div>
          </div>
          <button
            onClick={() => setCreateOpen(true)}
            className="flex-shrink-0 rounded-lg border border-violet-500/30 px-5 py-2 text-sm font-medium text-violet-400 transition-all hover:bg-violet-500/10"
          >
            Create Project
          </button>
        </div>
      </div>

      {/* Dialogs */}
      <CreateProjectDialog open={createOpen} onClose={() => setCreateOpen(false)} />
    </div>
  );
};

export default DashboardPage;
