const express = require('express');
const { db } = require('../lib/firestore');
const {
  FieldValue,
  MatchWorkflow,
  directConversationId,
  mapMatchRequest,
} = require('../lib/serializers');
const { sendError } = require('../lib/errors');

const router = express.Router();

async function fetchMatchRequestOr404(requestId, res) {
  const snap = await db.collection('matchRequests').doc(requestId).get();
  if (!snap.exists) {
    sendError(res, 404, 'Match request not found.', 'not_found');
    return null;
  }
  return snap;
}

/**
 * GET /api/match-requests/incoming — MatchRepository.observeIncomingRequests.
 */
router.get('/incoming', async (req, res) => {
  try {
    const uid = req.user.uid;
    const snap = await db
      .collection('matchRequests')
      .where('toUserId', '==', uid)
      .where('workflowStatus', '==', MatchWorkflow.PENDING)
      .get();
    const requests = snap.docs.map(mapMatchRequest);
    res.json({ requests, source: 'firestore' });
  } catch (error) {
    console.error('GET incoming match requests error:', error);
    sendError(res, 500, 'Failed to load incoming match requests.');
  }
});

/**
 * GET /api/match-requests/outgoing
 */
router.get('/outgoing', async (req, res) => {
  try {
    const uid = req.user.uid;
    const snap = await db
      .collection('matchRequests')
      .where('fromUserId', '==', uid)
      .where('workflowStatus', '==', MatchWorkflow.PENDING)
      .get();
    const requests = snap.docs.map(mapMatchRequest);
    res.json({ requests, source: 'firestore' });
  } catch (error) {
    console.error('GET outgoing match requests error:', error);
    sendError(res, 500, 'Failed to load outgoing match requests.');
  }
});

/**
 * POST /api/match-requests — sendMatchRequest.
 */
router.post('/', async (req, res) => {
  try {
    const uid = req.user.uid;
    const { toUserId, message } = req.body || {};
    if (!toUserId || typeof toUserId !== 'string') {
      return sendError(res, 400, 'toUserId is required.', 'validation_error');
    }
    if (toUserId === uid) {
      return sendError(res, 400, 'Cannot invite yourself.', 'validation_error');
    }

    const ref = db.collection('matchRequests').doc();
    await ref.set({
      schemaVersion: 1,
      fromUserId: uid,
      toUserId,
      workflowStatus: MatchWorkflow.PENDING,
      message: typeof message === 'string' ? message.trim() || null : null,
      createdAt: FieldValue.serverTimestamp(),
      resolvedAt: null,
    });

    const created = await ref.get();
    res.status(201).json({ request: mapMatchRequest(created) });
  } catch (error) {
    console.error('POST match request error:', error);
    sendError(res, 500, 'Failed to send match request.');
  }
});

/**
 * POST /api/match-requests/:requestId/accept
 */
router.post('/:requestId/accept', async (req, res) => {
  try {
    const uid = req.user.uid;
    const snap = await fetchMatchRequestOr404(req.params.requestId, res);
    if (!snap) return;

    const data = snap.data();
    if (data.toUserId !== uid) {
      return sendError(res, 403, 'Only the recipient can accept.', 'forbidden');
    }
    if (data.workflowStatus !== MatchWorkflow.PENDING) {
      return sendError(res, 409, 'Request already resolved.', 'conflict');
    }

    await snap.ref.update({
      workflowStatus: MatchWorkflow.ACCEPTED,
      resolvedAt: FieldValue.serverTimestamp(),
    });

    const conversationId = directConversationId(data.toUserId, data.fromUserId);
    const conversationRef = db.collection('conversations').doc(conversationId);
    const existing = await conversationRef.get();
    if (!existing.exists) {
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

    const updated = await snap.ref.get();
    res.json({
      request: mapMatchRequest(updated),
      conversationId,
    });
  } catch (error) {
    console.error('POST accept match error:', error);
    sendError(res, 500, 'Failed to accept match request.');
  }
});

/**
 * POST /api/match-requests/:requestId/decline
 */
router.post('/:requestId/decline', async (req, res) => {
  try {
    const uid = req.user.uid;
    const snap = await fetchMatchRequestOr404(req.params.requestId, res);
    if (!snap) return;

    const data = snap.data();
    if (data.toUserId !== uid) {
      return sendError(res, 403, 'Only the recipient can decline.', 'forbidden');
    }
    if (data.workflowStatus !== MatchWorkflow.PENDING) {
      return sendError(res, 409, 'Request already resolved.', 'conflict');
    }

    await snap.ref.update({
      workflowStatus: MatchWorkflow.DECLINED,
      resolvedAt: FieldValue.serverTimestamp(),
    });

    const updated = await snap.ref.get();
    res.json({ request: mapMatchRequest(updated) });
  } catch (error) {
    console.error('POST decline match error:', error);
    sendError(res, 500, 'Failed to decline match request.');
  }
});

module.exports = router;
