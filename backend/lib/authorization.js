const { db } = require('./firestore');

/**
 * Mirrors Firestore rules `canReadProjectDoc` — Admin SDK bypasses rules,
 * so every REST read must enforce the same policy in code.
 */
async function canReadProject(projectId, uid) {
  const projectRef = db.collection('projects').doc(projectId);
  const projectSnap = await projectRef.get();

  if (!projectSnap.exists) {
    return { allowed: false, reason: 'not_found', projectSnap: null };
  }

  const data = projectSnap.data() || {};
  const visibility = String(data.visibility || '');
  const ownerUserId = String(data.ownerUserId || '');

  if (visibility === 'public' || visibility === 'unlisted') {
    return { allowed: true, reason: 'public', projectSnap };
  }

  if (ownerUserId && ownerUserId === uid) {
    return { allowed: true, reason: 'owner', projectSnap };
  }

  if (visibility === 'private') {
    const memberSnap = await projectRef.collection('members').doc(uid).get();
    if (memberSnap.exists) {
      return { allowed: true, reason: 'member', projectSnap };
    }
  }

  return { allowed: false, reason: 'forbidden', projectSnap };
}

/**
 * Express helper — sends 404/403 and returns false when denied.
 */
async function assertCanReadProject(projectId, uid, res) {
  const access = await canReadProject(projectId, uid);

  if (access.reason === 'not_found') {
    res.status(404).json({ error: 'not_found', message: 'Project not found.' });
    return false;
  }

  if (!access.allowed) {
    res.status(403).json({
      error: 'forbidden',
      message: 'You do not have access to this project.',
    });
    return false;
  }

  return true;
}

module.exports = { canReadProject, assertCanReadProject };
