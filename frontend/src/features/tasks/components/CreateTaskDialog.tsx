import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useEffect } from 'react';
import { Loader2 } from 'lucide-react';
import {
  Dialog,
  DialogPanel,
  DialogHeader,
  DialogTitle,
  DialogBody,
  DialogFooter,
  DialogCloseButton,
} from '@/shared/components/ui/Dialog';
import { Input } from '@/shared/components/ui/Input';
import { Textarea } from '@/shared/components/ui/Textarea';
import { Select } from '@/shared/components/ui/Select';
import { Label } from '@/shared/components/ui/Label';
import { useCreateTaskMutation } from '../hooks/useTasks';
import type { TaskStatus, TaskPriority } from '../types';

const schema = z.object({
  title: z.string().min(1, 'Title is required').max(255, 'Too long'),
  description: z.string().max(2000).optional(),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const),
  status: z.enum(['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'BLOCKED'] as const),
  dueDate: z.string().optional(),
});

type FormData = z.infer<typeof schema>;

interface CreateTaskDialogProps {
  open: boolean;
  onClose: () => void;
  projectId: string;
  defaultStatus?: TaskStatus;
}

export function CreateTaskDialog({
  open,
  onClose,
  projectId,
  defaultStatus = 'TODO',
}: CreateTaskDialogProps) {
  const mutation = useCreateTaskMutation(projectId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: '',
      description: '',
      priority: 'MEDIUM' as TaskPriority,
      status: defaultStatus,
      dueDate: '',
    },
  });

  useEffect(() => {
    if (open) {
      reset({ title: '', description: '', priority: 'MEDIUM', status: defaultStatus, dueDate: '' });
    }
  }, [open, defaultStatus, reset]);

  const onSubmit = async (data: FormData) => {
    await mutation.mutateAsync({
      title: data.title.trim(),
      description: data.description?.trim() || undefined,
      priority: data.priority,
      dueDate: data.dueDate ? new Date(data.dueDate).toISOString() : undefined,
    });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogPanel>
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <DialogHeader>
            <DialogTitle>New Task</DialogTitle>
            <DialogCloseButton type="button" onClick={onClose} />
          </DialogHeader>

          <DialogBody className="space-y-4">
            {/* Title */}
            <div>
              <Label htmlFor="task-title" required>Title</Label>
              <Input
                id="task-title"
                placeholder="What needs to be done?"
                error={errors.title?.message}
                {...register('title')}
              />
            </div>

            {/* Description */}
            <div>
              <Label htmlFor="task-desc">Description</Label>
              <Textarea
                id="task-desc"
                rows={3}
                placeholder="Add more details…"
                error={errors.description?.message}
                {...register('description')}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              {/* Priority */}
              <div>
                <Label htmlFor="task-priority" required>Priority</Label>
                <Select id="task-priority" error={errors.priority?.message} {...register('priority')}>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </Select>
              </div>

              {/* Status */}
              <div>
                <Label htmlFor="task-status" required>Status</Label>
                <Select id="task-status" error={errors.status?.message} {...register('status')}>
                  <option value="TODO">To Do</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="IN_REVIEW">In Review</option>
                  <option value="DONE">Done</option>
                  <option value="BLOCKED">Blocked</option>
                </Select>
              </div>
            </div>

            {/* Due Date */}
            <div>
              <Label htmlFor="task-due">Due Date</Label>
              <Input
                id="task-due"
                type="date"
                error={errors.dueDate?.message}
                {...register('dueDate')}
              />
            </div>

            {mutation.isError && (
              <p className="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-400">
                {(mutation.error as Error).message ?? 'Failed to create task.'}
              </p>
            )}
          </DialogBody>

          <DialogFooter>
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg px-4 py-2 text-sm text-muted-foreground transition hover:bg-muted hover:text-foreground"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || mutation.isPending}
              className="inline-flex items-center gap-2 rounded-lg bg-violet-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-violet-700 disabled:opacity-60"
            >
              {(isSubmitting || mutation.isPending) && <Loader2 className="h-4 w-4 animate-spin" />}
              Create Task
            </button>
          </DialogFooter>
        </form>
      </DialogPanel>
    </Dialog>
  );
}
