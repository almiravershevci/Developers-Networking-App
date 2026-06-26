jest.mock('../../lib/firestore', () => {
  const { createFirestoreMock } = require('../helpers/mockFirestore');
  return {
    db: createFirestoreMock(),
    projectId: 'developers-networking-app',
    admin: { auth: jest.fn(() => ({ listUsers: jest.fn().mockResolvedValue({ users: [] }) })) },
  };
});

const request = require('supertest');
const { createApp } = require('../../app');
const { createTestAuthMiddleware } = require('../helpers/testUtils');
const { db } = require('../../lib/firestore');
const { MatchWorkflow } = require('../../lib/serializers');

describe('Match request routes', () => {
  const app = createApp({ authMiddleware: createTestAuthMiddleware('user-b') });

  beforeEach(async () => {
    db.__store.clear();
    await db.collection('matchRequests').doc('req_1').set({
      fromUserId: 'user-a',
      toUserId: 'user-b',
      workflowStatus: MatchWorkflow.PENDING,
    });
  });

  test('GET /api/v1/match-requests/incoming lists pending requests', async () => {
    const res = await request(app).get('/api/v1/match-requests/incoming');
    expect(res.status).toBe(200);
    expect(res.body.requests).toHaveLength(1);
  });

  test('POST /api/v1/match-requests rejects self invite', async () => {
    const res = await request(app)
      .post('/api/v1/match-requests')
      .send({ toUserId: 'user-b' });
    expect(res.status).toBe(400);
  });

  test('POST /api/v1/match-requests/:id/accept resolves and returns conversationId', async () => {
    const res = await request(app).post('/api/v1/match-requests/req_1/accept');
    expect(res.status).toBe(200);
    expect(res.body.conversationId).toContain('direct_');
    expect(res.body.request.workflowStatus).toBe(MatchWorkflow.ACCEPTED);
  });

  test('POST /api/v1/match-requests/:id/decline marks declined', async () => {
    const res = await request(app).post('/api/v1/match-requests/req_1/decline');
    expect(res.status).toBe(200);
    expect(res.body.request.workflowStatus).toBe(MatchWorkflow.DECLINED);
  });

  test('POST accept returns 404 for missing request', async () => {
    const res = await request(app).post('/api/v1/match-requests/missing/accept');
    expect(res.status).toBe(404);
  });
});

describe('Match request routes — recipient guard', () => {
  const app = createApp({ authMiddleware: createTestAuthMiddleware('user-c') });

  beforeEach(async () => {
    db.__store.clear();
    await db.collection('matchRequests').doc('req_1').set({
      fromUserId: 'user-a',
      toUserId: 'user-b',
      workflowStatus: MatchWorkflow.PENDING,
    });
  });

  test('non-recipient cannot accept', async () => {
    const res = await request(app).post('/api/v1/match-requests/req_1/accept');
    expect(res.status).toBe(403);
  });
});
