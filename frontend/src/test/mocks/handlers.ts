import { http, HttpResponse } from 'msw';

const BASE_URL = 'http://localhost:8080/api/v1';

// ── Project handlers ──────────────────────────────────────────────────────────

export const projectHandlers = [
  http.get(`${BASE_URL}/projects`, () => {
    return HttpResponse.json({
      content: [
        {
          id: 'proj-1',
          name: 'Alpha Project',
          description: 'First project',
          status: 'ACTIVE',
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z',
          ownerId: 1,
          ownerUsername: 'testuser',
        },
        {
          id: 'proj-2',
          name: 'Beta Project',
          description: 'Second project',
          status: 'PLANNING',
          createdAt: '2024-01-02T00:00:00Z',
          updatedAt: '2024-01-02T00:00:00Z',
          ownerId: 1,
          ownerUsername: 'testuser',
        },
      ],
      totalElements: 2,
      totalPages: 1,
      number: 0,
      size: 20,
    });
  }),

  http.get(`${BASE_URL}/projects/:id`, ({ params }) => {
    return HttpResponse.json({
      id: params.id,
      name: 'Alpha Project',
      description: 'First project',
      status: 'ACTIVE',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
      ownerId: 1,
      ownerUsername: 'testuser',
    });
  }),

  http.post(`${BASE_URL}/projects`, async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return HttpResponse.json(
      {
        id: 'new-proj-id',
        ...body,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        ownerId: 1,
        ownerUsername: 'testuser',
      },
      { status: 201 },
    );
  }),
];

// ── Task handlers ─────────────────────────────────────────────────────────────

export const taskHandlers = [
  http.get(`${BASE_URL}/projects/:projectId/tasks`, ({ params }) => {
    return HttpResponse.json({
      content: [
        {
          id: 'task-1',
          projectId: params.projectId,
          projectName: 'Alpha Project',
          title: 'Set up repository',
          description: 'Init the monorepo',
          status: 'TODO',
          priority: 'HIGH',
          assignee: null,
          createdBy: { id: 1, username: 'testuser', firstName: 'Test', lastName: 'User' },
          dueDate: null,
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z',
        },
        {
          id: 'task-2',
          projectId: params.projectId,
          projectName: 'Alpha Project',
          title: 'Write tests',
          description: 'Unit and integration',
          status: 'IN_PROGRESS',
          priority: 'MEDIUM',
          assignee: null,
          createdBy: { id: 1, username: 'testuser', firstName: 'Test', lastName: 'User' },
          dueDate: null,
          createdAt: '2024-01-02T00:00:00Z',
          updatedAt: '2024-01-02T00:00:00Z',
        },
      ],
      totalElements: 2,
      totalPages: 1,
      number: 0,
      size: 20,
    });
  }),
];

// ── Auth handlers ─────────────────────────────────────────────────────────────

export const authHandlers = [
  http.get(`${BASE_URL}/users/me`, () => {
    return HttpResponse.json({
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      role: ['ROLE_USER'],
      createdAt: '2024-01-01T00:00:00Z',
    });
  }),
];
