import { FieldValue, db } from "./firestoreHelpers";
import { SCHEMA_VERSION } from "../constants";

const MAX_SUGGESTIONS = 3;

export async function seedCollaboratorSuggestions(viewerUserId: string): Promise<number> {
  const existing = await db
    .collection("collaboratorSuggestions")
    .where("viewerUserId", "==", viewerUserId)
    .limit(1)
    .get();
  if (!existing.empty) return 0;

  const candidatesSnap = await db
    .collection("users")
    .where("profileVisibility", "==", "public")
    .limit(12)
    .get();

  const candidates = candidatesSnap.docs
    .filter((doc) => doc.id !== viewerUserId)
    .slice(0, MAX_SUGGESTIONS);

  if (candidates.length === 0) return 0;

  const batch = db.batch();
  candidates.forEach((doc, index) => {
    const data = doc.data();
    const skillTags = Array.isArray(data.skillTags) ? data.skillTags : [];
    const stackSummary = skillTags.slice(0, 3).join(" + ") || "Full stack";
    const ref = db.collection("collaboratorSuggestions").doc();
    batch.set(ref, {
      schemaVersion: SCHEMA_VERSION,
      viewerUserId,
      suggestedUserId: doc.id,
      stackSummary,
      matchScore: 95 - index * 2,
      rank: index + 1,
      availabilityNote: typeof data.headline === "string" ? data.headline : "Open to collaborate",
      updatedAt: FieldValue.serverTimestamp(),
    });
  });

  await batch.commit();
  return candidates.length;
}
