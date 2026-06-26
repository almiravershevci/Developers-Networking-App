const express = require('express');
const { db } = require('../lib/firestore');
const {
  EventRegistrationStatus,
  FieldValue,
  mapEvent,
} = require('../lib/serializers');
const { sendError } = require('../lib/errors');

const router = express.Router();

/**
 * GET /api/events — curated events calendar (EventsRepository).
 */
router.get('/', async (_req, res) => {
  try {
    const snap = await db.collection('events').limit(20).get();
    const events = snap.docs
      .map(mapEvent)
      .sort((a, b) => {
        const aTime = a.startsAt ? Date.parse(a.startsAt) : Number.MAX_SAFE_INTEGER;
        const bTime = b.startsAt ? Date.parse(b.startsAt) : Number.MAX_SAFE_INTEGER;
        return aTime - bTime;
      });
    res.json({ events, source: 'firestore' });
  } catch (error) {
    console.error('GET /api/events error:', error);
    sendError(res, 500, 'Failed to load events.');
  }
});

/**
 * GET /api/events/:eventId
 */
router.get('/:eventId', async (req, res) => {
  try {
    const snap = await db.collection('events').doc(req.params.eventId).get();
    if (!snap.exists) {
      return sendError(res, 404, 'Event not found.', 'not_found');
    }
    res.json({ event: mapEvent(snap) });
  } catch (error) {
    console.error('GET /api/events/:eventId error:', error);
    sendError(res, 500, 'Failed to load event.');
  }
});

/**
 * POST /api/events/:eventId/registrations/me — RSVP (EventsRepository.registerForEvent).
 */
router.post('/:eventId/registrations/me', async (req, res) => {
  try {
    const uid = req.user.uid;
    const { eventId } = req.params;
    const status = req.body?.status === EventRegistrationStatus.WAITLIST
      ? EventRegistrationStatus.WAITLIST
      : EventRegistrationStatus.GOING;

    const eventSnap = await db.collection('events').doc(eventId).get();
    if (!eventSnap.exists) {
      return sendError(res, 404, 'Event not found.', 'not_found');
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
  } catch (error) {
    console.error('POST registration error:', error);
    sendError(res, 500, 'Failed to register for event.');
  }
});

/**
 * DELETE /api/events/:eventId/registrations/me — unregister.
 */
router.delete('/:eventId/registrations/me', async (req, res) => {
  try {
    const uid = req.user.uid;
    const { eventId } = req.params;
    const ref = db.collection('events').doc(eventId).collection('registrations').doc(uid);
    const snap = await ref.get();
    if (!snap.exists) {
      return sendError(res, 404, 'Registration not found.', 'not_found');
    }
    await ref.delete();
    res.status(204).send();
  } catch (error) {
    console.error('DELETE registration error:', error);
    sendError(res, 500, 'Failed to unregister from event.');
  }
});

module.exports = router;
