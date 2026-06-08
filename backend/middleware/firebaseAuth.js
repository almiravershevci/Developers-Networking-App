const { admin } = require('../lib/firestore');

/**
 * Verifies Firebase ID tokens from the shared project (works for every teammate).
 * Accepts Authorization: Bearer <token> or legacy x-auth-token header.
 */
module.exports = async function firebaseAuth(req, res, next) {
  const authHeader = req.header('Authorization') || '';
  const bearer = authHeader.startsWith('Bearer ')
    ? authHeader.slice('Bearer '.length).trim()
    : null;
  const legacy = req.header('x-auth-token');
  const token = bearer || legacy;

  if (!token) {
    return res.status(401).json({ error: 'Missing Firebase ID token' });
  }

  try {
    const decoded = await admin.auth().verifyIdToken(token);
    req.user = {
      uid: decoded.uid,
      email: decoded.email || null,
    };
    next();
  } catch (error) {
    return res.status(401).json({ error: 'Invalid Firebase ID token' });
  }
};
