import { readFileSync } from "fs";

import admin from "firebase-admin";

/** Same project as .firebaserc — shared by the whole team. */
export const DEFAULT_FIREBASE_PROJECT_ID = "developers-networking-app";

export function resolveProjectId() {
  const fromEnv =
    process.env.FIREBASE_PROJECT_ID?.trim() ||
    process.env.GCLOUD_PROJECT?.trim() ||
    process.env.GOOGLE_CLOUD_PROJECT?.trim();
  if (fromEnv) return fromEnv;

  const credPath = process.env.GOOGLE_APPLICATION_CREDENTIALS?.trim();
  if (credPath) {
    try {
      const json = JSON.parse(readFileSync(credPath, "utf8"));
      if (json.project_id) return json.project_id;
    } catch {
      // fall through to default + error messages in requireAdminApp
    }
  }

  return DEFAULT_FIREBASE_PROJECT_ID;
}

/**
 * Initializes Firebase Admin SDK. Exits with a clear message if credentials are missing.
 */
export function requireAdminApp({ dryRun = false } = {}) {
  const credPath = process.env.GOOGLE_APPLICATION_CREDENTIALS?.trim();

  if (!dryRun && !credPath) {
    console.error("Missing GOOGLE_APPLICATION_CREDENTIALS.\n");
    console.error("1. Firebase Console → Project settings → Service accounts");
    console.error("2. Generate new private key (JSON) — store in team vault, never git");
    console.error("3. PowerShell (same window before npm run):\n");
    console.error(
      '   $env:GOOGLE_APPLICATION_CREDENTIALS="C:\\path\\to\\serviceAccount.json"',
    );
    console.error(
      '   $env:FIREBASE_PROJECT_ID="developers-networking-app"   # optional if JSON has project_id',
    );
    process.exit(1);
  }

  const projectId = resolveProjectId();

  if (!dryRun && credPath) {
    try {
      readFileSync(credPath, "utf8");
    } catch {
      console.error(`Service account file not found: ${credPath}`);
      process.exit(1);
    }
  }

  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.applicationDefault(),
      projectId,
    });
  }

  return {
    projectId,
    db: admin.firestore(),
    auth: admin.auth(),
    admin,
  };
}
