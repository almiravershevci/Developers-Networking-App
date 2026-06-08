import * as admin from "firebase-admin";
import { SCHEMA_VERSION } from "../constants";

export const db = admin.firestore();
export const FieldValue = admin.firestore.FieldValue;

export async function ensureUserStats(userId: string): Promise<void> {
  const ref = db.collection("userStats").doc(userId);
  const snap = await ref.get();
  if (snap.exists) return;

  await ref.set({
    schemaVersion: SCHEMA_VERSION,
    userId,
    openTasksCount: 0,
    updatedAt: FieldValue.serverTimestamp(),
  });
}

export async function createInboxNotification(params: {
  recipientUserId: string;
  notificationKind: string;
  title: string;
  body: string;
  deepLink?: string | null;
}): Promise<string> {
  const ref = await db.collection("inbox").add({
    schemaVersion: SCHEMA_VERSION,
    recipientUserId: params.recipientUserId,
    notificationKind: params.notificationKind,
    title: params.title,
    body: params.body,
    deepLink: params.deepLink ?? null,
    read: false,
    createdAt: FieldValue.serverTimestamp(),
  });
  return ref.id;
}

export async function createActivityItem(params: {
  audienceUserId: string;
  verb: string;
  summary: string;
  relatedProjectId?: string | null;
  metadata?: Record<string, unknown>;
}): Promise<void> {
  await db.collection("activity").add({
    schemaVersion: SCHEMA_VERSION,
    audienceUserId: params.audienceUserId,
    verb: params.verb,
    summary: params.summary,
    relatedProjectId: params.relatedProjectId ?? null,
    metadata: params.metadata ?? null,
    createdAt: FieldValue.serverTimestamp(),
  });
}

export async function adjustOpenTasksCount(userId: string, delta: number): Promise<void> {
  if (!delta) return;
  await db.collection("userStats").doc(userId).set(
    {
      openTasksCount: FieldValue.increment(delta),
      updatedAt: FieldValue.serverTimestamp(),
    },
    { merge: true },
  );
}

export async function fetchProjectTitle(projectId: string): Promise<string> {
  const snap = await db.collection("projects").doc(projectId).get();
  if (!snap.exists) return "Project";
  const title = snap.get("title");
  return typeof title === "string" && title.trim() ? title.trim() : "Project";
}

export async function fetchUserDisplayName(userId: string): Promise<string> {
  const snap = await db.collection("users").doc(userId).get();
  if (!snap.exists) return "Developer";
  const displayName = snap.get("displayName");
  return typeof displayName === "string" && displayName.trim()
    ? displayName.trim()
    : "Developer";
}
