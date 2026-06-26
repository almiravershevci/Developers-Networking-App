import * as admin from "firebase-admin";
import * as functions from "firebase-functions/v1";
import { db } from "./firestoreHelpers";

export async function sendPushForInbox(
  recipientUserId: string,
  payload: {
    inboxId: string;
    title: string;
    body: string;
    deepLink?: string | null;
    notificationKind?: string;
  },
): Promise<number> {
  const userSnap = await db.collection("users").doc(recipientUserId).get();
  if (!userSnap.exists) return 0;

  const tokens = Array.isArray(userSnap.get("fcmTokens"))
    ? (userSnap.get("fcmTokens") as string[]).filter(Boolean)
    : [];
  if (tokens.length === 0) return 0;

  const message: admin.messaging.MulticastMessage = {
    tokens,
    notification: {
      title: payload.title,
      body: payload.body,
    },
    data: {
      inboxId: payload.inboxId,
      deepLink: payload.deepLink ?? "",
      notificationKind: payload.notificationKind ?? "feed",
    },
  };

  const response = await admin.messaging().sendEachForMulticast(message);
  const staleTokens: string[] = [];
  response.responses.forEach((item, index) => {
    if (!item.success) {
      const code = item.error?.code ?? "";
      if (
        code === "messaging/registration-token-not-registered"
        || code === "messaging/invalid-registration-token"
      ) {
        staleTokens.push(tokens[index]);
      }
      functions.logger.warn("FCM delivery failed", {
        recipientUserId,
        code,
        message: item.error?.message,
      });
    }
  });

  if (staleTokens.length > 0) {
    await db.collection("users").doc(recipientUserId).update({
      fcmTokens: admin.firestore.FieldValue.arrayRemove(...staleTokens),
    });
  }

  return response.successCount;
}
