/**
 * Adds your Firebase Auth UID to every seeded conversation's participantIds
 * so Chat inbox + quick rooms work under Firestore security rules.
 *
 * PowerShell (same session):
 *   $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
 *   $env:FIREBASE_PROJECT_ID="your-project-id"
 *   $env:CHAT_TEST_UID="paste-your-auth-uid-here"
 *   cd firestore
 *   npm install
 *   npm run chat:add-me
 *
 * Dry run (no writes):
 *   npm run chat:add-me:dry
 */

import admin from "firebase-admin";

const dryRun = process.argv.includes("--dry-run");
const projectId = process.env.FIREBASE_PROJECT_ID;
const uid = process.env.CHAT_TEST_UID?.trim();

const CONVERSATION_IDS = [
  "conv_team_neon",
  "conv_aria_api",
  "conv_khaled_platform",
  "conv_hackathon_squad",
  "conv_design_crew",
];

if (!uid) {
  console.error(
    "Missing CHAT_TEST_UID.\n" +
      "Firebase Console → Authentication → Users → copy the User UID column for your account.\n" +
      'Then:  $env:CHAT_TEST_UID="that-uid"'
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

console.log(`Project: ${projectId || "(from service account)"}`);
console.log(`Adding UID to ${CONVERSATION_IDS.length} conversations: ${uid}`);
if (dryRun) {
  console.log("--dry-run: no writes");
  process.exit(0);
}

for (const conversationId of CONVERSATION_IDS) {
  const ref = db.collection("conversations").doc(conversationId);
  const snap = await ref.get();
  if (!snap.exists) {
    console.warn(`SKIP (missing): conversations/${conversationId}`);
    continue;
  }
  const existing = snap.data()?.participantIds ?? [];
  if (existing.includes(uid)) {
    console.log(`OK (already participant): ${conversationId}`);
    continue;
  }
  await ref.update({
    participantIds: admin.firestore.FieldValue.arrayUnion(uid),
  });
  console.log(`UPDATED: conversations/${conversationId}`);
}

console.log("\nDone. In the app: sign in → verify email → Chat tab → pull to refresh or reopen Chat.");
