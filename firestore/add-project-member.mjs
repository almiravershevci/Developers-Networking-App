/**
 * Adds your Firebase Auth UID as a contributor on the showcase project
 * so task create/update/delete passes Firestore security rules.
 *
 * PowerShell:
 *   $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
 *   $env:FIREBASE_PROJECT_ID="developers-networking-app"
 *   $env:PROJECT_MEMBER_UID="your-firebase-auth-uid"
 *   cd firestore
 *   npm run project:add-me
 */

import admin from "firebase-admin";

const dryRun = process.argv.includes("--dry-run");
const projectId = process.env.FIREBASE_PROJECT_ID;
const uid = process.env.PROJECT_MEMBER_UID?.trim();
const showcaseProjectId =
  process.env.SHOWCASE_PROJECT_ID?.trim() || "proj_devconnect_mobile";
const memberRole = process.env.PROJECT_MEMBER_ROLE?.trim() || "contributor";

if (!uid) {
  console.error(
    "Set PROJECT_MEMBER_UID to your Firebase Auth UID (Authentication → Users)."
  );
  process.exit(1);
}

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    projectId: projectId || undefined,
  });
}

const db = admin.firestore();
const memberRef = db
  .collection("projects")
  .doc(showcaseProjectId)
  .collection("members")
  .doc(uid);

const payload = {
  memberUserId: uid,
  memberRole,
  joinedAt: admin.firestore.FieldValue.serverTimestamp(),
};

console.log(
  `Project: ${projectId || "(from service account)"}\n` +
    `Member path: projects/${showcaseProjectId}/members/${uid}\n` +
    `Role: ${memberRole}` +
    (dryRun ? "\n--dry-run: no writes" : "")
);

if (dryRun) {
  process.exit(0);
}

const projectSnap = await db.collection("projects").doc(showcaseProjectId).get();
if (!projectSnap.exists) {
  console.error(`Project ${showcaseProjectId} not found. Run npm run seed first.`);
  process.exit(1);
}

await memberRef.set(payload, { merge: true });
console.log(`\nDone. Sign in as ${uid} and retry moving a task.`);
