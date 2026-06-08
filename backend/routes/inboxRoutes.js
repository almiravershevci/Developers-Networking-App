const express = require('express');
const { db } = require('../lib/firestore');
const { mapInboxNotification } = require('../lib/serializers');
const { sendError } = require('../lib/errors');

const router = express.Router();

/**
 * GET /api/inbox — Alerts tab (NotificationsRepository).
 */
router.get('/', async (req, res) => {
  try {
    const uid = req.user.uid;
    const limit = Math.min(Number(req.query.limit) || 25, 50);
    const snap = await db
      .collection('inbox')
      .where('recipientUserId', '==', uid)
      .orderBy('createdAt', 'desc')
      .limit(limit)
      .get();

    const notifications = snap.docs.map(mapInboxNotification);
    const unreadCount = notifications.filter((item) => !item.read).length;
    res.json({ notifications, unreadCount, source: 'firestore' });
  } catch (error) {
    console.error('GET /api/inbox error:', error);
    sendError(res, 500, 'Failed to load inbox.');
  }
});

/**
 * PATCH /api/inbox/:notificationId/read — markAsRead.
 */
router.patch('/:notificationId/read', async (req, res) => {
  try {
    const uid = req.user.uid;
    const { notificationId } = req.params;
    const ref = db.collection('inbox').doc(notificationId);
    const snap = await ref.get();

    if (!snap.exists) {
      return sendError(res, 404, 'Notification not found.', 'not_found');
    }
    if (snap.get('recipientUserId') !== uid) {
      return sendError(res, 403, 'Not your notification.', 'forbidden');
    }

    await ref.update({ read: true });
    const updated = await ref.get();
    res.json({ notification: mapInboxNotification(updated) });
  } catch (error) {
    console.error('PATCH inbox read error:', error);
    sendError(res, 500, 'Failed to mark notification as read.');
  }
});

module.exports = router;
