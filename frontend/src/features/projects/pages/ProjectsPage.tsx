import { useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Plus, SortAsc, SortDesc, Filter } from 'lucide-react';
import { ProjectCard } from '../components/ProjectCard';
import { ProjectGridSkeleton } from '../components/ProjectSkeleton';
import { ProjectEmptyState } from '../components/ProjectEmptyState';
import { CreateProjectDialog } from '../components/CreateProjectDialog';
import { useProjectsQuery } from '../hooks/useProjects';
import type { ProjectFilters, ProjectStatus } from '../types';

const PAGE_SIZE = 12;

export function ProjectsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [createOpen, setCreateOpen] = useState(false);

  // ── Derive filters from URL ──────────────────────────────────────────────
  const filters: ProjectFilters = {
    search: searchParams.get('search') ?? undefined,
    status: (searchParams.get('status') as ProjectStatus | null) ?? undefined,
    sort: (searchParams.get('sort') as ProjectFilters['sort']) ?? 'createdAt',
    direction: (searchParams.get('dir') as 'asc' | 'desc') ?? 'desc',
    page: parseInt(searchParams.get('page') ?? '0', 10),
    size: PAGE_SIZE,
  };

  const { data, isLoading, isError, isFetching } = useProjectsQuery(filters);

  // ── Filter helpers ───────────────────────────────────────────────────────
  const setParam = useCallback(
    (key: string, value: string | null) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (value) next.set(key, value);
        else next.delete(key);
        if (key !== 'page') next.delete('page'); // reset to p.0 on filter change
        return next;
      });
    },
    [setSearchParams],
  );

  const setPage = (p: number) =>
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set('page', String(p));
      return next;
    });

  const toggleSort = () => {
    const currentDir = filters.direction ?? 'desc';
    setParam('dir', currentDir === 'desc' ? 'asc' : 'desc');
  };

  const hasFilters = !!(filters.search || filters.status);

  return (
    <div className="space-y-6">
      {/* ── Page Header ───────────────────────────────────────────────── */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Projects</h1>
          <p className="mt-0.5 text-sm text-muted-foreground">
            Manage and track all your engineering projects.
          </p>
        </div>
        <button
          id="create-project-btn"
          onClick={() => setCreateOpen(true)}
          className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-2.5 text-sm font-semibold text-white
                     transition hover:bg-violet-700 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2
                     dark:ring-offset-background shadow-lg shadow-violet-500/20"
        >
          <Plus className="h-4 w-4" />
          New Project
        </button>
      </div>

      {/* ── Toolbar ───────────────────────────────────────────────────── */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        {/* Search */}
        <div className="relative flex-1">
          <svg
            className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
            fill="none" stroke="currentColor" viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            id="project-search"
            type="search"
            placeholder="Search projects…"
            value={filters.search ?? ''}
            onChange={(e) => setParam('search', e.target.value || null)}
            className="w-full rounded-xl border border-border bg-background py-2 pl-9 pr-4 text-sm
                       placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500"
          />
        </div>

        {/* Status filter */}
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4 text-muted-foreground" />
          <select
            id="status-filter"
            value={filters.status ?? ''}
            onChange={(e) => setParam('status', e.target.value || null)}
            className="rounded-xl border border-border bg-background py-2 pl-3 pr-8 text-sm
                       focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500"
          >
            <option value="">All statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="ON_HOLD">On Hold</option>
            <option value="COMPLETED">Completed</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </div>

        {/* Sort */}
        <div className="flex items-center gap-2">
          <select
            id="sort-select"
            value={filters.sort}
            onChange={(e) => setParam('sort', e.target.value)}
            className="rounded-xl border border-border bg-background py-2 pl-3 pr-8 text-sm
                       focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500"
          >
            <option value="createdAt">Created</option>
            <option value="updatedAt">Updated</option>
            <option value="name">Name</option>
          </select>
          <button
            id="sort-dir-btn"
            onClick={toggleSort}
            className="rounded-xl border border-border bg-background p-2 text-muted-foreground
                       transition hover:bg-muted hover:text-foreground"
            aria-label="Toggle sort direction"
          >
            {filters.direction === 'asc' ? <SortAsc className="h-4 w-4" /> : <SortDesc className="h-4 w-4" />}
          </button>
        </div>
      </div>

      {/* ── Content ───────────────────────────────────────────────────── */}
      <div className={`transition-opacity duration-200 ${isFetching && !isLoading ? 'opacity-60' : ''}`}>
        {isLoading ? (
          <ProjectGridSkeleton count={PAGE_SIZE} />
        ) : isError ? (
          <div className="flex flex-col items-center justify-center py-24 text-center">
            <p className="text-base font-medium text-foreground">Failed to load projects.</p>
            <p className="text-sm text-muted-foreground">Please refresh the page and try again.</p>
          </div>
        ) : !data || data.empty ? (
          <ProjectEmptyState hasFilters={hasFilters} onCreateClick={() => setCreateOpen(true)} />
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((project) => (
              <ProjectCard key={project.id} project={project} />
            ))}
          </div>
        )}
      </div>

      {/* ── Pagination ────────────────────────────────────────────────── */}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between border-t border-border pt-5">
          <p className="text-sm text-muted-foreground">
            Page {(filters.page ?? 0) + 1} of {data.totalPages}
            {' '}·{' '}
            {data.totalElements} project{data.totalElements !== 1 ? 's' : ''}
          </p>
          <div className="flex items-center gap-2">
            <button
              id="prev-page-btn"
              onClick={() => setPage((filters.page ?? 0) - 1)}
              disabled={(filters.page ?? 0) === 0}
              className="rounded-lg border border-border px-3 py-1.5 text-sm font-medium
                         transition hover:bg-muted disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Previous
            </button>
            <button
              id="next-page-btn"
              onClick={() => setPage((filters.page ?? 0) + 1)}
              disabled={data.last}
              className="rounded-lg border border-border px-3 py-1.5 text-sm font-medium
                         transition hover:bg-muted disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* ── Dialogs ───────────────────────────────────────────────────── */}
      <CreateProjectDialog open={createOpen} onClose={() => setCreateOpen(false)} />
    </div>
  );
}
