import { FieldValue, db } from "./firestoreHelpers";

const SHOWCASE_PROJECT_ID = process.env.SHOWCASE_PROJECT_ID?.trim() || "proj_devconnect_mobile";

const CONVERSATION_IDS = [
  "conv_team_neon",
  "conv_aria_api",
  "conv_khaled_platform",
  "conv_hackathon_squad",
  "conv_design_crew",
];

export async function onboardTeammate(uid: string): Promise<{ conversationsUpdated: number }> {
  let conversationsUpdated = 0;

  await db
    .collection("projects")
    .doc(SHOWCASE_PROJECT_ID)
    .collection("members")
    .doc(uid)
    .set(
      {
        memberUserId: uid,
        memberRole: "contributor",
        joinedAt: FieldValue.serverTimestamp(),
      },
      { merge: true },
    );

  for (const conversationId of CONVERSATION_IDS) {
    const ref = db.collection("conversations").doc(conversationId);
    const snap = await ref.get();
    if (!snap.exists) continue;

    const participantIds = Array.isArray(snap.get("participantIds"))
      ? (snap.get("participantIds") as string[])
      : [];
    if (participantIds.includes(uid)) continue;

    await ref.update({
      participantIds: FieldValue.arrayUnion(uid),
    });
    conversationsUpdated += 1;
  }

  return { conversationsUpdated };
}
