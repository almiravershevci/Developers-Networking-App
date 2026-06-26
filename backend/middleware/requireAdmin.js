const { db } = require('../lib/firestore');
const { AccountRole } = require('../lib/serializers');
const { sendError } = require('../lib/errors');
const { logger } = require('../lib/logger');

module.exports = async function requireAdmin(req, res, next) {
  try {
    const snap = await db.collection('users').doc(req.user.uid).get();
    if (!snap.exists || snap.get('accountRole') !== AccountRole.ADMIN) {
      return sendError(res, 403, 'Admin role required.', 'forbidden', req.requestId);
    }
    next();
  } catch (error) {
    logger.error('admin_check_failed', { message: error.message, requestId: req.requestId });
    return sendError(res, 500, 'Failed to verify admin role.', 'internal_error', req.requestId);
  }
};
