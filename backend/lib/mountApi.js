const meRoutes = require('../routes/meRoutes');
const dashboardRoutes = require('../routes/dashboardRoutes');
const projectRoutes = require('../routes/projectRoutes');
const eventsRoutes = require('../routes/eventsRoutes');
const inboxRoutes = require('../routes/inboxRoutes');
const matchRequestsRoutes = require('../routes/matchRequestsRoutes');
const conversationsRoutes = require('../routes/conversationsRoutes');
const adminRoutes = require('../routes/adminRoutes');
const { publicLimiter, writeLimiter } = require('../middleware/rateLimit');

/**
 * Mount versioned API routers under a prefix (e.g. /api/v1).
 * @param {import('express').Express} app
 * @param {string} prefix
 * @param {import('express').RequestHandler} auth
 * @param {import('express').RequestHandler} requireAdmin
 */
function mountApiRoutes(app, prefix, auth, requireAdmin) {
  app.use(`${prefix}/me`, auth, publicLimiter, meRoutes);
  app.use(`${prefix}/dashboard`, auth, publicLimiter, dashboardRoutes);
  app.use(`${prefix}/projects`, auth, publicLimiter, projectRoutes);
  app.use(`${prefix}/events`, auth, publicLimiter, eventsRoutes);
  app.use(`${prefix}/inbox`, auth, publicLimiter, inboxRoutes);
  app.use(`${prefix}/match-requests`, auth, publicLimiter, matchRequestsRoutes);
  app.use(`${prefix}/conversations`, auth, publicLimiter, conversationsRoutes);
  app.use(`${prefix}/admin`, auth, requireAdmin, writeLimiter, adminRoutes);
}

module.exports = { mountApiRoutes };
