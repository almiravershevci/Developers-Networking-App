const express = require('express');
const { db } = require('../lib/firestore');
const { assertCanReadProject } = require('../lib/authorization');
const { mapProject, mapTask } = require('../lib/serializers');
const { sendError } = require('../lib/errors');

const router = express.Router();

/**
 * GET /api/projects — SearchRepository + dashboard highlights.
 * Query: stack (optional client-side filter helper), limit
 */
router.get('/', async (req, res) => {
  try {
    const limit = Math.min(Number(req.query.limit) || 12, 50);
    const stack = typeof req.query.stack === 'string' ? req.query.stack.trim().toLowerCase() : '';

    const snap = await db
      .collection('projects')
      .where('visibility', '==', 'public')
      .where('lifecycleStatus', '==', 'recruiting')
      .limit(limit)
      .get();

    let projects = snap.docs.map(mapProject);
    if (stack) {
      projects = projects.filter(
        (project) =>
          project.primaryStackLabel.toLowerCase().includes(stack) ||
          project.stackTags.some((tag) => tag.toLowerCase().includes(stack)),
      );
    }

    res.json({ projects, source: 'firestore' });
  } catch (error) {
    console.error('GET /api/projects error:', error);
    sendError(res, 500, 'Failed to load projects.');
  }
});

/**
 * GET /api/projects/:projectId/tasks — TasksRepository / Kanban snapshot.
 * Enforces same visibility rules as Firestore security rules.
 */
router.get('/:projectId/tasks', async (req, res) => {
  try {
    const { projectId } = req.params;
    const allowed = await assertCanReadProject(projectId, req.user.uid, res);
    if (!allowed) return;

    const tasksSnap = await db
      .collection('projects')
      .doc(projectId)
      .collection('tasks')
      .get();

    const tasks = tasksSnap.docs.map(mapTask);
    res.json({ projectId, tasks, source: 'firestore' });
  } catch (error) {
    console.error('GET project tasks error:', error);
    sendError(res, 500, 'Failed to load project tasks.');
  }
});

/**
 * GET /api/projects/:projectId
 */
router.get('/:projectId', async (req, res) => {
  try {
    const { projectId } = req.params;
    const access = await assertCanReadProject(projectId, req.user.uid, res);
    if (!access) return;

    const snap = await db.collection('projects').doc(projectId).get();
    res.json({ project: mapProject(snap) });
  } catch (error) {
    console.error('GET /api/projects/:projectId error:', error);
    sendError(res, 500, 'Failed to load project.');
  }
});

module.exports = router;
