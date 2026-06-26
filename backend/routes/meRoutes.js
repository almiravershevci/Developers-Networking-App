const express = require('express');
const { db } = require('../lib/firestore');
const { mapUserProfile } = require('../lib/serializers');
const { sendError } = require('../lib/errors');

const router = express.Router();

/**
 * GET /api/me — current user profile (Auth + Firestore users/{uid}).
 */
router.get('/', async (req, res) => {
  try {
    const uid = req.user.uid;
    const userSnap = await db.collection('users').doc(uid).get();
    res.json({
      uid,
      email: req.user.email,
      profile: mapUserProfile(userSnap),
    });
  } catch (error) {
    console.error('GET /api/me error:', error);
    sendError(res, 500, 'Failed to load profile.');
  }
});

/**
 * PATCH /api/me — update profile fields mirrored in ProfileRepository.
 */
router.patch('/', async (req, res) => {
  try {
    const uid = req.user.uid;
    const { displayName, headline, bio } = req.body || {};
    const payload = {};

    if (typeof displayName === 'string') payload.displayName = displayName.trim();
    if (typeof headline === 'string') payload.headline = headline.trim();
    if (typeof bio === 'string') payload.bio = bio.trim();

    if (Object.keys(payload).length === 0) {
      return sendError(res, 400, 'Provide displayName, headline, or bio.', 'validation_error');
    }

    payload.updatedAt = new Date();
    await db.collection('users').doc(uid).set(payload, { merge: true });

    const userSnap = await db.collection('users').doc(uid).get();
    res.json({ profile: mapUserProfile(userSnap) });
  } catch (error) {
    console.error('PATCH /api/me error:', error);
    sendError(res, 500, 'Failed to update profile.');
  }
});

module.exports = router;
