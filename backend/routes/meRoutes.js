const express = require('express');
const { db } = require('../lib/firestore');
const { FieldValue } = require('../lib/serializers');
const { asyncHandler } = require('../lib/asyncHandler');
const { mapUserProfile } = require('../lib/serializers');
const { writeLimiter } = require('../middleware/rateLimit');
const { validate, patchMeSchema } = require('../middleware/validate');

const router = express.Router();

router.get(
  '/',
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const userSnap = await db.collection('users').doc(uid).get();
    res.json({
      uid,
      email: req.user.email,
      profile: mapUserProfile(userSnap),
    });
  }),
);

router.patch(
  '/',
  writeLimiter,
  validate(patchMeSchema),
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const { displayName, headline, bio } = req.validated;
    const payload = { updatedAt: FieldValue.serverTimestamp() };

    if (displayName !== undefined) payload.displayName = displayName;
    if (headline !== undefined) payload.headline = headline;
    if (bio !== undefined) payload.bio = bio;

    await db.collection('users').doc(uid).set(payload, { merge: true });
    res.json({ profile: mapUserProfile(await db.collection('users').doc(uid).get()) });
  }),
);

module.exports = router;
