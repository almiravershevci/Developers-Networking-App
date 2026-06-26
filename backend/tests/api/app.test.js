jest.mock('../../lib/firestore', () => {
  const { createFirestoreMock } = require('../helpers/mockFirestore');
  const db = createFirestoreMock();
  return {
    db,
    projectId: 'developers-networking-app',
    admin: {
      auth: jest.fn(() => ({
        listUsers: jest.fn().mockResolvedValue({ users: [] }),
        verifyIdToken: jest.fn(),
      })),
    },
  };
});

jest.mock('../../lib/authorization', () => ({
  requireProjectReadAccess: jest.fn(),
  canReadProject: jest.fn(),
}));

const request = require('supertest');
const { createApp } = require('../../app');
const { createTestAuthMiddleware } = require('../helpers/testUtils');
const { requireProjectReadAccess } = require('../../lib/authorization');
const { ApiError } = require('../../lib/errors');

describe('DevConnect API', () => {
  const app = createApp({ authMiddleware: createTestAuthMiddleware('user-a') });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('GET / returns service metadata', async () => {
    const res = await request(app).get('/');
    expect(res.status).toBe(200);
    expect(res.body.service).toBe('DevConnect API');
    expect(res.body.version).toBe('v1');
    expect(res.body.documentation).toBe('/docs');
  });

  test('GET /health returns ok when Firestore is reachable', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
    expect(res.body.checks.firestore).toBe('connected');
  });

  test('GET /openapi.yaml returns OpenAPI spec', async () => {
    const res = await request(app).get('/openapi.yaml');
    expect(res.status).toBe(200);
    expect(res.text).toContain('openapi:');
    expect(res.text).toContain('/api/v1/');
  });

  test('GET /api/v1/me returns profile payload', async () => {
    const res = await request(app).get('/api/v1/me');
    expect(res.status).toBe(200);
    expect(res.body.uid).toBe('user-a');
  });

  test('protected routes reject unauthenticated requests', async () => {
    const unauthenticatedApp = createApp({
      authMiddleware: (_req, res) =>
        res.status(401).json({ error: 'unauthorized', message: 'Missing Firebase ID token.' }),
    });

    const res = await request(unauthenticatedApp).get('/api/me');
    expect(res.status).toBe(401);
  });

  test('GET /api/projects/:id returns 403 when authorization denies access', async () => {
    requireProjectReadAccess.mockRejectedValue(
      new ApiError(403, 'forbidden', 'You do not have access to this project.'),
    );

    const res = await request(app).get('/api/projects/proj_secret');
    expect(res.status).toBe(403);
    expect(res.body.error).toBe('forbidden');
  });

  test('GET /api/projects/:id/tasks returns 404 when project is missing', async () => {
    requireProjectReadAccess.mockRejectedValue(
      new ApiError(404, 'not_found', 'Project not found.'),
    );

    const res = await request(app).get('/api/projects/missing/tasks');
    expect(res.status).toBe(404);
    expect(res.body.error).toBe('not_found');
  });

  test('GET /api/dashboard/stats legacy Android route is mounted', async () => {
    const res = await request(app).get('/api/dashboard/stats');
    expect(res.status).not.toBe(404);
  });

  test('GET /api/projects legacy Android route is mounted', async () => {
    const res = await request(app).get('/api/projects');
    expect(res.status).not.toBe(404);
  });

  test('unknown routes return structured 404', async () => {
    const res = await request(app).get('/api/does-not-exist');
    expect(res.status).toBe(404);
    expect(res.body.error).toBe('not_found');
  });

  test('GET /metrics exposes Prometheus text', async () => {
    const res = await request(app).get('/metrics');
    expect(res.status).toBe(200);
    expect(res.text).toContain('devconnect_http_requests_total');
  });

  test('legacy /api routes include deprecation headers', async () => {
    const res = await request(app).get('/api/v1/me');
    expect(res.headers.deprecation).toBeUndefined();

    const legacy = await request(app).get('/api/me');
    expect(legacy.headers.deprecation).toBe('true');
    expect(legacy.headers.sunset).toBeTruthy();
  });
});
