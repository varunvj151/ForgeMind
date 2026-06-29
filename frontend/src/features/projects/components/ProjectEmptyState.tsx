import { FolderPlus } from 'lucide-react';

interface ProjectEmptyStateProps {
  hasFilters?: boolean;
  onCreateClick?: () => void;
}

export function ProjectEmptyState({ hasFilters, onCreateClick }: ProjectEmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-24 text-center">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-violet-500/10">
        <FolderPlus className="h-8 w-8 text-violet-500" />
      </div>
      <h3 className="mb-1 text-lg font-semibold text-foreground">
        {hasFilters ? 'No matching projects' : 'No projects yet'}
      </h3>
      <p className="mb-6 max-w-sm text-sm text-muted-foreground">
        {hasFilters
          ? 'Try adjusting your search or filter to find what you\'re looking for.'
          : 'Create your first project to start organising your engineering work.'}
      </p>
      {!hasFilters && onCreateClick && (
        <button
          onClick={onCreateClick}
          className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-2.5 text-sm font-medium text-white
                     transition hover:bg-violet-700 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2"
        >
          <FolderPlus className="h-4 w-4" />
          Create Project
        </button>
      )}
    </div>
  );
}
