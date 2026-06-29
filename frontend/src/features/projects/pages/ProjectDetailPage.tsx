import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft, Edit2, Trash2, User, Calendar, Clock,
  Activity, CheckSquare, Users, ExternalLink, Kanban,
} from 'lucide-react';
import { formatDistanceToNow, format } from 'date-fns';
import { useQueryClient } from '@tanstack/react-query';
import { ProjectStatusBadge } from '../components/ProjectStatusBadge';
import { EditProjectDialog } from '../components/EditProjectDialog';
import { DeleteProjectDialog } from '../components/DeleteProjectDialog';
import { Skeleton } from '@/shared/components/ui/Skeleton';
import { useProjectQuery, projectKeys } from '../hooks/useProjects';
import { useStompSubscription } from '@/app/providers/WebSocketProvider';
import type { Project } from '../types';

// ─── Skeleton ──────────────────────────────────────────────────────────────────
function ProjectDetailSkeleton() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Skeleton className="h-8 w-8 rounded-lg" />
        <Skeleton className="h-7 w-48" />
      </div>
      <div className="rounded-2xl border border-border bg-card p-6 space-y-4">
        <Skeleton className="h-8 w-1/3" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-3/4" />
      </div>
    </div>
  );
}

// ─── Meta Item ─────────────────────────────────────────────────────────────────
function MetaItem({ icon: Icon, label, value }: {
  icon: React.ElementType; label: string; value: string;
}) {
  return (
    <div className="flex items-center gap-3 py-2.5 border-b border-border/50 last:border-0">
      <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-muted">
        <Icon className="h-3.5 w-3.5 text-muted-foreground" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-xs text-muted-foreground">{label}</p>
        <p className="text-sm font-medium text-foreground truncate">{value}</p>
      </div>
    </div>
  );
}

// ─── Placeholder Card ──────────────────────────────────────────────────────────
function PlaceholderSection({ icon: Icon, title, description }: {
  icon: React.ElementType; title: string; description: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-card/50 p-8 text-center">
      <div className="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-muted">
        <Icon className="h-5 w-5 text-muted-foreground" />
      </div>
      <p className="text-sm font-medium text-foreground">{title}</p>
      <p className="mt-1 text-xs text-muted-foreground">{description}</p>
    </div>
  );
}

// ─── Main Page ─────────────────────────────────────────────────────────────────
export function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [editOpen, setEditOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  const { data: project, isLoading, isError } = useProjectQuery(id);

  // ── Real-time updates via WebSocket ─────────────────────────────────────
  useStompSubscription(
    `/topic/projects/${id}`,
    (msg) => {
      try {
        const updated: Project = JSON.parse(msg.body);
        queryClient.setQueryData(projectKeys.detail(id ?? ''), updated);
      } catch {
        // Invalid payload — ignore
      }
    },
    !!id,
  );

  if (isLoading) return <ProjectDetailSkeleton />;

  if (isError || !project) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center">
        <p className="text-base font-semibold text-foreground">Project not found</p>
        <p className="mt-1 text-sm text-muted-foreground">
          This project may have been deleted or you may not have access.
        </p>
        <Link
          to="/projects"
          className="mt-5 inline-flex items-center gap-2 text-sm text-violet-600 hover:text-violet-700"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Projects
        </Link>
      </div>
    );
  }

  const updatedAgo = formatDistanceToNow(new Date(project.updatedAt), { addSuffix: true });
  const createdDate = format(new Date(project.createdAt), 'MMMM d, yyyy');
  const updatedDate = format(new Date(project.updatedAt), 'MMMM d, yyyy · h:mm a');

  return (
    <div className="space-y-6">
      {/* ── Breadcrumb ────────────────────────────────────────────────── */}
      <div className="flex items-center gap-3">
        <Link
          to="/projects"
          className="flex items-center gap-1.5 text-sm text-muted-foreground transition hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          Projects
        </Link>
        <span className="text-muted-foreground/50">/</span>
        <span className="truncate text-sm font-medium text-foreground">{project.name}</span>
      </div>

      {/* ── Main Grid ─────────────────────────────────────────────────── */}
      <div className="grid gap-6 lg:grid-cols-[1fr_280px]">

        {/* ── Left column ───────────────────────────────────────────── */}
        <div className="space-y-6">

          {/* Header card */}
          <div className="rounded-2xl border border-border bg-card p-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div className="flex-1 min-w-0">
                <div className="flex flex-wrap items-center gap-3 mb-2">
                  <h1 className="text-2xl font-bold text-foreground">{project.name}</h1>
                  <ProjectStatusBadge status={project.status} />
                </div>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  {project.description || (
                    <span className="italic opacity-60">No description provided.</span>
                  )}
                </p>
              </div>

              {/* Action buttons */}
              <div className="flex items-center gap-2 shrink-0">
                <Link
                  to={`/projects/${id}/board`}
                  id="open-board-btn"
                  className="inline-flex items-center gap-2 rounded-xl border border-violet-500/30 bg-violet-500/5 px-3.5 py-2
                             text-sm font-medium text-violet-600 dark:text-violet-400
                             transition hover:bg-violet-500/10 focus:outline-none focus:ring-2 focus:ring-violet-500/30"
                >
                  <Kanban className="h-3.5 w-3.5" />
                  Board
                </Link>
                <button
                  id="edit-project-btn"
                  onClick={() => setEditOpen(true)}
                  className="inline-flex items-center gap-2 rounded-xl border border-border bg-background px-3.5 py-2
                             text-sm font-medium text-foreground transition hover:bg-muted focus:outline-none focus:ring-2 focus:ring-border"
                >
                  <Edit2 className="h-3.5 w-3.5" />
                  Edit
                </button>
                <button
                  id="delete-project-btn"
                  onClick={() => setDeleteOpen(true)}
                  className="inline-flex items-center gap-2 rounded-xl border border-red-500/30 bg-red-500/5 px-3.5 py-2
                             text-sm font-medium text-red-600 dark:text-red-400
                             transition hover:bg-red-500/10 focus:outline-none focus:ring-2 focus:ring-red-500/50"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                  Delete
                </button>
              </div>
            </div>

            {/* Updated at tag */}
            <div className="mt-4 flex items-center gap-1.5 text-xs text-muted-foreground">
              <Clock className="h-3 w-3" />
              Updated {updatedAgo}
            </div>
          </div>

          {/* Task Statistics placeholder */}
          <div>
            <h2 className="mb-3 text-sm font-semibold text-foreground">Task Overview</h2>
            <PlaceholderSection
              icon={CheckSquare}
              title="Tasks coming soon"
              description="Task management UI will be available in the next sprint."
            />
          </div>

          {/* Activity feed placeholder */}
          <div>
            <h2 className="mb-3 text-sm font-semibold text-foreground">Recent Activity</h2>
            <PlaceholderSection
              icon={Activity}
              title="Activity feed coming soon"
              description="Track changes and updates to this project here."
            />
          </div>
        </div>

        {/* ── Right Sidebar ─────────────────────────────────────────── */}
        <div className="space-y-4">

          {/* Metadata */}
          <div className="rounded-2xl border border-border bg-card p-5">
            <h2 className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
              Details
            </h2>
            <div>
              <MetaItem icon={User} label="Owner" value={project.ownerUsername} />
              <MetaItem icon={Calendar} label="Created" value={createdDate} />
              <MetaItem icon={Clock} label="Last updated" value={updatedDate} />
            </div>
          </div>

          {/* Team placeholder */}
          <div className="rounded-2xl border border-border bg-card p-5">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
                Team
              </h2>
              <ExternalLink className="h-3.5 w-3.5 text-muted-foreground" />
            </div>
            <PlaceholderSection
              icon={Users}
              title="Team UI coming soon"
              description="Manage project members here."
            />
          </div>
        </div>
      </div>

      {/* ── Dialogs ───────────────────────────────────────────────────── */}
      <EditProjectDialog
        open={editOpen}
        onClose={() => setEditOpen(false)}
        project={project}
      />
      <DeleteProjectDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        project={project}
        onDeleted={() => navigate('/projects')}
      />
    </div>
  );
}
