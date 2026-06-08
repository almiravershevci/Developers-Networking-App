const { db } = require('./firestore');
const { ApiError } = require('./errors');

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

async function requireProjectReadAccess(projectId, uid) {
  const access = await canReadProject(projectId, uid);

  if (access.reason === 'not_found') {
    throw new ApiError(404, 'not_found', 'Project not found.');
  }
  if (!access.allowed) {
    throw new ApiError(403, 'forbidden', 'You do not have access to this project.');
  }

  return access.projectSnap;
}

module.exports = { canReadProject, requireProjectReadAccess };
