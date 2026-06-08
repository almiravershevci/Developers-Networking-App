const express = require('express');
const { db } = require('../lib/firestore');
const {
  FieldValue,
  MatchWorkflow,
  directConversationId,
  mapMatchRequest,
} = require('../lib/serializers');
const { asyncHandler } = require('../lib/asyncHandler');
const { ApiError } = require('../lib/errors');
const { writeLimiter } = require('../middleware/rateLimit');
const { validate, createMatchRequestSchema } = require('../middleware/validate');

const router = express.Router();

async function getMatchRequestOrThrow(requestId) {
  const snap = await db.collection('matchRequests').doc(requestId).get();
  if (!snap.exists) {
    throw new ApiError(404, 'not_found', 'Match request not found.');
  }
  return snap;
}

router.get(
  '/incoming',
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const snap = await db
      .collection('matchRequests')
      .where('toUserId', '==', uid)
      .where('workflowStatus', '==', MatchWorkflow.PENDING)
      .get();
    res.json({ requests: snap.docs.map(mapMatchRequest), source: 'firestore' });
  }),
);

router.get(
  '/outgoing',
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const snap = await db
      .collection('matchRequests')
      .where('fromUserId', '==', uid)
      .where('workflowStatus', '==', MatchWorkflow.PENDING)
      .get();
    res.json({ requests: snap.docs.map(mapMatchRequest), source: 'firestore' });
  }),
);

router.post(
  '/',
  writeLimiter,
  validate(createMatchRequestSchema),
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const { toUserId, message } = req.validated;
    if (toUserId === uid) {
      throw new ApiError(400, 'validation_error', 'Cannot invite yourself.');
    }

    const ref = db.collection('matchRequests').doc();
    await ref.set({
      schemaVersion: 1,
      fromUserId: uid,
      toUserId,
      workflowStatus: MatchWorkflow.PENDING,
      message: message?.trim() || null,
      createdAt: FieldValue.serverTimestamp(),
      resolvedAt: null,
    });

    res.status(201).json({ request: mapMatchRequest(await ref.get()) });
  }),
);

router.post(
  '/:requestId/accept',
  writeLimiter,
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const snap = await getMatchRequestOrThrow(req.params.requestId);
    const data = snap.data();

    if (data.toUserId !== uid) {
      throw new ApiError(403, 'forbidden', 'Only the recipient can accept.');
    }
    if (data.workflowStatus !== MatchWorkflow.PENDING) {
      throw new ApiError(409, 'conflict', 'Request already resolved.');
    }

    await snap.ref.update({
      workflowStatus: MatchWorkflow.ACCEPTED,
      resolvedAt: FieldValue.serverTimestamp(),
    });

    const conversationId = directConversationId(data.toUserId, data.fromUserId);
    const conversationRef = db.collection('conversations').doc(conversationId);
    if (!(await conversationRef.get()).exists) {
      await conversationRef.set({
        schemaVersion: 1,
        conversationKind: 'direct',
        title: null,
        projectId: null,
        participantIds: [data.toUserId, data.fromUserId],
        createdBy: data.toUserId,
        lastMessagePreview: 'Match accepted — say hello!',
        lastMessageAt: FieldValue.serverTimestamp(),
        createdAt: FieldValue.serverTimestamp(),
      });
    }

    res.json({
      request: mapMatchRequest(await snap.ref.get()),
      conversationId,
    });
  }),
);

router.post(
  '/:requestId/decline',
  writeLimiter,
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const snap = await getMatchRequestOrThrow(req.params.requestId);
    const data = snap.data();

    if (data.toUserId !== uid) {
      throw new ApiError(403, 'forbidden', 'Only the recipient can decline.');
    }
    if (data.workflowStatus !== MatchWorkflow.PENDING) {
      throw new ApiError(409, 'conflict', 'Request already resolved.');
    }

    await snap.ref.update({
      workflowStatus: MatchWorkflow.DECLINED,
      resolvedAt: FieldValue.serverTimestamp(),
    });

    res.json({ request: mapMatchRequest(await snap.ref.get()) });
  }),
);

module.exports = router;
