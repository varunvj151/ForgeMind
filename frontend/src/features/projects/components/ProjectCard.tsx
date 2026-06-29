import { Link } from 'react-router-dom';
import { Clock, User, ArrowRight } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import type { Project } from '../types';
import { ProjectStatusBadge } from './ProjectStatusBadge';

interface ProjectCardProps {
  project: Project;
}

export function ProjectCard({ project }: ProjectCardProps) {
  const updatedAgo = formatDistanceToNow(new Date(project.updatedAt), { addSuffix: true });
  const createdAgo = formatDistanceToNow(new Date(project.createdAt), { addSuffix: true });

  return (
    <Link
      to={`/projects/${project.id}`}
      className="group relative flex flex-col rounded-2xl border border-border bg-card p-5
                 transition-all duration-200 hover:border-violet-500/50 hover:shadow-lg
                 hover:shadow-violet-500/5 hover:-translate-y-0.5"
      aria-label={`Open project: ${project.name}`}
    >
      {/* Top row */}
      <div className="mb-3 flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <h3 className="truncate text-base font-semibold text-foreground group-hover:text-violet-600 dark:group-hover:text-violet-400 transition-colors">
            {project.name}
          </h3>
        </div>
        <ProjectStatusBadge status={project.status} />
      </div>

      {/* Description */}
      <p className="mb-4 line-clamp-2 flex-1 text-sm text-muted-foreground leading-relaxed">
        {project.description || (
          <span className="italic opacity-60">No description provided.</span>
        )}
      </p>

      {/* Footer */}
      <div className="flex items-center justify-between gap-2 text-xs text-muted-foreground">
        <div className="flex items-center gap-3">
          <span className="flex items-center gap-1">
            <User className="h-3 w-3 shrink-0" />
            {project.ownerUsername}
          </span>
          <span className="flex items-center gap-1">
            <Clock className="h-3 w-3 shrink-0" />
            {updatedAgo}
          </span>
        </div>
        <ArrowRight className="h-3.5 w-3.5 opacity-0 group-hover:opacity-100 transition-all group-hover:translate-x-0.5 text-violet-500" />
      </div>

      {/* Hover gradient accent */}
      <div className="pointer-events-none absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-gradient-to-br from-violet-500/[0.03] to-transparent" />

      {/* Created at tooltip-like footer */}
      <div className="mt-3 pt-3 border-t border-border/50 text-[11px] text-muted-foreground/60">
        Created {createdAgo}
      </div>
    </Link>
  );
}
