import { Loader2, Trash2, AlertTriangle } from 'lucide-react';
import {
  Dialog, DialogPanel, DialogHeader, DialogTitle,
  DialogBody, DialogFooter, DialogCloseButton,
} from '@/shared/components/ui/Dialog';
import { useDeleteProjectMutation } from '../hooks/useProjects';
import type { Project } from '../types';

interface DeleteProjectDialogProps {
  open: boolean;
  onClose: () => void;
  project: Project;
  onDeleted?: () => void;
}

export function DeleteProjectDialog({
  open, onClose, project, onDeleted,
}: DeleteProjectDialogProps) {
  const mutation = useDeleteProjectMutation();

  const handleDelete = async () => {
    await mutation.mutateAsync(project.id);
    onDeleted?.();
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogPanel>
        <DialogHeader>
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-red-500/10">
              <AlertTriangle className="h-4 w-4 text-red-500" />
            </div>
            <DialogTitle>Delete Project</DialogTitle>
          </div>
          <DialogCloseButton type="button" onClick={onClose} />
        </DialogHeader>

        <DialogBody>
          <p className="text-sm text-muted-foreground">
            Are you sure you want to permanently delete{' '}
            <span className="font-semibold text-foreground">"{project.name}"</span>?
            This action cannot be undone.
          </p>

          {mutation.isError && (
            <p className="mt-3 rounded-lg bg-red-500/10 px-4 py-2.5 text-sm text-red-600 dark:text-red-400">
              {(mutation.error as Error).message ?? 'Failed to delete project.'}
            </p>
          )}
        </DialogBody>

        <DialogFooter>
          <button
            type="button"
            onClick={onClose}
            disabled={mutation.isPending}
            className="rounded-lg px-4 py-2 text-sm font-medium text-muted-foreground
                       transition hover:bg-muted hover:text-foreground disabled:opacity-60"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleDelete}
            disabled={mutation.isPending}
            className="inline-flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white
                       transition hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500
                       disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {mutation.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Trash2 className="h-4 w-4" />
            )}
            Delete Project
          </button>
        </DialogFooter>
      </DialogPanel>
    </Dialog>
  );
}
