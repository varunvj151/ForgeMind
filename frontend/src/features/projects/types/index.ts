// ─── Project Status ───────────────────────────────────────────────────────────

export type ProjectStatus = 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED';

// ─── Project Response (mirrors backend ProjectResponse record) ─────────────────

export interface Project {
  id: string;
  name: string;
  description: string | null;
  status: ProjectStatus;
  createdAt: string; // ISO-8601 Instant string
  updatedAt: string;
  ownerId: number;
  ownerUsername: string;
}

// ─── Create / Update Request ───────────────────────────────────────────────────

export interface ProjectRequest {
  name: string;
  description?: string;
  status: ProjectStatus;
}

// ─── Spring Data Page<T> ───────────────────────────────────────────────────────

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page (0-indexed)
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ─── Query Filters ────────────────────────────────────────────────────────────

export interface ProjectFilters {
  search?: string;
  status?: ProjectStatus | '';
  sort?: 'name' | 'createdAt' | 'updatedAt';
  direction?: 'asc' | 'desc';
  page?: number;
  size?: number;
}
