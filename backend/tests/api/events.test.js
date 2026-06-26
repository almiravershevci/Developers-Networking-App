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

describe('Events routes', () => {
  const app = createApp({ authMiddleware: createTestAuthMiddleware('user-a') });

  beforeEach(async () => {
    db.__store.clear();
    await db.collection('events').doc('evt_1').set({
      title: 'Kotlin Meetup',
      eventStatus: 'scheduled',
    });
  });

  test('GET /api/v1/events lists events', async () => {
    const res = await request(app).get('/api/v1/events');
    expect(res.status).toBe(200);
    expect(res.body.events.length).toBeGreaterThanOrEqual(1);
  });

  test('GET /api/v1/events/:id returns event', async () => {
    const res = await request(app).get('/api/v1/events/evt_1');
    expect(res.status).toBe(200);
    expect(res.body.event.id).toBe('evt_1');
  });

  test('POST /api/v1/events/:id/registrations/me creates RSVP', async () => {
    const res = await request(app)
      .post('/api/v1/events/evt_1/registrations/me')
      .send({ status: 'going' });
    expect(res.status).toBe(201);
    expect(res.body.status).toBe('going');
  });

  test('DELETE /api/v1/events/:id/registrations/me removes RSVP', async () => {
    await request(app).post('/api/v1/events/evt_1/registrations/me').send({});
    const res = await request(app).delete('/api/v1/events/evt_1/registrations/me');
    expect(res.status).toBe(204);
  });

  test('POST registration returns 404 for missing event', async () => {
    const res = await request(app).post('/api/v1/events/missing/registrations/me').send({});
    expect(res.status).toBe(404);
  });
});
