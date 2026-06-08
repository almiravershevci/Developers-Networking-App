jest.mock('../../lib/firestore', () => {
  const { createFirestoreMock } = require('../helpers/mockFirestore');
  return {
    db: createFirestoreMock(),
    projectId: 'developers-networking-app',
    admin: { auth: jest.fn(() => ({ listUsers: jest.fn().mockResolvedValue({ users: [] }) })) },
  };
});

jest.mock('../../lib/auditLog', () => ({
  writeAuditLog: jest.fn().mockResolvedValue(undefined),
  AuditVerb: { ADMIN_BROADCAST: 'admin_broadcast' },
}));

const request = require('supertest');
const { createApp } = require('../../app');
const { createTestAuthMiddleware } = require('../helpers/testUtils');
const { writeAuditLog } = require('../../lib/auditLog');
const { db } = require('../../lib/firestore');

describe('Admin routes', () => {
  const adminApp = createApp({
    authMiddleware: (req, _res, next) => {
      req.user = { uid: 'admin-1', email: 'admin@example.com', accountRole: 'admin' };
      next();
    },
  });

  beforeEach(async () => {
    jest.clearAllMocks();
    db.__store.clear();
    await db.collection('users').doc('admin-1').set({ displayName: 'Admin', accountRole: 'admin' });
    await db.collection('users').doc('user-a').set({ displayName: 'Alice' });
    await db.collection('users').doc('user-b').set({ displayName: 'Bob' });
  });

  test('POST /api/v1/admin/inbox/broadcast sends to explicit audience', async () => {
    const res = await request(adminApp)
      .post('/api/v1/admin/inbox/broadcast')
      .send({ title: 'Team update', body: 'Hello team', audience: ['user-a'] });

    expect(res.status).toBe(201);
    expect(res.body.sent).toBe(1);
    expect(writeAuditLog).toHaveBeenCalled();
  });

  test('POST /api/v1/admin/inbox/broadcast validates payload', async () => {
    const res = await request(adminApp)
      .post('/api/v1/admin/inbox/broadcast')
      .send({ title: '', body: 'x' });

    expect(res.status).toBe(400);
    expect(res.body.error).toBe('validation_error');
  });

  test('POST /api/v1/admin/inbox/broadcast audience=all respects cap', async () => {
    const res = await request(adminApp)
      .post('/api/v1/admin/inbox/broadcast')
      .send({ title: 'All hands', body: 'Update', audience: 'all' });

    expect(res.status).toBe(201);
    expect(res.body.sent).toBe(3);
  });
});
