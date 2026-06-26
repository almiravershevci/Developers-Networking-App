jest.mock('../../lib/firestore', () => ({
  db: {
    collection: jest.fn().mockReturnValue({
      limit: jest.fn().mockReturnThis(),
      get: jest.fn().mockResolvedValue({ empty: true, docs: [] }),
    }),
  },
  projectId: 'developers-networking-app',
  admin: { auth: jest.fn() },
}));

jest.mock('../../lib/authorization', () => ({
  assertCanReadProject: jest.fn(),
  canReadProject: jest.fn(),
}));

const request = require('supertest');
const { createApp } = require('../../app');
const { createTestAuthMiddleware } = require('../helpers/testUtils');
const { assertCanReadProject } = require('../../lib/authorization');

describe('DevConnect API', () => {
  const app = createApp({ authMiddleware: createTestAuthMiddleware('user-a') });

  test('GET / returns service metadata', async () => {
    const res = await request(app).get('/');
    expect(res.status).toBe(200);
    expect(res.body.service).toBe('DevConnect API');
    expect(Array.isArray(res.body.routes)).toBe(true);
    expect(res.body.routes.length).toBeGreaterThan(15);
  });

  test('GET /health returns ok when Firestore is reachable', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
    expect(res.body.firestore).toBe('connected');
  });

  test('protected routes reject unauthenticated requests', async () => {
    const unauthenticatedApp = createApp({
      authMiddleware: (_req, res) =>
        res.status(401).json({ error: 'Missing Firebase ID token' }),
    });

    const res = await request(unauthenticatedApp).get('/api/me');
    expect(res.status).toBe(401);
  });

  test('GET /api/projects/:id returns 403 when authorization denies access', async () => {
    assertCanReadProject.mockImplementation(async (_projectId, _uid, res) => {
      res.status(403).json({
        error: 'forbidden',
        message: 'You do not have access to this project.',
      });
      return false;
    });

    const res = await request(app).get('/api/projects/proj_secret');
    expect(res.status).toBe(403);
    expect(res.body.error).toBe('forbidden');
  });

  test('GET /api/projects/:id/tasks returns 404 when project is missing', async () => {
    assertCanReadProject.mockImplementation(async (_projectId, _uid, res) => {
      res.status(404).json({ error: 'not_found', message: 'Project not found.' });
      return false;
    });

    const res = await request(app).get('/api/projects/missing/tasks');
    expect(res.status).toBe(404);
    expect(res.body.error).toBe('not_found');
  });

  test('unknown routes return structured 404', async () => {
    const res = await request(app).get('/api/does-not-exist');
    expect(res.status).toBe(404);
    expect(res.body.error).toBe('not_found');
  });
});
