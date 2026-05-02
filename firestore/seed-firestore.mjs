/**
 * Uploads firestore/SEED_COPY_PASTE.json to Cloud Firestore using the Admin SDK.
 *
 * Setup:
 *   1. Firebase Console → Project settings → Service accounts → Generate new private key
 *   2. Save the JSON file somewhere safe (do not commit it).
 *   3. PowerShell:
 *        $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
 *        $env:FIREBASE_PROJECT_ID="your-project-id"
 *   4. cd firestore && npm install && npm run seed
 *   See firestore/README.txt for full team notes.
 *
 * Options:
 *   --dry-run     Print paths only; no writes.
 *   --file PATH   Default: ./SEED_COPY_PASTE.json next to this script.
 */

import { readFileSync } from "fs";
import { dirname, join } from "path";
import { fileURLToPath } from "url";

import admin from "firebase-admin";

const __dirname = dirname(fileURLToPath(import.meta.url));

const args = process.argv.slice(2);
const dryRun = args.includes("--dry-run");
const fileArg = args.indexOf("--file");
const seedFile =
  fileArg >= 0 && args[fileArg + 1]
    ? args[fileArg + 1]
    : join(__dirname, "SEED_COPY_PASTE.json");

function pathToRef(db, pathStr) {
  const parts = pathStr.split("/").filter(Boolean);
  if (parts.length < 2 || parts.length % 2 !== 0) {
    throw new Error(`Invalid path (need collection/doc pairs): ${pathStr}`);
  }
  let ref = db.collection(parts[0]).doc(parts[1]);
  for (let i = 2; i < parts.length; i += 2) {
    ref = ref.collection(parts[i]).doc(parts[i + 1]);
  }
  return ref;
}

/** Turns createdAt_iso → createdAt as Firestore Timestamp; copies everything else. */
function transformValue(value) {
  if (value === null || value === undefined) {
    return value;
  }
  if (Array.isArray(value)) {
    return value.map((item) =>
      typeof item === "object" && item !== null && !Array.isArray(item)
        ? transformDocument(item)
        : item
    );
  }
  if (typeof value === "object") {
    return transformDocument(value);
  }
  return value;
}

function transformDocument(data) {
  const out = {};
  for (const [key, val] of Object.entries(data)) {
    if (key.endsWith("_iso")) {
      const newKey = key.slice(0, -4);
      if (typeof val !== "string") {
        throw new Error(`Expected ISO string for ${key}`);
      }
      out[newKey] = admin.firestore.Timestamp.fromDate(new Date(val));
      continue;
    }
    out[key] = transformValue(val);
  }
  return out;
}

function sortPaths(paths) {
  return [...paths].sort((a, b) => {
    const da = a.split("/").length;
    const db = b.split("/").length;
    if (da !== db) return da - db;
    return a.localeCompare(b);
  });
}

const projectId =
  process.env.FIREBASE_PROJECT_ID ||
  process.env.GCLOUD_PROJECT ||
  process.env.GOOGLE_CLOUD_PROJECT;

if (!projectId) {
  console.error(
    "Set FIREBASE_PROJECT_ID (or GCLOUD_PROJECT) to your Firebase project ID."
  );
  process.exit(1);
}

if (!process.env.GOOGLE_APPLICATION_CREDENTIALS && !dryRun) {
  console.error(
    "Set GOOGLE_APPLICATION_CREDENTIALS to the path of your service account JSON file."
  );
  process.exit(1);
}

const raw = JSON.parse(readFileSync(seedFile, "utf8"));
const documents = raw.documents;
if (!documents || typeof documents !== "object") {
  console.error("Seed file must contain a top-level \"documents\" object.");
  process.exit(1);
}

const entries = sortPaths(Object.keys(documents)).map((path) => ({
  path,
  data: transformDocument(documents[path]),
}));

if (dryRun) {
  console.log(`[dry-run] Would write ${entries.length} documents to project "${projectId}":`);
  for (const { path } of entries) {
    console.log(`  ${path}`);
  }
  process.exit(0);
}

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    projectId,
  });
}

const db = admin.firestore();

const BATCH_SIZE = 450;
let batch = db.batch();
let count = 0;
let total = 0;

async function commitBatch() {
  if (count === 0) return;
  await batch.commit();
  total += count;
  console.log(`Committed ${count} writes (total ${total}).`);
  batch = db.batch();
  count = 0;
}

for (const { path, data } of entries) {
  const ref = pathToRef(db, path);
  batch.set(ref, data, { merge: true });
  count++;
  if (count >= BATCH_SIZE) {
    await commitBatch();
  }
}
await commitBatch();

console.log(`Done. Wrote ${total} document(s) to Firestore.`);
