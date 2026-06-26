const express = require('express');
const { db } = require('../lib/firestore');
const { FieldValue } = require('../lib/serializers');
const { sendError } = require('../lib/errors');

const router = express.Router();

/**
 * POST /api/admin/inbox/broadcast — AdminRepository.sendNotification.
 */
router.post('/inbox/broadcast', async (req, res) => {
  try {
    const { title, body, audience = 'all' } = req.body || {};
    if (!title || !body) {
      return sendError(res, 400, 'title and body are required.', 'validation_error');
    }

    let recipientIds = [];
    if (audience === 'all') {
      const usersSnap = await db.collection('users').limit(500).get();
      recipientIds = usersSnap.docs.map((doc) => doc.id);
    } else if (typeof audience === 'string') {
      recipientIds = [audience];
    } else if (Array.isArray(audience)) {
      recipientIds = audience.filter((id) => typeof id === 'string');
    }

    const batch = db.batch();
    let sent = 0;
    for (const recipientUserId of recipientIds) {
      const ref = db.collection('inbox').doc();
      batch.set(ref, {
        schemaVersion: 1,
        recipientUserId,
        notificationKind: 'feed',
        title: String(title).trim(),
        body: String(body).trim(),
        deepLink: '/notifications',
        read: false,
        createdAt: FieldValue.serverTimestamp(),
      });
      sent += 1;
    }
    await batch.commit();

    res.status(201).json({ sent, audience: audience === 'all' ? 'all' : recipientIds });
  } catch (error) {
    console.error('Admin broadcast error:', error);
    sendError(res, 500, 'Failed to broadcast notification.');
  }
});

module.exports = router;
