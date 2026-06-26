const express = require('express');
const { db } = require('../lib/firestore');
const { FieldValue } = require('../lib/serializers');
const { asyncHandler } = require('../lib/asyncHandler');
const { validate, adminBroadcastSchema } = require('../middleware/validate');
const { writeAuditLog, AuditVerb } = require('../lib/auditLog');

const router = express.Router();

router.post(
  '/inbox/broadcast',
  validate(adminBroadcastSchema),
  asyncHandler(async (req, res) => {
    const { title, body, audience = 'all' } = req.validated;
    const adminUid = req.user.uid;

    let recipientIds = [];
    if (audience === 'all') {
      const usersSnap = await db.collection('users').limit(500).get();
      recipientIds = usersSnap.docs.map((doc) => doc.id);
    } else if (typeof audience === 'string') {
      recipientIds = [audience];
    } else {
      recipientIds = audience;
    }

    const batch = db.batch();
    for (const recipientUserId of recipientIds) {
      const ref = db.collection('inbox').doc();
      batch.set(ref, {
        schemaVersion: 1,
        recipientUserId,
        notificationKind: 'feed',
        title,
        body,
        deepLink: '/notifications',
        read: false,
        createdAt: FieldValue.serverTimestamp(),
      });
    }
    await batch.commit();

    await writeAuditLog({
      adminUid,
      action: AuditVerb.ADMIN_BROADCAST,
      summary: `Admin broadcast "${title}" to ${recipientIds.length} recipient(s)`,
      metadata: {
        requestId: req.requestId,
        audience: audience === 'all' ? 'all' : recipientIds,
        sent: recipientIds.length,
      },
    });

    res.status(201).json({
      sent: recipientIds.length,
      audience: audience === 'all' ? 'all' : recipientIds,
    });
  }),
);

module.exports = router;
