/**
 * Links a real Firebase Auth user to seeded demo data (chat, inbox, dashboard matches, activity, stats).
 *
 * PowerShell:
 *   $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
 *   $env:FIREBASE_PROJECT_ID="developers-networking-app"
 *   $env:DEMO_USER_UID="paste-your-auth-uid"
 *   cd firestore
 *   npm install
 *   npm run setup:demo-user
 *
 * Run `npm run seed` first if Firestore is empty.
 */

import admin from "firebase-admin";

const dryRun = process.argv.includes("--dry-run");
const projectId = process.env.FIREBASE_PROJECT_ID;
const uid = process.env.DEMO_USER_UID?.trim();
const sourceUid = process.env.DEMO_SOURCE_UID?.trim() || "demo_alex_uid";

const CONVERSATION_IDS = [
  "conv_team_neon",
  "conv_aria_api",
  "conv_khaled_platform",
  "conv_hackathon_squad",
  "conv_design_crew",
];

if (!uid) {
  console.error("Set DEMO_USER_UID to your Firebase Auth UID (Authentication → Users).");
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
console.log(`Linking demo data: ${sourceUid} → ${uid}`);
if (dryRun) console.log("--dry-run: no writes\n");

async function addToConversations() {
  for (const conversationId of CONVERSATION_IDS) {
    const ref = db.collection("conversations").doc(conversationId);
    const snap = await ref.get();
    if (!snap.exists) {
      console.warn(`SKIP conversation ${conversationId} (missing — run seed)`);
      continue;
    }
    const participants = snap.data().participantIds || [];
    if (participants.includes(uid)) {
      console.log(`  conversations/${conversationId}: already participant`);
      continue;
    }
    if (!dryRun) {
      await ref.update({ participantIds: admin.firestore.FieldValue.arrayUnion(uid) });
    }
    console.log(`  conversations/${conversationId}: added participant`);
  }
}

async function copyInbox() {
  const snap = await db.collection("inbox").where("recipientUserId", "==", sourceUid).get();
  if (snap.empty) {
    console.warn("No inbox docs to copy — run seed first.");
    return;
  }
  for (const doc of snap.docs) {
    const targetId = `${doc.id}_${uid.slice(0, 8)}`;
    if (dryRun) {
      console.log(`  would write inbox/${targetId}`);
      continue;
    }
    await db.collection("inbox").doc(targetId).set(
      { ...doc.data(), recipientUserId: uid },
      { merge: true },
    );
    console.log(`  inbox/${targetId}`);
  }
}

async function copyCollaboratorSuggestions() {
  const snap = await db
    .collection("collaboratorSuggestions")
    .where("viewerUserId", "==", sourceUid)
    .get();
  for (const doc of snap.docs) {
    const targetId = `${doc.id}_${uid.slice(0, 8)}`;
    if (dryRun) {
      console.log(`  would write collaboratorSuggestions/${targetId}`);
      continue;
    }
    await db.collection("collaboratorSuggestions").doc(targetId).set({
      ...doc.data(),
      viewerUserId: uid,
    });
    console.log(`  collaboratorSuggestions/${targetId}`);
  }
}

async function copyActivity() {
  const snap = await db.collection("activity").where("audienceUserId", "==", sourceUid).get();
  for (const doc of snap.docs) {
    const targetId = `${doc.id}_${uid.slice(0, 8)}`;
    if (dryRun) {
      console.log(`  would write activity/${targetId}`);
      continue;
    }
    await db.collection("activity").doc(targetId).set({
      ...doc.data(),
      audienceUserId: uid,
    });
    console.log(`  activity/${targetId}`);
  }
}

async function copyUserStats() {
  const sourceSnap = await db.collection("userStats").doc(sourceUid).get();
  if (!sourceSnap.exists) {
    console.warn("No userStats template — run seed first.");
    return;
  }
  if (dryRun) {
    console.log(`  would write userStats/${uid}`);
    return;
  }
  await db.collection("userStats").doc(uid).set({
    ...sourceSnap.data(),
    userId: uid,
  });
  console.log(`  userStats/${uid}`);
}

await addToConversations();
await copyInbox();
await copyCollaboratorSuggestions();
await copyActivity();
await copyUserStats();

console.log("\nDone. Sign in with a verified account and open Dashboard, Chat, Alerts, Profile.");
