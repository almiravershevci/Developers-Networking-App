const { FieldValue } = require('./serializers');
const { db } = require('./firestore');
const { logger } = require('./logger');

const AuditVerb = {
  ADMIN_BROADCAST: 'admin_broadcast',
  ADMIN_MODERATION: 'admin_moderation',
};

/**
 * Server-side audit trail (Admin SDK bypasses client activity create rules).
 * @param {{ adminUid: string, action: string, summary: string, metadata?: Record<string, unknown> }} entry
 */
async function writeAuditLog(entry) {
  try {
    await db.collection('activity').add({
      schemaVersion: 1,
      audienceUserId: entry.adminUid,
      verb: entry.action,
      summary: entry.summary,
      metadata: entry.metadata ?? null,
      relatedProjectId: null,
      createdAt: FieldValue.serverTimestamp(),
    });
    logger.info('audit_log', {
      adminUid: entry.adminUid,
      action: entry.action,
      summary: entry.summary,
    });
  } catch (error) {
    logger.error('audit_log_failed', {
      adminUid: entry.adminUid,
      action: entry.action,
      message: error.message,
    });
  }
}

module.exports = { writeAuditLog, AuditVerb };
