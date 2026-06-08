import * as admin from "firebase-admin";
import * as functions from "firebase-functions/v1";
import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";
import {
  ActivityVerb,
  boardColumnLabel,
  isOpenTaskColumn,
  NotificationKind,
} from "./constants";
import { seedCollaboratorSuggestions } from "./lib/collaboratorSuggestions";
import {
  adjustOpenTasksCount,
  createActivityItem,
  createInboxNotification,
  db,
  ensureUserStats,
  fetchProjectTitle,
  fetchUserDisplayName,
  FieldValue,
} from "./lib/firestoreHelpers";
import { onboardTeammate } from "./lib/teamAccess";

admin.initializeApp();

/**
 * Auth lifecycle: bootstrap denormalized stats, welcome inbox, and collaborator suggestions.
 */
export const onUserCreate = functions
  .region("us-central1")
  .auth.user()
  .onCreate(async (user) => {
    const uid = user.uid;
    if (!uid) return;

    await ensureUserStats(uid);

    await createInboxNotification({
      recipientUserId: uid,
      notificationKind: NotificationKind.FEED,
      title: "Welcome to DevConnect",
      body: "Your networking hub is ready — explore collaborators, projects, and events.",
      deepLink: "/dashboard",
    });

    const seeded = await seedCollaboratorSuggestions(uid);
    const access = await onboardTeammate(uid);
    functions.logger.info("onUserCreate completed", {
      uid,
      suggestionsSeeded: seeded,
      conversationsUpdated: access.conversationsUpdated,
    });
  });

/**
 * Task board automation: inbox alerts, activity feed entries, and openTasksCount stats.
 */
export const onTaskUpdated = onDocumentUpdated(
  {
    document: "projects/{projectId}/tasks/{taskId}",
    region: "us-central1",
  },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after) return;

    const projectId = event.params.projectId;
    const beforeColumn = String(before.boardColumn ?? "");
    const afterColumn = String(after.boardColumn ?? "");
    if (beforeColumn === afterColumn) return;

    const taskTitle = String(after.title ?? "Task").trim() || "Task";
    const assigneeUserId = typeof after.assigneeUserId === "string"
      ? after.assigneeUserId
      : undefined;
    const projectTitle = await fetchProjectTitle(projectId);
    const columnLabel = boardColumnLabel(afterColumn);
    const summary = `Task moved to ${columnLabel} in ${projectTitle}`;

    if (assigneeUserId) {
      await createInboxNotification({
        recipientUserId: assigneeUserId,
        notificationKind: NotificationKind.TASK_UPDATE,
        title: projectTitle,
        body: `Task moved to ${columnLabel}: ${taskTitle}`,
        deepLink: `/projects/${projectId}`,
      });

      await createActivityItem({
        audienceUserId: assigneeUserId,
        verb: ActivityVerb.STATUS_CHANGED,
        summary,
        relatedProjectId: projectId,
      });

      const beforeOpen = isOpenTaskColumn(beforeColumn);
      const afterOpen = isOpenTaskColumn(afterColumn);
      let delta = 0;
      if (beforeOpen && !afterOpen) delta = -1;
      if (!beforeOpen && afterOpen) delta = 1;
      await adjustOpenTasksCount(assigneeUserId, delta);
    }

    functions.logger.info("onTaskUpdated completed", {
      projectId,
      taskId: event.params.taskId,
      beforeColumn,
      afterColumn,
      assigneeUserId: assigneeUserId ?? null,
    });
  },
);

/**
 * Chat automation: participant inbox alerts and conversation preview timestamps.
 */
export const onMessageCreated = onDocumentCreated(
  {
    document: "conversations/{conversationId}/messages/{messageId}",
    region: "us-central1",
  },
  async (event) => {
    const message = event.data?.data();
    if (!message) return;

    const conversationId = event.params.conversationId;
    const senderId = String(message.senderId ?? "");
    const body = String(message.body ?? "").trim();
    if (!senderId || !body) return;

    const conversationRef = db.collection("conversations").doc(conversationId);
    const conversationSnap = await conversationRef.get();
    if (!conversationSnap.exists) return;

    const conversation = conversationSnap.data() ?? {};
    const participantIds = Array.isArray(conversation.participantIds)
      ? (conversation.participantIds as string[])
      : [];
    const conversationTitle = typeof conversation.title === "string" && conversation.title.trim()
      ? conversation.title.trim()
      : "Conversation";

    const preview = body.length > 120 ? `${body.slice(0, 117)}...` : body;
    await conversationRef.set(
      {
        lastMessagePreview: preview,
        lastMessageAt: FieldValue.serverTimestamp(),
      },
      { merge: true },
    );

    const senderName = await fetchUserDisplayName(senderId);
    const recipients = participantIds.filter((id) => id && id !== senderId);

    await Promise.all(
      recipients.map((recipientUserId) =>
        createInboxNotification({
          recipientUserId,
          notificationKind: NotificationKind.MESSAGE,
          title: "New message",
          body: `New message from ${senderName} in ${conversationTitle}`,
          deepLink: `/chat/${conversationId}`,
        }),
      ),
    );

    functions.logger.info("onMessageCreated completed", {
      conversationId,
      messageId: event.params.messageId,
      recipientCount: recipients.length,
    });
  },
);
