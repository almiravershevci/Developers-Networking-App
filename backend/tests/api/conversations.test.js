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

describe('Conversation routes', () => {
  const app = createApp({ authMiddleware: createTestAuthMiddleware('user-a') });

  beforeEach(async () => {
    await db.collection('conversations').doc('conv_1').set({
      participantIds: ['user-a', 'user-b'],
      title: 'Team chat',
    });
    await db.collection('conversations').doc('conv_1').collection('messages').doc('m1').set({
      senderId: 'user-b',
      body: 'Hello',
      createdAt: new Date().toISOString(),
    });
  });

  test('GET /api/v1/conversations lists participant threads', async () => {
    const res = await request(app).get('/api/v1/conversations');
    expect(res.status).toBe(200);
    expect(res.body.conversations).toHaveLength(1);
  });

  test('GET /api/v1/conversations/:id/messages returns messages', async () => {
    const res = await request(app).get('/api/v1/conversations/conv_1/messages');
    expect(res.status).toBe(200);
    expect(res.body.messages.length).toBeGreaterThanOrEqual(1);
  });

  test('POST /api/v1/conversations/:id/messages creates message', async () => {
    const res = await request(app)
      .post('/api/v1/conversations/conv_1/messages')
      .send({ body: 'Reply from tests' });
    expect(res.status).toBe(201);
    expect(res.body.message.body).toBe('Reply from tests');
  });

  test('non-participant cannot read messages', async () => {
    const outsiderApp = createApp({ authMiddleware: createTestAuthMiddleware('user-z') });
    const res = await request(outsiderApp).get('/api/v1/conversations/conv_1/messages');
    expect(res.status).toBe(403);
  });
});
