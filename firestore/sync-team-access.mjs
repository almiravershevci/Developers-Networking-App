/**
 * Backfill showcase access for EVERY Firebase Auth user (whole team).
 * Run once after pull, when a new teammate signs up, or before Cloud Functions are deployed.
 *
 * Grants:
 *   - projects/proj_devconnect_mobile/members/{uid}  (task board writes)
 *   - participantIds on all showcase conversations     (chat inbox)
 *
 * PowerShell:
 *   $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
 *   $env:FIREBASE_PROJECT_ID="developers-networking-app"
 *   cd firestore && npm install && npm run team:sync-access
 */

import { requireAdminApp } from "./lib/firebase-init.mjs";

const dryRun = process.argv.includes("--dry-run");
const showcaseProjectId =
  process.env.SHOWCASE_PROJECT_ID?.trim() || "proj_devconnect_mobile";

const CONVERSATION_IDS = [
  "conv_team_neon",
  "conv_aria_api",
  "conv_khaled_platform",
  "conv_hackathon_squad",
  "conv_design_crew",
];

const { projectId, db, auth, admin } = requireAdminApp({ dryRun });

let pageToken;
const uids = [];
do {
  const result = await auth.listUsers(1000, pageToken);
  uids.push(...result.users.map((u) => u.uid));
  pageToken = result.pageToken;
} while (pageToken);

console.log(`Firebase project: ${projectId}`);
console.log(`Team sync: ${uids.length} Auth user(s)`);
console.log(`  Project: projects/${showcaseProjectId}/members/{uid}`);
console.log(`  Chats: ${CONVERSATION_IDS.length} conversations`);
if (dryRun) {
  uids.forEach((uid) => console.log(`  would onboard ${uid}`));
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
  console.log(`  member ${uid}`);

  for (const conversationId of CONVERSATION_IDS) {
    const ref = db.collection("conversations").doc(conversationId);
    const snap = await ref.get();
    if (!snap.exists) {
      console.warn(`    skip missing conversation ${conversationId}`);
      continue;
    }
    const existing = snap.data()?.participantIds ?? [];
    if (existing.includes(uid)) continue;
    await ref.update({
      participantIds: admin.firestore.FieldValue.arrayUnion(uid),
    });
    console.log(`    chat ${conversationId}`);
  }
}

console.log("\nDone. Every Auth user can use Tasks, Chat, and inbox automation.");
