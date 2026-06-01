/**
 * Copies demo inbox notifications (recipient demo_alex_uid) for a real Auth user.
 *
 * PowerShell:
 *   $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
 *   $env:FIREBASE_PROJECT_ID="your-project-id"
 *   $env:INBOX_RECIPIENT_UID="your-firebase-auth-uid"
 *   cd firestore
 *   npm run inbox:copy-demo
 */

import admin from "firebase-admin";

const dryRun = process.argv.includes("--dry-run");
const projectId = process.env.FIREBASE_PROJECT_ID;
const recipientUid = process.env.INBOX_RECIPIENT_UID?.trim();
const sourceUid = process.env.INBOX_SOURCE_UID?.trim() || "demo_alex_uid";

if (!recipientUid) {
  console.error(
    "Set INBOX_RECIPIENT_UID to your Firebase Auth UID (Authentication → Users)."
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

const snap = await db
  .collection("inbox")
  .where("recipientUserId", "==", sourceUid)
  .get();

if (snap.empty) {
  console.error(`No inbox docs for source uid ${sourceUid}. Run npm run seed first.`);
  process.exit(1);
}

console.log(
  `Copying ${snap.size} notifications: ${sourceUid} → ${recipientUid}` +
    (dryRun ? " (dry run)" : "")
);

for (const doc of snap.docs) {
  const data = { ...doc.data(), recipientUserId: recipientUid };
  delete data.id;
  const targetId = `${doc.id}_${recipientUid.slice(0, 8)}`;
  if (dryRun) {
    console.log(`  would write inbox/${targetId}`);
    continue;
  }
  await db.collection("inbox").doc(targetId).set(data, { merge: true });
  console.log(`  inbox/${targetId}`);
}

console.log("\nDone. Open Alerts tab in the app (signed in as that user).");
