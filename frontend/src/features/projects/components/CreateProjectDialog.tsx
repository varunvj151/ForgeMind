import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import {
  Dialog, DialogPanel, DialogHeader, DialogTitle,
  DialogBody, DialogFooter, DialogCloseButton,
} from '@/shared/components/ui/Dialog';
import { Input } from '@/shared/components/ui/Input';
import { Textarea } from '@/shared/components/ui/Textarea';
import { Select } from '@/shared/components/ui/Select';
import { Label } from '@/shared/components/ui/Label';
import { useCreateProjectMutation } from '../hooks/useProjects';

// ─── Validation Schema ─────────────────────────────────────────────────────────
const schema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .max(100, 'Name must be at most 100 characters'),
  description: z
    .string()
    .max(2000, 'Description must be at most 2000 characters')
    .optional(),
  status: z.enum(['ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED'] as const),
});

type FormData = z.infer<typeof schema>;

interface CreateProjectDialogProps {
  open: boolean;
  onClose: () => void;
}

export function CreateProjectDialog({ open, onClose }: CreateProjectDialogProps) {
  const mutation = useCreateProjectMutation();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', description: '', status: 'ACTIVE' },
  });

  useEffect(() => {
    if (!open) reset();
  }, [open, reset]);

  const onSubmit = async (data: FormData) => {
    await mutation.mutateAsync({
      name: data.name.trim(),
      description: data.description?.trim(),
      status: data.status,
    });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogPanel>
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <DialogHeader>
            <DialogTitle>Create Project</DialogTitle>
            <DialogCloseButton type="button" onClick={onClose} />
          </DialogHeader>

          <DialogBody className="space-y-5">
            <div>
              <Label htmlFor="proj-name" required>Name</Label>
              <Input
                id="proj-name"
                placeholder="e.g. ForgeMind Platform"
                error={errors.name?.message}
                {...register('name')}
              />
            </div>

            <div>
              <Label htmlFor="proj-desc">Description</Label>
              <Textarea
                id="proj-desc"
                rows={3}
                placeholder="Brief description of the project…"
                error={errors.description?.message}
                {...register('description')}
              />
            </div>

            <div>
              <Label htmlFor="proj-status" required>Status</Label>
              <Select id="proj-status" error={errors.status?.message} {...register('status')}>
                <option value="ACTIVE">Active</option>
                <option value="ON_HOLD">On Hold</option>
                <option value="COMPLETED">Completed</option>
                <option value="ARCHIVED">Archived</option>
              </Select>
            </div>

            {mutation.isError && (
              <p className="rounded-lg bg-red-500/10 px-4 py-2.5 text-sm text-red-600 dark:text-red-400">
                {(mutation.error as Error).message ?? 'Failed to create project.'}
              </p>
            )}
          </DialogBody>

          <DialogFooter>
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg px-4 py-2 text-sm font-medium text-muted-foreground
                         transition hover:bg-muted hover:text-foreground focus:outline-none"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || mutation.isPending}
              className="inline-flex items-center gap-2 rounded-lg bg-violet-600 px-4 py-2 text-sm font-medium text-white
                         transition hover:bg-violet-700 focus:outline-none focus:ring-2 focus:ring-violet-500
                         disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {(isSubmitting || mutation.isPending) && <Loader2 className="h-4 w-4 animate-spin" />}
              Create Project
            </button>
          </DialogFooter>
        </form>
      </DialogPanel>
    </Dialog>
  );
}
