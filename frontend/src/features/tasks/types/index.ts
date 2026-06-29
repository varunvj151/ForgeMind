export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE' | 'BLOCKED';

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface AssigneeInfo {
  id: number; // The backend uses Long for id, so number in JS
  username: string;
  firstName: string;
  lastName: string;
}

export interface Task {
  id: string; // UUID
  projectId: string; // UUID
  projectName: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  assignee?: AssigneeInfo;
  createdBy: AssigneeInfo;
  dueDate?: string; // Instant mapped to ISO string
  createdAt: string; // Instant mapped to ISO string
  updatedAt: string; // Instant mapped to ISO string
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority: TaskPriority;
  assigneeId?: number; // Long
  dueDate?: string; // Instant mapped to ISO string
}

export interface UpdateTaskRequest {
  title: string;
  description?: string;
  dueDate?: string;
}

export interface UpdateTaskStatusRequest {
  status: TaskStatus;
}

export interface UpdateTaskPriorityRequest {
  priority: TaskPriority;
}

export interface UpdateTaskAssigneeRequest {
  assigneeId?: number; // pass null to unassign
}

// Reuse the generic Page wrapper
export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
