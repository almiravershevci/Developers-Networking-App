const express = require('express');
const { db } = require('../lib/firestore');
const { FieldValue, mapConversation, mapMessage } = require('../lib/serializers');
const { sendError } = require('../lib/errors');

const router = express.Router();

/**
 * GET /api/conversations — ChatRepository.observeChat (snapshot).
 */
router.get('/', async (req, res) => {
  try {
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
  } catch (error) {
    console.error('GET /api/conversations error:', error);
    sendError(res, 500, 'Failed to load conversations.');
  }
});

/**
 * GET /api/conversations/:conversationId/messages
 */
router.get('/:conversationId/messages', async (req, res) => {
  try {
    const uid = req.user.uid;
    const { conversationId } = req.params;
    const conversationSnap = await db.collection('conversations').doc(conversationId).get();
    if (!conversationSnap.exists) {
      return sendError(res, 404, 'Conversation not found.', 'not_found');
    }
    const participants = conversationSnap.get('participantIds') || [];
    if (!participants.includes(uid)) {
      return sendError(res, 403, 'Not a participant.', 'forbidden');
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
  } catch (error) {
    console.error('GET messages error:', error);
    sendError(res, 500, 'Failed to load messages.');
  }
});

/**
 * POST /api/conversations/:conversationId/messages — ChatRepository.sendMessage.
 */
router.post('/:conversationId/messages', async (req, res) => {
  try {
    const uid = req.user.uid;
    const { conversationId } = req.params;
    const body = typeof req.body?.body === 'string' ? req.body.body.trim() : '';
    if (!body) {
      return sendError(res, 400, 'Message body is required.', 'validation_error');
    }

    const conversationRef = db.collection('conversations').doc(conversationId);
    const conversationSnap = await conversationRef.get();
    if (!conversationSnap.exists) {
      return sendError(res, 404, 'Conversation not found.', 'not_found');
    }
    const participants = conversationSnap.get('participantIds') || [];
    if (!participants.includes(uid)) {
      return sendError(res, 403, 'Not a participant.', 'forbidden');
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

    const created = await messageRef.get();
    res.status(201).json({ message: mapMessage(created) });
  } catch (error) {
    console.error('POST message error:', error);
    sendError(res, 500, 'Failed to send message.');
  }
});

module.exports = router;
