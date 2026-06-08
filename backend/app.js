const express = require('express');
const cors = require('cors');
const path = require('path');
const helmet = require('helmet');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');

const firebaseAuth = require('./middleware/firebaseAuth');
const requireAdmin = require('./middleware/requireAdmin');
const { requestLogger } = require('./middleware/requestLogger');
const { notFoundHandler, errorHandler } = require('./middleware/errorHandler');
const { mountApiRoutes } = require('./lib/mountApi');
const { publicLimiter } = require('./middleware/rateLimit');
const { legacyApiDeprecationHeaders } = require('./middleware/deprecationHeaders');
const { metricsMiddleware, prometheusText } = require('./lib/metrics');
const { db, projectId, admin } = require('./lib/firestore');

const API_VERSION = 'v1';
const STARTED_AT = Date.now();

const API_ROUTES = [
  `GET  /api/${API_VERSION}/me`,
  `PATCH /api/${API_VERSION}/me`,
  `GET  /api/${API_VERSION}/dashboard/stats`,
  `GET  /api/${API_VERSION}/projects?limit&cursor`,
  `GET  /api/${API_VERSION}/projects/:projectId`,
  `GET  /api/${API_VERSION}/projects/:projectId/tasks`,
  `GET  /api/${API_VERSION}/events`,
  `GET  /api/${API_VERSION}/events/:eventId`,
  `POST /api/${API_VERSION}/events/:eventId/registrations/me`,
  `DELETE /api/${API_VERSION}/events/:eventId/registrations/me`,
  `GET  /api/${API_VERSION}/inbox?limit&cursor`,
  `PATCH /api/${API_VERSION}/inbox/:notificationId/read`,
  `GET  /api/${API_VERSION}/match-requests/incoming`,
  `GET  /api/${API_VERSION}/match-requests/outgoing`,
  `POST /api/${API_VERSION}/match-requests`,
  `POST /api/${API_VERSION}/match-requests/:requestId/accept`,
  `POST /api/${API_VERSION}/match-requests/:requestId/decline`,
  `GET  /api/${API_VERSION}/conversations`,
  `GET  /api/${API_VERSION}/conversations/:conversationId/messages`,
  `POST /api/${API_VERSION}/conversations/:conversationId/messages`,
  `POST /api/${API_VERSION}/admin/inbox/broadcast`,
];

/**
 * @param {{ authMiddleware?: Function }} [options]
 */
function createApp(options = {}) {
  const auth = options.authMiddleware || firebaseAuth;
  const app = express();

  app.disable('x-powered-by');
  app.use(helmet({
    contentSecurityPolicy: false,
    crossOriginEmbedderPolicy: false,
  }));
  app.use(express.json({ limit: '256kb' }));
  app.use(cors(buildCorsOptions()));
  app.use(metricsMiddleware);
  app.use(requestLogger);

  const openapiPath = path.join(__dirname, 'openapi.yaml');
  const openapiDocument = YAML.load(openapiPath);

  app.get('/', (_req, res) => {
    res.json({
      service: 'DevConnect API',
      version: API_VERSION,
      project: projectId,
      auth: 'Firebase ID token (Authorization: Bearer)',
      documentation: '/docs',
      openapi: '/openapi.yaml',
      health: '/health',
      routes: API_ROUTES,
      legacyPrefix: '/api (deprecated — use /api/v1)',
    });
  });

  app.get('/openapi.yaml', (_req, res) => {
    res.sendFile(openapiPath);
  });

  app.use('/docs', publicLimiter, swaggerUi.serve, swaggerUi.setup(openapiDocument, {
    customSiteTitle: 'DevConnect API',
  }));

  app.get('/metrics', (_req, res) => {
    res.type('text/plain; version=0.0.4').send(prometheusText());
  });

  app.get('/health', async (req, res) => {
    const checks = {
      firestore: 'unknown',
      auth: 'unknown',
    };

    try {
      await db.collection('users').limit(1).get();
      checks.firestore = 'connected';
    } catch (error) {
      checks.firestore = 'unavailable';
      return res.status(503).json({
        status: 'degraded',
        project: projectId,
        version: API_VERSION,
        uptimeSeconds: Math.floor((Date.now() - STARTED_AT) / 1000),
        checks,
        message: error.message,
        requestId: req.requestId,
      });
    }

    try {
      await admin.auth().listUsers(1);
      checks.auth = 'connected';
    } catch {
      checks.auth = 'degraded';
    }

    res.json({
      status: checks.auth === 'connected' ? 'ok' : 'degraded',
      project: projectId,
      version: API_VERSION,
      uptimeSeconds: Math.floor((Date.now() - STARTED_AT) / 1000),
      checks,
      timestamp: new Date().toISOString(),
      requestId: req.requestId,
    });
  });

  mountApiRoutes(app, `/api/${API_VERSION}`, auth, requireAdmin);
  app.use('/api', legacyApiDeprecationHeaders);
  mountApiRoutes(app, '/api', auth, requireAdmin);

  app.use(notFoundHandler);
  app.use(errorHandler);

  return app;
}

function buildCorsOptions() {
  const raw = process.env.CORS_ORIGINS || '';
  const origins = raw.split(',').map((value) => value.trim()).filter(Boolean);
  if (origins.length === 0) return {};
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

module.exports = { createApp, API_ROUTES, API_VERSION };
