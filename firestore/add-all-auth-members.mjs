/**
 * Adds every Firebase Auth user as a contributor on the showcase project.
 * Use when task writes fail with PERMISSION_DENIED (wrong UID vs member doc).
 *
 *   $env:GOOGLE_APPLICATION_CREDENTIALS="..."
 *   $env:FIREBASE_PROJECT_ID="developers-networking-app"
 *   cd firestore && npm run project:add-all-auth
 */

import admin from "firebase-admin";

const dryRun = process.argv.includes("--dry-run");
const projectId = process.env.FIREBASE_PROJECT_ID;
const showcaseProjectId =
  process.env.SHOWCASE_PROJECT_ID?.trim() || "proj_devconnect_mobile";

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    projectId: projectId || undefined,
  });
}

const db = admin.firestore();
const auth = admin.auth();

let pageToken;
const uids = [];
do {
  const result = await auth.listUsers(1000, pageToken);
  uids.push(...result.users.map((u) => u.uid));
  pageToken = result.pageToken;
} while (pageToken);

console.log(`Found ${uids.length} Auth user(s). Showcase: ${showcaseProjectId}`);
if (dryRun) {
  uids.forEach((uid) => console.log(`  would add members/${uid}`));
  process.exit(0);
}

for (const uid of uids) {
  await db
    .collection("projects")
    .doc(showcaseProjectId)
    .collection("members")
    .doc(uid)
    .set(
      {
        memberUserId: uid,
        memberRole: "contributor",
        joinedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true },
    );
  console.log(`  members/${uid}`);
}

console.log("\nDone. Task writes allowed for all Auth users on this project.");
