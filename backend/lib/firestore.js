const admin = require('firebase-admin');

const projectId =
  process.env.FIREBASE_PROJECT_ID ||
  process.env.GCLOUD_PROJECT ||
  'developers-networking-app';

if (!admin.apps.length) {
  admin.initializeApp({ projectId });
}

const db = admin.firestore();

module.exports = { admin, db, projectId };
