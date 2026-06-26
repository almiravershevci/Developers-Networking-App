const express = require('express');
const { db } = require('../lib/firestore');
const { paginateQuery, parseLimit } = require('../lib/pagination');
const { asyncHandler } = require('../lib/asyncHandler');
const { ApiError } = require('../lib/errors');
const { mapInboxNotification } = require('../lib/serializers');
const { validate, paginationQuerySchema } = require('../middleware/validate');

const router = express.Router();

router.get(
  '/',
  validate(paginationQuerySchema, 'query'),
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const limit = parseLimit(req.validated.limit, 25, 50);
    const collectionRef = db.collection('inbox');

    const query = collectionRef
      .where('recipientUserId', '==', uid)
      .orderBy('createdAt', 'desc');

    const { docs, pagination } = await paginateQuery(query, {
      limit,
      cursor: req.validated.cursor,
      collectionRef,
    });

    const notifications = docs.map(mapInboxNotification);
    res.json({
      notifications,
      unreadCount: notifications.filter((item) => !item.read).length,
      pagination,
      source: 'firestore',
    });
  }),
);

router.patch(
  '/:notificationId/read',
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const { notificationId } = req.params;
    const ref = db.collection('inbox').doc(notificationId);
    const snap = await ref.get();

    if (!snap.exists) {
      throw new ApiError(404, 'not_found', 'Notification not found.');
    }
    if (snap.get('recipientUserId') !== uid) {
      throw new ApiError(403, 'forbidden', 'Not your notification.');
    }

    await ref.update({ read: true });
    res.json({ notification: mapInboxNotification(await ref.get()) });
  }),
);

module.exports = router;
