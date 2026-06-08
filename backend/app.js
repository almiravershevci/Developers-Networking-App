const express = require('express');
const cors = require('cors');

const firebaseAuth = require('./middleware/firebaseAuth');
const requireAdmin = require('./middleware/requireAdmin');
const { requestLogger } = require('./middleware/requestLogger');
const { notFoundHandler, errorHandler } = require('./middleware/errorHandler');
const dashboardRoutes = require('./routes/dashboardRoutes');
const projectRoutes = require('./routes/projectRoutes');
const meRoutes = require('./routes/meRoutes');
const eventsRoutes = require('./routes/eventsRoutes');
const inboxRoutes = require('./routes/inboxRoutes');
const matchRequestsRoutes = require('./routes/matchRequestsRoutes');
const conversationsRoutes = require('./routes/conversationsRoutes');
const adminRoutes = require('./routes/adminRoutes');
const { db, projectId } = require('./lib/firestore');

const API_ROUTES = [
  'GET  /api/me',
  'PATCH /api/me',
  'GET  /api/dashboard/stats',
  'GET  /api/projects',
  'GET  /api/projects/:projectId',
  'GET  /api/projects/:projectId/tasks',
  'GET  /api/events',
  'GET  /api/events/:eventId',
  'POST /api/events/:eventId/registrations/me',
  'DELETE /api/events/:eventId/registrations/me',
  'GET  /api/inbox',
  'PATCH /api/inbox/:notificationId/read',
  'GET  /api/match-requests/incoming',
  'GET  /api/match-requests/outgoing',
  'POST /api/match-requests',
  'POST /api/match-requests/:requestId/accept',
  'POST /api/match-requests/:requestId/decline',
  'GET  /api/conversations',
  'GET  /api/conversations/:conversationId/messages',
  'POST /api/conversations/:conversationId/messages',
  'POST /api/admin/inbox/broadcast',
];

/**
 * Factory for the Express app — enables supertest without listening on a port.
 * @param {{ authMiddleware?: Function }} [options]
 */
function createApp(options = {}) {
  const auth = options.authMiddleware || firebaseAuth;
  const app = express();

  app.disable('x-powered-by');
  app.use(express.json({ limit: '256kb' }));
  app.use(cors(buildCorsOptions()));
  app.use(requestLogger);

  app.get('/', (_req, res) => {
    res.json({
      service: 'DevConnect API',
      version: '1.0.0',
      project: projectId,
      auth: 'Firebase ID token (Authorization: Bearer)',
      routes: API_ROUTES,
    });
  });

  app.get('/health', async (_req, res) => {
    try {
      await db.collection('users').limit(1).get();
      res.json({
        status: 'ok',
        firestore: 'connected',
        project: projectId,
        timestamp: new Date().toISOString(),
      });
    } catch (error) {
      res.status(503).json({
        status: 'degraded',
        firestore: 'unavailable',
        project: projectId,
        message: error.message,
      });
    }
  });

  app.use('/api/me', auth, meRoutes);
  app.use('/api/dashboard', auth, dashboardRoutes);
  app.use('/api/projects', auth, projectRoutes);
  app.use('/api/events', auth, eventsRoutes);
  app.use('/api/inbox', auth, inboxRoutes);
  app.use('/api/match-requests', auth, matchRequestsRoutes);
  app.use('/api/conversations', auth, conversationsRoutes);
  app.use('/api/admin', auth, requireAdmin, adminRoutes);

  app.use(notFoundHandler);
  app.use(errorHandler);

  return app;
}

function buildCorsOptions() {
  const raw = process.env.CORS_ORIGINS || '';
  const origins = raw
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean);

  if (origins.length === 0) {
    return {};
  }

  return {
    origin(origin, callback) {
      if (!origin || origins.includes(origin)) {
        callback(null, true);
        return;
      }
      callback(new Error('Not allowed by CORS'));
    },
  };
}

module.exports = { createApp, API_ROUTES };
