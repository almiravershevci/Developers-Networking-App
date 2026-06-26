const express = require('express');
const { db } = require('../lib/firestore');
const { requireProjectReadAccess } = require('../lib/authorization');
const { paginateQuery, parseLimit } = require('../lib/pagination');
const { asyncHandler } = require('../lib/asyncHandler');
const { mapProject, mapTask } = require('../lib/serializers');
const { sendJsonWithEtag } = require('../lib/etag');
const { validate, paginationQuerySchema } = require('../middleware/validate');

const router = express.Router();

router.get(
  '/',
  validate(paginationQuerySchema, 'query'),
  asyncHandler(async (req, res) => {
    const limit = parseLimit(req.validated.limit, 12, 50);
    const stack = typeof req.query.stack === 'string' ? req.query.stack.trim().toLowerCase() : '';
    const collectionRef = db.collection('projects');

    const query = collectionRef
      .where('visibility', '==', 'public')
      .where('lifecycleStatus', '==', 'recruiting')
      .orderBy('updatedAt', 'desc');

    const { docs, pagination } = await paginateQuery(query, {
      limit,
      cursor: req.validated.cursor,
      collectionRef,
    });

    let projects = docs.map(mapProject);
    if (stack) {
      projects = projects.filter(
        (project) =>
          project.primaryStackLabel.toLowerCase().includes(stack) ||
          project.stackTags.some((tag) => tag.toLowerCase().includes(stack)),
      );
    }

    sendJsonWithEtag(req, res, { projects, pagination, source: 'firestore' });
  }),
);

router.get(
  '/:projectId/tasks',
  asyncHandler(async (req, res) => {
    const { projectId } = req.params;
    await requireProjectReadAccess(projectId, req.user.uid);

    const tasksSnap = await db
      .collection('projects')
      .doc(projectId)
      .collection('tasks')
      .get();

    res.json({ projectId, tasks: tasksSnap.docs.map(mapTask), source: 'firestore' });
  }),
);

router.get(
  '/:projectId',
  asyncHandler(async (req, res) => {
    const { projectId } = req.params;
    const snap = await requireProjectReadAccess(projectId, req.user.uid);
    res.json({ project: mapProject(snap) });
  }),
);

module.exports = router;
