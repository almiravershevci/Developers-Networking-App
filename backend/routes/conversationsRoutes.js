const express = require('express');
const { db } = require('../lib/firestore');
const { FieldValue, mapConversation, mapMessage } = require('../lib/serializers');
const { asyncHandler } = require('../lib/asyncHandler');
const { ApiError } = require('../lib/errors');
const { writeLimiter } = require('../middleware/rateLimit');
const { validate, sendMessageSchema } = require('../middleware/validate');

const router = express.Router();

router.get(
  '/',
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const snap = await db
      .collection('conversations')
      .where('participantIds', 'array-contains', uid)
      .get();

    const conversations = snap.docs
      .map(mapConversation)
      .sort((a, b) => {
        const aTime = a.lastMessageAt ? Date.parse(a.lastMessageAt) : 0;
        const bTime = b.lastMessageAt ? Date.parse(b.lastMessageAt) : 0;
        return bTime - aTime;
      });

    res.json({ conversations, source: 'firestore' });
  }),
);

router.get(
  '/:conversationId/messages',
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const { conversationId } = req.params;
    const conversationSnap = await db.collection('conversations').doc(conversationId).get();

    if (!conversationSnap.exists) {
      throw new ApiError(404, 'not_found', 'Conversation not found.');
    }
    const participants = conversationSnap.get('participantIds') || [];
    if (!participants.includes(uid)) {
      throw new ApiError(403, 'forbidden', 'Not a participant.');
    }

    const messagesSnap = await db
      .collection('conversations')
      .doc(conversationId)
      .collection('messages')
      .get();

    const messages = messagesSnap.docs
      .map(mapMessage)
      .sort((a, b) => {
        const aTime = a.createdAt ? Date.parse(a.createdAt) : 0;
        const bTime = b.createdAt ? Date.parse(b.createdAt) : 0;
        return aTime - bTime;
      });

    res.json({ messages, source: 'firestore' });
  }),
);

router.post(
  '/:conversationId/messages',
  writeLimiter,
  validate(sendMessageSchema),
  asyncHandler(async (req, res) => {
    const uid = req.user.uid;
    const { conversationId } = req.params;
    const { body } = req.validated;

    const conversationRef = db.collection('conversations').doc(conversationId);
    const conversationSnap = await conversationRef.get();
    if (!conversationSnap.exists) {
      throw new ApiError(404, 'not_found', 'Conversation not found.');
    }
    const participants = conversationSnap.get('participantIds') || [];
    if (!participants.includes(uid)) {
      throw new ApiError(403, 'forbidden', 'Not a participant.');
    }

    const messageRef = conversationRef.collection('messages').doc();
    await messageRef.set({
      schemaVersion: 1,
      senderId: uid,
      body,
      messageKind: 'text',
      readByUserIds: [uid],
      createdAt: FieldValue.serverTimestamp(),
    });

    await conversationRef.update({
      lastMessagePreview: body.slice(0, 120),
      lastMessageAt: FieldValue.serverTimestamp(),
    });

    res.status(201).json({ message: mapMessage(await messageRef.get()) });
  }),
);

module.exports = router;
