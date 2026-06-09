================================================================================
FIRESTORE (team): rules, indexes, sample data
================================================================================

REPO LAYOUT
  firestore.rules          Security rules (deploy to Firebase)
  firestore.indexes.json   Composite indexes
  SEED_COPY_PASTE.json     Sample documents (paths + fields; use with seed script)
  seed-firestore.mjs       Uploads SEED_COPY_PASTE.json via Admin SDK
  package.json             Run: npm install (in this folder) before npm run seed

ANDROID APP
  The app uses the same Firebase project as in app/google-services.json.
  Get that file from Firebase Console if you do not have it. Do not commit
  service account keys; google-services.json is the normal client config.

  Firebase Console -> Authentication -> Sign-in method -> enable Email/Password.

  If signup shows PERMISSION_DENIED, deploy rules from project root:
    firebase deploy --only firestore:rules
  (Signup reads usernames before sign-in; rules must allow get on usernames.)
  For password reset and email verification links, add your app domain under
  Authentication -> Settings -> Authorized domains.

----------------------------------------------------------------------------
1) DEPLOY RULES + INDEXES (from repository root, Firebase CLI installed)
----------------------------------------------------------------------------
  firebase login
  firebase use <your-project-id>
  firebase deploy --only firestore

  (Or: firestore:rules and firestore:indexes separately if you prefer.)

----------------------------------------------------------------------------
2) SEED DATA (optional; needs a service account JSON on your machine only)
----------------------------------------------------------------------------
  PowerShell (same window for all three lines):

  $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
  $env:FIREBASE_PROJECT_ID="your-project-id"
  cd firestore
  npm install
  npm run seed

  Dry run (no write):  npm run seed:dry

  After you register in the app and verify email, link demo data to your Auth UID:

  $env:DEMO_USER_UID="your-firebase-auth-uid"
  npm run setup:demo-user

  Dry run: npm run setup:demo-user:dry

  Never commit the service account file. .gitignore already lists common names.

----------------------------------------------------------------------------
3) MANUAL CONSOLE ENTRY (if you do not use the script)
----------------------------------------------------------------------------
  Firestore has no "paste whole database" action. Add collections/documents
  in Firebase Console using the paths and field objects in SEED_COPY_PASTE.json
  (key = full path like users/demo_alex_uid). Fields ending in _iso are
  written as real Timestamps by the seed script; in the console you can use
  timestamp or string as you prefer.

----------------------------------------------------------------------------
4) PLACEHOLDER IDS
----------------------------------------------------------------------------
  Sample UIDs (demo_alex_uid, etc.) are for dev only. When you use Firebase
  Auth, map or replace with real UIDs in your data or app logic.

================================================================================
