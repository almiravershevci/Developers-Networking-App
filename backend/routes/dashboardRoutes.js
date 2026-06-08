const express = require('express');
const { db, projectId } = require('../lib/firestore');
const { asyncHandler } = require('../lib/asyncHandler');

const router = express.Router();

function greetingForHour(displayName) {
  const hour = new Date().getHours();
  const salutation =
    hour >= 5 && hour <= 11
      ? 'Good morning'
      : hour >= 12 && hour <= 16
        ? 'Good afternoon'
        : hour >= 17 && hour <= 21
          ? 'Good evening'
          : 'Hello';
  return `${salutation}, ${displayName}`;
}

router.get(
  '/stats',
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const [userSnap, statsSnap] = await Promise.all([
      db.collection('users').doc(uid).get(),
      db.collection('userStats').doc(uid).get(),
    ]);

    const displayName = userSnap.exists
      ? String(userSnap.get('displayName') || 'Developer').trim() || 'Developer'
      : 'Developer';

    const stats = statsSnap.exists
      ? {
          activeProjectsCount: Number(statsSnap.get('activeProjectsCount') || 0),
          openTasksCount: Number(statsSnap.get('openTasksCount') || 0),
          unreadMessagesCount: Number(statsSnap.get('unreadMessagesCount') || 0),
          pendingMatchRequestsCount: Number(statsSnap.get('pendingMatchRequestsCount') || 0),
          collaborationsCount: Number(statsSnap.get('collaborationsCount') || 0),
          ratingAggregate: statsSnap.get('ratingAggregate') ?? null,
        }
      : {
          activeProjectsCount: 0,
          openTasksCount: 0,
          unreadMessagesCount: 0,
          pendingMatchRequestsCount: 0,
          collaborationsCount: 0,
          ratingAggregate: null,
        };

    res.json({
      welcomeMessage: greetingForHour(displayName),
      stats,
      source: 'firestore',
      projectId,
    });
  }),
);

module.exports = router;
