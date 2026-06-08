const express = require('express');
const { db } = require('../lib/firestore');

const router = express.Router();

/**
 * Public recruiting projects from shared Firestore.
 * GET /api/projects
 */
router.get('/', async (_req, res) => {
  try {
    const snap = await db
      .collection('projects')
      .where('visibility', '==', 'public')
      .where('lifecycleStatus', '==', 'recruiting')
      .limit(12)
      .get();

    const projects = snap.docs.map((doc) => {
      const data = doc.data();
      return {
        id: doc.id,
        title: data.title || '',
        subtitle: data.subtitle || '',
        primaryStackLabel: data.primaryStackLabel || '',
        ownerUserId: data.ownerUserId || '',
        spotsOpen: Number(data.spotsOpen || 0),
        memberCount: Number(data.memberCount || 0),
      };
    });

    res.json({ projects, source: 'firestore' });
  } catch (error) {
    console.error('Projects API error:', error);
    res.status(500).json({ error: 'Failed to load projects.' });
  }
});

module.exports = router;
