const { db } = require('../lib/firestore');
const { AccountRole } = require('../lib/serializers');
const { sendError } = require('../lib/errors');

/**
 * Requires users/{uid}.accountRole == admin (same rule as Firestore admin console).
 */
module.exports = async function requireAdmin(req, res, next) {
  try {
    const snap = await db.collection('users').doc(req.user.uid).get();
    if (!snap.exists || snap.get('accountRole') !== AccountRole.ADMIN) {
      return sendError(res, 403, 'Admin role required.', 'forbidden');
    }
    next();
  } catch (error) {
    console.error('Admin check failed:', error);
    return sendError(res, 500, 'Failed to verify admin role.');
  }
};
