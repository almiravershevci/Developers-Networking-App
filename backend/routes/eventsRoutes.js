const express = require('express');
const { db } = require('../lib/firestore');
const {
  EventRegistrationStatus,
  FieldValue,
  mapEvent,
} = require('../lib/serializers');
const { asyncHandler } = require('../lib/asyncHandler');
const { ApiError } = require('../lib/errors');
const { writeLimiter } = require('../middleware/rateLimit');
const { validate, eventRegistrationSchema } = require('../middleware/validate');

const router = express.Router();

router.get(
  '/',
  asyncHandler(async (_req, res) => {
    const snap = await db.collection('events').limit(20).get();
    const events = snap.docs
      .map(mapEvent)
      .sort((a, b) => {
        const aTime = a.startsAt ? Date.parse(a.startsAt) : Number.MAX_SAFE_INTEGER;
        const bTime = b.startsAt ? Date.parse(b.startsAt) : Number.MAX_SAFE_INTEGER;
        return aTime - bTime;
      });
    res.json({ events, source: 'firestore' });
  }),
);

router.get(
  '/:eventId',
  asyncHandler(async (req, res) => {
    const snap = await db.collection('events').doc(req.params.eventId).get();
    if (!snap.exists) {
      throw new ApiError(404, 'not_found', 'Event not found.');
    }
    res.json({ event: mapEvent(snap) });
  }),
);

router.post(
  '/:eventId/registrations/me',
  writeLimiter,
  validate(eventRegistrationSchema),
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const { eventId } = req.params;
    const status = req.validated.status === EventRegistrationStatus.WAITLIST
      ? EventRegistrationStatus.WAITLIST
      : EventRegistrationStatus.GOING;

    const eventSnap = await db.collection('events').doc(eventId).get();
    if (!eventSnap.exists) {
      throw new ApiError(404, 'not_found', 'Event not found.');
    }

    await db
      .collection('events')
      .doc(eventId)
      .collection('registrations')
      .doc(uid)
      .set({
        schemaVersion: 1,
        userId: uid,
        status,
        registeredAt: FieldValue.serverTimestamp(),
      });

    res.status(201).json({ eventId, userId: uid, status });
  }),
);

router.delete(
  '/:eventId/registrations/me',
  writeLimiter,
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const { eventId } = req.params;
    const ref = db.collection('events').doc(eventId).collection('registrations').doc(uid);
    const snap = await ref.get();
    if (!snap.exists) {
      throw new ApiError(404, 'not_found', 'Registration not found.');
    }
    await ref.delete();
    res.status(204).send();
  }),
);

module.exports = router;
