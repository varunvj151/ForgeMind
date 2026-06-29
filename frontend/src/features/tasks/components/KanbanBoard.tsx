import { useState, useCallback } from 'react';
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  KeyboardSensor,
  useSensor,
  useSensors,
  closestCenter,
  type DragStartEvent,
  type DragEndEvent,
  type DragOverEvent,
} from '@dnd-kit/core';
import { sortableKeyboardCoordinates } from '@dnd-kit/sortable';
import { KanbanColumn } from './KanbanColumn';
import { TaskCard } from './TaskCard';
import { KANBAN_STATUSES, groupTasksByStatus, useChangeStatusMutation } from '../hooks/useTasks';
import type { Task, TaskStatus } from '../types';

interface KanbanBoardProps {
  projectId: string;
  tasks: Task[];
  onTaskClick: (task: Task) => void;
  onAddTask: (status: TaskStatus) => void;
}

export function KanbanBoard({ projectId, tasks, onTaskClick, onAddTask }: KanbanBoardProps) {
  const [activeTask, setActiveTask] = useState<Task | null>(null);
  const [overColumn, setOverColumn] = useState<TaskStatus | null>(null);

  const changeStatusMutation = useChangeStatusMutation(projectId);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  const handleDragStart = useCallback((event: DragStartEvent) => {
    const task = event.active.data.current?.task as Task | undefined;
    if (task) setActiveTask(task);
  }, []);

  const handleDragOver = useCallback((event: DragOverEvent) => {
    const over = event.over;
    if (!over) { setOverColumn(null); return; }
    // over.id may be either a TaskStatus (column) or a task id
    const id = over.id as string;
    if (KANBAN_STATUSES.includes(id as TaskStatus)) {
      setOverColumn(id as TaskStatus);
    } else {
      // Find which column the hovered task belongs to
      const hoveredTask = tasks.find((t) => t.id === id);
      setOverColumn(hoveredTask?.status ?? null);
    }
  }, [tasks]);

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event;
      setActiveTask(null);
      setOverColumn(null);

      if (!over || !active) return;

      const draggedTask = active.data.current?.task as Task | undefined;
      if (!draggedTask) return;

      // Determine target status
      const overId = over.id as string;
      let targetStatus: TaskStatus | null;

      if (KANBAN_STATUSES.includes(overId as TaskStatus)) {
        targetStatus = overId as TaskStatus;
      } else {
        const overTask = tasks.find((t) => t.id === overId);
        targetStatus = overTask?.status ?? null;
      }

      if (!targetStatus || draggedTask.status === targetStatus) return;

      changeStatusMutation.mutate({
        taskId: draggedTask.id,
        req: { status: targetStatus },
      });
    },
    [tasks, changeStatusMutation]
  );

  const grouped = groupTasksByStatus(tasks);

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragStart={handleDragStart}
      onDragOver={handleDragOver}
      onDragEnd={handleDragEnd}
    >
      {/* Horizontal scrolling board */}
      <div className="flex h-full gap-4 overflow-x-auto pb-4 pr-4">
        {KANBAN_STATUSES.map((status) => (
          <KanbanColumn
            key={status}
            status={status}
            tasks={grouped[status]}
            onTaskClick={onTaskClick}
            onAddTask={onAddTask}
            isOver={overColumn === status}
          />
        ))}
      </div>

      {/* Drag overlay — rendered in a portal above everything */}
      <DragOverlay dropAnimation={{ duration: 200, easing: 'ease' }}>
        {activeTask && (
          <div className="rotate-2 scale-105 opacity-90 shadow-2xl shadow-violet-500/20">
            <TaskCard task={activeTask} onClick={() => {}} isDragging />
          </div>
        )}
      </DragOverlay>
    </DndContext>
  );
}
